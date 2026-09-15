package celerbi.mirageprojector.client;

import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.light.engine.MirageDynamicLightSnapshot;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import celerbi.mirageprojector.network.ShoulderEquipmentStatePayload;
import celerbi.mirageprojector.network.ShoulderEquipmentInventoryPayload;
import celerbi.mirageprojector.equipment.ShoulderEquipment;
import celerbi.mirageprojector.equipment.ShoulderUpgradeFamilies;
import celerbi.mirageprojector.item.ShoulderUpgrade;
import celerbi.mirageprojector.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Client cache and render/light bridge for the Mirage Shoulder Equipment attachment. */
public final class ClientShoulderEquipment {
    private static final long STALE_TICKS = 60L;
    private static final double LIGHT_CULL_DISTANCE = 96.0D;
    private static final int LIGHT_REFRESH_TICKS = 2;
    private static final int LIGHT_STALE_TICKS = 4;
    private static final Map<UUID, SyncedState> STATES = new HashMap<>();
    private static final ItemStack[] LOCAL_BATTERIES = new ItemStack[ShoulderEquipment.EXPANDED_BATTERY_SLOTS];
    private static final ItemStack[] LOCAL_UPGRADES = new ItemStack[ShoulderEquipment.EXPANDED_UPGRADE_SLOTS];
    private static long lastLightSubmitTick = Long.MIN_VALUE;

    static {
        java.util.Arrays.fill(LOCAL_BATTERIES, ItemStack.EMPTY);
        java.util.Arrays.fill(LOCAL_UPGRADES, ItemStack.EMPTY);
    }

    private ClientShoulderEquipment() {
    }

    public static void acceptServerState(ShoulderEquipmentStatePayload payload) {
        if (payload == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean localOwner = minecraft.player != null && minecraft.player.getUUID().equals(payload.ownerId());
        boolean oldStrap = localOwner && strapPresent(payload.ownerId());
        long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        STATES.put(payload.ownerId(), new SyncedState(
                payload.strapPresent(),
                payload.shoulderDevice().copy(),
                now
        ));
        if (localOwner && oldStrap != payload.strapPresent()) {
            MirageEquipmentClientEvents.refreshIfInventoryOpen();
        }
    }

    public static void acceptInventoryState(ShoulderEquipmentInventoryPayload payload) {
        if (payload == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.getUUID().equals(payload.ownerId())) {
            return;
        }
        boolean oldExpansion = localExpansionInstalled();
        for (int i = 0; i < LOCAL_BATTERIES.length; i++) {
            LOCAL_BATTERIES[i] = i < payload.batteries().size()
                    ? payload.batteries().get(i).copy()
                    : ItemStack.EMPTY;
        }
        for (int i = 0; i < LOCAL_UPGRADES.length; i++) {
            LOCAL_UPGRADES[i] = i < payload.upgrades().size()
                    ? payload.upgrades().get(i).copy()
                    : ItemStack.EMPTY;
        }
        if (oldExpansion != localExpansionInstalled()) {
            MirageEquipmentClientEvents.refreshIfInventoryOpen();
        }
    }

    public static ItemStack localBattery(int index) {
        return index >= 0 && index < LOCAL_BATTERIES.length
                ? LOCAL_BATTERIES[index].copy()
                : ItemStack.EMPTY;
    }

    public static ItemStack localUpgrade(int index) {
        return index >= 0 && index < LOCAL_UPGRADES.length
                ? LOCAL_UPGRADES[index].copy()
                : ItemStack.EMPTY;
    }

    public static boolean localExpansionInstalled() {
        for (ItemStack stack : LOCAL_UPGRADES) {
            if (stack.getItem() instanceof ShoulderUpgrade upgrade
                    && ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION.equals(
                    upgrade.shoulderUpgradeFamily(stack))) {
                return true;
            }
        }
        return false;
    }

    public static int localActiveBatterySlots() {
        return localExpansionInstalled()
                ? ShoulderEquipment.EXPANDED_BATTERY_SLOTS
                : ShoulderEquipment.BASE_BATTERY_SLOTS;
    }

    public static int localActiveUpgradeSlots() {
        return localExpansionInstalled()
                ? ShoulderEquipment.EXPANDED_UPGRADE_SLOTS
                : ShoulderEquipment.BASE_UPGRADE_SLOTS;
    }

    public static boolean localStrapPresent() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && strapPresent(minecraft.player.getUUID());
    }

    public static ItemStack localDevice() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? ItemStack.EMPTY : device(minecraft.player.getUUID());
    }

    public static boolean strapPresent(UUID playerId) {
        SyncedState state = STATES.get(playerId);
        return state != null && state.strapPresent();
    }

    public static ItemStack device(UUID playerId) {
        SyncedState state = STATES.get(playerId);
        return state == null ? ItemStack.EMPTY : state.device().copy();
    }

    public static void submitShoulderLanterns(Minecraft minecraft, Vec3 cameraPosition) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        ClientLevel level = minecraft.level;
        long now = level.getGameTime();
        if (lastLightSubmitTick == now) {
            return;
        }
        lastLightSubmitTick = now;
        pruneStale(now);

        Vec3 camera = cameraPosition == null ? Vec3.ZERO : cameraPosition;
        double cullSq = LIGHT_CULL_DISTANCE * LIGHT_CULL_DISTANCE;
        for (Player player : level.players()) {
            MirageLightSourceId sourceId = MirageLightSourceId.entity("lantern_shoulder", player.getUUID());
            ItemStack stack = device(player.getUUID());
            if (!stack.is(ModItems.MIRAGE_LANTERN.get()) || !MirageLanternItem.emitting(stack)) {
                ClientDynamicMirageLightManager.remove(sourceId);
                continue;
            }

            Vec3 look = player.getLookAngle();
            if (look.lengthSqr() < 1.0E-6D) {
                look = new Vec3(0.0D, 0.0D, 1.0D);
            }
            look = look.normalize();
            Vec3 sourcePos = player.getEyePosition().add(look.scale(0.78D)).add(0.0D, -0.22D, 0.0D);
            if (camera.distanceToSqr(sourcePos) > cullSq) {
                ClientDynamicMirageLightManager.remove(sourceId);
                continue;
            }

            PortableLightMode mode = MirageLanternItem.mode(stack);
            ClientDynamicMirageLightManager.submit(new MirageDynamicLightSnapshot(
                    sourceId,
                    sourcePos,
                    mode.profile(look),
                    LIGHT_REFRESH_TICKS,
                    LIGHT_CULL_DISTANCE,
                    LIGHT_STALE_TICKS
            ));
        }
    }

    public static void renderMountedDevice(
            Player player,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (player == null || poseStack == null || bufferSource == null) {
            return;
        }
        ItemStack stack = device(player.getUUID());
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        // Initial right-shoulder mount. This is intentionally a small generic item render;
        // later cosmetic skins may replace the visible device without changing equipment state.
        poseStack.translate(-0.36D, -1.32D, 0.05D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-12.0F));
        poseStack.scale(0.52F, 0.52F, 0.52F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                player.level(),
                player.getId()
        );
        poseStack.popPose();
    }

    public static void resetSession() {
        STATES.clear();
        java.util.Arrays.fill(LOCAL_BATTERIES, ItemStack.EMPTY);
        java.util.Arrays.fill(LOCAL_UPGRADES, ItemStack.EMPTY);
        lastLightSubmitTick = Long.MIN_VALUE;
    }

    private static void pruneStale(long now) {
        Iterator<Map.Entry<UUID, SyncedState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SyncedState> entry = iterator.next();
            if (now - entry.getValue().lastSeenTick() > STALE_TICKS) {
                ClientDynamicMirageLightManager.remove(MirageLightSourceId.entity(
                        "lantern_shoulder",
                        entry.getKey()
                ));
                iterator.remove();
            }
        }
    }

    private record SyncedState(boolean strapPresent, ItemStack device, long lastSeenTick) {
    }
}

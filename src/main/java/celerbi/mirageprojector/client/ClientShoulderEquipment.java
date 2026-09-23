package celerbi.mirageprojector.client;

import celerbi.mirageprojector.item.MirageFlashlightItem;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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

    public static void submitShoulderFlashlights(Minecraft minecraft, Vec3 cameraPosition, float partialTick) {
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
            MirageLightSourceId sourceId = MirageLightSourceId.entity("flashlight_shoulder", player.getUUID());
            ItemStack stack = device(player.getUUID());
            if (!stack.is(ModItems.MIRAGE_FLASHLIGHT.get()) || !MirageFlashlightItem.emitting(stack)) {
                ClientDynamicMirageLightManager.remove(sourceId);
                continue;
            }

            Vec3 look = player.getViewVector(partialTick);
            if (look.lengthSqr() < 1.0E-6D) {
                look = new Vec3(0.0D, 0.0D, 1.0D);
            }
            look = look.normalize();
            Vec3 horizontalForward = new Vec3(look.x, 0.0D, look.z);
            if (horizontalForward.lengthSqr() < 1.0E-6D) {
                horizontalForward = new Vec3(0.0D, 0.0D, 1.0D);
            }
            horizontalForward = horizontalForward.normalize();
            Vec3 right = new Vec3(-horizontalForward.z, 0.0D, horizontalForward.x);
            Vec3 interpolatedPosition = new Vec3(
                    net.minecraft.util.Mth.lerp(partialTick, player.xo, player.getX()),
                    net.minecraft.util.Mth.lerp(partialTick, player.yo, player.getY()),
                    net.minecraft.util.Mth.lerp(partialTick, player.zo, player.getZ())
            );
            Vec3 eye = interpolatedPosition.add(0.0D, player.getEyeHeight(), 0.0D);
            Vec3 sourcePos = eye
                    .add(right.scale(0.30D))
                    .add(horizontalForward.scale(0.18D))
                    .add(0.0D, -0.34D, 0.0D);
            if (camera.distanceToSqr(sourcePos) > cullSq) {
                ClientDynamicMirageLightManager.remove(sourceId);
                continue;
            }

            PortableLightMode mode = MirageFlashlightItem.mode(stack);
            Vec3 emissionDirection = mode == PortableLightMode.AMBIENT ? new Vec3(0.0D, 1.0D, 0.0D) : look;
            if (mode == PortableLightMode.AMBIENT) {
                sourcePos = eye.add(right.scale(0.30D)).add(0.0D, 0.18D, 0.0D);
            }
            ClientDynamicMirageLightManager.submit(new MirageDynamicLightSnapshot(
                    sourceId,
                    sourcePos,
                    mode.profile(emissionDirection),
                    LIGHT_REFRESH_TICKS,
                    LIGHT_CULL_DISTANCE,
                    LIGHT_STALE_TICKS
            ));
        }
    }

    public static void renderMountedDevices(
            Minecraft minecraft,
            PoseStack poseStack,
            Vec3 cameraPosition,
            float partialTick
    ) {
        if (minecraft == null || minecraft.level == null || poseStack == null || cameraPosition == null) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        boolean renderedAny = false;
        for (Player player : minecraft.level.players()) {
            if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
                continue;
            }
            ItemStack stack = device(player.getUUID());
            if (stack.isEmpty()) {
                continue;
            }

            float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            double radians = Math.toRadians(bodyYaw);
            Vec3 bodyForward = new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
            Vec3 bodyRight = new Vec3(-bodyForward.z, 0.0D, bodyForward.x);
            Vec3 playerPosition = new Vec3(
                    Mth.lerp(partialTick, player.xo, player.getX()),
                    Mth.lerp(partialTick, player.yo, player.getY()),
                    Mth.lerp(partialTick, player.zo, player.getZ())
            );
            Vec3 anchor = playerPosition
                    .add(bodyRight.scale(-0.34D))
                    .add(bodyForward.scale(-0.12D))
                    .add(0.0D, 1.55D, 0.0D);
            int packedLight = LevelRenderer.getLightColor(
                    minecraft.level, BlockPos.containing(anchor.x, anchor.y, anchor.z));

            poseStack.pushPose();
            poseStack.translate(
                    anchor.x - cameraPosition.x,
                    anchor.y - cameraPosition.y,
                    anchor.z - cameraPosition.z
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0F));
            poseStack.scale(0.42F, 0.42F, 0.42F);
            minecraft.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    minecraft.level,
                    player.getId()
            );
            poseStack.popPose();
            renderedAny = true;
        }
        if (renderedAny) {
            bufferSource.endBatch();
        }
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
                        "flashlight_shoulder",
                        entry.getKey()
                ));
                iterator.remove();
            }
        }
    }

    private record SyncedState(boolean strapPresent, ItemStack device, long lastSeenTick) {
    }
}

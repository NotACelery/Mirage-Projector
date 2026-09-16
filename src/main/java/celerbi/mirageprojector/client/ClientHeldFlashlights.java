package celerbi.mirageprojector.client;

import celerbi.mirageprojector.item.MirageFlashlightItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.light.engine.MirageDynamicLightSnapshot;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Converts tracked player-held flashlight ItemStacks into player-following DYNAMIC_VISUAL sources.
 *
 * <p>No extra movement packet is needed: player position/look already arrive through vanilla
 * entity tracking and the held ItemStack carries the synchronized Mirage mode/cell summary.</p>
 */
public final class ClientHeldFlashlights {
    private static final double CULL_DISTANCE = 96.0D;
    private static final int REFRESH_TICKS = 2;
    private static final int STALE_TICKS = 4;
    private static long lastSubmitTick = Long.MIN_VALUE;

    private ClientHeldFlashlights() {
    }

    public static void submitVisiblePlayers(Minecraft minecraft, Vec3 cameraPosition) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        ClientLevel level = minecraft.level;
        long now = level.getGameTime();
        if (lastSubmitTick == now) {
            return;
        }
        lastSubmitTick = now;

        Vec3 camera = cameraPosition == null ? Vec3.ZERO : cameraPosition;
        double cullSq = CULL_DISTANCE * CULL_DISTANCE;
        for (Player player : level.players()) {
            submitHand(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), camera, cullSq);
            submitHand(player, InteractionHand.OFF_HAND, player.getOffhandItem(), camera, cullSq);
        }
    }

    public static void resetSession() {
        lastSubmitTick = Long.MIN_VALUE;
    }

    private static void submitHand(
            Player player,
            InteractionHand hand,
            ItemStack stack,
            Vec3 camera,
            double cullSq
    ) {
        String kind = hand == InteractionHand.MAIN_HAND ? "flashlight_main" : "flashlight_off";
        MirageLightSourceId sourceId = MirageLightSourceId.entity(kind, player.getUUID());
        if (!stack.is(ModItems.MIRAGE_FLASHLIGHT.get()) || !(stack.getItem() instanceof MirageFlashlightItem)) {
            ClientDynamicMirageLightManager.remove(sourceId);
            return;
        }
        if (!MirageFlashlightItem.emitting(stack)) {
            ClientDynamicMirageLightManager.remove(sourceId);
            return;
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 sourcePos = player.getEyePosition().add(look.scale(0.82D)).add(0.0D, -0.08D, 0.0D);
        if (camera.distanceToSqr(sourcePos) > cullSq) {
            ClientDynamicMirageLightManager.remove(sourceId);
            return;
        }

        PortableLightMode mode = MirageFlashlightItem.mode(stack);
        ClientDynamicMirageLightManager.submit(new MirageDynamicLightSnapshot(
                sourceId,
                sourcePos,
                mode.profile(look),
                REFRESH_TICKS,
                CULL_DISTANCE,
                STALE_TICKS
        ));
    }

}

package celerbi.mirageprojector.client;

import celerbi.mirageprojector.item.MirageFlashlightItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.network.EntityScannerProgressPayload;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ClientEntityScanner {
    private static boolean active;
    private static int progress;
    private static int totalTicks = 30;

    private ClientEntityScanner() {
    }

    public static void accept(EntityScannerProgressPayload payload) {
        active = payload.active();
        progress = Math.max(0, payload.progress());
        totalTicks = Math.max(1, payload.totalTicks());
    }

    public static boolean active() {
        return active;
    }

    public static boolean isUsingScanner() {
        var player = Minecraft.getInstance().player;
        return player != null && player.isUsingItem() && player.getUseItem().is(ModItems.ENTITY_SCANNER.get());
    }

    public static int progress() {
        return progress;
    }

    public static int totalTicks() {
        return totalTicks;
    }

    public static void reset() {
        active = false;
        progress = 0;
        totalTicks = 30;
    }

    /** Applies after vanilla setupAnim so only the device-holding arm is altered. */
    public static void applyHeldDevicePose(LivingEntity entity, HumanoidModel<?> model) {
        if (!(entity instanceof Player player) || model == null) {
            return;
        }
        // First-person has its own hand renderer.  Altering the shared humanoid model there
        // reads as a thrown trident instead of a held scanner/light, so leave that view vanilla.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == player && minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        HeldPresentation presentation = activePresentation(player);
        if (presentation == null) {
            return;
        }
        HumanoidArm arm = presentation.hand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        float xRotation = presentation.ambient() ? (float) Math.toRadians(-135.0D) : (float) Math.toRadians(-90.0D);
        if (arm == HumanoidArm.RIGHT) {
            model.rightArm.xRot = xRotation;
            model.rightArm.yRot = 0.0F;
            model.rightArm.zRot = 0.0F;
        } else {
            model.leftArm.xRot = xRotation;
            model.leftArm.yRot = 0.0F;
            model.leftArm.zRot = 0.0F;
        }
    }

    private static HeldPresentation activePresentation(Player player) {
        if (player.isUsingItem() && player.getUseItem().is(ModItems.ENTITY_SCANNER.get())) {
            return new HeldPresentation(player.getUsedItemHand(), false);
        }
        if (!MirageFlashlightItem.emitting(player.getMainHandItem())) {
            return null;
        }
        PortableLightMode mode = MirageFlashlightItem.mode(player.getMainHandItem());
        return switch (mode) {
            case FOCUS, FLOOD -> new HeldPresentation(InteractionHand.MAIN_HAND, false);
            case AMBIENT -> new HeldPresentation(InteractionHand.MAIN_HAND, true);
            case OFF -> null;
        };
    }

    private record HeldPresentation(InteractionHand hand, boolean ambient) {
    }
}

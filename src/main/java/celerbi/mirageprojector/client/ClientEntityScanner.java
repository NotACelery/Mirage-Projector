package celerbi.mirageprojector.client;

import celerbi.mirageprojector.network.EntityScannerProgressPayload;
import celerbi.mirageprojector.registry.ModItems;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class ClientEntityScanner {
    private static boolean active;
    private static int progress;
    private static int totalTicks = 30;
    private static final Map<PlayerModel<?>, ArmPose> ARM_POSES = new IdentityHashMap<>();

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
        ARM_POSES.clear();
    }

    public static void poseScanningArm(Player player, PlayerRenderer renderer) {
        if (!player.isUsingItem() || !player.getUseItem().is(ModItems.ENTITY_SCANNER.get())) {
            return;
        }
        PlayerModel<?> model = renderer.getModel();
        var arm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? model.rightArm : model.leftArm)
                : (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? model.leftArm : model.rightArm);
        ARM_POSES.put(model, new ArmPose(arm, arm.xRot, arm.yRot, arm.zRot));
        arm.xRot = 0.0F;
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
    }

    public static void restoreScanningArm(PlayerRenderer renderer) {
        PlayerModel<?> model = renderer.getModel();
        ArmPose pose = ARM_POSES.remove(model);
        if (pose == null) {
            return;
        }
        pose.arm().xRot = pose.xRot();
        pose.arm().yRot = pose.yRot();
        pose.arm().zRot = pose.zRot();
    }

    private record ArmPose(net.minecraft.client.model.geom.ModelPart arm, float xRot, float yRot, float zRot) {
    }
}

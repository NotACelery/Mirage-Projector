package celerbi.mirageprojector.client;

import celerbi.mirageprojector.block.ChargingStationBlock;
import celerbi.mirageprojector.blockentity.ChargingStationBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Shows queue/charging/output contents physically through the Charging Station shell. */
public final class ChargingStationRenderer implements BlockEntityRenderer<ChargingStationBlockEntity> {
    public ChargingStationRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ChargingStationBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (be.getLevel() == null) return;
        Direction facing = be.getBlockState().hasProperty(ChargingStationBlock.FACING)
                ? be.getBlockState().getValue(ChargingStationBlock.FACING) : Direction.NORTH;
        float yaw = switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));

        double[] xs = {-0.27D, -0.09D, 0.09D, 0.27D};
        // Local -Z is the block's FACING/front output after the yaw transform.
        // Inputs sit at the back (+Z), active cell stays centered, completed cells sit near output (-Z).
        for (int i=0;i<ChargingStationBlockEntity.INPUT_COUNT;i++)
            renderStack(be, be.inventory().getStackInSlot(ChargingStationBlockEntity.INPUT_START+i), pose, buffers, xs[i], 0.53D, 0.25D, 0.16F);
        renderStack(be, be.inventory().getStackInSlot(ChargingStationBlockEntity.CHARGING_SLOT), pose, buffers, 0.0D, 0.60D, 0.0D, 0.20F);
        for (int i=0;i<ChargingStationBlockEntity.OUTPUT_COUNT;i++)
            renderStack(be, be.inventory().getStackInSlot(ChargingStationBlockEntity.OUTPUT_START+i), pose, buffers, xs[i], 0.53D, -0.25D, 0.16F);
        pose.popPose();
    }

    private static void renderStack(ChargingStationBlockEntity be, ItemStack stack, PoseStack pose, MultiBufferSource buffers,
                                    double x, double y, double z, float scale) {
        if (stack == null || stack.isEmpty()) return;
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                pose, buffers, be.getLevel(), (int)(be.getBlockPos().asLong() + x*100 + z*100));
        pose.popPose();
    }
}

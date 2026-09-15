package celerbi.mirageprojector.client;

import celerbi.mirageprojector.block.MirageLightProjectorBlock;
import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
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

/** Renders the real inserted rechargeable medium in the projector's rear cradle. */
public final class MirageLightProjectorRenderer implements BlockEntityRenderer<MirageLightProjectorBlockEntity> {
    public MirageLightProjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            MirageLightProjectorBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (blockEntity.getLevel() == null || !blockEntity.hasEnergyCell()) {
            return;
        }
        ItemStack cell = blockEntity.energyCell();
        Direction facing = blockEntity.getBlockState().hasProperty(MirageLightProjectorBlock.FACING)
                ? blockEntity.getBlockState().getValue(MirageLightProjectorBlock.FACING)
                : Direction.NORTH;
        float yaw = switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.38D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.translate(0.0D, 0.0D, 0.19D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.22F, 0.22F, 0.22F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                cell,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                (int) blockEntity.getBlockPos().asLong()
        );
        poseStack.popPose();
    }
}

package celerbi.mirageprojector.client;

import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class CoreBoosterRenderer implements BlockEntityRenderer<CoreBoosterBlockEntity> {
    public CoreBoosterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            CoreBoosterBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        ItemStack coreStack = blockEntity.material().centerStack();
        if (coreStack.isEmpty()) {
            return;
        }

        double gameTime = blockEntity.getLevel().getGameTime() + partialTick;
        long seed = blockEntity.getBlockPos().asLong();
        float rotation = (float) ((gameTime * 3.0D + Math.floorMod(seed, 360L)) % 360.0D);
        float bob = (float) (Math.sin(gameTime * 0.10D + Math.floorMod(seed, 97L)) * 0.010D);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(18.0F));
        poseStack.scale(0.27F, 0.27F, 0.27F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                coreStack,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                (int) seed
        );
        poseStack.popPose();
    }
}

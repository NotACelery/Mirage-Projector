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
        ItemStack glowDust = blockEntity.hasChargingDust()
                ? blockEntity.chargingDust()
                : ItemStack.EMPTY;
        if (coreStack.isEmpty() && glowDust.isEmpty()) {
            return;
        }

        double gameTime = blockEntity.getLevel().getGameTime() + partialTick;
        long seed = blockEntity.getBlockPos().asLong();
        float rotation = (float) ((gameTime * 3.0D + Math.floorMod(seed, 360L)) % 360.0D);
        float bob = (float) (Math.sin(gameTime * 0.10D + Math.floorMod(seed, 97L)) * 0.010D);

        if (!coreStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, (glowDust.isEmpty() ? 0.50D : 0.42D) + bob, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(18.0F));
            poseStack.scale(0.24F, 0.24F, 0.24F);
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

        if (!glowDust.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, (coreStack.isEmpty() ? 0.50D : 0.61D) - bob * 0.35D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 1.20F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
            // The charging medium stays deliberately compact and on the central axis so
            // generated-item geometry cannot clip through the Booster's inner glass shells.
            poseStack.scale(0.13F, 0.13F, 0.13F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    glowDust,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    (int) (seed ^ 0x5A17C9E3L)
            );
            poseStack.popPose();
        }
    }
}

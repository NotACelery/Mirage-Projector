package celerbi.mirageprojector.client;

import celerbi.mirageprojector.blockentity.ImprovedCoreBlockEntity;
import celerbi.mirageprojector.registry.ModBlocks;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders exactly one physical material item at the center of an Improved Core.
 * This deliberately mirrors the projector chamber's holographic Core behavior:
 * fixed/world-like item transform, slight tilt, clockwise Y rotation, full-bright.
 */
public final class ImprovedCoreRenderer implements BlockEntityRenderer<ImprovedCoreBlockEntity> {
    public ImprovedCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            ImprovedCoreBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        ItemStack coreStack = centerStack(blockEntity.getBlockState());
        if (coreStack.isEmpty()) {
            return;
        }

        double gameTime = blockEntity.getLevel().getGameTime() + partialTick;
        long seed = blockEntity.getBlockPos().asLong();
        float rotation = (float) ((gameTime * 3.0D + Math.floorMod(seed, 360L)) % 360.0D);
        float bob = (float) (Math.sin(gameTime * 0.10D + Math.floorMod(seed, 97L)) * 0.0125D);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(18.0F));
        poseStack.scale(0.32F, 0.32F, 0.32F);
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

    private static ItemStack centerStack(BlockState state) {
        if (state.is(ModBlocks.IMPROVED_GLASS_CORE.get())) {
            return Items.GLASS.getDefaultInstance();
        }
        if (state.is(ModBlocks.IMPROVED_QUARTZ_CORE.get())) {
            return Items.QUARTZ.getDefaultInstance();
        }
        if (state.is(ModBlocks.IMPROVED_AMETHYST_CORE.get())) {
            return Items.AMETHYST_SHARD.getDefaultInstance();
        }
        if (state.is(ModBlocks.IMPROVED_DIAMOND_CORE.get())) {
            return Items.DIAMOND.getDefaultInstance();
        }
        if (state.is(ModBlocks.IMPROVED_NETHERITE_CORE.get())) {
            return Items.NETHERITE_INGOT.getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }
}

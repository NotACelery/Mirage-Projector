package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.client.ProjectionRenderBuffers;
import celerbi.mirageprojector.client.ProjectionRenderContext;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private MultiBufferSource mirageProjector$ghostHeldItemBuffer(MultiBufferSource original) {
        ProjectionSettings settings = ProjectionRenderContext.activeSettings();
        if (settings == null || settings.opacityPercent() >= 100) {
            return original;
        }
        return ProjectionRenderBuffers.wrapHeldItem(original, settings, ProjectionRenderContext.lateDepthStableGhost());
    }
}

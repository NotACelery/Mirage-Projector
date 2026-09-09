package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.client.ProjectionRenderBuffers;
import celerbi.mirageprojector.client.ProjectionRenderContext;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Normalizes every third-person item held by a Mirage projection onto the same
 * colour-only Ghost pipeline used by its body and armor.
 *
 * <p>The normal entity buffer wrapper cannot see every path chosen internally
 * by ItemRenderer: block/item models may request chunk-style solid/cutout
 * layers. Those layers still write depth and caused water behind a projected
 * sword/tool/block to disappear. This hook is active only while Mirage is
 * rendering its temporary entity and replaces just the MultiBufferSource
 * passed to ItemInHandRenderer.</p>
 */
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
        return ProjectionRenderBuffers.wrapHeldItem(original, settings);
    }
}

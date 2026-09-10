package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.ProjectionRenderContext;
import celerbi.mirageprojector.client.ProjectionRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Extends Mirage's ghost pass to colored cutout layers such as cat collars.
 * Without this, the opaque/cutout overlay writes depth on top of a no-depth-write
 * projected body and can appear to float in front of the entity in the preview/world.
 */
@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {
    @Redirect(
            method = "coloredCutoutModelCopyLayerRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            ),
            require = 1
    )
    private static RenderType mirageProjector$ghostColoredCutout(ResourceLocation texture) {
        if (ProjectionRenderContext.ghostActive()) {
            return ProjectionRenderTypes.ghostEntity(texture);
        }
        return RenderType.entityCutoutNoCull(texture);
    }
}

package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.ProjectionRenderContext;
import celerbi.mirageprojector.client.ProjectionRenderTypes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Routes only Mirage's base LivingEntity body through the no-depth-write ghost pass. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void mirageProjector$projectionRenderType(
            T entity,
            boolean showBody,
            boolean translucent,
            boolean showOutline,
            CallbackInfoReturnable<RenderType> callback
    ) {
        if (!ProjectionRenderContext.ghostActiveFor(entity) || !showBody || showOutline) {
            return;
        }

        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        callback.setReturnValue(ProjectionRenderTypes.ghostEntity(renderer.getTextureLocation(entity)));
    }
}

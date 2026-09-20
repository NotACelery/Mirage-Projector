package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.ProjectionRenderContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps a fish snapshot in its aquatic render state without changing the world around it. */
@Mixin(Entity.class)
public abstract class EntityWaterProjectionMixin {
    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void mirageProjector$fishProjectionIsAquatic(CallbackInfoReturnable<Boolean> callback) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof AbstractFish && ProjectionRenderContext.activeEntity() == entity) {
            callback.setReturnValue(true);
        }
    }
}

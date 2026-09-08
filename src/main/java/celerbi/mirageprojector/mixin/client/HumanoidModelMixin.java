package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.HumanoidPoseController;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies projector-owned poses after vanilla has built the normal humanoid frame. */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {
    @Inject(method = "setupAnim*", at = @At("RETURN"))
    private void mirageProjector$applyProjectionPose(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo callbackInfo
    ) {
        HumanoidPoseController.applyToModel(entity, (HumanoidModel<?>) (Object) this);
    }
}

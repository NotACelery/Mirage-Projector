package celerbi.mirageprojector.mixin;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Covers gameplay/debug callers that ask LevelLightEngine for combined raw brightness
 * directly. This is deliberately above BlockLightEngine: Mirage values are never
 * returned from the vanilla block-light propagation engine itself.
 */
@Mixin(LevelLightEngine.class)
public abstract class LevelLightEngineMirageLightMixin {
    @Inject(method = "getRawBrightness", at = @At("RETURN"), cancellable = true)
    private void mirage$mergeRawBrightness(
            BlockPos pos,
            int ambientDarkness,
            CallbackInfoReturnable<Integer> cir
    ) {
        int mirage = MirageLightEngine.virtualBlockLight((LevelLightEngine) (Object) this, pos);
        if (mirage > cir.getReturnValueI()) {
            cir.setReturnValue(mirage);
        }
    }
}

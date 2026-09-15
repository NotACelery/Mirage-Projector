package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Final visual bridge for Mirage virtual block light.
 *
 * <p>Gameplay/debug queries already see the virtual field through Level/LevelLightEngine, but
 * baked block vertices consume LevelRenderer packed light coordinates. Merge the Mirage block
 * channel there as well so the field is visible, not merely detectable by light-level overlays.</p>
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMirageLightMixin {
    @Inject(
            method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void mirage$mergePackedLight(
            BlockAndTintGetter level,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        merge(level, pos, cir);
    }

    @Inject(
            method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void mirage$mergePackedLightWithState(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        merge(level, pos, cir);
    }

    private static void merge(
            BlockAndTintGetter level,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (level == null || pos == null) {
            return;
        }
        int packed = cir.getReturnValueI();

        // Do not query BlockAndTintGetter#getLightEngine() here. Sodium supplies a
        // LevelSlice during chunk meshing whose getLightEngine() intentionally throws
        // UnsupportedOperationException. This mixin is client-only, so the authoritative
        // Mirage render field is the active ClientLevel regardless of the temporary view
        // object used by the chunk compiler.
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) {
            return;
        }
        int virtualBlock = MirageLightEngine.virtualBlockLight(clientLevel, pos);
        int vanillaBlock = LightTexture.block(packed);
        if (virtualBlock <= vanillaBlock) {
            return;
        }
        cir.setReturnValue(LightTexture.pack(virtualBlock, LightTexture.sky(packed)));
    }
}

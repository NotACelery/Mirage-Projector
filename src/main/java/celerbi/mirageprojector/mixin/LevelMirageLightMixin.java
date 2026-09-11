package celerbi.mirageprojector.mixin;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * dev.75a authoritative Mirage-light bridge for real Level queries.
 *
 * Mirage light is merged only at read time. It is never fed back into vanilla's
 * BlockLightEngine as a new emission source, so the solved virtual field cannot
 * recursively spread or create hidden secondary emitters.
 */
@Mixin(Level.class)
public abstract class LevelMirageLightMixin {
    @Shadow
    public abstract LevelLightEngine getLightEngine();

    public int getBrightness(LightLayer lightType, BlockPos pos) {
        int vanilla = getLightEngine().getLayerListener(lightType).getLightValue(pos);
        if (lightType != LightLayer.BLOCK) {
            return vanilla;
        }
        return Math.max(vanilla, MirageLightEngine.virtualBlockLight(getLightEngine(), pos));
    }

    public int getRawBrightness(BlockPos pos, int ambientDarkness) {
        int sky = getBrightness(LightLayer.SKY, pos) - ambientDarkness;
        int block = getBrightness(LightLayer.BLOCK, pos);
        return Math.max(block, Math.max(sky, 0));
    }
}

package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Chunk compilation uses RenderChunkRegion rather than ClientLevel directly.
 * Mirror the Level bridge here so baked block vertices see the same virtual
 * Mirage block-light value as ordinary world queries.
 */
@Mixin(RenderChunkRegion.class)
public abstract class RenderChunkRegionMirageLightMixin {
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

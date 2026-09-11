package celerbi.mirageprojector.light.engine;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

/** Stable facade for future static and dynamic Mirage lighting consumers. */
public final class MirageLightEngine {
    private MirageLightEngine() {
    }

    public static MirageLightWorld.UpdateResult updateSource(
            Level level,
            MirageLightSource source,
            boolean forceRebuild
    ) {
        return MirageLightWorld.updateSource(level, source, forceRebuild);
    }

    public static boolean removeSource(Level level, MirageLightSourceId sourceId) {
        return MirageLightWorld.removeSource(level, sourceId);
    }

    public static int virtualBlockLight(Level level, BlockPos pos) {
        return MirageLightWorld.levelAt(level, pos);
    }

    public static int virtualBlockLight(LevelLightEngine lightEngine, BlockPos pos) {
        return MirageLightWorld.levelAt(lightEngine, pos);
    }

    public static Set<Long> sectionKeys(Level level) {
        return MirageLightWorld.sectionKeys(level);
    }

    @Nullable
    public static MirageLightField field(Level level, MirageLightSourceId sourceId) {
        return MirageLightWorld.field(level, sourceId);
    }

    public static List<MirageLightSource> sources(Level level) {
        return MirageLightWorld.sources(level);
    }

    public static MirageLightWorld.WorldStats stats(Level level) {
        return MirageLightWorld.stats(level);
    }

    /**
     * Exact horizontal intersection test between a source's Manhattan radius and a
     * chunk column. Used by chunk lifecycle and tracking-scoped source delivery.
     */
    public static boolean sourceTouchesChunk(MirageLightSource source, ChunkPos chunkPos) {
        if (source == null || chunkPos == null || !source.active()) {
            return false;
        }
        BlockPos origin = source.origin();
        int dx = distanceToRange(origin.getX(), chunkPos.getMinBlockX(), chunkPos.getMaxBlockX());
        int dz = distanceToRange(origin.getZ(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ());
        return dx + dz <= source.profile().maxRadius();
    }

    private static int distanceToRange(int value, int min, int max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0;
    }

    public static void clear(Level level) {
        MirageLightWorld.clear(level);
    }
}

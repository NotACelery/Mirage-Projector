package celerbi.mirageprojector.light.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public static List<MirageLightSource> removeSourcesAtOrigin(Level level, BlockPos origin) {
        return MirageLightWorld.removeSourcesAtOrigin(level, origin);
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

    public static void installAuthoritativeSection(Level level, long sectionKey, byte[] levels) {
        MirageLightWorld.setAuthoritativeSection(level, sectionKey, levels);
    }

    public static void clearAuthoritativeSection(Level level, long sectionKey) {
        MirageLightWorld.clearAuthoritativeSection(level, sectionKey);
    }

    public static Set<Long> clearAuthoritativeChunk(Level level, ChunkPos chunkPos) {
        return MirageLightWorld.clearAuthoritativeChunk(level, chunkPos);
    }

    public static Set<Long> replaceAuthoritativeChunk(Level level, ChunkPos chunkPos, Map<Long, byte[]> sections) {
        return MirageLightWorld.replaceAuthoritativeChunk(level, chunkPos, sections);
    }

    public static byte[] copyAggregateSectionLevels(Level level, long sectionKey) {
        return MirageLightWorld.copyAggregateSectionLevels(level, sectionKey);
    }

    public static Set<Long> aggregateSectionKeysForChunk(Level level, ChunkPos chunkPos) {
        return MirageLightWorld.aggregateSectionKeysForChunk(level, chunkPos);
    }

    public static Set<Long> authoritativeSectionKeysForChunk(Level level, ChunkPos chunkPos) {
        return MirageLightWorld.authoritativeSectionKeysForChunk(level, chunkPos);
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

    /**
     * Broad dependency-window membership used by chunk lifecycle convergence.
     * Unlike sourceTouchesChunk(), this includes the entire axis-aligned chunk window
     * covered by origin +/- radius so edge/corner chunk transitions cannot be missed.
     */
    public static boolean sourceDependsOnChunk(MirageLightSource source, ChunkPos chunkPos) {
        if (source == null || chunkPos == null || !source.active()) {
            return false;
        }
        BlockPos origin = source.origin();
        int radius = source.profile().maxRadius();
        int minChunkX = Math.floorDiv(origin.getX() - radius, 16);
        int maxChunkX = Math.floorDiv(origin.getX() + radius, 16);
        int minChunkZ = Math.floorDiv(origin.getZ() - radius, 16);
        int maxChunkZ = Math.floorDiv(origin.getZ() + radius, 16);
        return chunkPos.x >= minChunkX && chunkPos.x <= maxChunkX
                && chunkPos.z >= minChunkZ && chunkPos.z <= maxChunkZ;
    }


    /**
     * Complete chunk-column dependency window for one source, independent of which
     * chunks happen to be loaded or which voxels a previous solve managed to reach.
     *
     * For the normal Mature Cluster radius (~30 blocks) this is at most a 5x5 window
     * around the source chunk depending on the source's local position inside that
     * chunk. Boosted profiles automatically expand the window from their real radius.
     *
     * We deliberately use the full axis-aligned chunk window instead of the tighter
     * Manhattan diamond here. These are only readiness checks (not forced chunk loads),
     * so the extra edge/corner probes are cheap and make chunk-load convergence robust
     * against a solve being clipped before it can discover the next boundary itself.
     */
    public static Set<Long> dependencyChunks(MirageLightSource source) {
        if (source == null || !source.active()) {
            return Set.of();
        }

        BlockPos origin = source.origin();
        int radius = source.profile().maxRadius();
        int minChunkX = Math.floorDiv(origin.getX() - radius, 16);
        int maxChunkX = Math.floorDiv(origin.getX() + radius, 16);
        int minChunkZ = Math.floorDiv(origin.getZ() - radius, 16);
        int maxChunkZ = Math.floorDiv(origin.getZ() + radius, 16);

        Set<Long> result = new HashSet<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                result.add(ChunkPos.asLong(chunkX, chunkZ));
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Queryable subset of {@link #dependencyChunks(MirageLightSource)}.
     *
     * This snapshot must be captured BEFORE a solve when it is stored as the solve's
     * readiness fingerprint. Capturing it after the solve creates a race where a chunk
     * can become queryable after the solver already hit that border, causing the field
     * to remain clipped while the stored fingerprint falsely claims the chunk was used.
     */
    public static Set<Long> queryableDependencyChunks(Level level, MirageLightSource source) {
        if (level == null || source == null || !source.active()) {
            return Set.of();
        }

        Set<Long> result = new HashSet<>();
        for (long packed : dependencyChunks(source)) {
            ChunkPos chunkPos = new ChunkPos(packed);
            if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) != null) {
                result.add(packed);
            }
        }
        return Set.copyOf(result);
    }

    /**
     * dev.76c atomic STATIC_WORLD readiness gate.
     *
     * A static source is publishable only when every chunk column in its complete
     * dependency window is attached/queryable on the authoritative server. This does
     * not force-load chunks; it only prevents a clipped solve from becoming visible.
     */
    public static boolean allDependencyChunksQueryable(Level level, MirageLightSource source) {
        if (level == null || source == null || !source.active()) {
            return false;
        }
        Set<Long> dependencies = dependencyChunks(source);
        if (dependencies.isEmpty()) {
            return false;
        }
        for (long packed : dependencies) {
            ChunkPos chunkPos = new ChunkPos(packed);
            if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Backward-compatible alias used by older diagnostics/tests. dev.75h intentionally
     * widens readiness tracking from the exact touched diamond to the complete dependency
     * window, so callers converge even when a previous solve was clipped at a boundary.
     */
    public static Set<Long> queryableChunksTouched(Level level, MirageLightSource source) {
        return queryableDependencyChunks(level, source);
    }

    /** Exact readiness predicate shared by the solver and lifecycle convergence code. */
    public static boolean isChunkQueryable(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
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

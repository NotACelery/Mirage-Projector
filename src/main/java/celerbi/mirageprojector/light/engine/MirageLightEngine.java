package celerbi.mirageprojector.light.engine;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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

    public static void clear(Level level) {
        MirageLightWorld.clear(level);
    }
}

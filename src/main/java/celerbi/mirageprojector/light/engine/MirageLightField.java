package celerbi.mirageprojector.light.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

/** Immutable solved contribution of one source. */
public final class MirageLightField {
    private final MirageLightSource source;
    private final Map<Long, MirageLightSection> sections;
    private final SolveStats stats;

    MirageLightField(
            MirageLightSource source,
            Map<Long, MirageLightSection> sections,
            SolveStats stats
    ) {
        this.source = source;
        this.sections = Collections.unmodifiableMap(new LinkedHashMap<>(sections));
        this.stats = stats;
    }

    public MirageLightSource source() {
        return source;
    }

    public Map<Long, MirageLightSection> sections() {
        return sections;
    }

    public SolveStats stats() {
        return stats;
    }

    public int energyAt(BlockPos pos) {
        MirageLightSection section = sections.get(SectionPos.asLong(pos));
        return section == null ? 0 : section.energyAt(pos);
    }

    public int visibleLevelAt(BlockPos pos) {
        return source.profile().visibleLevelFromEnergy(energyAt(pos));
    }

    public int sectionCount() {
        return sections.size();
    }

    public record SolveStats(
            int queuedNodes,
            int settledNodes,
            int litVoxels,
            int blockedEdges,
            int unloadedEdges,
            int maxQueueSize,
            long solveNanos
    ) {
        public double solveMillis() {
            return solveNanos / 1_000_000.0D;
        }
    }
}

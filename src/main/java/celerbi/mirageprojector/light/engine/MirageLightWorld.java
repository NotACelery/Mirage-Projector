package celerbi.mirageprojector.light.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Per-Level source fields plus an O(1) max-light aggregate layer. */
public final class MirageLightWorld {
    private static final Map<Level, LevelState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private MirageLightWorld() {
    }

    public static UpdateResult updateSource(Level level, MirageLightSource source, boolean forceRebuild) {
        if (level == null || source == null) {
            return UpdateResult.SKIPPED;
        }
        if (!source.active()) {
            boolean removed = removeSource(level, source.id());
            return removed ? UpdateResult.REMOVED : UpdateResult.SKIPPED;
        }

        LevelState state = state(level);
        synchronized (state) {
            SourceEntry previous = state.sources.get(source.id());
            if (!forceRebuild && previous != null && previous.source.equals(source)) {
                return new UpdateResult(false, false, previous.field.stats());
            }

            MirageLightField field = MirageLightSolver.solve(level, source);
            if (previous != null) {
                state.removeFieldContribution(previous.field);
            }
            state.sources.put(source.id(), new SourceEntry(source, field));
            state.addFieldContribution(field);
            return new UpdateResult(true, false, field.stats());
        }
    }

    public static boolean removeSource(Level level, MirageLightSourceId sourceId) {
        if (level == null || sourceId == null) {
            return false;
        }
        LevelState state = existingState(level);
        if (state == null) {
            return false;
        }
        synchronized (state) {
            SourceEntry removed = state.sources.remove(sourceId);
            if (removed == null) {
                return false;
            }
            state.removeFieldContribution(removed.field);
            return true;
        }
    }

    public static int levelAt(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        LevelState state = existingState(level);
        if (state == null) {
            return 0;
        }
        synchronized (state) {
            AggregateSection section = state.aggregateSections.get(SectionPos.asLong(pos));
            return section == null ? 0 : section.levelAt(pos);
        }
    }

    @Nullable
    public static MirageLightField field(Level level, MirageLightSourceId sourceId) {
        LevelState state = existingState(level);
        if (state == null) {
            return null;
        }
        synchronized (state) {
            SourceEntry entry = state.sources.get(sourceId);
            return entry == null ? null : entry.field;
        }
    }

    public static List<MirageLightSource> sources(Level level) {
        LevelState state = existingState(level);
        if (state == null) {
            return List.of();
        }
        synchronized (state) {
            return state.sources.values().stream().map(SourceEntry::source).toList();
        }
    }

    public static WorldStats stats(Level level) {
        LevelState state = existingState(level);
        if (state == null) {
            return new WorldStats(0, 0, 0);
        }
        synchronized (state) {
            int litCells = state.aggregateSections.values().stream()
                    .mapToInt(AggregateSection::nonZeroCount)
                    .sum();
            return new WorldStats(state.sources.size(), state.aggregateSections.size(), litCells);
        }
    }

    public static void clear(Level level) {
        if (level != null) {
            synchronized (STATES) {
                STATES.remove(level);
            }
        }
    }

    private static LevelState state(Level level) {
        synchronized (STATES) {
            return STATES.computeIfAbsent(level, ignored -> new LevelState());
        }
    }

    @Nullable
    private static LevelState existingState(Level level) {
        synchronized (STATES) {
            return STATES.get(level);
        }
    }

    public record UpdateResult(
            boolean rebuilt,
            boolean removed,
            @Nullable MirageLightField.SolveStats solveStats
    ) {
        private static final UpdateResult SKIPPED = new UpdateResult(false, false, null);
        private static final UpdateResult REMOVED = new UpdateResult(false, true, null);
    }

    public record WorldStats(int sources, int sections, int litCells) {
    }

    private record SourceEntry(MirageLightSource source, MirageLightField field) {
    }

    private static final class LevelState {
        private final Map<MirageLightSourceId, SourceEntry> sources = new HashMap<>();
        private final Map<Long, AggregateSection> aggregateSections = new HashMap<>();

        void addFieldContribution(MirageLightField field) {
            MirageLightSourceId sourceId = field.source().id();
            MirageLightProfile profile = field.source().profile();
            for (Map.Entry<Long, MirageLightSection> entry : field.sections().entrySet()) {
                aggregateSections.computeIfAbsent(entry.getKey(), ignored -> new AggregateSection())
                        .put(sourceId, entry.getValue(), profile);
            }
        }

        void removeFieldContribution(MirageLightField field) {
            Set<Long> emptySections = new HashSet<>();
            for (Long sectionKey : field.sections().keySet()) {
                AggregateSection aggregate = aggregateSections.get(sectionKey);
                if (aggregate == null) {
                    continue;
                }
                aggregate.remove(field.source().id());
                if (aggregate.empty()) {
                    emptySections.add(sectionKey);
                }
            }
            emptySections.forEach(aggregateSections::remove);
        }
    }

    private static final class AggregateSection {
        /*
         * Keep references to immutable solved sections instead of materializing a second
         * 4096-byte visible-light array for every source/section pair. The fixed-point
         * field remains authoritative; only the aggregate max layer is materialized.
         */
        private final Map<MirageLightSourceId, SectionContribution> contributions = new HashMap<>();
        private final byte[] levels = new byte[MirageLightSection.SIZE];
        private int nonZeroCount;

        void put(
                MirageLightSourceId sourceId,
                MirageLightSection section,
                MirageLightProfile profile
        ) {
            contributions.put(sourceId, new SectionContribution(section, profile));
            rebuild();
        }

        void remove(MirageLightSourceId sourceId) {
            if (contributions.remove(sourceId) != null) {
                rebuild();
            }
        }

        int levelAt(BlockPos pos) {
            return Byte.toUnsignedInt(levels[MirageLightSection.index(pos)]);
        }

        int nonZeroCount() {
            return nonZeroCount;
        }

        boolean empty() {
            return contributions.isEmpty();
        }

        private void rebuild() {
            java.util.Arrays.fill(levels, (byte) 0);
            nonZeroCount = 0;
            for (SectionContribution contribution : contributions.values()) {
                for (int i = 0; i < levels.length; i++) {
                    int energy = contribution.section().energyAtIndex(i);
                    if (energy <= 0) {
                        continue;
                    }
                    int candidate = contribution.profile().visibleLevelFromEnergy(energy);
                    if (candidate > Byte.toUnsignedInt(levels[i])) {
                        levels[i] = (byte) candidate;
                    }
                }
            }
            for (byte level : levels) {
                if (level != 0) {
                    nonZeroCount++;
                }
            }
        }
    }

    private record SectionContribution(MirageLightSection section, MirageLightProfile profile) {
    }
}

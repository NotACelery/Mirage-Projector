package celerbi.mirageprojector.light.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

/** Per-Level source fields plus an O(1) max-light aggregate layer. */
public final class MirageLightWorld {
    private static final Map<Level, LevelState> STATES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<LevelLightEngine, LevelState> ENGINE_STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private MirageLightWorld() {
    }

    public static UpdateResult updateSource(Level level, MirageLightSource source, boolean forceRebuild) {
        if (level == null || source == null) {
            return UpdateResult.SKIPPED;
        }
        // dev.76 hard authority boundary: STATIC_WORLD geometry is never solved on a
        // client Level. Clients install server-resolved section snapshots instead.
        if (level.isClientSide && source.runtimeMode() == MirageLightRuntimeMode.STATIC_WORLD) {
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
                return new UpdateResult(false, false, previous.field.stats(), Set.of());
            }

            // Snapshot only the aggregate sections that existed before this source update.
            // New-only sections are always changed; existing sections are compared byte-for-byte
            // after max aggregation so overlapping sources do not cause redundant network sends.
            Map<Long, byte[]> before = new HashMap<>();
            if (previous != null) {
                for (Long sectionKey : previous.field.sections().keySet()) {
                    AggregateSection aggregate = state.aggregateSections.get(sectionKey);
                    before.put(sectionKey, aggregate == null ? null : aggregate.copyLevels());
                }
            }

            MirageLightField field = MirageLightSolver.solve(level, source);

            // dev.76c atomic publication contract: a STATIC_WORLD solve that touched an
            // unavailable chunk is an incomplete candidate, never authoritative state.
            // Keep the previous complete field (if any) untouched and let the source
            // lifecycle retry after its full dependency window becomes queryable.
            if (source.runtimeMode() == MirageLightRuntimeMode.STATIC_WORLD
                    && field.stats().unloadedEdges() > 0) {
                return new UpdateResult(false, false, field.stats(), Set.of());
            }

            state.sources.put(source.id(), new SourceEntry(source, field));
            if (previous == null) {
                state.addFieldContribution(field);
            } else {
                state.replaceFieldContribution(previous.field, field);
            }

            Set<Long> touched = new HashSet<>(field.sections().keySet());
            if (previous != null) {
                touched.addAll(previous.field.sections().keySet());
            }
            Set<Long> changedSections = new HashSet<>();
            for (Long sectionKey : touched) {
                AggregateSection aggregate = state.aggregateSections.get(sectionKey);
                byte[] after = aggregate == null ? null : aggregate.copyLevels();
                if (!before.containsKey(sectionKey) || !sameLevels(before.get(sectionKey), after)) {
                    changedSections.add(sectionKey);
                }
            }
            return new UpdateResult(true, false, field.stats(), Set.copyOf(changedSections));
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

    /**
     * Remove every virtual source anchored at one world position.
     *
     * This is intentionally stronger than source-id removal. Block-backed sources may
     * survive development-version migrations or tracking drift under an older kind/id;
     * once the physical source block is gone, no virtual field at that exact origin is
     * allowed to survive.
     */
    public static List<MirageLightSource> removeSourcesAtOrigin(Level level, BlockPos origin) {
        if (level == null || origin == null) {
            return List.of();
        }
        LevelState state = existingState(level);
        if (state == null) {
            return List.of();
        }
        synchronized (state) {
            List<MirageLightSourceId> ids = state.sources.entrySet().stream()
                    .filter(entry -> entry.getValue().source.origin().equals(origin))
                    .map(Map.Entry::getKey)
                    .toList();
            if (ids.isEmpty()) {
                return List.of();
            }
            java.util.ArrayList<MirageLightSource> removedSources = new java.util.ArrayList<>(ids.size());
            for (MirageLightSourceId id : ids) {
                SourceEntry removed = state.sources.remove(id);
                if (removed == null) {
                    continue;
                }
                removedSources.add(removed.source);
                state.removeFieldContribution(removed.field);
            }
            return List.copyOf(removedSources);
        }
    }

    public static int levelAt(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        return levelAt(existingState(level), pos);
    }

    public static int levelAt(LevelLightEngine lightEngine, BlockPos pos) {
        if (lightEngine == null || pos == null) {
            return 0;
        }
        LevelState state;
        synchronized (ENGINE_STATES) {
            state = ENGINE_STATES.get(lightEngine);
        }
        return levelAt(state, pos);
    }

    private static int levelAt(@Nullable LevelState state, BlockPos pos) {
        if (state == null) {
            return 0;
        }
        synchronized (state) {
            long sectionKey = SectionPos.asLong(pos);
            int solved = 0;
            AggregateSection aggregate = state.aggregateSections.get(sectionKey);
            if (aggregate != null) {
                solved = aggregate.levelAt(pos);
            }
            AuthoritativeSection authoritative = state.authoritativeSections.get(sectionKey);
            if (authoritative != null) {
                solved = Math.max(solved, authoritative.levelAt(pos));
            }
            return solved;
        }
    }

    /**
     * Install one server-resolved STATIC_WORLD section on a client Level.
     * Values are final visible Mirage block-light levels (0..15), never source energy.
     */
    public static void setAuthoritativeSection(Level level, long sectionKey, byte[] levels) {
        if (level == null || levels == null || levels.length != MirageLightSection.SIZE) {
            return;
        }
        LevelState state = state(level);
        synchronized (state) {
            AuthoritativeSection section = new AuthoritativeSection(levels);
            if (section.nonZeroCount() == 0) {
                state.authoritativeSections.remove(sectionKey);
            } else {
                state.authoritativeSections.put(sectionKey, section);
            }
        }
    }

    public static void clearAuthoritativeSection(Level level, long sectionKey) {
        LevelState state = existingState(level);
        if (state == null) {
            return;
        }
        synchronized (state) {
            state.authoritativeSections.remove(sectionKey);
        }
    }

    public static Set<Long> clearAuthoritativeChunk(Level level, ChunkPos chunkPos) {
        LevelState state = existingState(level);
        if (state == null || chunkPos == null) {
            return Set.of();
        }
        synchronized (state) {
            Set<Long> removed = new HashSet<>();
            for (Long sectionKey : List.copyOf(state.authoritativeSections.keySet())) {
                SectionPos section = SectionPos.of(sectionKey);
                if (section.x() == chunkPos.x && section.z() == chunkPos.z) {
                    state.authoritativeSections.remove(sectionKey);
                    removed.add(sectionKey);
                }
            }
            return Set.copyOf(removed);
        }
    }


    /**
     * Atomically replace all authoritative client-mirror sections for one chunk column.
     * The state lock prevents readers from observing a temporary clear-before-set hole.
     */
    public static Set<Long> replaceAuthoritativeChunk(
            Level level,
            ChunkPos chunkPos,
            Map<Long, byte[]> replacement
    ) {
        if (level == null || chunkPos == null) {
            return Set.of();
        }
        LevelState state = state(level);
        Map<Long, byte[]> safeReplacement = replacement == null ? Map.of() : replacement;
        synchronized (state) {
            Map<Long, byte[]> normalized = new HashMap<>();
            for (Map.Entry<Long, byte[]> entry : safeReplacement.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null
                        || entry.getValue().length != MirageLightSection.SIZE) {
                    continue;
                }
                SectionPos section = SectionPos.of(entry.getKey());
                if (section.x() != chunkPos.x || section.z() != chunkPos.z) {
                    continue;
                }
                normalized.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
            }

            Set<Long> oldKeys = new HashSet<>();
            for (Long sectionKey : state.authoritativeSections.keySet()) {
                SectionPos section = SectionPos.of(sectionKey);
                if (section.x() == chunkPos.x && section.z() == chunkPos.z) {
                    oldKeys.add(sectionKey);
                }
            }

            Set<Long> touched = new HashSet<>(oldKeys);
            touched.addAll(normalized.keySet());
            Set<Long> dirty = new HashSet<>();
            for (Long sectionKey : touched) {
                AuthoritativeSection oldSection = state.authoritativeSections.get(sectionKey);
                byte[] nextLevels = normalized.get(sectionKey);
                byte[] oldLevels = oldSection == null ? null : oldSection.copyLevels();
                if (!sameLevels(oldLevels, nextLevels)) {
                    dirty.add(sectionKey);
                }
            }

            oldKeys.forEach(state.authoritativeSections::remove);
            for (Map.Entry<Long, byte[]> entry : normalized.entrySet()) {
                AuthoritativeSection section = new AuthoritativeSection(entry.getValue());
                if (section.nonZeroCount() > 0) {
                    state.authoritativeSections.put(entry.getKey(), section);
                }
            }
            return Set.copyOf(dirty);
        }
    }

    public static byte[] copyAggregateSectionLevels(Level level, long sectionKey) {
        LevelState state = existingState(level);
        if (state == null) {
            return null;
        }
        synchronized (state) {
            AggregateSection section = state.aggregateSections.get(sectionKey);
            return section == null ? null : section.copyLevels();
        }
    }

    public static Set<Long> aggregateSectionKeysForChunk(Level level, ChunkPos chunkPos) {
        LevelState state = existingState(level);
        if (state == null || chunkPos == null) {
            return Set.of();
        }
        synchronized (state) {
            Set<Long> result = new HashSet<>();
            for (Long sectionKey : state.aggregateSections.keySet()) {
                SectionPos section = SectionPos.of(sectionKey);
                if (section.x() == chunkPos.x && section.z() == chunkPos.z) {
                    result.add(sectionKey);
                }
            }
            return Set.copyOf(result);
        }
    }

    /** Client-mirror section keys installed from authoritative server snapshots. */
    public static Set<Long> authoritativeSectionKeysForChunk(Level level, ChunkPos chunkPos) {
        LevelState state = existingState(level);
        if (state == null || chunkPos == null) {
            return Set.of();
        }
        synchronized (state) {
            Set<Long> result = new HashSet<>();
            for (Long sectionKey : state.authoritativeSections.keySet()) {
                SectionPos section = SectionPos.of(sectionKey);
                if (section.x() == chunkPos.x && section.z() == chunkPos.z) {
                    result.add(sectionKey);
                }
            }
            return Set.copyOf(result);
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

    public static Set<Long> sectionKeys(Level level) {
        LevelState state = existingState(level);
        if (state == null) {
            return Set.of();
        }
        synchronized (state) {
            Set<Long> result = new HashSet<>(state.aggregateSections.keySet());
            result.addAll(state.authoritativeSections.keySet());
            return Set.copyOf(result);
        }
    }

    public static void clear(Level level) {
        if (level == null) {
            return;
        }
        LevelState removed;
        synchronized (STATES) {
            removed = STATES.remove(level);
        }
        if (removed != null) {
            synchronized (ENGINE_STATES) {
                ENGINE_STATES.remove(level.getLightEngine());
            }
        }
    }

    private static LevelState state(Level level) {
        synchronized (STATES) {
            LevelState state = STATES.computeIfAbsent(level, ignored -> new LevelState());
            synchronized (ENGINE_STATES) {
                ENGINE_STATES.put(level.getLightEngine(), state);
            }
            return state;
        }
    }

    @Nullable
    private static LevelState existingState(Level level) {
        synchronized (STATES) {
            return STATES.get(level);
        }
    }

    private static boolean sameLevels(@Nullable byte[] first, @Nullable byte[] second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return Arrays.equals(first, second);
    }

    public record UpdateResult(
            boolean rebuilt,
            boolean removed,
            @Nullable MirageLightField.SolveStats solveStats,
            Set<Long> changedSections
    ) {
        private static final UpdateResult SKIPPED = new UpdateResult(false, false, null, Set.of());
        private static final UpdateResult REMOVED = new UpdateResult(false, true, null, Set.of());
    }

    public record WorldStats(int sources, int sections, int litCells) {
    }

    private record SourceEntry(MirageLightSource source, MirageLightField field) {
    }

    private static final class LevelState {
        private final Map<MirageLightSourceId, SourceEntry> sources = new HashMap<>();
        private final Map<Long, AggregateSection> aggregateSections = new HashMap<>();
        private final Map<Long, AuthoritativeSection> authoritativeSections = new HashMap<>();

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

        void replaceFieldContribution(MirageLightField previous, MirageLightField updated) {
            MirageLightSourceId sourceId = updated.source().id();
            MirageLightProfile profile = updated.source().profile();
            Set<Long> touched = new HashSet<>(previous.sections().keySet());
            touched.addAll(updated.sections().keySet());
            for (Long sectionKey : touched) {
                MirageLightSection replacement = updated.sections().get(sectionKey);
                AggregateSection aggregate = aggregateSections.get(sectionKey);
                if (replacement == null) {
                    if (aggregate != null) {
                        aggregate.remove(sourceId);
                        if (aggregate.empty()) {
                            aggregateSections.remove(sectionKey);
                        }
                    }
                    continue;
                }
                if (aggregate == null) {
                    aggregate = new AggregateSection();
                    aggregateSections.put(sectionKey, aggregate);
                }
                aggregate.put(sourceId, replacement, profile);
            }
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

        byte[] copyLevels() {
            return Arrays.copyOf(levels, levels.length);
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

    private static final class AuthoritativeSection {
        private final byte[] levels;
        private final int nonZeroCount;

        private AuthoritativeSection(byte[] levels) {
            this.levels = Arrays.copyOf(levels, levels.length);
            int count = 0;
            for (byte level : this.levels) {
                if ((level & 0xFF) != 0) {
                    count++;
                }
            }
            this.nonZeroCount = count;
        }

        int levelAt(BlockPos pos) {
            return Byte.toUnsignedInt(levels[MirageLightSection.index(pos)]);
        }

        int nonZeroCount() {
            return nonZeroCount;
        }

        byte[] copyLevels() {
            return Arrays.copyOf(levels, levels.length);
        }
    }

    private record SectionContribution(MirageLightSection section, MirageLightProfile profile) {
    }
}

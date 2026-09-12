package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.light.LightProfile;
import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightField;
import celerbi.mirageprojector.light.engine.MirageLightProfile;
import celerbi.mirageprojector.light.engine.MirageLightRuntimeMode;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import celerbi.mirageprojector.light.engine.MirageLightWorld;
import celerbi.mirageprojector.network.MirageLightNetwork;
import celerbi.mirageprojector.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mature Crying Obsidian -> Mirage Light Engine adapter.
 *
 * dev.76 publishes server-resolved STATIC_WORLD sections while retaining the causal solver,
 * obstacle-detour decay and old-world cleanup. The old
 * mirage_projector:crying_light_node block remains registered only so worlds from
 * dev.65-dev.74 can load and clean those legacy relays safely.
 */
public final class CryingObsidianLightField {
    private static final int MAX_EFFECTIVE_REFLECTED_BOOST = BeaconRelayState.MAX_EFFECTIVE_BOOSTERS;
    private static final int MAX_CONCEPTUAL_LIGHT = 15 + MAX_EFFECTIVE_REFLECTED_BOOST;
    private static final int MAX_FIELD_RADIUS = MAX_CONCEPTUAL_LIGHT * 2;
    private static final String MIRAGE_LIGHT_SOURCE_KIND = "mature_crying_cluster";
    private static final int CRYING_LIGHT_RGB = 0xA84CFF;

    private static final List<NodeOffset> LEGACY_AXIAL_OFFSETS = createLegacyAxialOffsets();
    private static final List<NodeOffset> LEGACY_DEV69_DIFFUSE_OFFSETS = createLegacyDiffuseOffsets();

    /** Server-side source registry used by same-tick terrain invalidation. */
    private static final Map<ServerLevel, Set<BlockPos>> ACTIVE_SOURCES =
            Collections.synchronizedMap(new WeakHashMap<>());

    /** Sources that requested a rebuild while at least one dependency chunk was absent. */
    private static final Map<ServerLevel, Set<BlockPos>> PENDING_REBUILDS =
            Collections.synchronizedMap(new WeakHashMap<>());


    /**
     * dev.76g source-centric chunk watchdog.
     *
     * Each energized Mature Cluster tracks a cheap signature of the chunk columns in
     * its local watch window. Chunk load/unload and geometry events bump a per-chunk
     * epoch. The expensive light solve runs only when that signature actually changes.
     */
    private static final int WATCHDOG_HEARTBEAT_TICKS = 20;
    private static final Map<ServerLevel, Map<Long, Long>> CHUNK_WATCH_EPOCHS =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<ServerLevel, Map<BlockPos, SourceWatchState>> SOURCE_WATCH_STATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CryingObsidianLightField() {
    }

    public static void clearLevel(ServerLevel level) {
        if (level != null) {
            ACTIVE_SOURCES.remove(level);
            PENDING_REBUILDS.remove(level);
            CHUNK_WATCH_EPOCHS.remove(level);
            SOURCE_WATCH_STATES.remove(level);
        }
    }

    public static void refresh(ServerLevel level, BlockPos sourcePos, BlockState sourceState) {
        refreshInternal(level, sourcePos, sourceState, false, true);
    }

    /**
     * Re-solve one block-backed source from its origin using current server geometry.
     * dev.76 publishes any changed aggregate sections to watching clients; clients never
     * re-solve STATIC_WORLD source geometry themselves.
     */
    public static void forceRefreshSource(ServerLevel level, BlockPos sourcePos) {
        if (level == null || sourcePos == null || !MirageLightEngine.isChunkQueryable(level, sourcePos)) {
            return;
        }
        refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true, true);
    }

    private static void refreshInternal(
            ServerLevel level,
            BlockPos sourcePos,
            BlockState sourceState,
            boolean forceRebuild,
            boolean notifyClients
    ) {
        SourceFieldSpec spec = sourceFieldSpec(level, sourcePos, sourceState);
        if (!spec.active()) {
            unregisterSource(level, sourcePos);
            removeMirageSource(level, sourcePos);
            cleanupLegacyPhysicalRelays(level, sourcePos);
            return;
        }

        registerSource(level, sourcePos);
        refreshMirageSolverField(level, sourcePos, forceRebuild, notifyClients);
    }

    public static void removeSourceNow(ServerLevel level, BlockPos sourcePos) {
        clearPending(level, sourcePos);
        unregisterSource(level, sourcePos);
        removeMirageSource(level, sourcePos, true);
        cleanupLegacyPhysicalRelays(level, sourcePos);
        level.getLightEngine().checkBlock(sourcePos);
    }


    /**
     * Chunk-load discovery is the persistence bridge for virtual light. Mature
     * crystals do not own a BlockEntity, so sources are rediscovered from block
     * states whenever their chunk becomes live.
     */
    public static void discoverSourcesInChunk(ServerLevel level, ChunkAccess chunk) {
        if (level == null || chunk == null) {
            return;
        }
        LevelChunkSection[] sections = chunk.getSections();
        ChunkPos chunkPos = chunk.getPos();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir() || !section.maybeHas(CryingObsidianLightField::isMatureCrystalState)) {
                continue;
            }
            int sectionY = level.getSectionYFromSectionIndex(sectionIndex);
            int baseY = sectionY << 4;
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (!isMatureCrystalState(state)) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(
                                chunkPos.getBlockX(localX),
                                baseY + localY,
                                chunkPos.getBlockZ(localZ)
                        );
                        level.scheduleTick(pos, state.getBlock(), 1);
                        if (isActiveMatureState(state)) {
                            registerSource(level, pos);
                        }
                    }
                }
            }
        }
    }

    /** Remove authoritative sources whose origin chunk is leaving the live level. */
    public static void removeSourcesInChunk(ServerLevel level, ChunkPos chunkPos) {
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null || sources.isEmpty()) {
            return;
        }
        List<BlockPos> leaving = new ArrayList<>();
        for (BlockPos sourcePos : sources) {
            if (new ChunkPos(sourcePos).equals(chunkPos)) {
                leaving.add(sourcePos);
            }
        }
        for (BlockPos sourcePos : leaving) {
            unregisterSource(level, sourcePos);
            removeMirageSource(level, sourcePos);
        }
    }

    /**
     * Re-solve loaded sources whose horizontal radius intersects newly arrived
     * chunk geometry. Multiple chunk-load events are coalesced by the lifecycle
     * event bridge before this method is called.
     */
    public static void refreshSourcesForLoadedChunks(ServerLevel level, Iterable<ChunkPos> loadedChunks) {
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null || sources.isEmpty()) {
            return;
        }
        List<ChunkPos> chunks = new ArrayList<>();
        loadedChunks.forEach(chunks::add);
        if (chunks.isEmpty()) {
            return;
        }

        List<BlockPos> impacted = new ArrayList<>();
        List<BlockPos> stale = new ArrayList<>();
        for (BlockPos sourcePos : sources) {
            if (!level.hasChunkAt(sourcePos)) {
                continue;
            }
            BlockState state = level.getBlockState(sourcePos);
            if (!isActiveMatureState(state)) {
                stale.add(sourcePos);
                continue;
            }
            MirageLightSource source = sourceFor(level, sourcePos, state);
            if (source == null) {
                continue;
            }
            for (ChunkPos chunkPos : chunks) {
                if (MirageLightEngine.sourceDependsOnChunk(source, chunkPos)) {
                    impacted.add(sourcePos);
                    break;
                }
            }
        }

        for (BlockPos stalePos : stale) {
            unregisterSource(level, stalePos);
            removeMirageSource(level, stalePos);
        }
        for (BlockPos sourcePos : impacted) {
            refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true, true);
        }
    }

    /**
     * Global migration sweep for the internal relay block. Palette prefiltering
     * keeps ordinary chunks cheap; only sections that may contain our legacy block
     * are walked voxel-by-voxel.
     */
    public static int cleanupLegacyNodesInChunk(ServerLevel level, ChunkAccess chunk) {
        if (level == null || chunk == null) {
            return 0;
        }
        List<BlockPos> legacy = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        ChunkPos chunkPos = chunk.getPos();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir()
                    || !section.maybeHas(state -> state.is(ModBlocks.CRYING_LIGHT_NODE.get()))) {
                continue;
            }
            int sectionY = level.getSectionYFromSectionIndex(sectionIndex);
            int baseY = sectionY << 4;
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        if (!section.getBlockState(localX, localY, localZ)
                                .is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                            continue;
                        }
                        legacy.add(new BlockPos(
                                chunkPos.getBlockX(localX),
                                baseY + localY,
                                chunkPos.getBlockZ(localZ)
                        ));
                    }
                }
            }
        }
        for (BlockPos pos : legacy) {
            level.removeBlock(pos, false);
            level.getLightEngine().checkBlock(pos);
        }
        return legacy.size();
    }

    private static boolean isMatureCrystalState(BlockState state) {
        return state.getBlock() instanceof CryingObsidianCrystalBlock crystal && crystal.stage().isMature();
    }

    private static boolean isActiveMatureState(BlockState state) {
        return isMatureCrystalState(state) && state.getValue(CryingObsidianCrystalBlock.ENERGIZED);
    }

    private static MirageLightSource sourceFor(ServerLevel level, BlockPos sourcePos, BlockState sourceState) {
        SourceFieldSpec spec = sourceFieldSpec(level, sourcePos, sourceState);
        if (!spec.active()) {
            return null;
        }
        MirageLightProfile profile = MirageLightProfile.halfDecayExtended(
                spec.conceptualLight(),
                CRYING_LIGHT_RGB
        );
        return new MirageLightSource(
                mirageSourceId(sourcePos),
                sourcePos,
                profile,
                MirageLightRuntimeMode.STATIC_WORLD
        );
    }

    /**
     * Reflected power tier. Quartz supplies radiance; Diamond supplies a smaller
     * focus contribution. Glass/Amethyst/Netherite keep their separate visual roles.
     */
    public static int fieldTier(BeaconRelayState relay) {
        if (relay == null || !relay.modified()) {
            return 0;
        }
        int focusBonus = (relay.reflectedFocusTier() + 1) / 2;
        return Mth.clamp(
                relay.reflectedRadianceTier() + focusBonus,
                0,
                MAX_EFFECTIVE_REFLECTED_BOOST
        );
    }

    public static LightProfile profileForRelay(BeaconRelayState relay) {
        int conceptualLight = 15 + fieldTier(relay);
        return LightProfile.extended(15, conceptualLight * 2, 20);
    }

    /**
     * Rebuild each loaded Mature source touched by one or more terrain changes.
     * Block events are coalesced until LevelTickEvent.Post by
     * CryingObsidianLightInvalidationEvents, so each source solves at most once for the
     * final world state of that server tick.
     */
    public static void refreshSourcesNearNow(ServerLevel level, Iterable<BlockPos> changedPositions) {
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null || sources.isEmpty()) {
            return;
        }

        boolean any = false;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : changedPositions) {
            any = true;
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        if (!any) {
            return;
        }

        List<BlockPos> impacted = new ArrayList<>();
        List<BlockPos> stale = new ArrayList<>();
        for (BlockPos sourcePos : sources) {
            if (!level.hasChunkAt(sourcePos)) {
                continue;
            }
            BlockState state = level.getBlockState(sourcePos);
            if (!isActiveMatureState(state)) {
                stale.add(sourcePos);
                continue;
            }

            if (sourcePos.getX() >= minX - MAX_FIELD_RADIUS
                    && sourcePos.getX() <= maxX + MAX_FIELD_RADIUS
                    && sourcePos.getY() >= minY - MAX_FIELD_RADIUS
                    && sourcePos.getY() <= maxY + MAX_FIELD_RADIUS
                    && sourcePos.getZ() >= minZ - MAX_FIELD_RADIUS
                    && sourcePos.getZ() <= maxZ + MAX_FIELD_RADIUS) {
                impacted.add(sourcePos);
            }
        }

        for (BlockPos stalePos : stale) {
            unregisterSource(level, stalePos);
            removeMirageSource(level, stalePos);
            cleanupLegacyPhysicalRelays(level, stalePos);
        }
        for (BlockPos sourcePos : impacted) {
            refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true, true);
        }
    }

    /**
     * Safety audit for block-backed Mature sources.
     *
     * Normal teardown is event-driven, but development-world migrations and unusual block
     * replacement paths can bypass the expected callback order. Once per second the lifecycle
     * bridge can cheaply prune any authoritative source whose origin chunk is loaded but no
     * longer contains an energized Mature Cluster.
     */
    public static int pruneStaleSources(ServerLevel level) {
        if (level == null) {
            return 0;
        }
        int removedCount = 0;
        for (MirageLightSource source : MirageLightEngine.sources(level)) {
            if (!MIRAGE_LIGHT_SOURCE_KIND.equals(source.id().kind())
                    || !level.hasChunkAt(source.origin())) {
                continue;
            }
            BlockState state = level.getBlockState(source.origin());
            if (isActiveMatureState(state)) {
                continue;
            }
            unregisterSource(level, source.origin());
            removeMirageSource(level, source.origin(), true);
            cleanupLegacyPhysicalRelays(level, source.origin());
            level.getLightEngine().checkBlock(source.origin());
            removedCount++;
        }
        return removedCount;
    }


    /** Mark one chunk column as changed for every source-centric watcher in this level. */
    public static void noteChunkChanged(ServerLevel level, ChunkPos chunkPos) {
        if (level == null || chunkPos == null) {
            return;
        }
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null || sources.isEmpty()) {
            return;
        }

        // Do not build a world-wide chunk revision database. A change matters only when
        // it falls inside the largest possible Mature Cluster watch window (radius 38 ->
        // +/-3 chunk columns) around at least one active source.
        int maxChunkRadius = Math.max(2, (MAX_FIELD_RADIUS + 15) / 16);
        boolean relevant = false;
        for (BlockPos sourcePos : sources) {
            ChunkPos sourceChunk = new ChunkPos(sourcePos);
            if (Math.abs(sourceChunk.x - chunkPos.x) <= maxChunkRadius
                    && Math.abs(sourceChunk.z - chunkPos.z) <= maxChunkRadius) {
                relevant = true;
                break;
            }
        }
        if (!relevant) {
            return;
        }

        Map<Long, Long> epochs = CHUNK_WATCH_EPOCHS.computeIfAbsent(level, ignored -> new ConcurrentHashMap<>());
        long chunkKey = chunkPos.toLong();
        epochs.merge(chunkKey, 1L, Long::sum);
    }

    public static void noteBlockChanged(ServerLevel level, BlockPos pos) {
        if (level != null && pos != null) {
            noteChunkChanged(level, new ChunkPos(pos));
        }
    }

    /**
     * dev.76g source-centric 5x5+ watchdog.
     *
     * The base radius-30 Mature Cluster watches +/-2 chunks around its origin (5x5).
     * Boosted fields automatically widen this to +/-3 when their real radius requires it.
     * A watchdog tick never force-loads chunks. If the watched signature changes, the source
     * is force-rebuilt from its origin; MirageLightSolver remains responsible for wall/
     * face occlusion and detour decay, so this watchdog cannot shine through a wall.
     *
     * Every second, even with a stable signature, the source sends a tiny chunk-revision
     * manifest. Clients request only missing/stale chunk snapshots, which makes login/chunk
     * reload convergence self-healing without continuously resending section arrays.
     */
    public static void verifyActiveSources(ServerLevel level) {
        if (level == null) {
            return;
        }
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null || sources.isEmpty()) {
            return;
        }

        Map<BlockPos, SourceWatchState> states = SOURCE_WATCH_STATES.computeIfAbsent(
                level,
                ignored -> new ConcurrentHashMap<>()
        );
        long gameTime = level.getGameTime();

        for (BlockPos sourcePos : List.copyOf(sources)) {
            if (!MirageLightEngine.isChunkQueryable(level, sourcePos)) {
                continue;
            }
            BlockState state = level.getBlockState(sourcePos);
            MirageLightSource source = sourceFor(level, sourcePos, state);
            if (source == null) {
                unregisterSource(level, sourcePos);
                removeMirageSource(level, sourcePos, true);
                continue;
            }

            long signature = watchSignature(level, source);
            SourceWatchState previous = states.get(sourcePos);
            boolean changed = previous == null || previous.signature() != signature;
            boolean heartbeat = previous == null || gameTime >= previous.nextHeartbeatTick();

            if (changed) {
                // Rebuild from the source, not from the changed chunk. This is the key
                // dev.76g rule: every relevant chunk lifecycle/geometry edge causes the
                // complete causal field to be revalidated from the energized Cluster.
                refreshMirageSolverField(level, sourcePos, true, true);
            }

            if (changed || heartbeat) {
                MirageLightNetwork.broadcastRevisionManifestForSource(level, source);
            }

            long nextHeartbeat = (changed || heartbeat)
                    ? gameTime + WATCHDOG_HEARTBEAT_TICKS
                    : previous.nextHeartbeatTick();
            states.put(sourcePos.immutable(), new SourceWatchState(signature, nextHeartbeat));
        }
    }

    private static long watchSignature(ServerLevel level, MirageLightSource source) {
        int chunkRadius = Math.max(2, (source.profile().maxRadius() + 15) / 16);
        ChunkPos sourceChunk = new ChunkPos(source.origin());
        Map<Long, Long> epochs = CHUNK_WATCH_EPOCHS.get(level);
        long hash = 0xcbf29ce484222325L;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkPos chunkPos = new ChunkPos(sourceChunk.x + dx, sourceChunk.z + dz);
                long chunkKey = chunkPos.toLong();
                boolean queryable = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) != null;
                long epoch = epochs == null ? 0L : epochs.getOrDefault(chunkKey, 0L);
                long value = chunkKey ^ Long.rotateLeft(epoch, 17) ^ (queryable ? 0x9E3779B97F4A7C15L : 0L);
                hash ^= value;
                hash *= 0x100000001b3L;
            }
        }
        return hash;
    }

    /**
     * Retry only rebuilds that were deliberately deferred by the dev.76c atomic gate.
     * At ~25 chunk probes for a normal radius-30 Cluster this is cheap enough to run
     * every server tick and removes the old multi-second convergence delay.
     */
    public static void retryPendingSources(ServerLevel level) {
        if (level == null) {
            return;
        }
        Set<BlockPos> pending = PENDING_REBUILDS.get(level);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        for (BlockPos sourcePos : List.copyOf(pending)) {
            if (!MirageLightEngine.isChunkQueryable(level, sourcePos)) {
                continue;
            }
            BlockState state = level.getBlockState(sourcePos);
            MirageLightSource source = sourceFor(level, sourcePos, state);
            if (source == null) {
                pending.remove(sourcePos);
                unregisterSource(level, sourcePos);
                removeMirageSource(level, sourcePos, true);
                continue;
            }
            if (!MirageLightEngine.allDependencyChunksQueryable(level, source)) {
                continue;
            }
            refreshMirageSolverField(level, sourcePos, true, true);
        }

        if (pending.isEmpty()) {
            PENDING_REBUILDS.remove(level);
        }
    }

    public static int pendingSourceCount(ServerLevel level) {
        Set<BlockPos> pending = level == null ? null : PENDING_REBUILDS.get(level);
        return pending == null ? 0 : pending.size();
    }

    private static void markPending(ServerLevel level, BlockPos sourcePos) {
        PENDING_REBUILDS.computeIfAbsent(level, ignored -> ConcurrentHashMap.newKeySet())
                .add(sourcePos.immutable());
    }

    private static void clearPending(ServerLevel level, BlockPos sourcePos) {
        Set<BlockPos> pending = PENDING_REBUILDS.get(level);
        if (pending == null) {
            return;
        }
        pending.remove(sourcePos);
        if (pending.isEmpty()) {
            PENDING_REBUILDS.remove(level);
        }
    }

    private static SourceFieldSpec sourceFieldSpec(
            ServerLevel level,
            BlockPos sourcePos,
            BlockState sourceState
    ) {
        if (!(sourceState.getBlock() instanceof CryingObsidianCrystalBlock crystal)
                || !sourceState.getValue(CryingObsidianCrystalBlock.ENERGIZED)
                || !crystal.stage().isMature()) {
            return SourceFieldSpec.INACTIVE;
        }

        int baseLight = crystal.stage().vanillaPoweredLight();
        if (baseLight <= 0) {
            return SourceFieldSpec.INACTIVE;
        }

        BeaconRelayState relay = CryingObsidianCrystalOptics.relayStateBelow(level, sourcePos);
        int conceptualLight = Math.min(MAX_CONCEPTUAL_LIGHT, baseLight + fieldTier(relay));
        return new SourceFieldSpec(conceptualLight);
    }

    private static MirageLightSourceId mirageSourceId(BlockPos sourcePos) {
        return MirageLightSourceId.block(MIRAGE_LIGHT_SOURCE_KIND, sourcePos);
    }

    private static void refreshMirageSolverField(
            ServerLevel level,
            BlockPos sourcePos,
            boolean forceRebuild,
            boolean notifyClients
    ) {
        MirageLightSource source = sourceFor(level, sourcePos, level.getBlockState(sourcePos));
        if (source == null) {
            clearPending(level, sourcePos);
            removeMirageSource(level, sourcePos);
            return;
        }

        // dev.76c: STATIC_WORLD publication is atomic. Never solve/publish from a
        // partial server chunk window. Existing complete light stays visible until the
        // replacement can be solved against the whole dependency window.
        if (!MirageLightEngine.allDependencyChunksQueryable(level, source)) {
            markPending(level, sourcePos);
            return;
        }

        MirageLightWorld.UpdateResult result = MirageLightEngine.updateSource(level, source, forceRebuild);
        if (!result.rebuilt() && result.solveStats() != null && result.solveStats().unloadedEdges() > 0) {
            // Defensive race guard: should be rare on the main server thread, but if a
            // dependency vanished between readiness check and solve, keep old state.
            markPending(level, sourcePos);
            return;
        }
        clearPending(level, sourcePos);
        MirageLightField solvedField = MirageLightEngine.field(level, source.id());
        if (result.rebuilt() && notifyClients && !result.changedSections().isEmpty()) {
            MirageLightNetwork.broadcastSections(level, result.changedSections());
        }
        if (result.rebuilt() && result.solveStats() != null) {
            MirageProjector.LOGGER.debug(
                    "Mirage authoritative light solved at {}: {} voxels / {} sections / {} ms",
                    sourcePos,
                    result.solveStats().litVoxels(),
                    solvedField == null ? 0 : solvedField.sectionCount(),
                    String.format(java.util.Locale.ROOT, "%.3f", result.solveStats().solveMillis())
            );
        }
    }

    private static void removeMirageSource(ServerLevel level, BlockPos sourcePos) {
        removeMirageSource(level, sourcePos, false);
    }

    private static void removeMirageSource(ServerLevel level, BlockPos sourcePos, boolean forceNotifyClients) {
        Set<Long> touchedSections = new java.util.HashSet<>();
        for (MirageLightSource existing : MirageLightEngine.sources(level)) {
            if (!existing.origin().equals(sourcePos)) {
                continue;
            }
            MirageLightField field = MirageLightEngine.field(level, existing.id());
            if (field != null) {
                touchedSections.addAll(field.sections().keySet());
            }
        }
        List<MirageLightSource> removed = MirageLightEngine.removeSourcesAtOrigin(level, sourcePos);
        if ((!removed.isEmpty() || forceNotifyClients) && !touchedSections.isEmpty()) {
            MirageLightNetwork.broadcastSections(level, touchedSections);
        }
    }

    /**
     * Migration-only cleanup for the physical relay lattices used by dev.65-dev.74.
     * Never removes minecraft:light or any other mod's block.
     */
    private static void cleanupLegacyPhysicalRelays(ServerLevel level, BlockPos sourcePos) {
        for (NodeOffset offset : LEGACY_AXIAL_OFFSETS) {
            removeLegacyNode(level, sourcePos.offset(offset.dx(), offset.dy(), offset.dz()));
        }
        for (NodeOffset offset : LEGACY_DEV69_DIFFUSE_OFFSETS) {
            removeLegacyNode(level, sourcePos.offset(offset.dx(), offset.dy(), offset.dz()));
        }
    }

    private static void removeLegacyNode(ServerLevel level, BlockPos nodePos) {
        if (level.isOutsideBuildHeight(nodePos) || !level.hasChunkAt(nodePos)) {
            return;
        }
        if (level.getBlockState(nodePos).is(ModBlocks.CRYING_LIGHT_NODE.get())) {
            level.removeBlock(nodePos, false);
            level.getLightEngine().checkBlock(nodePos);
        }
    }

    private static void registerSource(ServerLevel level, BlockPos sourcePos) {
        ACTIVE_SOURCES.computeIfAbsent(level, ignored -> ConcurrentHashMap.newKeySet())
                .add(sourcePos.immutable());
    }

    private static void unregisterSource(ServerLevel level, BlockPos sourcePos) {
        clearPending(level, sourcePos);
        Map<BlockPos, SourceWatchState> watchStates = SOURCE_WATCH_STATES.get(level);
        if (watchStates != null) {
            watchStates.remove(sourcePos);
            if (watchStates.isEmpty()) {
                SOURCE_WATCH_STATES.remove(level);
            }
        }
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null) {
            return;
        }
        sources.remove(sourcePos);
        if (sources.isEmpty()) {
            ACTIVE_SOURCES.remove(level);
            CHUNK_WATCH_EPOCHS.remove(level);
            SOURCE_WATCH_STATES.remove(level);
        }
    }

    private static List<NodeOffset> createLegacyAxialOffsets() {
        List<NodeOffset> offsets = new ArrayList<>();
        int[][] directions = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };
        for (int[] direction : directions) {
            for (int distance = 1; distance <= MAX_FIELD_RADIUS; distance++) {
                offsets.add(new NodeOffset(
                        direction[0] * distance,
                        direction[1] * distance,
                        direction[2] * distance
                ));
            }
        }
        return List.copyOf(offsets);
    }

    /**
     * Exact dev.69 sampled face-diagonal lattice, retained only for cleanup.
     */
    private static List<NodeOffset> createLegacyDiffuseOffsets() {
        List<NodeOffset> offsets = new ArrayList<>();
        int[][] directions = {
                {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
                {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
                {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
        };
        for (int[] direction : directions) {
            double directionLength = Math.sqrt(
                    direction[0] * direction[0]
                            + direction[1] * direction[1]
                            + direction[2] * direction[2]
            );
            int maxSteps = Math.max(1, (int) Math.ceil(25.0D / directionLength));
            int previousDecayBucket = Integer.MIN_VALUE;
            for (int step = 1; step <= maxSteps; step++) {
                int dx = direction[0] * step;
                int dy = direction[1] * step;
                int dz = direction[2] * step;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 25.0D + 1.0E-6D) {
                    break;
                }
                int decayBucket = Math.max(0, (int) Math.floor((distance - 1.0D) / 2.0D));
                if (decayBucket == previousDecayBucket) {
                    continue;
                }
                previousDecayBucket = decayBucket;
                offsets.add(new NodeOffset(dx, dy, dz));
            }
        }
        return List.copyOf(offsets);
    }

    private record SourceFieldSpec(int conceptualLight) {
        private static final SourceFieldSpec INACTIVE = new SourceFieldSpec(0);

        private boolean active() {
            return conceptualLight > 0;
        }
    }

    private record NodeOffset(int dx, int dy, int dz) {
    }

    private record SourceWatchState(long signature, long nextHeartbeatTick) {
    }

}

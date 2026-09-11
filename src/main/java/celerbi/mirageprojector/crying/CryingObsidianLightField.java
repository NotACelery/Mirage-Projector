package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.light.LightProfile;
import celerbi.mirageprojector.light.engine.MirageLightEngine;
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
 * dev.75d retains the authoritative virtual-light lifecycle and obstacle-detour decay. The old
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

    private CryingObsidianLightField() {
    }

    public static void clearLevel(ServerLevel level) {
        if (level != null) {
            ACTIVE_SOURCES.remove(level);
        }
    }

    public static void refresh(ServerLevel level, BlockPos sourcePos, BlockState sourceState) {
        refreshInternal(level, sourcePos, sourceState, false);
    }

    private static void refreshInternal(
            ServerLevel level,
            BlockPos sourcePos,
            BlockState sourceState,
            boolean forceRebuild
    ) {
        SourceFieldSpec spec = sourceFieldSpec(level, sourcePos, sourceState);
        if (!spec.active()) {
            unregisterSource(level, sourcePos);
            removeMirageSource(level, sourcePos);
            cleanupLegacyPhysicalRelays(level, sourcePos);
            return;
        }

        registerSource(level, sourcePos);
        refreshMirageSolverField(level, sourcePos, forceRebuild);
    }

    public static void removeSourceNow(ServerLevel level, BlockPos sourcePos) {
        unregisterSource(level, sourcePos);
        removeMirageSource(level, sourcePos);
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
                if (MirageLightEngine.sourceTouchesChunk(source, chunkPos)) {
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
            refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true);
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
            refreshInternal(level, sourcePos, level.getBlockState(sourcePos), true);
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
            boolean forceRebuild
    ) {
        MirageLightSource source = sourceFor(level, sourcePos, level.getBlockState(sourcePos));
        if (source == null) {
            removeMirageSource(level, sourcePos);
            return;
        }

        MirageLightWorld.UpdateResult result = MirageLightEngine.updateSource(level, source, forceRebuild);
        if (result.rebuilt()) {
            MirageLightNetwork.broadcastUpsert(level, source);
        }
        if (result.rebuilt() && result.solveStats() != null) {
            var solvedField = MirageLightEngine.field(level, source.id());
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
        MirageLightSourceId id = mirageSourceId(sourcePos);
        if (MirageLightEngine.removeSource(level, id)) {
            MirageLightNetwork.broadcastRemove(level, id, sourcePos);
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
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null) {
            return;
        }
        sources.remove(sourcePos);
        if (sources.isEmpty()) {
            ACTIVE_SOURCES.remove(level);
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
}

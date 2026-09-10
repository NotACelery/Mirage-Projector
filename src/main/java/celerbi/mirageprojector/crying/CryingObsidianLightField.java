package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.block.CryingObsidianLightNodeBlock;
import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.light.LightProfile;
import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightProfile;
import celerbi.mirageprojector.light.engine.MirageLightRuntimeMode;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import celerbi.mirageprojector.light.engine.MirageLightWorld;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CryingObsidianLightField {
    private static final int MAX_EFFECTIVE_REFLECTED_BOOST = BeaconRelayState.MAX_EFFECTIVE_BOOSTERS;
    private static final int MAX_CONCEPTUAL_LIGHT = 15 + MAX_EFFECTIVE_REFLECTED_BOOST;
    private static final int MAX_NODE_DISTANCE = MAX_CONCEPTUAL_LIGHT * 2;
    private static final int AXIAL_RAY_COUNT = 6;
    private static final String MIRAGE_LIGHT_SOURCE_KIND = "mature_crying_cluster";
    private static final int CRYING_LIGHT_RGB = 0xA84CFF;

    /*
     * dev.70 keeps world-light relays on six causal axial branches only. dev.69's
     * long face-diagonal Glass branches looked attractive in open air, but every
     * auxiliary block is an omnidirectional vanilla light source. Those diagonal
     * relays could therefore illuminate the dark side of a wall even when the
     * corresponding axial branch was correctly blocked. Glass still affects the
     * reflected/residual beam renderer; a safe widened static-light topology can be
     * reintroduced later only if it preserves source causality through geometry.
     */
    private static final List<NodeOffset> OFFSETS = createOffsets();
    private static final List<NodeOffset> LEGACY_DEV69_DIFFUSE_OFFSETS = createLegacyDiffuseOffsets();

    /*
     * Active sources are indexed per ServerLevel so player terrain edits can request
     * a one-tick refresh instead of waiting for the 20-tick optics fallback. The map
     * is weak by level and stores immutable positions only; the server thread owns all
     * mutations.
     */
    private static final Map<ServerLevel, Set<BlockPos>> ACTIVE_SOURCES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<ServerLevel, Set<BlockPos>> DEV69_DIFFUSE_CLEANED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CryingObsidianLightField() {
    }

    public static void refresh(ServerLevel level, BlockPos sourcePos, BlockState sourceState) {
        refreshInternal(level, sourcePos, sourceState, false);
    }

    private static void refreshInternal(
            ServerLevel level,
            BlockPos sourcePos,
            BlockState sourceState,
            boolean forceMirageSolverRebuild
    ) {
        SourceFieldSpec spec = sourceFieldSpec(level, sourcePos, sourceState);
        if (!spec.active()) {
            unregisterSource(level, sourcePos);
            MirageLightEngine.removeSource(level, mirageSourceId(sourcePos));
            return;
        }
        registerSource(level, sourcePos);
        refreshMirageSolverField(level, sourcePos, spec, forceMirageSolverRebuild);

        /*
         * dev.74 foundation runs the new voxel solver in parallel with the dev.73
         * physical relay backend. The relays remain authoritative for visible/gameplay
         * light until dev.75 wires the virtual section layer into Minecraft queries.
         */
        boolean[] blockedRays = new boolean[AXIAL_RAY_COUNT];
        for (NodeOffset offset : OFFSETS) {
            BlockPos nodePos = sourcePos.offset(offset.dx(), offset.dy(), offset.dz());
            if (level.isOutsideBuildHeight(nodePos) || !level.hasChunkAt(nodePos)) {
                continue;
            }

            BlockState existing = level.getBlockState(nodePos);
            int desired = 0;
            if (!blockedRays[offset.rayIndex()]) {
                int pathPenalty = tracePathPenalty(level, sourcePos, offset);
                if (pathPenalty == Integer.MAX_VALUE) {
                    blockedRays[offset.rayIndex()] = true;
                } else {
                    desired = desiredFromSpec(spec, offset, pathPenalty);
                    if (isFullOpaqueBlock(level, nodePos, existing)) {
                        // The obstacle sits exactly on a sampled relay position. It is
                        // not part of tracePathPenalty (which excludes the endpoint),
                        // so explicitly terminate the entire downstream branch here.
                        desired = 0;
                        blockedRays[offset.rayIndex()] = true;
                    }
                }
            }

            if (desired > 0 && canHost(existing)) {
                applyDesiredNode(level, nodePos, existing, desired);
            } else if (existing.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                // dev.69 deferred this to a scheduled node tick. Reconcile now so a
                // newly placed wall removes every stale relay in the same refresh.
                reconcileNodeNow(level, nodePos);
            }
        }
        cleanupLegacyDev69DiffuseNodes(level, sourcePos);
    }

    public static int desiredLevelAt(ServerLevel level, BlockPos nodePos) {
        return desiredLevelAt(level, nodePos, null);
    }

    public static void removeSourceNow(ServerLevel level, BlockPos sourcePos) {
        unregisterSource(level, sourcePos);
        MirageLightEngine.removeSource(level, mirageSourceId(sourcePos));
        for (NodeOffset offset : OFFSETS) {
            BlockPos nodePos = sourcePos.offset(offset.dx(), offset.dy(), offset.dz());
            if (level.isOutsideBuildHeight(nodePos) || !level.hasChunkAt(nodePos)) {
                continue;
            }
            BlockState existing = level.getBlockState(nodePos);
            if (!existing.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                continue;
            }
            int desired = desiredLevelAt(level, nodePos, sourcePos);
            if (desired <= 0) {
                level.removeBlock(nodePos, false);
                level.getLightEngine().checkBlock(nodePos);
                continue;
            }
            if (existing.getValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL) != desired) {
                level.setBlock(
                        nodePos,
                        existing.setValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL, desired),
                        Block.UPDATE_CLIENTS
                );
                level.getLightEngine().checkBlock(nodePos);
            }
        }
        cleanupLegacyDev69DiffuseNodes(level, sourcePos);
        // cleanupLegacyDev69DiffuseNodes records one-time migration state; make sure a
        // source that has just been removed does not leave a dead registry entry behind.
        unregisterSource(level, sourcePos);
        level.getLightEngine().checkBlock(sourcePos);
    }

    private static int desiredLevelAt(
            ServerLevel level,
            BlockPos nodePos,
            @Nullable BlockPos excludedSource
    ) {
        int desired = 0;
        for (NodeOffset offset : OFFSETS) {
            BlockPos sourcePos = nodePos.offset(-offset.dx(), -offset.dy(), -offset.dz());
            if (excludedSource != null && sourcePos.equals(excludedSource)) {
                continue;
            }
            if (level.isOutsideBuildHeight(sourcePos) || !level.hasChunkAt(sourcePos)) {
                continue;
            }
            BlockState sourceState = level.getBlockState(sourcePos);
            int candidate = desiredFromSource(level, sourcePos, sourceState, offset);
            if (candidate > desired) {
                desired = candidate;
                if (desired >= 15) {
                    return 15;
                }
            }
        }
        return desired;
    }

    /**
     * Legacy/general profile tier now represents reflected power only. Glass no longer
     * grants generic extra range merely because the incoming Beacon beam became wider.
     * Quartz adds Radiance and Diamond adds focused axial reach; the four-effective-Core
     * cap means the combined reflected power tier remains bounded to 0..4.
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

    private static int desiredFromSource(
            ServerLevel level,
            BlockPos sourcePos,
            BlockState sourceState,
            NodeOffset offset
    ) {
        return desiredFromSpec(level, sourcePos, sourceFieldSpec(level, sourcePos, sourceState), offset);
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
        int axialConceptualLight = Math.min(MAX_CONCEPTUAL_LIGHT, baseLight + fieldTier(relay));
        double axialMaxDistance = axialConceptualLight * 2.0D;
        return new SourceFieldSpec(axialConceptualLight, axialMaxDistance);
    }

    private static int desiredFromSpec(
            SourceFieldSpec spec,
            NodeOffset offset,
            int pathPenalty
    ) {
        if (!spec.active() || offset.euclideanDistance() > spec.axialMaxDistance() + 1.0E-6D) {
            return 0;
        }
        int halfDecaySteps = Math.max(0, (int) Math.floor((offset.euclideanDistance() - 1.0D) / 2.0D));
        return Mth.clamp(spec.axialConceptualLight() - halfDecaySteps - pathPenalty, 0, 15);
    }

    private static int desiredFromSpec(
            ServerLevel level,
            BlockPos sourcePos,
            SourceFieldSpec spec,
            NodeOffset offset
    ) {
        if (!spec.active() || offset.euclideanDistance() > spec.axialMaxDistance() + 1.0E-6D) {
            return 0;
        }
        int pathPenalty = tracePathPenalty(level, sourcePos, offset);
        if (pathPenalty == Integer.MAX_VALUE) {
            return 0;
        }
        BlockPos nodePos = sourcePos.offset(offset.dx(), offset.dy(), offset.dz());
        if (isFullOpaqueBlock(level, nodePos, level.getBlockState(nodePos))) {
            return 0;
        }
        return desiredFromSpec(spec, offset, pathPenalty);
    }

    private static MirageLightSourceId mirageSourceId(BlockPos sourcePos) {
        return MirageLightSourceId.block(MIRAGE_LIGHT_SOURCE_KIND, sourcePos);
    }

    private static void refreshMirageSolverField(
            ServerLevel level,
            BlockPos sourcePos,
            SourceFieldSpec spec,
            boolean forceRebuild
    ) {
        MirageLightProfile profile = MirageLightProfile.halfDecayExtended(
                spec.axialConceptualLight(),
                CRYING_LIGHT_RGB
        );
        MirageLightSource source = new MirageLightSource(
                mirageSourceId(sourcePos),
                sourcePos,
                profile,
                MirageLightRuntimeMode.STATIC_WORLD
        );
        MirageLightWorld.UpdateResult result = MirageLightEngine.updateSource(level, source, forceRebuild);
        if (result.rebuilt() && result.solveStats() != null) {
            var solvedField = MirageLightEngine.field(level, source.id());
            MirageProjector.LOGGER.debug(
                    "Mirage shadow light solved at {}: {} voxels / {} sections / {} ms",
                    sourcePos,
                    result.solveStats().litVoxels(),
                    solvedField == null ? 0 : solvedField.sectionCount(),
                    String.format(java.util.Locale.ROOT, "%.3f", result.solveStats().solveMillis())
            );
        }
    }

    private static void applyDesiredNode(ServerLevel level, BlockPos nodePos, BlockState existing, int desired) {
        int effectiveDesired = desired;
        if (existing.is(ModBlocks.CRYING_LIGHT_NODE.get())
                && existing.getValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL) != desired) {
            effectiveDesired = Math.max(desired, desiredLevelAt(level, nodePos));
        }

        if (existing.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
            if (existing.getValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL) != effectiveDesired) {
                level.setBlock(
                        nodePos,
                        existing.setValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL, effectiveDesired),
                        Block.UPDATE_CLIENTS
                );
                level.getLightEngine().checkBlock(nodePos);
            }
            return;
        }

        level.setBlock(
                nodePos,
                ModBlocks.CRYING_LIGHT_NODE.get().defaultBlockState()
                        .setValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL, effectiveDesired),
                Block.UPDATE_CLIENTS
        );
        level.getLightEngine().checkBlock(nodePos);
    }

    private static void reconcileNodeNow(ServerLevel level, BlockPos nodePos) {
        BlockState existing = level.getBlockState(nodePos);
        if (!existing.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
            return;
        }
        int desired = desiredLevelAt(level, nodePos);
        if (desired <= 0) {
            level.removeBlock(nodePos, false);
            level.getLightEngine().checkBlock(nodePos);
            return;
        }
        if (existing.getValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL) != desired) {
            level.setBlock(
                    nodePos,
                    existing.setValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL, desired),
                    Block.UPDATE_CLIENTS
            );
            level.getLightEngine().checkBlock(nodePos);
        }
    }

    private static boolean isFullOpaqueBlock(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.is(ModBlocks.CRYING_LIGHT_NODE.get())
                && !state.isAir()
                && state.getLightBlock(level, pos) >= 15;
    }

    private static void registerSource(ServerLevel level, BlockPos sourcePos) {
        ACTIVE_SOURCES.computeIfAbsent(level, ignored -> ConcurrentHashMap.newKeySet())
                .add(sourcePos.immutable());
    }

    private static void unregisterSource(ServerLevel level, BlockPos sourcePos) {
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources != null) {
            sources.remove(sourcePos);
            if (sources.isEmpty()) {
                ACTIVE_SOURCES.remove(level);
            }
        }
        Set<BlockPos> cleaned = DEV69_DIFFUSE_CLEANED.get(level);
        if (cleaned != null) {
            cleaned.remove(sourcePos);
            if (cleaned.isEmpty()) {
                DEV69_DIFFUSE_CLEANED.remove(level);
            }
        }
    }

    /**
     * Rebuild each active Mature source touched by one or more terrain changes. The
     * event bridge calls this from LevelTickEvent.Post, after placement/break logic has
     * committed the final block states. Each source is refreshed at most once per tick
     * even if the player edited several blocks around it.
     */
    public static void refreshSourcesNearNow(ServerLevel level, Iterable<BlockPos> changedPositions) {
        Set<BlockPos> sources = ACTIVE_SOURCES.get(level);
        if (sources == null || sources.isEmpty()) {
            return;
        }

        List<BlockPos> changes = new ArrayList<>();
        changedPositions.forEach(pos -> changes.add(pos.immutable()));
        if (changes.isEmpty()) {
            return;
        }

        List<BlockPos> impacted = new ArrayList<>();
        List<BlockPos> stale = new ArrayList<>();
        for (BlockPos sourcePos : sources) {
            if (!level.hasChunkAt(sourcePos)) {
                continue;
            }
            BlockState state = level.getBlockState(sourcePos);
            if (!(state.getBlock() instanceof CryingObsidianCrystalBlock crystal)
                    || !crystal.stage().isMature()
                    || !state.getValue(CryingObsidianCrystalBlock.ENERGIZED)) {
                stale.add(sourcePos);
                continue;
            }
            for (BlockPos changedPos : changes) {
                if (Math.abs(sourcePos.getX() - changedPos.getX()) <= MAX_NODE_DISTANCE
                        && Math.abs(sourcePos.getY() - changedPos.getY()) <= MAX_NODE_DISTANCE
                        && Math.abs(sourcePos.getZ() - changedPos.getZ()) <= MAX_NODE_DISTANCE) {
                    impacted.add(sourcePos);
                    break;
                }
            }
        }
        for (BlockPos stalePos : stale) {
            unregisterSource(level, stalePos);
            MirageLightEngine.removeSource(level, mirageSourceId(stalePos));
        }
        for (BlockPos sourcePos : impacted) {
            BlockState state = level.getBlockState(sourcePos);
            refreshInternal(level, sourcePos, state, true);
        }
    }

    private static boolean canHost(BlockState state) {
        return state.is(ModBlocks.CRYING_LIGHT_NODE.get()) || state.isAir();
    }

    /**
     * Preserve source causality for each reflected branch. Fully opaque blocks terminate
     * the branch, so no downstream Mirage node can teleport light behind a wall. Partial
     * light blockers add extra loss on top of the half-speed distance decay instead of
     * being treated as transparent. Once nodes are placed, Minecraft's own block-light
     * engine still performs local spreading/AO around geometry, including natural corner
     * wrap, so Mirage does not try to paint individual block faces itself.
     */
    private static int tracePathPenalty(ServerLevel level, BlockPos sourcePos, NodeOffset offset) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos previous = sourcePos;
        int penalty = 0;

        for (int step = 1; step < offset.pathSteps(); step++) {
            float progress = step / (float) offset.pathSteps();
            int x = sourcePos.getX() + Math.round(offset.dx() * progress);
            int y = sourcePos.getY() + Math.round(offset.dy() * progress);
            int z = sourcePos.getZ() + Math.round(offset.dz() * progress);
            cursor.set(x, y, z);
            if (cursor.equals(previous)) {
                continue;
            }

            BlockState state = level.getBlockState(cursor);
            if (!state.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                int lightBlock = state.getLightBlock(level, cursor);
                if (lightBlock >= 15) {
                    return Integer.MAX_VALUE;
                }
                if (lightBlock > 1) {
                    penalty += lightBlock - 1;
                    if (penalty >= 15) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
            previous = cursor.immutable();
        }
        return penalty;
    }

    private static void cleanupLegacyDev69DiffuseNodes(ServerLevel level, BlockPos sourcePos) {
        Set<BlockPos> cleaned = DEV69_DIFFUSE_CLEANED.computeIfAbsent(
                level,
                ignored -> ConcurrentHashMap.newKeySet()
        );
        if (!cleaned.add(sourcePos.immutable())) {
            return;
        }
        for (NodeOffset offset : LEGACY_DEV69_DIFFUSE_OFFSETS) {
            BlockPos nodePos = sourcePos.offset(offset.dx(), offset.dy(), offset.dz());
            if (level.isOutsideBuildHeight(nodePos) || !level.hasChunkAt(nodePos)) {
                continue;
            }
            if (level.getBlockState(nodePos).is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                reconcileNodeNow(level, nodePos);
            }
        }
    }

    private static List<NodeOffset> createLegacyDiffuseOffsets() {
        List<NodeOffset> offsets = new ArrayList<>();
        int[][] diffuseDirections = {
                {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
                {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
                {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
        };
        int rayIndex = AXIAL_RAY_COUNT;
        for (int[] direction : diffuseDirections) {
            addLegacySampledRayOffsets(offsets, rayIndex++, direction[0], direction[1], direction[2], 25);
        }
        return List.copyOf(offsets);
    }

    private static List<NodeOffset> createOffsets() {
        List<NodeOffset> offsets = new ArrayList<>();
        int[][] axialDirections = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };
        for (int rayIndex = 0; rayIndex < axialDirections.length; rayIndex++) {
            int[] direction = axialDirections[rayIndex];
            addDenseAxialRayOffsets(
                    offsets,
                    rayIndex,
                    direction[0],
                    direction[1],
                    direction[2],
                    MAX_NODE_DISTANCE
            );
        }
        return List.copyOf(offsets);
    }

    /**
     * dev.73 makes the half-decay contract explicit in world state instead of relying
     * on vanilla propagation to fill every second sample. Every axial air cell gets a
     * Mirage relay with the exact target level for that distance:
     * 15,15,14,14,...,1,1. This removes the dev.72 QA failure where only a middle
     * section of the curve visibly held each level for two blocks.
     */
    private static void addDenseAxialRayOffsets(
            List<NodeOffset> offsets,
            int rayIndex,
            int dirX,
            int dirY,
            int dirZ,
            int maxDistance
    ) {
        double directionLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        int maxSteps = Math.max(1, (int) Math.ceil(maxDistance / directionLength));
        for (int step = 1; step <= maxSteps; step++) {
            int dx = dirX * step;
            int dy = dirY * step;
            int dz = dirZ * step;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > maxDistance + 1.0E-6D) {
                break;
            }
            offsets.add(new NodeOffset(rayIndex, dx, dy, dz, step, distance));
        }
    }

    /**
     * dev.69 created diagonal Glass relays only once per half-decay bucket. Keep that
     * exact historical lattice for migration cleanup instead of scanning the new dense
     * axial topology in directions that never existed as current world-light branches.
     */
    private static void addLegacySampledRayOffsets(
            List<NodeOffset> offsets,
            int rayIndex,
            int dirX,
            int dirY,
            int dirZ,
            int maxDistance
    ) {
        double directionLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        int maxSteps = Math.max(1, (int) Math.ceil(maxDistance / directionLength));
        int previousDecayBucket = Integer.MIN_VALUE;

        for (int step = 1; step <= maxSteps; step++) {
            int dx = dirX * step;
            int dy = dirY * step;
            int dz = dirZ * step;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > maxDistance + 1.0E-6D) {
                break;
            }

            int decayBucket = Math.max(0, (int) Math.floor((distance - 1.0D) / 2.0D));
            if (decayBucket == previousDecayBucket) {
                continue;
            }
            previousDecayBucket = decayBucket;
            offsets.add(new NodeOffset(rayIndex, dx, dy, dz, step, distance));
        }
    }


    private record SourceFieldSpec(
            int axialConceptualLight,
            double axialMaxDistance
    ) {
        private static final SourceFieldSpec INACTIVE = new SourceFieldSpec(0, 0.0D);

        private boolean active() {
            return axialConceptualLight > 0;
        }
    }

    private record NodeOffset(
            int rayIndex,
            int dx,
            int dy,
            int dz,
            int pathSteps,
            double euclideanDistance
    ) {
    }
}

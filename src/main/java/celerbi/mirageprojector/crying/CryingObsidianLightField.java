package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.block.CryingObsidianLightNodeBlock;
import celerbi.mirageprojector.light.LightProfile;
import celerbi.mirageprojector.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CryingObsidianLightField {
    private static final int MAX_EFFECTIVE_REFLECTED_BOOST = BeaconRelayState.MAX_EFFECTIVE_BOOSTERS;
    private static final int MAX_CONCEPTUAL_LIGHT = 15 + MAX_EFFECTIVE_REFLECTED_BOOST;
    private static final int MAX_NODE_DISTANCE = MAX_CONCEPTUAL_LIGHT * 2 - 1;
    private static final int MAX_DIFFUSE_NODE_DISTANCE = 25;

    /*
     * Six primary reflection branches preserve the dev.67 half-decay baseline.
     * Glass/Diffusion can additionally activate sparse face-diagonal branches.
     * Body diagonals are intentionally omitted: 12 secondary branches already broaden
     * the volume substantially without turning every Mature Cluster into hundreds of
     * continuously revalidated light sources.
     */
    private static final List<NodeOffset> OFFSETS = createOffsets();

    private CryingObsidianLightField() {
    }

    public static void refresh(ServerLevel level, BlockPos sourcePos, BlockState sourceState) {
        SourceFieldSpec spec = sourceFieldSpec(level, sourcePos, sourceState);
        if (!spec.active()) {
            return;
        }
        for (NodeOffset offset : OFFSETS) {
            BlockPos nodePos = sourcePos.offset(offset.dx(), offset.dy(), offset.dz());
            if (level.isOutsideBuildHeight(nodePos) || !level.hasChunkAt(nodePos)) {
                continue;
            }

            int desired = desiredFromSpec(level, sourcePos, spec, offset);
            BlockState existing = level.getBlockState(nodePos);
            if (desired > 0 && canHost(existing)) {
                if (existing.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                    if (existing.getValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL) != desired) {
                        level.setBlock(
                                nodePos,
                                existing.setValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL, desired),
                                Block.UPDATE_CLIENTS
                        );
                        level.getLightEngine().checkBlock(nodePos);
                    }
                } else {
                    level.setBlock(
                            nodePos,
                            ModBlocks.CRYING_LIGHT_NODE.get().defaultBlockState()
                                    .setValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL, desired),
                            Block.UPDATE_CLIENTS
                    );
                    level.getLightEngine().checkBlock(nodePos);
                }
            } else if (existing.is(ModBlocks.CRYING_LIGHT_NODE.get())) {
                level.scheduleTick(nodePos, ModBlocks.CRYING_LIGHT_NODE.get(), 1);
            }
        }
    }

    public static int desiredLevelAt(ServerLevel level, BlockPos nodePos) {
        return desiredLevelAt(level, nodePos, null);
    }

    public static void removeSourceNow(ServerLevel level, BlockPos sourcePos) {
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
        return LightProfile.extended(15, conceptualLight * 2 - 1, 20);
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
        double axialMaxDistance = axialConceptualLight * 2.0D - 1.0D;
        int diffusionTier = relay.reflectedDiffusionTier();
        int diffuseConceptualLight = diffusionTier <= 0
                ? 0
                : Math.min(MAX_CONCEPTUAL_LIGHT, 12 + diffusionTier + relay.reflectedRadianceTier());
        double diffuseMaxDistance = diffusionTier <= 0
                ? 0.0D
                : Math.min(
                        axialMaxDistance,
                        9.0D + diffusionTier * 4.0D + relay.reflectedRadianceTier() * 2.0D
                );

        return new SourceFieldSpec(
                axialConceptualLight,
                axialMaxDistance,
                diffuseConceptualLight,
                diffuseMaxDistance
        );
    }

    private static int desiredFromSpec(
            ServerLevel level,
            BlockPos sourcePos,
            SourceFieldSpec spec,
            NodeOffset offset
    ) {
        if (!spec.active()) {
            return 0;
        }

        int conceptualLight = offset.kind() == RayKind.DIFFUSE
                ? spec.diffuseConceptualLight()
                : spec.axialConceptualLight();
        double maxDistance = offset.kind() == RayKind.DIFFUSE
                ? spec.diffuseMaxDistance()
                : spec.axialMaxDistance();

        if (conceptualLight <= 0 || offset.euclideanDistance() > maxDistance + 1.0E-6D) {
            return 0;
        }

        int pathPenalty = tracePathPenalty(level, sourcePos, offset);
        if (pathPenalty == Integer.MAX_VALUE) {
            return 0;
        }

        int halfDecaySteps = Math.max(0, (int) Math.floor((offset.euclideanDistance() - 1.0D) / 2.0D));
        return Mth.clamp(conceptualLight - halfDecaySteps - pathPenalty, 0, 15);
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

    private static List<NodeOffset> createOffsets() {
        List<NodeOffset> offsets = new ArrayList<>();

        int[][] axialDirections = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };
        for (int[] direction : axialDirections) {
            addRayOffsets(
                    offsets,
                    direction[0],
                    direction[1],
                    direction[2],
                    RayKind.AXIAL,
                    MAX_NODE_DISTANCE
            );
        }

        int[][] diffuseDirections = {
                {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
                {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
                {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
        };
        for (int[] direction : diffuseDirections) {
            addRayOffsets(
                    offsets,
                    direction[0],
                    direction[1],
                    direction[2],
                    RayKind.DIFFUSE,
                    MAX_DIFFUSE_NODE_DISTANCE
            );
        }

        return List.copyOf(offsets);
    }

    private static void addRayOffsets(
            List<NodeOffset> offsets,
            int dirX,
            int dirY,
            int dirZ,
            RayKind kind,
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
            offsets.add(new NodeOffset(kind, dx, dy, dz, step, distance));
        }
    }


    private record SourceFieldSpec(
            int axialConceptualLight,
            double axialMaxDistance,
            int diffuseConceptualLight,
            double diffuseMaxDistance
    ) {
        private static final SourceFieldSpec INACTIVE = new SourceFieldSpec(0, 0.0D, 0, 0.0D);

        private boolean active() {
            return axialConceptualLight > 0;
        }
    }

    private enum RayKind {
        AXIAL,
        DIFFUSE
    }

    private record NodeOffset(
            RayKind kind,
            int dx,
            int dy,
            int dz,
            int pathSteps,
            double euclideanDistance
    ) {
    }
}

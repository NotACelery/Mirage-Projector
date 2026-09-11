package celerbi.mirageprojector.light.engine;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

/**
 * Fixed-point voxel flood solver.
 *
 * Every accepted step is causally connected to the source through adjacent voxels.
 * There are no physical relay emitters and no source-to-target teleport. A wall can
 * therefore cut connectivity, while light can still walk around a real edge by paying
 * the additional path length exactly as vanilla-style grid propagation does.
 *
 * The work queue is bucketed by remaining fixed-point energy. Current profiles fit in
 * an unsigned byte, so this avoids allocating one Java queue-node object per voxel and
 * avoids PriorityQueue O(log N) churn for the 38k-76k-cell fields used by the Cluster.
 */
public final class MirageLightSolver {
    private MirageLightSolver() {
    }

    public static MirageLightField solve(Level level, MirageLightSource source) {
        long started = System.nanoTime();
        if (level == null || source == null) {
            throw new IllegalArgumentException("MirageLightSolver requires a Level and source");
        }

        Map<Long, MirageLightSection> sections = new HashMap<>();
        if (!source.active() || !source.profile().runtimeImplemented()) {
            return emptyField(source, sections, started, 0);
        }

        MirageLightProfile profile = source.profile();
        BlockPos origin = source.origin();
        if (level.isOutsideBuildHeight(origin) || !level.hasChunkAt(origin)) {
            return emptyField(source, sections, started, 1);
        }

        int initialEnergy = profile.initialEnergyUnits();
        Long2ByteOpenHashMap bestEnergy = new Long2ByteOpenHashMap(estimateVoxelCapacity(profile.maxRadius()));
        bestEnergy.defaultReturnValue((byte) 0);
        LongArrayFIFOQueue[] buckets = new LongArrayFIFOQueue[initialEnergy + 1];

        long originKey = origin.asLong();
        bestEnergy.put(originKey, (byte) initialEnergy);
        enqueue(buckets, initialEnergy, originKey);
        setFieldEnergy(sections, origin, initialEnergy);

        int queuedNodes = 1;
        int settledNodes = 0;
        int blockedEdges = 0;
        int unloadedEdges = 0;
        int queuedEntries = 1;
        int maxQueueSize = 1;

        for (int energy = initialEnergy; energy > 0; ) {
            LongArrayFIFOQueue bucket = buckets[energy];
            if (bucket == null || bucket.isEmpty()) {
                energy--;
                continue;
            }

            long currentKey = bucket.dequeueLong();
            queuedEntries--;
            int authoritative = Byte.toUnsignedInt(bestEnergy.get(currentKey));
            if (authoritative != energy) {
                continue;
            }
            settledNodes++;

            BlockPos currentPos = BlockPos.of(currentKey);
            int currentDirectDistance = manhattanDistance(origin, currentPos);
            for (Direction direction : Direction.values()) {
                BlockPos nextPos = currentPos.relative(direction);
                if (level.isOutsideBuildHeight(nextPos) || !level.hasChunkAt(nextPos)) {
                    unloadedEdges++;
                    continue;
                }
                if (!withinRadius(origin, nextPos, profile.maxRadius())) {
                    continue;
                }

                int edgeCost = MirageLightOcclusion.edgeCost(level, currentPos, nextPos, direction, profile);
                if (edgeCost == MirageLightOcclusion.BLOCKED) {
                    blockedEdges++;
                    continue;
                }

                /*
                 * dev.75d detour penalty. In open space an optimal path moves monotonically
                 * away from the source, so every step keeps the profile's normal half-decay
                 * cost. Geometry can force a path to overshoot and later move back toward
                 * the source. That inward step represents two units of (pathLength - direct
                 * Manhattan distance), so charge two detour extras here. This makes only
                 * obstacle-caused travel pay the stronger vanilla-like decay; there is no
                 * abrupt global mode switch behind a wall.
                 */
                int nextDirectDistance = manhattanDistance(origin, nextPos);
                if (nextDirectDistance < currentDirectDistance) {
                    edgeCost += profile.detourBacktrackPenaltyUnits();
                }

                int nextEnergy = energy - edgeCost;
                if (nextEnergy <= 0) {
                    continue;
                }

                long nextKey = nextPos.asLong();
                int previous = Byte.toUnsignedInt(bestEnergy.get(nextKey));
                if (nextEnergy <= previous) {
                    continue;
                }

                bestEnergy.put(nextKey, (byte) nextEnergy);
                setFieldEnergy(sections, nextPos, nextEnergy);
                enqueue(buckets, nextEnergy, nextKey);
                queuedNodes++;
                queuedEntries++;
                maxQueueSize = Math.max(maxQueueSize, queuedEntries);
            }
        }

        int litVoxels = bestEnergy.size();
        return new MirageLightField(
                source,
                sections,
                new MirageLightField.SolveStats(
                        queuedNodes,
                        settledNodes,
                        litVoxels,
                        blockedEdges,
                        unloadedEdges,
                        maxQueueSize,
                        System.nanoTime() - started
                )
        );
    }

    private static MirageLightField emptyField(
            MirageLightSource source,
            Map<Long, MirageLightSection> sections,
            long started,
            int unloadedEdges
    ) {
        return new MirageLightField(
                source,
                sections,
                new MirageLightField.SolveStats(
                        0,
                        0,
                        0,
                        0,
                        unloadedEdges,
                        0,
                        System.nanoTime() - started
                )
        );
    }

    private static void enqueue(LongArrayFIFOQueue[] buckets, int energy, long packedPos) {
        LongArrayFIFOQueue bucket = buckets[energy];
        if (bucket == null) {
            bucket = new LongArrayFIFOQueue();
            buckets[energy] = bucket;
        }
        bucket.enqueue(packedPos);
    }

    private static int estimateVoxelCapacity(int radius) {
        // Manhattan-ball volume is ~4/3*r^3. Keep the initial table bounded; fastutil
        // grows automatically for unusually obstructed/revisited fields.
        long estimate = 1L + (4L * radius * radius * radius) / 3L;
        return (int) Math.max(64L, Math.min(131_072L, estimate));
    }

    private static boolean withinRadius(BlockPos origin, BlockPos pos, int maxRadius) {
        return manhattanDistance(origin, pos) <= maxRadius;
    }

    private static int manhattanDistance(BlockPos origin, BlockPos pos) {
        return Math.abs(pos.getX() - origin.getX())
                + Math.abs(pos.getY() - origin.getY())
                + Math.abs(pos.getZ() - origin.getZ());
    }

    private static void setFieldEnergy(
            Map<Long, MirageLightSection> sections,
            BlockPos pos,
            int energyUnits
    ) {
        long sectionKey = SectionPos.asLong(pos);
        sections.computeIfAbsent(sectionKey, ignored -> new MirageLightSection())
                .setMaxEnergy(pos, energyUnits);
    }
}

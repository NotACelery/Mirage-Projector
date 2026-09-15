package celerbi.mirageprojector.client;

import celerbi.mirageprojector.block.MirageLightProjectorBlock;
import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import celerbi.mirageprojector.light.engine.MirageDynamicLightSnapshot;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

/** Discovers synchronized placed light projectors and feeds them into DYNAMIC_VISUAL. */
public final class ClientPlacedLightProjectors {
    private static final int CULL_RADIUS_BLOCKS = 64;
    private static long lastSubmitTick = Long.MIN_VALUE;

    private ClientPlacedLightProjectors() {
    }

    public static void submitNearby(Minecraft minecraft, Vec3 cameraPosition) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        ClientLevel level = minecraft.level;
        long now = level.getGameTime();
        if (lastSubmitTick == now) {
            return;
        }
        lastSubmitTick = now;

        Vec3 camera = cameraPosition == null ? Vec3.ZERO : cameraPosition;
        int centerChunkX = BlockPos.containing(camera).getX() >> 4;
        int centerChunkZ = BlockPos.containing(camera).getZ() >> 4;
        int chunkRadius = (CULL_RADIUS_BLOCKS + 15) >> 4;
        double cullSq = (double) CULL_RADIUS_BLOCKS * CULL_RADIUS_BLOCKS;

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof MirageLightProjectorBlockEntity projector)) {
                        continue;
                    }
                    BlockPos pos = entry.getKey();
                    MirageLightSourceId sourceId = MirageLightSourceId.block("light_projector", pos);
                    if (!projector.emitting()) {
                        ClientDynamicMirageLightManager.remove(sourceId);
                        continue;
                    }
                    Direction facing = projector.getBlockState().hasProperty(MirageLightProjectorBlock.FACING)
                            ? projector.getBlockState().getValue(MirageLightProjectorBlock.FACING)
                            : Direction.NORTH;
                    Vec3 direction = Vec3.atLowerCornerOf(facing.getNormal());
                    // Seed the voxel source just outside the opaque projector chassis. Starting
                    // inside the block made the first propagation edge self-occlude.
                    Vec3 sourcePos = Vec3.atCenterOf(pos).add(direction.scale(0.72D)).add(0.0D, 0.08D, 0.0D);
                    if (camera.distanceToSqr(sourcePos) > cullSq) {
                        continue;
                    }
                    ClientDynamicMirageLightManager.submit(new MirageDynamicLightSnapshot(
                            sourceId,
                            sourcePos,
                            projector.mode().profile(direction),
                            MirageDynamicLightSnapshot.DEFAULT_REFRESH_TICKS,
                            CULL_RADIUS_BLOCKS,
                            MirageDynamicLightSnapshot.DEFAULT_STALE_TICKS
                    ));
                }
            }
        }
    }

    public static void resetSession() {
        lastSubmitTick = Long.MIN_VALUE;
    }
}

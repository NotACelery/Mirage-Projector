package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server handshake requesting the authoritative Mirage light snapshot for
 * one chunk the client has just loaded.
 *
 * This is deliberately redundant with ChunkWatchEvent.Sent. Chunk tracking order can
 * vary during login/respawn; the explicit request guarantees that every client-loaded
 * chunk eventually receives its current STATIC_WORLD Mirage sections even if an earlier
 * watch notification was missed or arrived before the source was published.
 */
public record RequestMirageLightChunkPayload(long packedChunkPos) implements CustomPacketPayload {
    public static final Type<RequestMirageLightChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "request_mirage_light_chunk")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestMirageLightChunkPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RequestMirageLightChunkPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new RequestMirageLightChunkPayload(buffer.readLong());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, RequestMirageLightChunkPayload payload) {
                    buffer.writeLong(payload.packedChunkPos());
                }
            };

    @Override
    public Type<RequestMirageLightChunkPayload> type() {
        return TYPE;
    }

    public static void handle(RequestMirageLightChunkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ChunkPos chunkPos = new ChunkPos(payload.packedChunkPos());

            // Safety bound only; vanilla clients cannot normally keep chunks anywhere near
            // this far from the player. It prevents arbitrary remote probing without tying
            // correctness to our own duplicate watched-chunk registry.
            ChunkPos playerChunk = player.chunkPosition();
            int dx = Math.abs(chunkPos.x - playerChunk.x);
            int dz = Math.abs(chunkPos.z - playerChunk.z);
            if (dx > MirageLightNetwork.MAX_CLIENT_SYNC_CHUNK_DISTANCE
                    || dz > MirageLightNetwork.MAX_CLIENT_SYNC_CHUNK_DISTANCE) {
                return;
            }

            MirageLightNetwork.sendChunkSnapshot(player, player.serverLevel(), chunkPos);
        });
    }
}

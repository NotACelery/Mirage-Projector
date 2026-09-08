package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
import celerbi.mirageprojector.server.ServerAssetTransfer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UploadAssetChunkPayload(
        String assetId,
        int chunkIndex,
        int totalChunks,
        int totalBytes,
        byte[] data
) implements CustomPacketPayload {
    public static final Type<UploadAssetChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "upload_asset_chunk")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UploadAssetChunkPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UploadAssetChunkPayload decode(RegistryFriendlyByteBuf buffer) {
            return new UploadAssetChunkPayload(
                    buffer.readUtf(64),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readByteArray(ProjectionAssetRules.NETWORK_CHUNK_BYTES)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, UploadAssetChunkPayload payload) {
            buffer.writeUtf(payload.assetId(), 64);
            buffer.writeVarInt(payload.chunkIndex());
            buffer.writeVarInt(payload.totalChunks());
            buffer.writeVarInt(payload.totalBytes());
            buffer.writeByteArray(payload.data());
        }
    };

    @Override
    public Type<UploadAssetChunkPayload> type() {
        return TYPE;
    }

    public static void handle(UploadAssetChunkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerAssetTransfer.receiveUploadChunk(player, payload);
            }
        });
    }
}

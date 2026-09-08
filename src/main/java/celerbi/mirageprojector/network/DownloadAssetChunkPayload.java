package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
import celerbi.mirageprojector.client.ClientAssetTransport;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DownloadAssetChunkPayload(
        String assetId,
        int chunkIndex,
        int totalChunks,
        int totalBytes,
        byte[] data
) implements CustomPacketPayload {
    public static final Type<DownloadAssetChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "download_asset_chunk")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DownloadAssetChunkPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DownloadAssetChunkPayload decode(RegistryFriendlyByteBuf buffer) {
            return new DownloadAssetChunkPayload(
                    buffer.readUtf(64),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readByteArray(ProjectionAssetRules.NETWORK_CHUNK_BYTES)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, DownloadAssetChunkPayload payload) {
            buffer.writeUtf(payload.assetId(), 64);
            buffer.writeVarInt(payload.chunkIndex());
            buffer.writeVarInt(payload.totalChunks());
            buffer.writeVarInt(payload.totalBytes());
            buffer.writeByteArray(payload.data());
        }
    };

    @Override
    public Type<DownloadAssetChunkPayload> type() {
        return TYPE;
    }

    public static void handle(DownloadAssetChunkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientAssetTransport.receiveDownloadChunk(payload));
    }
}

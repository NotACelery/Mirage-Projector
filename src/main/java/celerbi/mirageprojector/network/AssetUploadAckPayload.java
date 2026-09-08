package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientAssetTransport;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Final server acknowledgement for a normalized image upload. */
public record AssetUploadAckPayload(String assetId, boolean accepted, String message) implements CustomPacketPayload {
    public static final Type<AssetUploadAckPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "asset_upload_ack")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AssetUploadAckPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AssetUploadAckPayload decode(RegistryFriendlyByteBuf buffer) {
            return new AssetUploadAckPayload(buffer.readUtf(64), buffer.readBoolean(), buffer.readUtf(160));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, AssetUploadAckPayload payload) {
            buffer.writeUtf(payload.assetId(), 64);
            buffer.writeBoolean(payload.accepted());
            buffer.writeUtf(payload.message() == null ? "" : payload.message(), 160);
        }
    };

    @Override
    public Type<AssetUploadAckPayload> type() {
        return TYPE;
    }

    public static void handle(AssetUploadAckPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientAssetTransport.receiveUploadAck(payload));
    }
}

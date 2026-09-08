package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.server.ServerAssetTransfer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestAssetPayload(String assetId) implements CustomPacketPayload {
    public static final Type<RequestAssetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "request_asset")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestAssetPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestAssetPayload decode(RegistryFriendlyByteBuf buffer) {
            return new RequestAssetPayload(buffer.readUtf(64));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, RequestAssetPayload payload) {
            buffer.writeUtf(payload.assetId(), 64);
        }
    };

    @Override
    public Type<RequestAssetPayload> type() {
        return TYPE;
    }

    public static void handle(RequestAssetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ServerAssetTransfer.sendAsset(player, payload.assetId());
            }
        });
    }
}

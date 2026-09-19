package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientEntityScanner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EntityScannerProgressPayload(boolean active, int progress, int totalTicks) implements CustomPacketPayload {
    public static final Type<EntityScannerProgressPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "entity_scanner_progress")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityScannerProgressPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EntityScannerProgressPayload decode(RegistryFriendlyByteBuf buffer) {
            return new EntityScannerProgressPayload(buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, EntityScannerProgressPayload payload) {
            buffer.writeBoolean(payload.active());
            buffer.writeVarInt(payload.progress());
            buffer.writeVarInt(payload.totalTicks());
        }
    };

    @Override
    public Type<EntityScannerProgressPayload> type() {
        return TYPE;
    }

    public static void handle(EntityScannerProgressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientEntityScanner.accept(payload));
    }
}

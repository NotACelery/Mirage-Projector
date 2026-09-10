package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.DebugHandbookScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class OpenDebugHandbookPayload implements CustomPacketPayload {
    public static final OpenDebugHandbookPayload INSTANCE = new OpenDebugHandbookPayload();
    public static final Type<OpenDebugHandbookPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_debug_handbook"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDebugHandbookPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenDebugHandbookPayload decode(RegistryFriendlyByteBuf buffer) {
            return INSTANCE;
        }
        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenDebugHandbookPayload payload) {
        }
    };
    private OpenDebugHandbookPayload() {
    }
    @Override
    public Type<OpenDebugHandbookPayload> type() {
        return TYPE;
    }
    public static void handle(OpenDebugHandbookPayload payload, IPayloadContext context) {
        context.enqueueWork(DebugHandbookScreen::open);
    }
}

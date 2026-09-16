package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.PresentationRemoteScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenPresentationRemotePayload(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<OpenPresentationRemotePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_presentation_remote")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPresentationRemotePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenPresentationRemotePayload decode(RegistryFriendlyByteBuf buffer) {
            return new OpenPresentationRemotePayload(buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenPresentationRemotePayload payload) {
            buffer.writeBoolean(payload.hand() == InteractionHand.OFF_HAND);
        }
    };

    @Override
    public Type<OpenPresentationRemotePayload> type() {
        return TYPE;
    }

    public static void handle(OpenPresentationRemotePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new PresentationRemoteScreen(payload.hand())));
    }
}

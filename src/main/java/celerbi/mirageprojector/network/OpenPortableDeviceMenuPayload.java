package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Opens the real portable-device container for a server-authoritative device location. */
public record OpenPortableDeviceMenuPayload(PortableDeviceSource source) implements CustomPacketPayload {
    public static final Type<OpenPortableDeviceMenuPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_portable_device_menu")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPortableDeviceMenuPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenPortableDeviceMenuPayload decode(RegistryFriendlyByteBuf buffer) {
            return new OpenPortableDeviceMenuPayload(PortableDeviceSource.byOrdinal(buffer.readVarInt()));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenPortableDeviceMenuPayload payload) {
            buffer.writeVarInt(payload.source().ordinal());
        }
    };

    @Override
    public Type<OpenPortableDeviceMenuPayload> type() {
        return TYPE;
    }

    public static void handle(OpenPortableDeviceMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PortableDeviceMenu.open(player, payload.source());
            }
        });
    }
}

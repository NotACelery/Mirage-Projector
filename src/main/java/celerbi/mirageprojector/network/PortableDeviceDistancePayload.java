package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative camera-relative distance for Hand Projector output. */
public record PortableDeviceDistancePayload(PortableDeviceSource source, int distancePixels) implements CustomPacketPayload {
    public static final Type<PortableDeviceDistancePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_device_distance")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDeviceDistancePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PortableDeviceDistancePayload decode(RegistryFriendlyByteBuf buffer) {
            return new PortableDeviceDistancePayload(PortableDeviceSource.byOrdinal(buffer.readVarInt()), buffer.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PortableDeviceDistancePayload payload) {
            buffer.writeVarInt(payload.source().ordinal());
            buffer.writeVarInt(payload.distancePixels());
        }
    };

    @Override
    public Type<PortableDeviceDistancePayload> type() {
        return TYPE;
    }

    public static void handle(PortableDeviceDistancePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack device = payload.source().resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
            MirageHandProjectorItem.setProjectionDistancePixels(device, payload.distancePixels());
            MirageHandProjectorItem.publishState(player, device);
            payload.source().commit(player, device);
            if (player.containerMenu instanceof PortableDeviceMenu menu) menu.broadcastChanges();
        });
    }
}

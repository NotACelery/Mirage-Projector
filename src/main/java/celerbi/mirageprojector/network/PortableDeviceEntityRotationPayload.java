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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative static rotation update for a Hand Projector entity source. */
public record PortableDeviceEntityRotationPayload(PortableDeviceSource source, int degrees)
        implements CustomPacketPayload {
    public static final Type<PortableDeviceEntityRotationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_device_entity_rotation")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDeviceEntityRotationPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PortableDeviceEntityRotationPayload decode(RegistryFriendlyByteBuf buffer) {
            return new PortableDeviceEntityRotationPayload(
                    PortableDeviceSource.byOrdinal(buffer.readVarInt()),
                    buffer.readVarInt()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PortableDeviceEntityRotationPayload payload) {
            buffer.writeVarInt(payload.source().ordinal());
            buffer.writeVarInt(payload.degrees());
        }
    };

    @Override
    public Type<PortableDeviceEntityRotationPayload> type() {
        return TYPE;
    }

    public static void handle(PortableDeviceEntityRotationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack device = payload.source().resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)
                    || !MirageHandProjectorItem.setPortableEntityRotation(
                    device, Mth.clamp(payload.degrees(), 0, 360), player.level())) {
                return;
            }
            MirageHandProjectorItem.publishState(player, device);
            payload.source().commit(player, device);
            if (player.containerMenu instanceof PortableDeviceMenu menu) {
                menu.broadcastChanges();
            }
        });
    }
}

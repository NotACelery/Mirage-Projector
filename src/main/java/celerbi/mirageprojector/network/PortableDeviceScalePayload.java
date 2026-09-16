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

/** Server-authoritative scale update for the Hand Projector compact chassis. */
public record PortableDeviceScalePayload(PortableDeviceSource source, int scalePixels) implements CustomPacketPayload {
    public static final Type<PortableDeviceScalePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_device_scale")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDeviceScalePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PortableDeviceScalePayload decode(RegistryFriendlyByteBuf buffer) {
            return new PortableDeviceScalePayload(
                    PortableDeviceSource.byOrdinal(buffer.readVarInt()),
                    buffer.readVarInt()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PortableDeviceScalePayload payload) {
            buffer.writeVarInt(payload.source().ordinal());
            buffer.writeVarInt(payload.scalePixels());
        }
    };

    @Override
    public Type<PortableDeviceScalePayload> type() {
        return TYPE;
    }

    public static void handle(PortableDeviceScalePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack device = payload.source().resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)
                    || !MirageHandProjectorItem.setPortableScale(device, payload.scalePixels(), player.level())) {
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

package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
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

/** Direct image-source import for the Hand Projector. Asset bytes use the shared upload channel. */
public record PortableDeviceImagePayload(
        PortableDeviceSource source,
        String assetId,
        int width,
        int height
) implements CustomPacketPayload {
    public static final Type<PortableDeviceImagePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_device_image")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDeviceImagePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PortableDeviceImagePayload decode(RegistryFriendlyByteBuf buffer) {
            return new PortableDeviceImagePayload(
                    PortableDeviceSource.byOrdinal(buffer.readVarInt()),
                    buffer.readUtf(64),
                    buffer.readVarInt(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PortableDeviceImagePayload payload) {
            buffer.writeVarInt(payload.source().ordinal());
            buffer.writeUtf(payload.assetId(), 64);
            buffer.writeVarInt(payload.width());
            buffer.writeVarInt(payload.height());
        }
    };

    @Override
    public Type<PortableDeviceImagePayload> type() {
        return TYPE;
    }

    public static void handle(PortableDeviceImagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !ProjectionAssetRules.isValidAssetId(payload.assetId())
                    || payload.width() <= 0 || payload.height() <= 0
                    || payload.width() > 2048 || payload.height() > 2048) {
                return;
            }
            ItemStack device = payload.source().resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)
                    || !MirageHandProjectorItem.setImageSource(
                    device, payload.assetId(), payload.width(), payload.height(), player.level())) {
                return;
            }
            MirageHandProjectorItem.publishState(player, device);
            payload.source().commit(player, device);
            if (player.containerMenu instanceof PortableDeviceMenu menu) {
                menu.refreshSourceSnapshot();
                menu.broadcastChanges();
            }
        });
    }
}

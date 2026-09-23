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

/** Server-authoritative continuous War Banner size and height updates. */
public record PortableDeviceWarBannerPayload(PortableDeviceSource source, Parameter parameter, int value)
        implements CustomPacketPayload {
    public static final Type<PortableDeviceWarBannerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_device_war_banner")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDeviceWarBannerPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public PortableDeviceWarBannerPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new PortableDeviceWarBannerPayload(
                            PortableDeviceSource.byOrdinal(buffer.readVarInt()),
                            Parameter.byOrdinal(buffer.readVarInt()),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, PortableDeviceWarBannerPayload payload) {
                    buffer.writeVarInt(payload.source().ordinal());
                    buffer.writeVarInt(payload.parameter().ordinal());
                    buffer.writeVarInt(payload.value());
                }
            };

    @Override
    public Type<PortableDeviceWarBannerPayload> type() {
        return TYPE;
    }

    public static void handle(PortableDeviceWarBannerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack device = payload.source().resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)
                    || !MirageHandProjectorItem.warBannerActive(device)) {
                return;
            }
            if (payload.parameter() == Parameter.SIZE) {
                MirageHandProjectorItem.setWarBannerSize(device, payload.value());
            } else {
                MirageHandProjectorItem.setWarBannerHeight(device, payload.value());
            }
            MirageHandProjectorItem.publishState(player, device);
            payload.source().commit(player, device);
            if (player.containerMenu instanceof PortableDeviceMenu menu) {
                menu.broadcastChanges();
            }
        });
    }

    public enum Parameter {
        SIZE, HEIGHT;

        static Parameter byOrdinal(int ordinal) {
            return ordinal == HEIGHT.ordinal() ? HEIGHT : SIZE;
        }
    }
}

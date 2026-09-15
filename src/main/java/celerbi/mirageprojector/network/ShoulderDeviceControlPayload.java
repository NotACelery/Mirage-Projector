package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.equipment.ShoulderEquipment;
import celerbi.mirageprojector.equipment.ShoulderEquipmentRuntime;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Small controls exposed by the initial Shoulder Device configuration screen. */
public record ShoulderDeviceControlPayload(Action action) implements CustomPacketPayload {
    public static final Type<ShoulderDeviceControlPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "shoulder_device_control")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShoulderDeviceControlPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ShoulderDeviceControlPayload decode(RegistryFriendlyByteBuf buffer) {
                    int ordinal = buffer.readVarInt();
                    Action[] values = Action.values();
                    return new ShoulderDeviceControlPayload(
                            ordinal >= 0 && ordinal < values.length ? values[ordinal] : Action.CYCLE_LANTERN_MODE
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ShoulderDeviceControlPayload payload) {
                    buffer.writeVarInt(payload.action().ordinal());
                }
            };

    @Override
    public Type<ShoulderDeviceControlPayload> type() {
        return TYPE;
    }

    public static void handle(ShoulderDeviceControlPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ShoulderEquipment equipment = ShoulderEquipmentRuntime.get(player);
            ItemStack device = equipment.device();
            if (device.isEmpty()) {
                return;
            }

            switch (payload.action()) {
                case CYCLE_LANTERN_MODE -> {
                    if (!(device.getItem() instanceof MirageLanternItem)) {
                        return;
                    }
                    PortableLightMode next = MirageLanternItem.mode(device).next();
                    MirageLanternItem.setMode(device, next);
                    player.displayClientMessage(Component.translatable(next.hudTranslationKey()), true);
                }
                case TOGGLE_PROJECTOR -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) {
                        return;
                    }
                    if (!MirageHandProjectorItem.projectionEnabled(device)
                            && (!MirageHandProjectorItem.hasProjectionProfile(device)
                            || !MirageHandProjectorItem.hasProjectedContent(device)
                            || !MirageHandProjectorItem.hasEnergyCell(device)
                            || MirageHandProjectorItem.energyPercent(device) <= 0)) {
                        player.displayClientMessage(MirageHandProjectorItem.hudComponent(device), true);
                        return;
                    }
                    MirageHandProjectorItem.setProjectionEnabled(
                            device,
                            !MirageHandProjectorItem.projectionEnabled(device)
                    );
                    player.displayClientMessage(MirageHandProjectorItem.hudComponent(device), true);
                    MirageHandProjectorItem.publishState(player, device);
                }
                case CYCLE_BANNER_PRESENTATION -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)
                            || MirageHandProjectorItem.sourceMode(device)
                            != celerbi.mirageprojector.ProjectionSettings.SourceMode.BANNER) {
                        return;
                    }
                    MirageHandProjectorItem.cycleBannerPresentation(device);
                    MirageHandProjectorItem.publishState(player, device);
                }
                case CYCLE_WAR_BANNER_FACING -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)
                            || !MirageHandProjectorItem.warBannerActive(device)) {
                        return;
                    }
                    MirageHandProjectorItem.cycleWarBannerFacing(device);
                    MirageHandProjectorItem.publishState(player, device);
                }
                case WAR_BANNER_SIZE_DOWN -> adjustWarBanner(player, device, true, -MirageHandProjectorItem.WAR_BANNER_SIZE_STEP_PERCENT);
                case WAR_BANNER_SIZE_UP -> adjustWarBanner(player, device, true, MirageHandProjectorItem.WAR_BANNER_SIZE_STEP_PERCENT);
                case WAR_BANNER_HEIGHT_DOWN -> adjustWarBanner(player, device, false, -MirageHandProjectorItem.WAR_BANNER_HEIGHT_STEP_PIXELS);
                case WAR_BANNER_HEIGHT_UP -> adjustWarBanner(player, device, false, MirageHandProjectorItem.WAR_BANNER_HEIGHT_STEP_PIXELS);
            }
            ShoulderEquipmentRuntime.broadcast(player, equipment);
        });
    }

    private static void adjustWarBanner(ServerPlayer player, ItemStack device, boolean size, int delta) {
        if (!(device.getItem() instanceof MirageHandProjectorItem)
                || !MirageHandProjectorItem.warBannerActive(device)) {
            return;
        }
        if (size) {
            MirageHandProjectorItem.adjustWarBannerSize(device, delta);
        } else {
            MirageHandProjectorItem.adjustWarBannerHeight(device, delta);
        }
        MirageHandProjectorItem.publishState(player, device);
    }

    public enum Action {
        CYCLE_LANTERN_MODE,
        TOGGLE_PROJECTOR,
        CYCLE_BANNER_PRESENTATION,
        CYCLE_WAR_BANNER_FACING,
        WAR_BANNER_SIZE_DOWN,
        WAR_BANNER_SIZE_UP,
        WAR_BANNER_HEIGHT_DOWN,
        WAR_BANNER_HEIGHT_UP
    }
}

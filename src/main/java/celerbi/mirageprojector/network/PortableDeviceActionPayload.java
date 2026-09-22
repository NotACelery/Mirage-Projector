package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageFlashlightItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Configuration actions from the Flashlight / Hand Projector container GUI. */
public record PortableDeviceActionPayload(PortableDeviceSource source, Action action) implements CustomPacketPayload {
    public static final Type<PortableDeviceActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_device_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDeviceActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PortableDeviceActionPayload decode(RegistryFriendlyByteBuf buffer) {
            PortableDeviceSource source = PortableDeviceSource.byOrdinal(buffer.readVarInt());
            int ordinal = buffer.readVarInt();
            Action[] values = Action.values();
            Action action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : Action.CYCLE_FLASHLIGHT_MODE;
            return new PortableDeviceActionPayload(source, action);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PortableDeviceActionPayload payload) {
            buffer.writeVarInt(payload.source().ordinal());
            buffer.writeVarInt(payload.action().ordinal());
        }
    };

    @Override
    public Type<PortableDeviceActionPayload> type() {
        return TYPE;
    }

    public static void handle(PortableDeviceActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack device = payload.source().resolve(player);
            if (!PortableDeviceMenu.supported(device)) {
                return;
            }

            switch (payload.action()) {
                case CYCLE_FLASHLIGHT_MODE -> {
                    if (!(device.getItem() instanceof MirageFlashlightItem)) return;
                    MirageFlashlightItem.setMode(device, MirageFlashlightItem.mode(device).next());
                }
                case TOGGLE_PROJECTOR -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    if (!MirageHandProjectorItem.projectionEnabled(device)
                            && (!MirageHandProjectorItem.hasProjectionProfile(device)
                            || !MirageHandProjectorItem.hasProjectedContent(device)
                            || !MirageHandProjectorItem.hasCore(device)
                            || !MirageHandProjectorItem.hasEnergyCell(device)
                            || MirageHandProjectorItem.energyPercent(device) <= 0
                            || !MirageHandProjectorItem.portablePowerAvailable(device, player.registryAccess()))) {
                        return;
                    }
                    MirageHandProjectorItem.setProjectionEnabled(device, !MirageHandProjectorItem.projectionEnabled(device));
                }
                case CYCLE_BANNER_PRESENTATION -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)
                            || MirageHandProjectorItem.sourceMode(device) != ProjectionSettings.SourceMode.BANNER) return;
                    MirageHandProjectorItem.cycleBannerPresentation(device);
                }
                case CYCLE_IMAGE_PRESENTATION -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)
                            || MirageHandProjectorItem.sourceMode(device) != ProjectionSettings.SourceMode.IMAGE) return;
                    MirageHandProjectorItem.cycleImagePresentation(device);
                }
                case TOGGLE_IMAGE_LIFT -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    MirageHandProjectorItem.toggleImageLift(device, player.level());
                }
                case TOGGLE_IMAGE_ROTATION -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    MirageHandProjectorItem.toggleImageRotation(device, player.level());
                }
                case TOGGLE_ENTITY_ROTATION -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    MirageHandProjectorItem.togglePortableEntityRotation(device, player.level());
                }
                case CYCLE_IMAGE_FACE_MODE -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    MirageHandProjectorItem.cycleImageFaceMode(device, player.level());
                }
                case CYCLE_WAR_BANNER_FACING -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem) || !MirageHandProjectorItem.warBannerActive(device)) return;
                    MirageHandProjectorItem.cycleWarBannerFacing(device);
                }
                case WAR_BANNER_SIZE_DOWN -> adjust(device, true, -MirageHandProjectorItem.WAR_BANNER_SIZE_STEP_PERCENT);
                case WAR_BANNER_SIZE_UP -> adjust(device, true, MirageHandProjectorItem.WAR_BANNER_SIZE_STEP_PERCENT);
                case WAR_BANNER_HEIGHT_DOWN -> adjust(device, false, -MirageHandProjectorItem.WAR_BANNER_HEIGHT_STEP_PIXELS);
                case WAR_BANNER_HEIGHT_UP -> adjust(device, false, MirageHandProjectorItem.WAR_BANNER_HEIGHT_STEP_PIXELS);
                case SELECT_IMAGE -> selectSource(device, player, ProjectionSettings.SourceMode.IMAGE);
                case SELECT_ITEM -> { /* Legacy packet: Item is no longer a Hand Projector workspace. */ }
                case SELECT_ENTITY -> selectSource(device, player, ProjectionSettings.SourceMode.ENTITY);
                case SELECT_BANNER -> selectSource(device, player, ProjectionSettings.SourceMode.BANNER);
            }

            if (device.getItem() instanceof MirageHandProjectorItem) {
                MirageHandProjectorItem.publishState(player, device);
            }
            payload.source().commit(player, device);
            if (player.containerMenu instanceof PortableDeviceMenu menu) {
                menu.refreshSourceSnapshot();
                menu.broadcastChanges();
            }
        });
    }

    private static void selectSource(ItemStack device, ServerPlayer player, ProjectionSettings.SourceMode sourceMode) {
        if (!(device.getItem() instanceof MirageHandProjectorItem)) {
            return;
        }
        MirageHandProjectorItem.selectSourceMode(device, sourceMode, player.level());
    }

    private static void adjust(ItemStack device, boolean size, int delta) {
        if (!(device.getItem() instanceof MirageHandProjectorItem) || !MirageHandProjectorItem.warBannerActive(device)) {
            return;
        }
        if (size) MirageHandProjectorItem.adjustWarBannerSize(device, delta);
        else MirageHandProjectorItem.adjustWarBannerHeight(device, delta);
    }

    public enum Action {
        CYCLE_FLASHLIGHT_MODE,
        TOGGLE_PROJECTOR,
        CYCLE_BANNER_PRESENTATION,
        CYCLE_IMAGE_PRESENTATION,
        TOGGLE_IMAGE_LIFT,
        TOGGLE_IMAGE_ROTATION,
        TOGGLE_ENTITY_ROTATION,
        CYCLE_IMAGE_FACE_MODE,
        CYCLE_WAR_BANNER_FACING,
        WAR_BANNER_SIZE_DOWN,
        WAR_BANNER_SIZE_UP,
        WAR_BANNER_HEIGHT_DOWN,
        WAR_BANNER_HEIGHT_UP,
        SELECT_IMAGE,
        SELECT_ITEM,
        SELECT_ENTITY,
        SELECT_BANNER
    }
}

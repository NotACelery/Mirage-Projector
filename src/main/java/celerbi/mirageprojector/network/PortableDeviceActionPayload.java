package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Configuration actions from the Lantern / Hand Projector container GUI. */
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
            Action action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : Action.CYCLE_LANTERN_MODE;
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
                case CYCLE_LANTERN_MODE -> {
                    if (!(device.getItem() instanceof MirageLanternItem)) return;
                    MirageLanternItem.setMode(device, MirageLanternItem.mode(device).next());
                }
                case TOGGLE_PROJECTOR -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    if (!MirageHandProjectorItem.projectionEnabled(device)
                            && (!MirageHandProjectorItem.hasProjectionProfile(device)
                            || !MirageHandProjectorItem.hasProjectedContent(device)
                            || !MirageHandProjectorItem.hasEnergyCell(device)
                            || MirageHandProjectorItem.energyPercent(device) <= 0)) {
                        return;
                    }
                    MirageHandProjectorItem.setProjectionEnabled(device, !MirageHandProjectorItem.projectionEnabled(device));
                }
                case COPY_TARGET_PROJECTOR -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
                    Vec3 eye = player.getEyePosition();
                    Vec3 end = eye.add(player.getLookAngle().scale(6.0D));
                    BlockHitResult hit = player.level().clip(new ClipContext(
                            eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                    if (hit.getType() != HitResult.Type.BLOCK
                            || !(player.level().getBlockEntity(hit.getBlockPos()) instanceof MirageProjectorBlockEntity source)) {
                        return;
                    }
                    MirageHandProjectorItem.copyPortableProfile(device, source, player.registryAccess());
                }
                case CYCLE_BANNER_PRESENTATION -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem)
                            || MirageHandProjectorItem.sourceMode(device) != ProjectionSettings.SourceMode.BANNER) return;
                    MirageHandProjectorItem.cycleBannerPresentation(device);
                }
                case CYCLE_WAR_BANNER_FACING -> {
                    if (!(device.getItem() instanceof MirageHandProjectorItem) || !MirageHandProjectorItem.warBannerActive(device)) return;
                    MirageHandProjectorItem.cycleWarBannerFacing(device);
                }
                case WAR_BANNER_SIZE_DOWN -> adjust(device, true, -MirageHandProjectorItem.WAR_BANNER_SIZE_STEP_PERCENT);
                case WAR_BANNER_SIZE_UP -> adjust(device, true, MirageHandProjectorItem.WAR_BANNER_SIZE_STEP_PERCENT);
                case WAR_BANNER_HEIGHT_DOWN -> adjust(device, false, -MirageHandProjectorItem.WAR_BANNER_HEIGHT_STEP_PIXELS);
                case WAR_BANNER_HEIGHT_UP -> adjust(device, false, MirageHandProjectorItem.WAR_BANNER_HEIGHT_STEP_PIXELS);
            }

            if (device.getItem() instanceof MirageHandProjectorItem) {
                MirageHandProjectorItem.publishState(player, device);
            }
            payload.source().commit(player, device);
        });
    }

    private static void adjust(ItemStack device, boolean size, int delta) {
        if (!(device.getItem() instanceof MirageHandProjectorItem) || !MirageHandProjectorItem.warBannerActive(device)) {
            return;
        }
        if (size) MirageHandProjectorItem.adjustWarBannerSize(device, delta);
        else MirageHandProjectorItem.adjustWarBannerHeight(device, delta);
    }

    public enum Action {
        CYCLE_LANTERN_MODE,
        TOGGLE_PROJECTOR,
        COPY_TARGET_PROJECTOR,
        CYCLE_BANNER_PRESENTATION,
        CYCLE_WAR_BANNER_FACING,
        WAR_BANNER_SIZE_DOWN,
        WAR_BANNER_SIZE_UP,
        WAR_BANNER_HEIGHT_DOWN,
        WAR_BANNER_HEIGHT_UP
    }
}

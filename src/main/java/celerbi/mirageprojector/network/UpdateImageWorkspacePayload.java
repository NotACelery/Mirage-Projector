package celerbi.mirageprojector.network;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateImageWorkspacePayload(
        BlockPos pos,
        String frontId, int frontWidth, int frontHeight,
        String backId, int backWidth, int backHeight,
        String eastId, int eastWidth, int eastHeight,
        String westId, int westWidth, int westHeight,
        ImageSourceBank sourceBank,
        ProjectionSettings.ImageLayoutMode imageLayoutMode,
        ProjectionSettings.BackFaceMode backFaceMode,
        boolean flipVertical,
        boolean scanlines
) implements CustomPacketPayload {
    public static final Type<UpdateImageWorkspacePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "update_image_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateImageWorkspacePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UpdateImageWorkspacePayload decode(RegistryFriendlyByteBuf b) {
            return new UpdateImageWorkspacePayload(
                    b.readBlockPos(),
                    b.readUtf(128), b.readVarInt(), b.readVarInt(),
                    b.readUtf(128), b.readVarInt(), b.readVarInt(),
                    b.readUtf(128), b.readVarInt(), b.readVarInt(),
                    b.readUtf(128), b.readVarInt(), b.readVarInt(),
                    ImageSourceBank.read(b),
                    ProjectionSettings.ImageLayoutMode.fromOrdinal(b.readVarInt()),
                    ProjectionSettings.BackFaceMode.fromOrdinal(b.readVarInt()), b.readBoolean(), b.readBoolean()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf b, UpdateImageWorkspacePayload p) {
            b.writeBlockPos(p.pos());
            writeAsset(b, p.frontId(), p.frontWidth(), p.frontHeight());
            writeAsset(b, p.backId(), p.backWidth(), p.backHeight());
            writeAsset(b, p.eastId(), p.eastWidth(), p.eastHeight());
            writeAsset(b, p.westId(), p.westWidth(), p.westHeight());
            (p.sourceBank() == null ? new ImageSourceBank() : p.sourceBank()).write(b);
            b.writeVarInt(p.imageLayoutMode().ordinal());
            b.writeVarInt(p.backFaceMode().ordinal());
            b.writeBoolean(p.flipVertical());
            b.writeBoolean(p.scanlines());
        }
    };

    private static void writeAsset(RegistryFriendlyByteBuf buffer, String id, int width, int height) {
        buffer.writeUtf(id == null ? "" : id, 128);
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
    }

    @Override
    public Type<UpdateImageWorkspacePayload> type() {
        return TYPE;
    }

    public static void handle(UpdateImageWorkspacePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D
                    || !player.level().hasChunkAt(pos)) return;
            if (player.level().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
                ProjectionSettings merged = projector.settings().withImageWorkspace(
                        payload.frontId(), payload.frontWidth(), payload.frontHeight(),
                        payload.backId(), payload.backWidth(), payload.backHeight(),
                        payload.eastId(), payload.eastWidth(), payload.eastHeight(),
                        payload.westId(), payload.westWidth(), payload.westHeight(),
                        payload.imageLayoutMode(), payload.backFaceMode(), payload.flipVertical(), payload.scanlines()
                );
                projector.applyImageWorkspace(merged, payload.sourceBank());
            }
        });
    }
}

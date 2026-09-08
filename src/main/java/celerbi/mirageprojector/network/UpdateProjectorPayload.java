package celerbi.mirageprojector.network;

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

public record UpdateProjectorPayload(BlockPos pos, ProjectionSettings settings) implements CustomPacketPayload {
    public static final Type<UpdateProjectorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "update_projector")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateProjectorPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public UpdateProjectorPayload decode(RegistryFriendlyByteBuf buffer) {
                    BlockPos pos = buffer.readBlockPos();
                    String imageId = buffer.readUtf(128);
                    int imageWidth = buffer.readVarInt();
                    int imageHeight = buffer.readVarInt();
                    String backImageId = buffer.readUtf(128);
                    int backImageWidth = buffer.readVarInt();
                    int backImageHeight = buffer.readVarInt();

                    ProjectionSettings settings = new ProjectionSettings(
                            imageId,
                            imageWidth,
                            imageHeight,
                            backImageId,
                            backImageWidth,
                            backImageHeight,
                            ProjectionSettings.SourceMode.fromOrdinal(buffer.readVarInt()),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readBoolean(),
                            buffer.readVarInt(),
                            buffer.readBoolean(),
                            buffer.readFloat(),
                            buffer.readBoolean(),
                            ProjectionSettings.FloatMode.fromOrdinal(buffer.readVarInt()),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            ProjectionSettings.BackFaceMode.fromOrdinal(buffer.readVarInt()),
                            buffer.readBoolean(),
                            buffer.readBoolean()
                    ).sanitized();
                    return new UpdateProjectorPayload(pos, settings);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, UpdateProjectorPayload payload) {
                    ProjectionSettings s = payload.settings().sanitized();
                    buffer.writeBlockPos(payload.pos());
                    writeAsset(buffer, s.imageId(), s.imageWidth(), s.imageHeight());
                    writeAsset(buffer, s.backImageId(), s.backImageWidth(), s.backImageHeight());
                    buffer.writeVarInt(s.sourceMode().ordinal());
                    buffer.writeVarInt(s.scalePixels());
                    buffer.writeVarInt(s.liftPixels());
                    buffer.writeBoolean(s.rotationEnabled());
                    buffer.writeVarInt(s.rotationPeriodTicks());
                    buffer.writeBoolean(s.clockwise());
                    buffer.writeFloat(s.rotationOffsetDegrees());
                    buffer.writeBoolean(s.floatingEnabled());
                    buffer.writeVarInt(s.floatMode().ordinal());
                    buffer.writeVarInt(s.floatAmplitudePixels());
                    buffer.writeVarInt(s.floatCycleTicks());
                    buffer.writeVarInt(s.floatIntervalDegrees());
                    buffer.writeVarInt(s.backFaceMode().ordinal());
                    buffer.writeBoolean(s.flipVertical());
                    buffer.writeBoolean(s.debugChassisOverride());
                }

                private void writeAsset(RegistryFriendlyByteBuf buffer, String id, int width, int height) {
                    buffer.writeUtf(id, 128);
                    buffer.writeVarInt(width);
                    buffer.writeVarInt(height);
                }
            };

    @Override
    public Type<UpdateProjectorPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateProjectorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            BlockPos pos = payload.pos();
            double distance = player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (distance > 64.0D || !player.level().hasChunkAt(pos)) {
                return;
            }

            if (player.level().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
                ProjectionSettings settings = payload.settings();
                if (settings.debugChassisOverride() && !player.isCreative()) {
                    settings = settings.withDebugChassisOverride(false);
                }
                projector.applySettings(settings);
            }
        });
    }
}

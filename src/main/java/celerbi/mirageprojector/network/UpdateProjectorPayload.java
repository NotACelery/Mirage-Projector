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

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateProjectorPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UpdateProjectorPayload decode(RegistryFriendlyByteBuf buffer) {
            return new UpdateProjectorPayload(buffer.readBlockPos(), ProjectionSettings.read(buffer));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, UpdateProjectorPayload payload) {
            buffer.writeBlockPos(payload.pos());
            payload.settings().write(buffer);
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

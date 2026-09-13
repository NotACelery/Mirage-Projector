package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetProjectionEnabledPayload(BlockPos pos, boolean enabled) implements CustomPacketPayload {
    public static final Type<SetProjectionEnabledPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "set_projection_enabled")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SetProjectionEnabledPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SetProjectionEnabledPayload decode(RegistryFriendlyByteBuf buffer) {
            return new SetProjectionEnabledPayload(buffer.readBlockPos(), buffer.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, SetProjectionEnabledPayload payload) {
            buffer.writeBlockPos(payload.pos());
            buffer.writeBoolean(payload.enabled());
        }
    };

    @Override
    public Type<SetProjectionEnabledPayload> type() {
        return TYPE;
    }

    public static void handle(SetProjectionEnabledPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D
                    || !player.level().hasChunkAt(pos)) {
                return;
            }
            if (!(player.level().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector)) {
                return;
            }
            if (!projector.setProjectionEnabled(payload.enabled())
                    && !payload.enabled() && projector.endResonanceLocksControls()) {
                player.displayClientMessage(
                        Component.translatable("message.mirage_projector.end_resonance.turn_off_blocked"),
                        true
                );
            }
        });
    }
}

package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative request to eject and invalidate the Wall/Data-show presentation remote. */
public record UnpairPresentationRemotePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<UnpairPresentationRemotePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "unpair_presentation_remote")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UnpairPresentationRemotePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UnpairPresentationRemotePayload decode(RegistryFriendlyByteBuf buffer) {
            return new UnpairPresentationRemotePayload(buffer.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, UnpairPresentationRemotePayload payload) {
            buffer.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<UnpairPresentationRemotePayload> type() {
        return TYPE;
    }

    public static void handle(UnpairPresentationRemotePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D
                    || !player.serverLevel().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                return;
            }
            if (!(player.serverLevel().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector)
                    || projector.chassisProfile() != ProjectionChassisProfile.WALL) {
                return;
            }
            if (projector.unpairPresentationRemote(player)) {
                player.displayClientMessage(Component.translatable(
                        "message.mirage_projector.presentation_remote.unpaired"), true);
            }
        });
    }
}

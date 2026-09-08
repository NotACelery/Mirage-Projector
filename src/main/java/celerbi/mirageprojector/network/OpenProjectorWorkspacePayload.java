package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Returns from a source workspace to the primary global presentation/Core editor. */
public record OpenProjectorWorkspacePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenProjectorWorkspacePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_projector_workspace")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenProjectorWorkspacePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenProjectorWorkspacePayload decode(RegistryFriendlyByteBuf buffer) {
            return new OpenProjectorWorkspacePayload(buffer.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenProjectorWorkspacePayload payload) {
            buffer.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<OpenProjectorWorkspacePayload> type() {
        return TYPE;
    }

    public static void handle(OpenProjectorWorkspacePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D
                    || !player.level().hasChunkAt(pos)) {
                return;
            }
            if (player.level().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
                ((IPlayerExtension) player).openMenu(projector, projector::writeMenuData);
            }
        });
    }
}

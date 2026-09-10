package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BannerWorkspaceActionPayload(BlockPos pos, Action action) implements CustomPacketPayload {
    public static final Type<BannerWorkspaceActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "banner_workspace_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BannerWorkspaceActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public BannerWorkspaceActionPayload decode(RegistryFriendlyByteBuf buffer) {
            return new BannerWorkspaceActionPayload(buffer.readBlockPos(), Action.fromOrdinal(buffer.readVarInt()));
        }
        @Override public void encode(RegistryFriendlyByteBuf buffer, BannerWorkspaceActionPayload payload) {
            buffer.writeBlockPos(payload.pos());
            buffer.writeVarInt(payload.action().ordinal());
        }
    };
    @Override
    public Type<BannerWorkspaceActionPayload> type() {
        return TYPE;
    }

    public static void handle(BannerWorkspaceActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D || !player.level().hasChunkAt(pos)) {
                return;
            }
            if (!(player.level().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector)) {
                return;
            }
            switch (payload.action()) {
                case COPY_PRIMARY_TO_ALL -> projector.copyPrimaryBannerToAllFaces();
                case CLEAR_ALL -> projector.clearAllBannerSnapshots();
            }
        });
    }

    public enum Action {
        COPY_PRIMARY_TO_ALL,
        CLEAR_ALL;

        public static Action fromOrdinal(int ordinal) {
            Action[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : CLEAR_ALL;
        }
    }
}

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

public record SetProjectionSourcePayload(BlockPos pos, ProjectionSettings.SourceMode sourceMode) implements CustomPacketPayload {
    public static final Type<SetProjectionSourcePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "set_projection_source"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetProjectionSourcePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public SetProjectionSourcePayload decode(RegistryFriendlyByteBuf b) { return new SetProjectionSourcePayload(b.readBlockPos(), ProjectionSettings.SourceMode.fromOrdinal(b.readVarInt())); }
        @Override public void encode(RegistryFriendlyByteBuf b, SetProjectionSourcePayload p) { b.writeBlockPos(p.pos()); b.writeVarInt(p.sourceMode().ordinal()); }
    };
    @Override public Type<SetProjectionSourcePayload> type() { return TYPE; }

    public static void handle(SetProjectionSourcePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D || !player.level().hasChunkAt(pos)) return;
            if (player.level().getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
                projector.applySettings(projector.settings().withSourceMode(payload.sourceMode()));
            }
        });
    }
}

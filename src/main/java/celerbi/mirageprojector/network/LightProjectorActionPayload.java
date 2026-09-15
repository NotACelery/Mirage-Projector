package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Small server-authoritative controls for the placed light-projector GUI. */
public record LightProjectorActionPayload(BlockPos pos, Action action) implements CustomPacketPayload {
    public static final Type<LightProjectorActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "light_projector_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LightProjectorActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public LightProjectorActionPayload decode(RegistryFriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            int ordinal = buffer.readVarInt();
            Action[] values = Action.values();
            return new LightProjectorActionPayload(pos, ordinal >= 0 && ordinal < values.length ? values[ordinal] : Action.CYCLE_MODE);
        }
        @Override public void encode(RegistryFriendlyByteBuf buffer, LightProjectorActionPayload payload) {
            buffer.writeBlockPos(payload.pos());
            buffer.writeVarInt(payload.action().ordinal());
        }
    };
    @Override public Type<LightProjectorActionPayload> type() { return TYPE; }
    public static void handle(LightProjectorActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || player.distanceToSqr(payload.pos().getX() + 0.5D, payload.pos().getY() + 0.5D, payload.pos().getZ() + 0.5D) > 64.0D
                    || !(player.level().getBlockEntity(payload.pos()) instanceof MirageLightProjectorBlockEntity projector)) return;
            if (payload.action() == Action.CYCLE_MODE) projector.cycleMode();
        });
    }
    public enum Action { CYCLE_MODE }
}

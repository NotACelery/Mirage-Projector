package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Explicit non-inventory actions from the Entity/Humanoid workspace. */
public record EntityWorkspaceActionPayload(
        BlockPos pos,
        Action action,
        VirtualEquipmentSnapshots.Channel channel
) implements CustomPacketPayload {
    public static final Type<EntityWorkspaceActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "entity_workspace_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityWorkspaceActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EntityWorkspaceActionPayload decode(RegistryFriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            Action action = Action.fromOrdinal(buffer.readVarInt());
            VirtualEquipmentSnapshots.Channel channel = channelFromOrdinal(buffer.readVarInt());
            return new EntityWorkspaceActionPayload(pos, action, channel);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, EntityWorkspaceActionPayload payload) {
            buffer.writeBlockPos(payload.pos());
            buffer.writeVarInt(payload.action().ordinal());
            buffer.writeVarInt(payload.channel() == null ? -1 : payload.channel().ordinal());
        }
    };

    @Override
    public Type<EntityWorkspaceActionPayload> type() {
        return TYPE;
    }

    public static void handle(EntityWorkspaceActionPayload payload, IPayloadContext context) {
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

            switch (payload.action()) {
                case APPLY -> {
                    if (payload.channel() != null) {
                        projector.applyEntityEquipment(payload.channel(), false);
                    }
                }
                case REPLACE -> {
                    if (payload.channel() != null) {
                        projector.applyEntityEquipment(payload.channel(), true);
                    }
                }
                case CLEAR_PROJECTED -> {
                    if (payload.channel() != null) {
                        projector.clearProjectedEntityEquipment(payload.channel());
                    }
                }
                case RETURN_STAGING -> projector.returnPhysicalStagingTo(player);
                case CAPTURE_EQUIPPED -> {
                    if (projector.stagedEntityCard().isEmpty()
                            || projector.stagedEntityCardKind() == EntityScanData.Kind.HUMANOID) {
                        projector.captureEquippedHumanoidLoadout(player);
                    }
                }
                case CYCLE_POSE -> {
                    if (projector.stagedEntityCard().isEmpty()
                            || projector.stagedEntityCardKind() == EntityScanData.Kind.HUMANOID) {
                        projector.cycleHumanoidPose();
                    }
                }
            }
        });
    }

    private static VirtualEquipmentSnapshots.Channel channelFromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VirtualEquipmentSnapshots.Channel.values().length) {
            return null;
        }
        return VirtualEquipmentSnapshots.Channel.values()[ordinal];
    }

    public enum Action {
        APPLY,
        REPLACE,
        CLEAR_PROJECTED,
        RETURN_STAGING,
        CAPTURE_EQUIPPED,
        CYCLE_POSE;

        static Action fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                return APPLY;
            }
            return values()[ordinal];
        }
    }
}

package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.menu.EntityProjectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Opens the dedicated Entity/Humanoid workspace for one nearby projector. */
public record OpenEntityWorkspacePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenEntityWorkspacePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_entity_workspace")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEntityWorkspacePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenEntityWorkspacePayload decode(RegistryFriendlyByteBuf buffer) {
            return new OpenEntityWorkspacePayload(buffer.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenEntityWorkspacePayload payload) {
            buffer.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<OpenEntityWorkspacePayload> type() {
        return TYPE;
    }

    public static void handle(OpenEntityWorkspacePayload payload, IPayloadContext context) {
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

            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> new EntityProjectorMenu(containerId, inventory, projector),
                    Component.translatable("container.mirage_projector.entity_workspace")
            );
            ((IPlayerExtension) player).openMenu(provider, buffer -> buffer.writeBlockPos(pos));
        });
    }
}

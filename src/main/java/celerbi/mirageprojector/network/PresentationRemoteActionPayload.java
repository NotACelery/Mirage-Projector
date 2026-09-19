package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.item.PresentationRemoteItem;
import celerbi.mirageprojector.registry.ModItems;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PresentationRemoteActionPayload(InteractionHand hand, int direction) implements CustomPacketPayload {
    public static final Type<PresentationRemoteActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "presentation_remote_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PresentationRemoteActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PresentationRemoteActionPayload decode(RegistryFriendlyByteBuf buffer) {
            return new PresentationRemoteActionPayload(
                    buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
                    Math.max(-1, Math.min(1, buffer.readByte()))
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PresentationRemoteActionPayload payload) {
            buffer.writeBoolean(payload.hand() == InteractionHand.OFF_HAND);
            buffer.writeByte(Math.max(-1, Math.min(1, payload.direction())));
        }
    };

    @Override
    public Type<PresentationRemoteActionPayload> type() {
        return TYPE;
    }

    public static void handle(PresentationRemoteActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || payload.direction() == 0) {
                return;
            }
            ItemStack remote = player.getItemInHand(payload.hand());
            if (!remote.is(ModItems.PRESENTATION_REMOTE.get())) {
                return;
            }
            Optional<PresentationRemoteItem.Binding> binding = PresentationRemoteItem.binding(remote);
            if (binding.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.mirage_projector.presentation_remote.unbound"), true);
                return;
            }
            PresentationRemoteItem.Binding linked = binding.get();
            ResourceLocation dimensionId = ResourceLocation.tryParse(linked.dimension());
            if (dimensionId == null) {
                return;
            }
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
            if (!player.level().dimension().equals(dimensionKey)) {
                player.displayClientMessage(Component.translatable("message.mirage_projector.presentation_remote.wrong_dimension"), true);
                return;
            }
            ServerLevel targetLevel = player.getServer().getLevel(dimensionKey);
            if (targetLevel == null || !targetLevel.hasChunk(linked.pos().getX() >> 4, linked.pos().getZ() >> 4)) {
                player.displayClientMessage(Component.translatable("message.mirage_projector.presentation_remote.unavailable"), true);
                return;
            }
            if (!(targetLevel.getBlockEntity(linked.pos()) instanceof MirageProjectorBlockEntity projector)
                    || projector.chassisProfile() != ProjectionChassisProfile.WALL
                    || projector.presentationLinkId() == null
                    || !projector.presentationLinkId().equals(linked.linkId())) {
                player.displayClientMessage(Component.translatable("message.mirage_projector.presentation_remote.unavailable"), true);
                return;
            }
            if (!projector.stepPresentationSlide(payload.direction(), false)) {
                player.displayClientMessage(Component.translatable("message.mirage_projector.presentation_remote.no_slides"), true);
                return;
            }
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.presentation_remote.slide", projector.wallSlideIndex() + 1
            ), true);
        });
    }
}

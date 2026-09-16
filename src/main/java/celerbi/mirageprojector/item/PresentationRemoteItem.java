package celerbi.mirageprojector.item;

import celerbi.mirageprojector.network.OpenPresentationRemotePayload;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/** Handheld three-position presentation controller bound to one Mirage Wall Projector. */
public final class PresentationRemoteItem extends Item {
    private static final String LINK_ID = "MiragePresentationLinkId";
    private static final String LINK_DIMENSION = "MiragePresentationDimension";
    private static final String LINK_POS = "MiragePresentationPos";

    public PresentationRemoteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static void bind(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos, UUID linkId) {
        if (stack == null || stack.isEmpty() || dimension == null || pos == null || linkId == null) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putUUID(LINK_ID, linkId);
            tag.putString(LINK_DIMENSION, dimension.location().toString());
            tag.putLong(LINK_POS, pos.asLong());
        });
    }

    public static void unbind(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(LINK_ID);
            tag.remove(LINK_DIMENSION);
            tag.remove(LINK_POS);
        });
    }

    public static Optional<Binding> binding(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID(LINK_ID) || !tag.contains(LINK_DIMENSION) || !tag.contains(LINK_POS)) {
            return Optional.empty();
        }
        String dimension = tag.getString(LINK_DIMENSION);
        if (dimension.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new Binding(tag.getUUID(LINK_ID), dimension, BlockPos.of(tag.getLong(LINK_POS))));
    }

    public static boolean isBound(ItemStack stack) {
        return binding(stack).isPresent();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openController(serverPlayer, stack, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        // Docking into a Wall/Data-show is handled first by PresentationRemoteInteractionEvents.
        // For every other target, RMB behaves exactly like using the controller in the air.
        if (!context.getLevel().isClientSide && player instanceof ServerPlayer serverPlayer) {
            openController(serverPlayer, context.getItemInHand(), context.getHand());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    private static void openController(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (!isBound(stack)) {
            player.displayClientMessage(Component.translatable("message.mirage_projector.presentation_remote.unbound"), true);
            return;
        }
        PacketDistributor.sendToPlayer(player, new OpenPresentationRemotePayload(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Optional<Binding> binding = binding(stack);
        if (binding.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.mirage_projector.presentation_remote.unbound")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mirage_projector.presentation_remote.dock")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            return;
        }
        Binding linked = binding.get();
        tooltip.add(Component.translatable("tooltip.mirage_projector.presentation_remote.bound")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(linked.dimension() + "  "
                        + linked.pos().getX() + ", " + linked.pos().getY() + ", " + linked.pos().getZ())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.mirage_projector.presentation_remote.use")
                .withStyle(ChatFormatting.GRAY));
    }

    public record Binding(UUID linkId, String dimension, BlockPos pos) {
    }
}

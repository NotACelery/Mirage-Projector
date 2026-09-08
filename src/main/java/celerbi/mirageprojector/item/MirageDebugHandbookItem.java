package celerbi.mirageprojector.item;

import celerbi.mirageprojector.network.OpenDebugHandbookPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/** Temporary in-game manual for the active development line. Pages are translated client-side. */
public final class MirageDebugHandbookItem extends Item {
    public MirageDebugHandbookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, OpenDebugHandbookPayload.INSTANCE);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

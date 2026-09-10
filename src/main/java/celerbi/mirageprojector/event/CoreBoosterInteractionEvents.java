package celerbi.mirageprojector.event;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class CoreBoosterInteractionEvents {
    private CoreBoosterInteractionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof CoreBoosterBlockEntity booster)) {
            return;
        }

        var player = event.getEntity();
        ItemStack held = event.getItemStack();

        if (player.isShiftKeyDown()) {
            if (booster.empty()) {
                return;
            }
            ItemStack expected = booster.material().centerStack();
            if (!held.isEmpty() && (expected.isEmpty() || !held.is(expected.getItem()))) {
                return;
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            if (event.getLevel().isClientSide) {
                return;
            }
            ItemStack returned = booster.extractMaterial();
            if (!returned.isEmpty()) {
                if (held.isEmpty()) {
                    player.setItemInHand(event.getHand(), returned);
                } else if (ItemStack.isSameItemSameComponents(held, returned)
                        && held.getCount() < held.getMaxStackSize()) {
                    held.grow(1);
                } else if (!player.getInventory().add(returned)) {
                    player.drop(returned, false);
                }
            }
            player.displayClientMessage(Component.translatable("message.mirage_projector.core_booster.extracted"), true);
            return;
        }

        CoreBoosterMaterial material = CoreBoosterMaterial.fromInsertStack(held);
        if (!material.present()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getLevel().isClientSide) {
            return;
        }

        if (!booster.empty()) {
            player.displayClientMessage(Component.translatable("message.mirage_projector.core_booster.occupied"), true);
            return;
        }

        if (booster.insert(held)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.core_booster.inserted",
                    material.displayComponent()
            ), true);
        }
    }
}

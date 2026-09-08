package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.EntityScanCardItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Gives deliberate scan-card sneak-use priority over vanilla entity interactions. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class EntityScanInteractionEvents {
    private EntityScanInteractionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.getEntity().isShiftKeyDown() || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof EntityScanCardItem scanCard)) {
            return;
        }

        InteractionResult result = scanCard.scanTarget(stack, event.getEntity(), target);
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}

package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.EntityScanCardItem;
import celerbi.mirageprojector.item.ScanCodexItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class EntityScanInteractionEvents {
    private EntityScanInteractionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.getEntity().isShiftKeyDown() || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        InteractionResult result;
        if (stack.getItem() instanceof EntityScanCardItem scanCard) {
            result = scanCard.scanTarget(stack, event.getEntity(), target);
        } else if (stack.getItem() instanceof ScanCodexItem codex) {
            result = codex.scanTarget(stack, event.getEntity(), target);
        } else {
            return;
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}

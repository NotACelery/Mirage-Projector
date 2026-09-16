package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.ScanCodexItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** High-priority capture gesture reserved for the Scan Codex. Entity Scan Cards are containers only. */
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
        if (!(stack.getItem() instanceof ScanCodexItem codex)) {
            return;
        }

        InteractionResult result = codex.scanTarget(stack, event.getEntity(), target);
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}

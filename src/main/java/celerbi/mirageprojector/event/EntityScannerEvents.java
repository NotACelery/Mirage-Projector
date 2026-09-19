package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.EntityScannerItem;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;

@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class EntityScannerEvents {
    private EntityScannerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EntityScannerItem.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EntityScannerItem.clearSession(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (interceptEntityInteraction(event.getEntity(), event.getTarget())) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (interceptEntityInteraction(event.getEntity(), event.getTarget())) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    private static boolean interceptEntityInteraction(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.entity.Entity target
    ) {
        if (!player.getMainHandItem().is(celerbi.mirageprojector.registry.ModItems.ENTITY_SCANNER.get())
                || !(target instanceof net.minecraft.world.entity.LivingEntity living)) {
            return false;
        }
        EntityScannerItem.beginScanning(player, living);
        return true;
    }
}

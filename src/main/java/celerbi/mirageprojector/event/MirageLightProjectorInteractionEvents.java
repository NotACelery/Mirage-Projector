package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** All battery and mode manipulation now lives in the Light Projector GUI. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class MirageLightProjectorInteractionEvents {
    private MirageLightProjectorInteractionEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof MirageLightProjectorBlockEntity projector)) {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            ((IPlayerExtension) player).openMenu(projector, buffer -> buffer.writeBlockPos(event.getPos()));
        }
    }
}

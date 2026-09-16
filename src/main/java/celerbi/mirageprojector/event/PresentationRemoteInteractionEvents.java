package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Physical pairing dock for the Mirage Wall/Data-show presentation remote. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class PresentationRemoteInteractionEvents {
    private PresentationRemoteInteractionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof MirageProjectorBlockEntity projector)
                || projector.chassisProfile() != ProjectionChassisProfile.WALL) {
            return;
        }

        ItemStack held = event.getItemStack();
        var player = event.getEntity();

        if (held.is(ModItems.PRESENTATION_REMOTE.get())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            if (event.getLevel().isClientSide) {
                return;
            }
            if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (projector.hasDockedPresentationRemote()) {
                player.displayClientMessage(Component.translatable(
                        "message.mirage_projector.presentation_remote.dock_occupied"), true);
                return;
            }
            if (projector.insertPresentationRemote(held, serverLevel)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                player.displayClientMessage(Component.translatable(
                        "message.mirage_projector.presentation_remote.paired"), true);
            }
            return;
        }

        // Sneak + empty hand retrieves the paired remote before the generic Table/Wall pickup
        // gesture gets a chance to pack the whole projector.
        if (player.isShiftKeyDown() && held.isEmpty() && projector.hasDockedPresentationRemote()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            if (event.getLevel().isClientSide || !(event.getLevel() instanceof ServerLevel serverLevel)) {
                return;
            }
            ItemStack remote = projector.extractPresentationRemote(serverLevel);
            if (!remote.isEmpty() && !player.addItem(remote)) {
                player.drop(remote, false);
            }
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.presentation_remote.retrieved"), true);
        }
    }
}

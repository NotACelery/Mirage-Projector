package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.scan.ScanCodexLecternService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Integrates the Mirage Scan Codex with the vanilla lectern instead of registering a separate copy station.
 */
@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class ScanCodexLecternEvents {
    private ScanCodexLecternEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LecternBlock)
                || !(level.getBlockEntity(pos) instanceof LecternBlockEntity lectern)) {
            return;
        }

        ItemStack mounted = lectern.getBook();
        if (mounted.is(ModItems.SCAN_CODEX.get())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            if (!level.isClientSide && event.getEntity() instanceof ServerPlayer player) {
                ScanCodexLecternService.openMenu(player, pos, mounted);
            }
            return;
        }

        ItemStack held = event.getItemStack();
        if (!held.is(ModItems.SCAN_CODEX.get()) || state.getValue(LecternBlock.HAS_BOOK)) {
            return;
        }

        // The vanilla helper owns lectern book insertion/state updates. We only expand the accepted book family.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (level.isClientSide) {
            return;
        }

        ScanCodexItem.ensureCodexId(held);
        LecternBlock.tryPlaceBook(event.getEntity(), level, pos, state, held);
    }
}

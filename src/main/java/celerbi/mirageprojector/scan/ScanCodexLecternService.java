package celerbi.mirageprojector.scan;

import celerbi.mirageprojector.compat.EasyMobFarmCompat;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.menu.ScanCodexMenu;
import celerbi.mirageprojector.network.OpenLecternScanCodexPayload;
import celerbi.mirageprojector.registry.ModItems;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server authority for a Scan Codex mounted in a vanilla lectern. */
public final class ScanCodexLecternService {
    private static final double MAX_USE_DISTANCE_SQR = 64.0D;

    private ScanCodexLecternService() {
    }

    public static Optional<ItemStack> codexAt(ServerPlayer player, BlockPos pos, UUID expectedCodexId) {
        if (player == null || pos == null || expectedCodexId == null || ScanCodexItem.isZero(expectedCodexId)) {
            return Optional.empty();
        }
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > MAX_USE_DISTANCE_SQR) {
            return Optional.empty();
        }
        if (!(player.level().getBlockState(pos).getBlock() instanceof LecternBlock)
                || !(player.level().getBlockEntity(pos) instanceof LecternBlockEntity lectern)) {
            return Optional.empty();
        }
        ItemStack codex = lectern.getBook();
        if (!codex.is(ModItems.SCAN_CODEX.get()) || !expectedCodexId.equals(ScanCodexItem.codexId(codex))) {
            return Optional.empty();
        }
        return Optional.of(codex);
    }

    public static void openMenu(ServerPlayer player, BlockPos pos, ItemStack codex) {
        if (player == null || pos == null || codex == null || !codex.is(ModItems.SCAN_CODEX.get())) {
            return;
        }
        UUID id = ScanCodexItem.ensureCodexId(codex);
        boolean easyMobFarm = EasyMobFarmCompat.isLoaded();
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new ScanCodexMenu(
                        containerId,
                        inventory,
                        true,
                        pos,
                        id,
                        easyMobFarm
                ),
                Component.translatable("gui.mirage_projector.scan_codex.title")
        );
        ((IPlayerExtension) player).openMenu(provider, buffer -> {
            buffer.writeBoolean(true);
            buffer.writeBlockPos(pos);
            buffer.writeUUID(id);
            buffer.writeBoolean(easyMobFarm);
        });
        sendSnapshot(player, pos, codex, false);
    }

    public static void sendSnapshot(ServerPlayer player, BlockPos pos, ItemStack codex, boolean openScreen) {
        if (player == null || pos == null || codex == null || !codex.is(ModItems.SCAN_CODEX.get())) {
            return;
        }
        UUID id = ScanCodexItem.ensureCodexId(codex);
        ScanCodexSavedData data = ScanCodexSavedData.get(player.getServer());
        UUID selected = ScanCodexItem.selectedScanId(codex)
                .filter(scanId -> data.contains(id, scanId))
                .orElse(new UUID(0L, 0L));
        if (ScanCodexItem.isZero(selected)) {
            ScanCodexItem.setSelectedScanId(codex, null);
        }
        CompoundTag selectedRoot = ScanCodexItem.isZero(selected)
                ? new CompoundTag()
                : data.copyScanRoot(id, selected).orElseGet(CompoundTag::new);
        PacketDistributor.sendToPlayer(player, new OpenLecternScanCodexPayload(
                pos,
                id,
                selected,
                openScreen,
                data.summaries(id),
                selectedRoot
        ));
    }

    public static boolean takeCodex(ServerPlayer player, BlockPos pos, UUID expectedCodexId) {
        Optional<ItemStack> mounted = codexAt(player, pos, expectedCodexId);
        if (mounted.isEmpty()
                || !(player.level().getBlockEntity(pos) instanceof LecternBlockEntity lectern)) {
            return false;
        }

        ItemStack returned = mounted.get().copy();
        var state = player.level().getBlockState(pos);
        lectern.clearContent();
        LecternBlock.resetBookState(player, player.level(), pos, state, false);
        if (!player.getInventory().add(returned)) {
            player.drop(returned, false);
        }
        return true;
    }
}

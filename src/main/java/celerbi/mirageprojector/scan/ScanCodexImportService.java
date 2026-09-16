package celerbi.mirageprojector.scan;

import celerbi.mirageprojector.compat.EasyMobFarmCompat;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.registry.ModItems;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative destructive import path for physical scan cards into a Scan Codex. */
public final class ScanCodexImportService {
    private ScanCodexImportService() {
    }

    public static boolean isMirageImportCard(ItemStack stack) {
        return stack != null
                && stack.is(ModItems.ENTITY_SCAN_CARD.get())
                && EntityScanData.hasScan(stack);
    }

    public static Result importCard(ServerPlayer player, ItemStack codex, ItemStack sourceCard) {
        if (player == null || codex == null || !ScanCodexItem.isCodex(codex)) {
            return Result.INVALID_CODEX;
        }
        if (sourceCard == null || sourceCard.isEmpty()) {
            return Result.NO_CARD;
        }

        Result result;
        if (sourceCard.is(ModItems.ENTITY_SCAN_CARD.get())) {
            result = importMirageCard(player, codex, sourceCard);
        } else if (EasyMobFarmCompat.isCaptureCard(sourceCard)) {
            result = fromEasyMobFarm(EasyMobFarmCompat.importCaptureCard(player, codex, sourceCard));
        } else {
            result = Result.INVALID_CARD;
        }

        // Import is deliberately destructive: the physical source is the price of adding its
        // snapshot to the persistent Codex library. Never consume on validation/import failure.
        if (result == Result.SUCCESS) {
            sourceCard.shrink(1);
        }
        return result;
    }

    private static Result importMirageCard(ServerPlayer player, ItemStack codex, ItemStack sourceCard) {
        Optional<CompoundTag> root = EntityScanData.copyRoot(sourceCard);
        if (root.isEmpty()) {
            return Result.EMPTY_CARD;
        }
        if (root.get().sizeInBytes() > EntityScanData.MAX_ENTITY_NBT_BYTES) {
            return Result.TOO_LARGE;
        }
        Optional<EntityScanData.View> parsed = EntityScanData.readRoot(root.get());
        if (parsed.isEmpty()) {
            return Result.INVALID_CARD;
        }

        UUID codexId = ScanCodexItem.ensureCodexId(codex);
        ScanCodexSavedData saved = ScanCodexSavedData.get(player.getServer());
        if (!saved.canAddType(codexId, parsed.get().entityType())) {
            return Result.TYPE_LIMIT;
        }
        UUID importedId = saved.addScanRoot(codexId, root.get());
        if (ScanCodexItem.isZero(importedId)) {
            return Result.FAILED;
        }
        ScanCodexItem.setSelectedScanId(codex, importedId);
        return Result.SUCCESS;
    }

    private static Result fromEasyMobFarm(EasyMobFarmCompat.ImportResult result) {
        return switch (result) {
            case SUCCESS -> Result.SUCCESS;
            case MOD_NOT_LOADED -> Result.MOD_NOT_LOADED;
            case INVALID_CARD -> Result.INVALID_CARD;
            case EMPTY_CARD -> Result.EMPTY_CARD;
            case TYPE_LIMIT -> Result.TYPE_LIMIT;
            case TOO_LARGE -> Result.TOO_LARGE;
            case FAILED -> Result.FAILED;
        };
    }

    public enum Result {
        SUCCESS,
        INVALID_CODEX,
        NO_CARD,
        INVALID_CARD,
        EMPTY_CARD,
        MOD_NOT_LOADED,
        TYPE_LIMIT,
        TOO_LARGE,
        FAILED
    }
}

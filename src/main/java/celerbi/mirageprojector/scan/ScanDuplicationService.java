package celerbi.mirageprojector.scan;

import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.registry.ModItems;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative copy path from one exact Codex capture into one blank Mirage Entity Scan Card. */
public final class ScanDuplicationService {
    private ScanDuplicationService() {
    }

    public static Result duplicateInto(ServerPlayer player, ItemStack codex, UUID scanId, ItemStack targetCard) {
        if (player == null || codex == null || !codex.is(ModItems.SCAN_CODEX.get())) {
            return Result.INVALID_CODEX;
        }
        if (targetCard == null || targetCard.isEmpty() || !targetCard.is(ModItems.ENTITY_SCAN_CARD.get())) {
            return Result.NO_CARD;
        }
        if (EntityScanData.hasScan(targetCard)) {
            return Result.CARD_FILLED;
        }

        UUID codexId = ScanCodexItem.codexId(codex);
        if (ScanCodexItem.isZero(codexId)) {
            return Result.INVALID_CODEX;
        }
        if (ScanCodexItem.isZero(scanId)) {
            return Result.NO_SELECTION;
        }

        Optional<CompoundTag> storedRoot = ScanCodexSavedData.get(player.getServer())
                .copyScanRoot(codexId, scanId);
        if (storedRoot.isEmpty() || EntityScanData.readRoot(storedRoot.get()).isEmpty()) {
            return Result.MISSING_SCAN;
        }

        if (!EntityScanData.writeRootToCard(targetCard, storedRoot.get())) {
            return Result.INVALID_SCAN;
        }
        return Result.SUCCESS;
    }

    public enum Result {
        SUCCESS,
        INVALID_CODEX,
        NO_SELECTION,
        MISSING_SCAN,
        NO_CARD,
        CARD_FILLED,
        INVALID_SCAN
    }
}

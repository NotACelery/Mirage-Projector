package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.menu.ScanCodexMenu;
import celerbi.mirageprojector.scan.ScanCodexLecternService;
import celerbi.mirageprojector.scan.ScanCodexSavedData;
import celerbi.mirageprojector.scan.ScanDuplicationService;
import celerbi.mirageprojector.scan.ScanCodexImportService;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative Scan Codex actions while the physical Codex is mounted in a vanilla lectern. */
public record LecternScanCodexActionPayload(
        BlockPos pos,
        UUID codexId,
        UUID scanId,
        Action action
) implements CustomPacketPayload {
    public static final Type<LecternScanCodexActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "lectern_scan_codex_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LecternScanCodexActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LecternScanCodexActionPayload decode(RegistryFriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            UUID codexId = buffer.readUUID();
            UUID scanId = buffer.readUUID();
            int raw = buffer.readVarInt();
            Action[] values = Action.values();
            Action action = values[Math.max(0, Math.min(raw, values.length - 1))];
            return new LecternScanCodexActionPayload(pos, codexId, scanId, action);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LecternScanCodexActionPayload payload) {
            buffer.writeBlockPos(payload.pos());
            buffer.writeUUID(payload.codexId());
            buffer.writeUUID(payload.scanId());
            buffer.writeVarInt(payload.action().ordinal());
        }
    };

    @Override
    public Type<LecternScanCodexActionPayload> type() {
        return TYPE;
    }

    public static void handle(LecternScanCodexActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack codex = ScanCodexLecternService.codexAt(player, payload.pos(), payload.codexId())
                    .orElse(ItemStack.EMPTY);
            if (codex.isEmpty()) {
                return;
            }

            if (payload.action() == Action.TAKE_CODEX) {
                player.closeContainer();
                ScanCodexLecternService.takeCodex(player, payload.pos(), payload.codexId());
                return;
            }

            ScanCodexMenu menu = ScanCodexMenu.current(player, payload.pos(), payload.codexId());
            ScanCodexSavedData data = ScanCodexSavedData.get(player.getServer());

            if (payload.action() == Action.RETURN_DUPLICATE_CARD) {
                if (menu != null) {
                    menu.returnDuplicateCard(player);
                }
                return;
            }
            if (payload.action() == Action.RETURN_IMPORT_CARD) {
                if (menu != null) {
                    menu.returnImportCard(player);
                }
                return;
            }

            if (payload.action() == Action.IMPORT_CARD) {
                if (menu == null) {
                    return;
                }
                ScanCodexImportService.Result result = ScanCodexImportService.importCard(
                        player,
                        codex,
                        menu.importCard()
                );
                menu.broadcastChanges();
                player.displayClientMessage(importMessage(result), true);
                if (result == ScanCodexImportService.Result.SUCCESS) {
                    ScanCodexLecternService.sendSnapshot(player, payload.pos(), codex, false);
                }
                return;
            }

            if (ScanCodexItem.isZero(payload.scanId()) || !data.contains(payload.codexId(), payload.scanId())) {
                return;
            }

            switch (payload.action()) {
                case SELECT -> {
                    ScanCodexItem.setSelectedScanId(codex, payload.scanId());
                    ScanCodexLecternService.sendSnapshot(player, payload.pos(), codex, false);
                }
                case TOGGLE_FAVORITE -> {
                    data.toggleFavorite(payload.codexId(), payload.scanId());
                    ScanCodexLecternService.sendSnapshot(player, payload.pos(), codex, false);
                }
                case DELETE -> {
                    data.delete(payload.codexId(), payload.scanId());
                    if (payload.scanId().equals(ScanCodexItem.selectedScanId(codex).orElse(null))) {
                        ScanCodexItem.setSelectedScanId(codex, null);
                    }
                    ScanCodexLecternService.sendSnapshot(player, payload.pos(), codex, false);
                }
                case DUPLICATE -> {
                    if (menu == null) {
                        return;
                    }
                    ScanCodexItem.setSelectedScanId(codex, payload.scanId());
                    ScanDuplicationService.Result result = ScanDuplicationService.duplicateInto(
                            player,
                            codex,
                            payload.scanId(),
                            menu.duplicateCard()
                    );
                    menu.broadcastChanges();
                    player.displayClientMessage(duplicateMessage(result), true);
                    ScanCodexLecternService.sendSnapshot(player, payload.pos(), codex, false);
                }
                case IMPORT_CARD, TAKE_CODEX, RETURN_DUPLICATE_CARD, RETURN_IMPORT_CARD -> {
                    // handled above
                }
            }
        });
    }

    private static Component duplicateMessage(ScanDuplicationService.Result result) {
        return switch (result) {
            case SUCCESS -> Component.translatable("message.mirage_projector.scan_codex.duplicate.success");
            case NO_SELECTION -> Component.translatable("message.mirage_projector.scan_codex.duplicate.no_selection");
            case MISSING_SCAN -> Component.translatable("message.mirage_projector.scan_codex.duplicate.missing_scan");
            case NO_CARD -> Component.translatable("message.mirage_projector.scan_codex.duplicate.no_card");
            case CARD_FILLED -> Component.translatable("message.mirage_projector.scan_codex.duplicate.card_filled");
            case INVALID_SCAN -> Component.translatable("message.mirage_projector.scan_codex.duplicate.invalid_scan");
            case INVALID_CODEX -> Component.translatable("message.mirage_projector.scan_codex.duplicate.invalid_codex");
        };
    }

    private static Component importMessage(ScanCodexImportService.Result result) {
        return switch (result) {
            case SUCCESS -> Component.translatable("message.mirage_projector.scan_codex.import.success_consumed");
            case INVALID_CODEX -> Component.translatable("message.mirage_projector.scan_codex.import.invalid_codex");
            case NO_CARD -> Component.translatable("message.mirage_projector.scan_codex.import.no_card");
            case MOD_NOT_LOADED -> Component.translatable("message.mirage_projector.scan_codex.import.mod_missing");
            case INVALID_CARD -> Component.translatable("message.mirage_projector.scan_codex.import.invalid_card");
            case EMPTY_CARD -> Component.translatable("message.mirage_projector.scan_codex.import.empty_card");
            case TYPE_LIMIT -> Component.translatable(
                    "message.mirage_projector.scan_codex.type_limit_short",
                    ScanCodexSavedData.MAX_SCANS_PER_ENTITY_TYPE
            );
            case TOO_LARGE -> Component.translatable("message.mirage_projector.scan_codex.import.too_large");
            case FAILED -> Component.translatable("message.mirage_projector.scan_codex.import.failed");
        };
    }

    public enum Action {
        SELECT,
        TOGGLE_FAVORITE,
        DELETE,
        DUPLICATE,
        IMPORT_CARD,
        RETURN_DUPLICATE_CARD,
        RETURN_IMPORT_CARD,
        TAKE_CODEX
    }
}

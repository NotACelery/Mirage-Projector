package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.scan.ScanCodexSavedData;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative mutations from the Codex browser. */
public record ScanCodexActionPayload(
        UUID codexId,
        UUID scanId,
        Action action
) implements CustomPacketPayload {
    public static final Type<ScanCodexActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "scan_codex_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanCodexActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ScanCodexActionPayload decode(RegistryFriendlyByteBuf buffer) {
            UUID codexId = buffer.readUUID();
            UUID scanId = buffer.readUUID();
            int raw = buffer.readVarInt();
            Action[] values = Action.values();
            Action action = values[Math.max(0, Math.min(raw, values.length - 1))];
            return new ScanCodexActionPayload(codexId, scanId, action);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ScanCodexActionPayload payload) {
            buffer.writeUUID(payload.codexId());
            buffer.writeUUID(payload.scanId());
            buffer.writeVarInt(payload.action().ordinal());
        }
    };

    @Override
    public Type<ScanCodexActionPayload> type() {
        return TYPE;
    }

    public static void handle(ScanCodexActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack codex = ScanCodexItem.findOwnedCodex(player, payload.codexId());
            if (codex.isEmpty()) {
                return;
            }
            ScanCodexSavedData data = ScanCodexSavedData.get(player.getServer());
            if (!data.contains(payload.codexId(), payload.scanId())) {
                return;
            }
            switch (payload.action()) {
                case SELECT -> ScanCodexItem.setSelectedScanId(codex, payload.scanId());
                case TOGGLE_FAVORITE -> data.toggleFavorite(payload.codexId(), payload.scanId());
            }
            ScanCodexItem.sendSnapshot(player, codex, false);
        });
    }

    public enum Action {
        SELECT,
        TOGGLE_FAVORITE
    }
}

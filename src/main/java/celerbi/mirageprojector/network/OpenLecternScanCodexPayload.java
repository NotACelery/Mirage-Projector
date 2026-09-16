package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientScanCodex;
import celerbi.mirageprojector.scan.ScanCodexEntrySummary;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Codex snapshot routed to an already-open vanilla-lectern Codex menu. */
public record OpenLecternScanCodexPayload(
        BlockPos pos,
        UUID codexId,
        UUID selectedScanId,
        boolean openScreen,
        List<ScanCodexEntrySummary> entries,
        CompoundTag selectedScanRoot
) implements CustomPacketPayload {
    public static final Type<OpenLecternScanCodexPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_lectern_scan_codex")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLecternScanCodexPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenLecternScanCodexPayload decode(RegistryFriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            UUID codexId = buffer.readUUID();
            UUID selected = buffer.readUUID();
            boolean open = buffer.readBoolean();
            int count = buffer.readVarInt();
            List<ScanCodexEntrySummary> entries = OpenScanCodexPayload.readEntries(buffer, count);
            CompoundTag selectedRoot = buffer.readNbt();
            return new OpenLecternScanCodexPayload(
                    pos,
                    codexId,
                    selected,
                    open,
                    entries,
                    selectedRoot == null ? new CompoundTag() : selectedRoot
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenLecternScanCodexPayload payload) {
            buffer.writeBlockPos(payload.pos());
            buffer.writeUUID(payload.codexId());
            buffer.writeUUID(payload.selectedScanId());
            buffer.writeBoolean(payload.openScreen());
            buffer.writeVarInt(payload.entries().size());
            OpenScanCodexPayload.writeEntries(buffer, payload.entries());
            buffer.writeNbt(payload.selectedScanRoot());
        }
    };

    public OpenLecternScanCodexPayload {
        entries = entries == null ? List.of() : List.copyOf(entries);
        selectedScanRoot = selectedScanRoot == null ? new CompoundTag() : selectedScanRoot.copy();
    }

    @Override
    public Type<OpenLecternScanCodexPayload> type() {
        return TYPE;
    }

    public OpenScanCodexPayload asCodexSnapshot() {
        return new OpenScanCodexPayload(codexId, selectedScanId, openScreen, entries, selectedScanRoot);
    }

    public static void handle(OpenLecternScanCodexPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientScanCodex.acceptLectern(payload));
    }
}

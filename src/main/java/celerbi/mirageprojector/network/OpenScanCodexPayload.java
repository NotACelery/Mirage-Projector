package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientScanCodex;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.scan.ScanCodexEntrySummary;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Codex metadata plus only the currently selected frozen scan, never the whole heavy library. */
public record OpenScanCodexPayload(
        UUID codexId,
        UUID selectedScanId,
        boolean openScreen,
        List<ScanCodexEntrySummary> entries,
        CompoundTag selectedScanRoot
) implements CustomPacketPayload {
    public static final Type<OpenScanCodexPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "open_scan_codex")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScanCodexPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenScanCodexPayload decode(RegistryFriendlyByteBuf buffer) {
            UUID codexId = buffer.readUUID();
            UUID selected = buffer.readUUID();
            boolean open = buffer.readBoolean();
            int count = buffer.readVarInt();
            List<ScanCodexEntrySummary> entries = readEntries(buffer, count);
            CompoundTag selectedRoot = buffer.readNbt();
            return new OpenScanCodexPayload(
                    codexId,
                    selected,
                    open,
                    entries,
                    selectedRoot == null ? new CompoundTag() : selectedRoot
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, OpenScanCodexPayload payload) {
            buffer.writeUUID(payload.codexId());
            buffer.writeUUID(payload.selectedScanId());
            buffer.writeBoolean(payload.openScreen());
            buffer.writeVarInt(payload.entries().size());
            writeEntries(buffer, payload.entries());
            buffer.writeNbt(payload.selectedScanRoot());
        }
    };

    public OpenScanCodexPayload {
        entries = entries == null ? List.of() : List.copyOf(entries);
        selectedScanRoot = selectedScanRoot == null ? new CompoundTag() : selectedScanRoot.copy();
    }

    @Override
    public Type<OpenScanCodexPayload> type() {
        return TYPE;
    }

    public static void handle(OpenScanCodexPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientScanCodex.accept(payload));
    }

    public static List<ScanCodexEntrySummary> readEntries(RegistryFriendlyByteBuf buffer, int count) {
        List<ScanCodexEntrySummary> entries = new ArrayList<>(Math.max(0, count));
        for (int i = 0; i < count; i++) {
            UUID scanId = buffer.readUUID();
            ResourceLocation entityType = ResourceLocation.parse(buffer.readUtf());
            EntityScanData.Kind kind = EntityScanData.Kind.fromSerialized(buffer.readUtf());
            String displayName = buffer.readUtf();
            String nameplate = buffer.readUtf();
            boolean playerSource = buffer.readBoolean();
            boolean customNamed = buffer.readBoolean();
            boolean favorite = buffer.readBoolean();
            int equipmentCount = buffer.readVarInt();
            entries.add(new ScanCodexEntrySummary(
                    scanId, entityType, kind, displayName, nameplate,
                    playerSource, customNamed, favorite, equipmentCount
            ));
        }
        return entries;
    }

    public static void writeEntries(RegistryFriendlyByteBuf buffer, List<ScanCodexEntrySummary> entries) {
        for (ScanCodexEntrySummary entry : entries) {
            buffer.writeUUID(entry.scanId());
            buffer.writeUtf(entry.entityType().toString());
            buffer.writeUtf(entry.kind().serializedName());
            buffer.writeUtf(entry.displayName());
            buffer.writeUtf(entry.nameplateText());
            buffer.writeBoolean(entry.playerSource());
            buffer.writeBoolean(entry.customNamed());
            buffer.writeBoolean(entry.favorite());
            buffer.writeVarInt(entry.equipmentCount());
        }
    }
}

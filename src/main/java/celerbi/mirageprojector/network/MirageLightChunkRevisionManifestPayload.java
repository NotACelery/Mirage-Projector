package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientMirageLightSync;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Small server -> client watchdog manifest for STATIC_WORLD Mirage light.
 *
 * dev.76g does not continuously resend section payloads. An energized Mature Cluster
 * periodically advertises the authoritative revision of the chunk columns in its watch
 * window. The client compares those revisions against its installed mirror and requests
 * only chunks that are missing or stale.
 */
public record MirageLightChunkRevisionManifestPayload(
        long packedSourcePos,
        List<Entry> entries
) implements CustomPacketPayload {
    private static final int MAX_ENTRIES = 81;

    public static final Type<MirageLightChunkRevisionManifestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "mirage_light_chunk_revision_manifest")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MirageLightChunkRevisionManifestPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MirageLightChunkRevisionManifestPayload decode(RegistryFriendlyByteBuf buffer) {
                    long sourcePos = buffer.readLong();
                    int count = buffer.readVarInt();
                    if (count < 0 || count > MAX_ENTRIES) {
                        throw new IllegalArgumentException("Invalid Mirage revision manifest size: " + count);
                    }
                    List<Entry> entries = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        long chunkPos = buffer.readLong();
                        long revision = buffer.readVarLong();
                        int sectionCount = buffer.readVarInt();
                        if (sectionCount < 0 || sectionCount > 64) {
                            throw new IllegalArgumentException("Invalid Mirage manifest section count: " + sectionCount);
                        }
                        entries.add(new Entry(chunkPos, revision, sectionCount));
                    }
                    return new MirageLightChunkRevisionManifestPayload(sourcePos, List.copyOf(entries));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, MirageLightChunkRevisionManifestPayload payload) {
                    buffer.writeLong(payload.packedSourcePos());
                    List<Entry> entries = payload.entries() == null ? List.of() : payload.entries();
                    if (entries.size() > MAX_ENTRIES) {
                        throw new IllegalArgumentException("Too many Mirage revision manifest entries: " + entries.size());
                    }
                    buffer.writeVarInt(entries.size());
                    for (Entry entry : entries) {
                        buffer.writeLong(entry.packedChunkPos());
                        buffer.writeVarLong(entry.revision());
                        buffer.writeVarInt(entry.sectionCount());
                    }
                }
            };

    public MirageLightChunkRevisionManifestPayload {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    @Override
    public Type<MirageLightChunkRevisionManifestPayload> type() {
        return TYPE;
    }

    public static void handle(MirageLightChunkRevisionManifestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientMirageLightSync.applyRevisionManifest(payload));
    }

    public record Entry(long packedChunkPos, long revision, int sectionCount) {
    }
}

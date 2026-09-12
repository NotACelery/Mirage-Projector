package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientMirageLightSync;
import celerbi.mirageprojector.light.engine.MirageLightSection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * dev.76f atomic server-authoritative Mirage light snapshot for one chunk column.
 *
 * A snapshot completely replaces the client's Mirage section state for the chunk in one
 * payload. This avoids the old CLEAR_CHUNK + N SET_SECTION packet sequence, which could be
 * interleaved with login/unwatch events and leave an otherwise-correct chunk permanently blank.
 *
 * revision is monotonic per server chunk while its Mirage aggregate changes. The client ignores
 * snapshots older than the newest revision it has already installed for that chunk.
 */
public record MirageLightChunkSnapshotPayload(
        long packedChunkPos,
        long revision,
        List<SectionData> sections
) implements CustomPacketPayload {
    public static final int PACKED_SECTION_BYTES = MirageLightSection.SIZE / 2;
    private static final int MAX_SECTIONS_PER_CHUNK = 64;

    public static final Type<MirageLightChunkSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "mirage_light_chunk_snapshot")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MirageLightChunkSnapshotPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MirageLightChunkSnapshotPayload decode(RegistryFriendlyByteBuf buffer) {
                    long chunkPos = buffer.readLong();
                    long revision = buffer.readLong();
                    int count = buffer.readVarInt();
                    if (count < 0 || count > MAX_SECTIONS_PER_CHUNK) {
                        throw new IllegalArgumentException("Invalid Mirage chunk snapshot section count: " + count);
                    }
                    List<SectionData> sections = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        int sectionY = buffer.readInt();
                        byte[] packed = new byte[PACKED_SECTION_BYTES];
                        buffer.readBytes(packed);
                        sections.add(new SectionData(sectionY, packed));
                    }
                    return new MirageLightChunkSnapshotPayload(chunkPos, revision, List.copyOf(sections));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, MirageLightChunkSnapshotPayload payload) {
                    buffer.writeLong(payload.packedChunkPos());
                    buffer.writeLong(payload.revision());
                    List<SectionData> sections = payload.sections() == null ? List.of() : payload.sections();
                    if (sections.size() > MAX_SECTIONS_PER_CHUNK) {
                        throw new IllegalArgumentException("Too many Mirage sections in chunk snapshot: " + sections.size());
                    }
                    buffer.writeVarInt(sections.size());
                    for (SectionData section : sections) {
                        buffer.writeInt(section.sectionY());
                        byte[] packed = section.packedLevels();
                        if (packed == null || packed.length != PACKED_SECTION_BYTES) {
                            throw new IllegalArgumentException("Mirage section payload must contain exactly "
                                    + PACKED_SECTION_BYTES + " packed bytes");
                        }
                        buffer.writeBytes(packed);
                    }
                }
            };

    public MirageLightChunkSnapshotPayload {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    public static SectionData section(int sectionY, byte[] levels) {
        if (levels == null || levels.length != MirageLightSection.SIZE) {
            throw new IllegalArgumentException("Mirage light section must contain exactly "
                    + MirageLightSection.SIZE + " levels");
        }
        byte[] packed = new byte[PACKED_SECTION_BYTES];
        for (int i = 0; i < packed.length; i++) {
            int low = Byte.toUnsignedInt(levels[i * 2]) & 0x0F;
            int high = Byte.toUnsignedInt(levels[i * 2 + 1]) & 0x0F;
            packed[i] = (byte) (low | (high << 4));
        }
        return new SectionData(sectionY, packed);
    }

    @Override
    public Type<MirageLightChunkSnapshotPayload> type() {
        return TYPE;
    }

    public static void handle(MirageLightChunkSnapshotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientMirageLightSync.applyChunkSnapshot(payload));
    }

    public record SectionData(int sectionY, byte[] packedLevels) {
        public SectionData {
            packedLevels = packedLevels == null ? new byte[0] : packedLevels.clone();
        }

        public byte[] unpackLevels() {
            if (packedLevels.length != PACKED_SECTION_BYTES) {
                return new byte[MirageLightSection.SIZE];
            }
            byte[] levels = new byte[MirageLightSection.SIZE];
            for (int i = 0; i < packedLevels.length; i++) {
                int packed = Byte.toUnsignedInt(packedLevels[i]);
                levels[i * 2] = (byte) (packed & 0x0F);
                levels[i * 2 + 1] = (byte) ((packed >>> 4) & 0x0F);
            }
            return levels;
        }
    }
}

package celerbi.mirageprojector.light.engine;

import net.minecraft.core.BlockPos;

import java.util.UUID;

/** Stable identity of one Mirage light source inside a Level. */
public record MirageLightSourceId(String kind, long key) {
    public MirageLightSourceId {
        kind = kind == null || kind.isBlank() ? "unknown" : kind;
    }

    public static MirageLightSourceId block(String kind, BlockPos pos) {
        return new MirageLightSourceId(kind, pos == null ? BlockPos.ZERO.asLong() : pos.asLong());
    }

    /** Stable per-entity identity suitable for client-side moving-light attachments. */
    public static MirageLightSourceId entity(String kind, UUID uuid) {
        if (uuid == null) {
            return new MirageLightSourceId(kind, 0L);
        }
        return new MirageLightSourceId(kind, uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
    }

    /** Explicit key factory for item/hand/device consumers that already own a stable key. */
    public static MirageLightSourceId keyed(String kind, long key) {
        return new MirageLightSourceId(kind, key);
    }
}

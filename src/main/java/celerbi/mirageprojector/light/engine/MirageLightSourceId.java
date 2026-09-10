package celerbi.mirageprojector.light.engine;

import net.minecraft.core.BlockPos;

/** Stable identity of one Mirage light source inside a Level. */
public record MirageLightSourceId(String kind, long key) {
    public MirageLightSourceId {
        kind = kind == null || kind.isBlank() ? "unknown" : kind;
    }

    public static MirageLightSourceId block(String kind, BlockPos pos) {
        return new MirageLightSourceId(kind, pos == null ? BlockPos.ZERO.asLong() : pos.asLong());
    }
}

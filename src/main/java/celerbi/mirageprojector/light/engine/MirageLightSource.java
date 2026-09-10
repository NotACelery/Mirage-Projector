package celerbi.mirageprojector.light.engine;

import net.minecraft.core.BlockPos;

/** Immutable input to the Mirage light solver. */
public record MirageLightSource(
        MirageLightSourceId id,
        BlockPos origin,
        MirageLightProfile profile,
        MirageLightRuntimeMode runtimeMode
) {
    public MirageLightSource {
        id = id == null ? MirageLightSourceId.block("unknown", BlockPos.ZERO) : id;
        origin = origin == null ? BlockPos.ZERO : origin.immutable();
        profile = profile == null ? MirageLightProfile.halfDecayExtended(0, 0xFFFFFF) : profile;
        runtimeMode = runtimeMode == null ? MirageLightRuntimeMode.STATIC_WORLD : runtimeMode;
    }

    public boolean active() {
        return profile.conceptualLight() > 0 && profile.maxRadius() > 0;
    }
}

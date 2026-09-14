package celerbi.mirageprojector.light.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * One client-visible moving Mirage light sample.
 *
 * <p>Consumers (lanterns, moving projectors, entity-attached emitters) submit the latest
 * transform repeatedly. The client runtime decides when the voxel field actually needs a
 * rebuild, so callers do not have to solve or dirty world sections every render frame.</p>
 */
public record MirageDynamicLightSnapshot(
        MirageLightSourceId id,
        Vec3 position,
        MirageLightProfile profile,
        int refreshIntervalTicks,
        double cullDistanceBlocks,
        int staleAfterTicks
) {
    public static final int DEFAULT_REFRESH_TICKS = 2;
    public static final int DEFAULT_STALE_TICKS = 4;
    public static final double DEFAULT_CULL_DISTANCE = 96.0D;

    public MirageDynamicLightSnapshot {
        id = id == null ? MirageLightSourceId.keyed("dynamic_unknown", 0L) : id;
        position = position == null ? Vec3.ZERO : position;
        profile = profile == null ? MirageLightProfile.halfDecayExtended(0, 0xFFFFFF) : profile;
        refreshIntervalTicks = Mth.clamp(refreshIntervalTicks, 1, 40);
        cullDistanceBlocks = Mth.clamp(cullDistanceBlocks, 8.0D, 256.0D);
        staleAfterTicks = Mth.clamp(staleAfterTicks, refreshIntervalTicks + 1, 100);
    }

    public static MirageDynamicLightSnapshot of(
            MirageLightSourceId id,
            Vec3 position,
            MirageLightProfile profile
    ) {
        return new MirageDynamicLightSnapshot(
                id,
                position,
                profile,
                DEFAULT_REFRESH_TICKS,
                DEFAULT_CULL_DISTANCE,
                DEFAULT_STALE_TICKS
        );
    }

    public MirageLightSource asVoxelSource() {
        return new MirageLightSource(
                id,
                BlockPos.containing(position),
                profile,
                MirageLightRuntimeMode.DYNAMIC_VISUAL
        );
    }

    public boolean active() {
        return profile.conceptualLight() > 0 && profile.maxRadius() > 0;
    }
}

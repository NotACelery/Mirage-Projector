package celerbi.mirageprojector.light;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record LightProfile(
        int baseLight,
        int maxRadius,
        LightDecayMode decayMode,
        Vec3 direction,
        float coneAngleDegrees,
        int refreshIntervalTicks,
        int rotationPeriodTicks
) {
    public LightProfile {
        baseLight = Mth.clamp(baseLight, 0, 15);
        maxRadius = Math.max(0, maxRadius);
        decayMode = decayMode == null ? LightDecayMode.VANILLA : decayMode;
        direction = direction == null ? Vec3.ZERO : direction;
        coneAngleDegrees = Mth.clamp(coneAngleDegrees, 1.0F, 360.0F);
        refreshIntervalTicks = Math.max(1, refreshIntervalTicks);
        rotationPeriodTicks = Math.max(0, rotationPeriodTicks);
    }

    public static LightProfile vanilla(int baseLight) {
        return new LightProfile(baseLight, baseLight, LightDecayMode.VANILLA, Vec3.ZERO, 360.0F, 20, 0);
    }

    public static LightProfile extended(int baseLight, int maxRadius, int refreshIntervalTicks) {
        return new LightProfile(
                baseLight,
                maxRadius,
                LightDecayMode.EXTEND,
                Vec3.ZERO,
                360.0F,
                refreshIntervalTicks,
                0
        );
    }

    public static LightProfile concentratedPlaceholder(int baseLight, int maxRadius, int refreshIntervalTicks) {
        return new LightProfile(
                baseLight,
                maxRadius,
                LightDecayMode.CONCENTRATE,
                Vec3.ZERO,
                360.0F,
                refreshIntervalTicks,
                0
        );
    }

    public static LightProfile directionalPlaceholder(
            int baseLight,
            int maxRadius,
            Vec3 direction,
            float coneAngleDegrees,
            int refreshIntervalTicks
    ) {
        return new LightProfile(
                baseLight,
                maxRadius,
                LightDecayMode.DIRECTIONAL_SPOT,
                direction,
                coneAngleDegrees,
                refreshIntervalTicks,
                0
        );
    }

    public static LightProfile rotatingDirectionalPlaceholder(
            int baseLight,
            int maxRadius,
            Vec3 direction,
            float coneAngleDegrees,
            int refreshIntervalTicks,
            int rotationPeriodTicks
    ) {
        return new LightProfile(
                baseLight,
                maxRadius,
                LightDecayMode.ROTATING_DIRECTIONAL_SPOT,
                direction,
                coneAngleDegrees,
                refreshIntervalTicks,
                rotationPeriodTicks
        );
    }

    public boolean runtimeImplemented() {
        return decayMode.runtimeImplemented();
    }

    public Vec3 normalizedDirection() {
        return direction.lengthSqr() <= 1.0E-8D ? Vec3.ZERO : direction.normalize();
    }
}

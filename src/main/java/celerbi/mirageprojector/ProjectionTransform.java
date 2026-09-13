package celerbi.mirageprojector;

import net.minecraft.util.Mth;

/**
 * Source-agnostic projection transform.
 *
 * <p>1.0 still exposes the familiar scale/lift/spin/float controls, but authoritative
 * state already carries a normalized quaternion orientation so 1.2 free manipulation
 * does not need a save-format rewrite. Current 1.0 renderers intentionally leave the
 * quaternion at identity.</p>
 */
public record ProjectionTransform(
        int scalePixels,
        int liftPixels,
        boolean rotationEnabled,
        int rotationPeriodTicks,
        boolean clockwise,
        float rotationOffsetDegrees,
        float orientationX,
        float orientationY,
        float orientationZ,
        float orientationW,
        boolean floatingEnabled,
        ProjectionSettings.FloatMode floatMode,
        int floatAmplitudePixels,
        int floatCycleTicks,
        int floatIntervalDegrees
) {
    public static final Orientation IDENTITY_ORIENTATION = new Orientation(0.0F, 0.0F, 0.0F, 1.0F);

    public static ProjectionTransform fromSettings(ProjectionSettings settings) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        return new ProjectionTransform(
                safe.scalePixels(),
                safe.liftPixels(),
                safe.rotationEnabled(),
                safe.rotationPeriodTicks(),
                safe.clockwise(),
                safe.rotationOffsetDegrees(),
                safe.orientationX(),
                safe.orientationY(),
                safe.orientationZ(),
                safe.orientationW(),
                safe.floatingEnabled(),
                safe.floatMode(),
                safe.floatAmplitudePixels(),
                safe.floatCycleTicks(),
                safe.floatIntervalDegrees()
        );
    }

    public ProjectionTransform sanitized() {
        Orientation orientation = normalizeOrientation(orientationX, orientationY, orientationZ, orientationW);
        return new ProjectionTransform(
                Mth.clamp(scalePixels, ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, ProjectionSettings.DEBUG_MAX_SCALE_PIXELS),
                Mth.clamp(liftPixels, 0, ProjectionSettings.DEBUG_MAX_LIFT_PIXELS),
                rotationEnabled,
                Mth.clamp(rotationPeriodTicks, 5, 20 * 60),
                clockwise,
                Mth.wrapDegrees(rotationOffsetDegrees),
                orientation.x(), orientation.y(), orientation.z(), orientation.w(),
                floatingEnabled,
                floatMode == null ? ProjectionSettings.FloatMode.TIME : floatMode,
                Mth.clamp(floatAmplitudePixels, 0, ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS),
                Mth.clamp(floatCycleTicks, 5, 20 * 60),
                Mth.clamp(floatIntervalDegrees, 1, 360)
        );
    }

    public Orientation orientation() {
        return new Orientation(orientationX, orientationY, orientationZ, orientationW);
    }

    public static Orientation normalizeOrientation(float x, float y, float z, float w) {
        double lengthSquared = (double) x * x + (double) y * y + (double) z * z + (double) w * w;
        if (!Double.isFinite(lengthSquared) || lengthSquared < 1.0E-12D) {
            return IDENTITY_ORIENTATION;
        }
        double inverseLength = 1.0D / Math.sqrt(lengthSquared);
        return new Orientation(
                (float) (x * inverseLength),
                (float) (y * inverseLength),
                (float) (z * inverseLength),
                (float) (w * inverseLength)
        );
    }

    public record Orientation(float x, float y, float z, float w) {
    }
}

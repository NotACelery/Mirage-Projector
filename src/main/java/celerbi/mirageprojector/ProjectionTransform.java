package celerbi.mirageprojector;

import net.minecraft.util.Mth;

/**
 * Source-agnostic projection transform.
 *
 * <p>Lift is the single vertical placement axis for fixed projectors. The stored distance value is
 * reserved for Mirage Prism radial face spacing; independent horizontal/vertical translation is intentionally disabled.
 * Orientation is persisted as a normalized quaternion so Tilt can later grow into full free
 * orientation without another save-format rewrite.</p>
 */
public record ProjectionTransform(
        int scalePixels,
        int liftPixels,
        int distanceOffsetPixels,
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
                safe.distanceOffsetPixels(),
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
                Mth.clamp(distanceOffsetPixels, 0, PrismProjectionSpacing.MAX_DISTANCE_PIXELS),
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

    public float tiltDegrees() {
        return tiltDegrees(orientationX, orientationY, orientationZ, orientationW);
    }

    public static Orientation orientationFromTiltDegrees(float tiltDegrees) {
        float clamped = Mth.clamp(tiltDegrees, -ProjectionSettings.MAX_TILT_DEGREES, ProjectionSettings.MAX_TILT_DEGREES);
        double halfRadians = Math.toRadians(clamped) * 0.5D;
        return normalizeOrientation((float) Math.sin(halfRadians), 0.0F, 0.0F, (float) Math.cos(halfRadians));
    }

    public static float tiltDegrees(float x, float y, float z, float w) {
        Orientation q = normalizeOrientation(x, y, z, w);
        double numerator = 2.0D * ((double) q.w() * q.x() + (double) q.y() * q.z());
        double denominator = 1.0D - 2.0D * ((double) q.x() * q.x() + (double) q.y() * q.y());
        float degrees = (float) Math.toDegrees(Math.atan2(numerator, denominator));
        return Mth.clamp(degrees, -ProjectionSettings.MAX_TILT_DEGREES, ProjectionSettings.MAX_TILT_DEGREES);
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

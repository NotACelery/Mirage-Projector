package celerbi.mirageprojector;

import net.minecraft.util.Mth;

/**
 * Shared Projection Power calculator.
 *
 * <p>Settings are deliberately NOT destroyed when a core becomes insufficient.
 * Instead the projector becomes inactive until a compatible core/settings combination
 * is restored. This is important for swapping cores without losing a carefully tuned
 * Mirage configuration.</p>
 */
public final class ProjectionPower {
    /** Compact production envelope: reference Plane is 10x10 pixels. */
    public static final int COMPACT_MAX_SCALE_PIXELS = 10;
    /** Compact chassis can lift a Mirage up to two blocks before a larger chassis is expected. */
    public static final int COMPACT_MAX_LIFT_PIXELS = 32;
    /** Compact physical mechanism is not intended for very wide bobbing motion. */
    public static final int COMPACT_MAX_FLOAT_PIXELS = 4;

    private ProjectionPower() {
    }

    public static Status evaluate(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            boolean hasProjectedItem
    ) {
        ProjectionSettings s = settings.sanitized();
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;

        int used = calculateUsedPower(s, hasProjectedItem);
        if (!safeCore.present()) {
            return new Status(false, used, 0, Failure.NO_CORE);
        }

        if (s.scalePixels() > safeCore.maxScalePixels()) {
            return new Status(false, used, safeCore.power(), Failure.CORE_SCALE_LIMIT);
        }
        if (s.liftPixels() > safeCore.maxLiftPixels()) {
            return new Status(false, used, safeCore.power(), Failure.CORE_LIFT_LIMIT);
        }
        if (s.floatAmplitudePixels() > safeCore.maxFloatPixels()) {
            return new Status(false, used, safeCore.power(), Failure.CORE_FLOAT_LIMIT);
        }

        // Bobbing moves downward from the configured base position. It may never travel
        // through the physical top of the pedestal, regardless of debug/chassis overrides.
        if (s.floatingEnabled() && s.floatAmplitudePixels() > s.liftPixels()) {
            return new Status(false, used, safeCore.power(), Failure.PHYSICAL_FLOAT_LIMIT);
        }

        if (!s.debugChassisOverride()) {
            if (s.scalePixels() > COMPACT_MAX_SCALE_PIXELS) {
                return new Status(false, used, safeCore.power(), Failure.CHASSIS_SCALE_LIMIT);
            }
            if (s.liftPixels() > COMPACT_MAX_LIFT_PIXELS) {
                return new Status(false, used, safeCore.power(), Failure.CHASSIS_LIFT_LIMIT);
            }
            if (s.floatAmplitudePixels() > COMPACT_MAX_FLOAT_PIXELS) {
                return new Status(false, used, safeCore.power(), Failure.CHASSIS_FLOAT_LIMIT);
            }
        }

        if (used > safeCore.power()) {
            return new Status(false, used, safeCore.power(), Failure.POWER_EXCEEDED);
        }
        return new Status(true, used, safeCore.power(), Failure.NONE);
    }

    public static int calculateUsedPower(ProjectionSettings s, boolean hasProjectedItem) {
        int power = 1; // base field stabilization

        if (s.sourceMode() == ProjectionSettings.SourceMode.ITEM && hasProjectedItem) {
            int side = Math.max(1, s.scalePixels());
            power += Math.max(1, Mth.ceil((side * side) / 256.0D));
            power += 2; // 3D/item-renderer surcharge
        } else if (s.sourceMode() == ProjectionSettings.SourceMode.IMAGE && s.hasImage()) {
            int width = Math.max(1, s.scalePixels());
            int height;
            if (s.imageWidth() >= s.imageHeight()) {
                height = Math.max(1, Math.round(width * s.imageHeight() / (float) s.imageWidth()));
            } else {
                height = width;
                width = Math.max(1, Math.round(height * s.imageWidth() / (float) s.imageHeight()));
            }
            power += Math.max(1, Mth.ceil((width * height) / 256.0D));
            if (s.backFaceMode() == ProjectionSettings.BackFaceMode.INDEPENDENT && s.hasBackImage()) {
                power += 1;
            }
        } else {
            // Empty-state book is still a tiny active projection.
            power += 1;
        }

        if (s.liftPixels() > 0) {
            power += Mth.ceil(s.liftPixels() / 16.0D);
        }
        if (s.rotationEnabled()) {
            power += 1;
        }
        if (s.floatingEnabled() && s.floatAmplitudePixels() > 0) {
            power += Math.max(1, Mth.ceil(s.floatAmplitudePixels() / 2.0D));
            if (s.floatMode() == ProjectionSettings.FloatMode.ROTATION_SYNCED) {
                power += 1;
            }
        }
        return power;
    }

    public enum Failure {
        NONE("Ready"),
        NO_CORE("No Projection Core installed"),
        CORE_SCALE_LIMIT("Scale exceeds this Core's limit"),
        CORE_LIFT_LIMIT("Projection Lift exceeds this Core's limit"),
        CORE_FLOAT_LIMIT("Float amplitude exceeds this Core's limit"),
        PHYSICAL_FLOAT_LIMIT("Float amplitude would intersect the pedestal"),
        CHASSIS_SCALE_LIMIT("Scale exceeds the Compact chassis envelope"),
        CHASSIS_LIFT_LIMIT("Lift exceeds the Compact chassis envelope"),
        CHASSIS_FLOAT_LIMIT("Float amplitude exceeds the Compact chassis envelope"),
        POWER_EXCEEDED("Projection Power budget exceeded");

        private final String message;

        Failure(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }

    public record Status(boolean active, int usedPower, int availablePower, Failure failure) {
        public float fillRatio() {
            if (availablePower <= 0) {
                return 0.0F;
            }
            return Mth.clamp(usedPower / (float) availablePower, 0.0F, 1.0F);
        }

        public String summary() {
            if (active) {
                return "Power: " + usedPower + " / " + availablePower;
            }
            return failure.message() + " · " + usedPower + " / " + availablePower;
        }
    }
}

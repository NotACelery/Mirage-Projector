package celerbi.mirageprojector.light.device;

import celerbi.mirageprojector.light.engine.MirageDynamicLightProfiles;
import celerbi.mirageprojector.light.engine.MirageLightProfile;
import java.util.Locale;
import net.minecraft.world.phys.Vec3;

/**
 * Shared operating modes for the 1.1 portable/placed Mirage illumination family.
 *
 * <p>The numeric values below are deliberately centralized QA balance values. The mode
 * contract and cycle order are stable; range, cone angle and drain can be tuned without
 * changing the device or light-engine architecture.</p>
 */
public enum PortableLightMode {
    // Sixteen source-to-end samples: 15x4, 14x4, 13x4, 12x4.
    FOCUS(15, 4, 15, 2, 22.0F, 4),
    // Flood trades peak brightness for coverage.  It must never reach Focus' level-15
    // core, otherwise it is simply a wider Focus beam with no meaningful trade-off.
    // Ten samples: 13,13 / 12,12 / 11,11 / 10,10 / 9,9.
    FLOOD(13, 2, 9, 1, 72.0F, 2),
    // Twenty samples from the source: 15,15 through 6,6.
    AMBIENT(15, 2, 19, 0, 360.0F, 8),
    OFF(0, 1, 0, 0, 360.0F, 0);

    private static final int RGB = 0xCBB7FF;

    private final int conceptualLight;
    private final int substepsPerLightLevel;
    private final int maxRadius;
    private final int detourExtraCostUnits;
    private final float coneAngleDegrees;
    private final int chargePerSecond;

    PortableLightMode(
            int conceptualLight,
            int substepsPerLightLevel,
            int maxRadius,
            int detourExtraCostUnits,
            float coneAngleDegrees,
            int chargePerSecond
    ) {
        this.conceptualLight = conceptualLight;
        this.substepsPerLightLevel = substepsPerLightLevel;
        this.maxRadius = maxRadius;
        this.detourExtraCostUnits = detourExtraCostUnits;
        this.coneAngleDegrees = coneAngleDegrees;
        this.chargePerSecond = chargePerSecond;
    }

    public PortableLightMode next() {
        return switch (this) {
            case FOCUS -> FLOOD;
            case FLOOD -> AMBIENT;
            case AMBIENT -> OFF;
            case OFF -> FOCUS;
        };
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String hudTranslationKey() {
        return "hud.mirage_projector.light_projector.mode." + serializedName();
    }

    public String displayTranslationKey() {
        return "mode.mirage_projector.portable_light." + serializedName();
    }

    public int chargePerSecond() {
        return chargePerSecond;
    }

    public boolean emitsLight() {
        return conceptualLight > 0 && maxRadius > 0;
    }

    public MirageLightProfile profile(Vec3 direction) {
        if (!emitsLight()) {
            return MirageDynamicLightProfiles.ambient(0, 1, 0, 0, RGB);
        }
        if (this == AMBIENT) {
            return MirageDynamicLightProfiles.ambientExactPlateaus(
                    conceptualLight,
                    substepsPerLightLevel,
                    maxRadius,
                    detourExtraCostUnits,
                    RGB,
                    conceptualLight * substepsPerLightLevel
            );
        }
        Vec3 safeDirection = direction == null || direction.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : direction.normalize();
        return MirageDynamicLightProfiles.directionalConeExactPlateaus(
                conceptualLight,
                substepsPerLightLevel,
                maxRadius,
                detourExtraCostUnits,
                safeDirection,
                coneAngleDegrees,
                RGB,
                conceptualLight * substepsPerLightLevel
        );
    }

    /**
     * Fixed Mirage Light Projectors use the same energy cost and mode identity as a Flashlight,
     * but their optic/chassis gives every operating mode 50% more reach. The additional reach
     * naturally continues into lower levels instead of changing the handheld mode's core output.
     */
    public MirageLightProfile lightProjectorProfile(Vec3 direction) {
        int projectorRadius = Math.round((maxRadius + 1) * 1.5F) - 1;
        if (this == AMBIENT) {
            return MirageDynamicLightProfiles.ambientExactPlateaus(
                    conceptualLight,
                    substepsPerLightLevel,
                    projectorRadius,
                    detourExtraCostUnits,
                    RGB,
                    conceptualLight * substepsPerLightLevel
            );
        }
        Vec3 safeDirection = direction == null || direction.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : direction.normalize();
        return MirageDynamicLightProfiles.directionalConeExactPlateaus(
                conceptualLight,
                substepsPerLightLevel,
                projectorRadius,
                detourExtraCostUnits,
                safeDirection,
                coneAngleDegrees,
                RGB,
                conceptualLight * substepsPerLightLevel
        );
    }

    public static PortableLightMode byName(String name) {
        if (name != null) {
            for (PortableLightMode value : values()) {
                if (value.serializedName().equalsIgnoreCase(name)) {
                    return value;
                }
            }
        }
        return FOCUS;
    }
}

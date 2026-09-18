package celerbi.mirageprojector.light.engine;

import celerbi.mirageprojector.light.LightDecayMode;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Profile factories for portable/moving Mirage emitters.
 *
 * <p>These factories intentionally accept the balance values from the caller. Focus/Flood/
 * Ambient ranges and battery draw belong to the device layer, not to the lighting engine.</p>
 */
public final class MirageDynamicLightProfiles {
    private MirageDynamicLightProfiles() {
    }

    public static MirageLightProfile ambient(
            int conceptualLight,
            int substepsPerLightLevel,
            int maxRadius,
            int detourExtraCostUnits,
            int rgb
    ) {
        return ambientExactPlateaus(
                conceptualLight,
                substepsPerLightLevel,
                maxRadius,
                detourExtraCostUnits,
                rgb,
                -1
        );
    }

    /**
     * Creates an omnidirectional profile whose source can start at an exact fixed-point energy.
     * This is used by portable lights where, for example, 15,15 / 14,14 must be exact rather
     * than receiving the regular solver's protective extra substep.
     */
    public static MirageLightProfile ambientExactPlateaus(
            int conceptualLight,
            int substepsPerLightLevel,
            int maxRadius,
            int detourExtraCostUnits,
            int rgb,
            int initialEnergyUnits
    ) {
        int safeSubsteps = Mth.clamp(substepsPerLightLevel, 1, 8);
        return new MirageLightProfile(
                conceptualLight,
                safeSubsteps,
                1,
                detourExtraCostUnits,
                maxRadius,
                LightDecayMode.EXTEND,
                MirageLightShape.OMNIDIRECTIONAL,
                Vec3.ZERO,
                360.0F,
                rgb,
                initialEnergyUnits
        );
    }

    public static MirageLightProfile directionalCone(
            int conceptualLight,
            int substepsPerLightLevel,
            int maxRadius,
            int detourExtraCostUnits,
            Vec3 direction,
            float coneAngleDegrees,
            int rgb
    ) {
        return directionalConeExactPlateaus(
                conceptualLight,
                substepsPerLightLevel,
                maxRadius,
                detourExtraCostUnits,
                direction,
                coneAngleDegrees,
                rgb,
                -1
        );
    }

    /** See {@link #ambientExactPlateaus(int, int, int, int, int, int)}. */
    public static MirageLightProfile directionalConeExactPlateaus(
            int conceptualLight,
            int substepsPerLightLevel,
            int maxRadius,
            int detourExtraCostUnits,
            Vec3 direction,
            float coneAngleDegrees,
            int rgb,
            int initialEnergyUnits
    ) {
        int safeSubsteps = Mth.clamp(substepsPerLightLevel, 1, 8);
        return new MirageLightProfile(
                conceptualLight,
                safeSubsteps,
                1,
                detourExtraCostUnits,
                maxRadius,
                LightDecayMode.EXTEND,
                MirageLightShape.DIRECTIONAL_CONE,
                direction,
                coneAngleDegrees,
                rgb,
                initialEnergyUnits
        );
    }
}

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
                rgb
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
                rgb
        );
    }
}

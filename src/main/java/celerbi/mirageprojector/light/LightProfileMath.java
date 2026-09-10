package celerbi.mirageprojector.light;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class LightProfileMath {
    private LightProfileMath() {
    }

    public static int concentratedLevel(LightProfile profile, double distance) {
        if (profile == null || profile.maxRadius() <= 0 || distance >= profile.maxRadius()) {
            return 0;
        }
        if (distance <= 0.0D) {
            return profile.baseLight();
        }
        double remaining = 1.0D - distance / profile.maxRadius();
        return Mth.clamp((int) Math.ceil(profile.baseLight() * remaining), 0, 15);
    }

    public static boolean insideDirectionalCone(LightProfile profile, Vec3 offset) {
        if (profile == null || offset == null || offset.lengthSqr() <= 1.0E-8D) {
            return false;
        }
        Vec3 forward = profile.normalizedDirection();
        if (forward.lengthSqr() <= 1.0E-8D || offset.length() > profile.maxRadius()) {
            return false;
        }
        double halfAngle = Math.toRadians(profile.coneAngleDegrees() * 0.5D);
        double threshold = Math.cos(halfAngle);
        return forward.dot(offset.normalize()) >= threshold;
    }
}

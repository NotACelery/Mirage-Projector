package celerbi.mirageprojector.light.engine;

import celerbi.mirageprojector.light.LightDecayMode;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Solver-facing light profile.
 *
 * Mirage uses fixed-point energy instead of vanilla's integer 0..15 while it
 * propagates. substepsPerLightLevel=2 means one air edge costs half of one
 * visible Minecraft light level, producing 15,15 / 14,14 / ... / 1,1 without
 * planting secondary light-emitting blocks.
 */
public record MirageLightProfile(
        int conceptualLight,
        int substepsPerLightLevel,
        int airStepCostUnits,
        int detourExtraCostUnits,
        int maxRadius,
        LightDecayMode decayMode,
        MirageLightShape shape,
        Vec3 direction,
        float coneAngleDegrees,
        int rgb
) {
    public static final int MAX_SAFE_RADIUS = 64;

    public MirageLightProfile {
        conceptualLight = Mth.clamp(conceptualLight, 0, 31);
        substepsPerLightLevel = Mth.clamp(substepsPerLightLevel, 1, 8);
        airStepCostUnits = Mth.clamp(airStepCostUnits, 1, substepsPerLightLevel * 8);
        detourExtraCostUnits = Mth.clamp(detourExtraCostUnits, 0, substepsPerLightLevel * 8);
        maxRadius = Mth.clamp(maxRadius, 0, MAX_SAFE_RADIUS);
        decayMode = decayMode == null ? LightDecayMode.VANILLA : decayMode;
        shape = shape == null ? MirageLightShape.OMNIDIRECTIONAL : shape;
        direction = direction == null ? Vec3.ZERO : direction;
        coneAngleDegrees = Mth.clamp(coneAngleDegrees, 1.0F, 360.0F);
        rgb &= 0xFFFFFF;
    }

    public static MirageLightProfile vanilla(int lightLevel, int rgb) {
        int safe = Mth.clamp(lightLevel, 0, 15);
        return new MirageLightProfile(
                safe,
                1,
                1,
                0,
                safe,
                LightDecayMode.VANILLA,
                MirageLightShape.OMNIDIRECTIONAL,
                Vec3.ZERO,
                360.0F,
                rgb
        );
    }

    public static MirageLightProfile extended(
            int conceptualLight,
            int substepsPerLightLevel,
            int rgb
    ) {
        int safeConceptual = Mth.clamp(conceptualLight, 0, 31);
        int safeSubsteps = Mth.clamp(substepsPerLightLevel, 1, 8);
        return new MirageLightProfile(
                safeConceptual,
                safeSubsteps,
                1,
                Math.max(0, safeSubsteps - 1),
                Math.min(MAX_SAFE_RADIUS, safeConceptual * safeSubsteps),
                LightDecayMode.EXTEND,
                MirageLightShape.OMNIDIRECTIONAL,
                Vec3.ZERO,
                360.0F,
                rgb
        );
    }

    public static MirageLightProfile halfDecayExtended(int conceptualLight, int rgb) {
        return extended(conceptualLight, 2, rgb);
    }

    /** Energy at the source before the first propagation edge is crossed. */
    public int initialEnergyUnits() {
        if (conceptualLight <= 0) {
            return 0;
        }
        return conceptualLight * substepsPerLightLevel + (substepsPerLightLevel - 1);
    }

    /** Convert fixed-point solver energy to the vanilla-compatible 0..15 result. */
    public int visibleLevelFromEnergy(int energyUnits) {
        if (energyUnits <= 0) {
            return 0;
        }
        int visible = (energyUnits + substepsPerLightLevel - 1) / substepsPerLightLevel;
        return Mth.clamp(visible, 0, 15);
    }

    /**
     * Additional fixed-point cost assigned to each path step that exists only because
     * geometry forced a detour beyond the source-to-voxel Manhattan minimum.
     *
     * A cardinal step that moves back toward the source reduces Manhattan distance by
     * one while increasing travelled path length by one, so it accounts for two detour
     * steps at once. The solver therefore applies {@code 2 * detourExtraCostUnits} on
     * that backtracking edge. For half-decay EXTEND (substeps=2, air cost=1), the
     * default extra cost is 1: open travel costs half a visible level per block, while
     * obstacle-only extra travel costs one full visible level per block overall.
     */
    public int detourBacktrackPenaltyUnits() {
        return detourExtraCostUnits * 2;
    }

    public Vec3 normalizedDirection() {
        return direction.lengthSqr() <= 1.0E-8D ? Vec3.ZERO : direction.normalize();
    }

    public boolean runtimeImplemented() {
        return shape.runtimeImplemented()
                && (decayMode == LightDecayMode.VANILLA || decayMode == LightDecayMode.EXTEND);
    }
}

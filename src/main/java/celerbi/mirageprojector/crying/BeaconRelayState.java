package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.CoreBoosterMaterial;
import net.minecraft.util.Mth;

public record BeaconRelayState(
        int effectiveBoosters,
        int glassCount,
        int quartzCount,
        int amethystCount,
        int diamondCount,
        boolean reversed,
        float widthScale,
        float innerRadiusScale,
        float outerRadiusScale,
        float brightnessScale,
        float rotationSpeedScale
) {
    public static final int MAX_EFFECTIVE_BOOSTERS = 4;
    public static final BeaconRelayState BASE = new BeaconRelayState(
            0,
            0,
            0,
            0,
            0,
            false,
            1.0F,
            1.0F,
            1.0F,
            1.0F,
            1.0F
    );

    public BeaconRelayState apply(CoreBoosterMaterial material) {
        if (material == null || !material.present() || effectiveBoosters >= MAX_EFFECTIVE_BOOSTERS) {
            return this;
        }

        int nextGlass = glassCount + (material == CoreBoosterMaterial.GLASS ? 1 : 0);
        int nextQuartz = quartzCount + (material == CoreBoosterMaterial.QUARTZ ? 1 : 0);
        int nextAmethyst = amethystCount + (material == CoreBoosterMaterial.AMETHYST ? 1 : 0);
        int nextDiamond = diamondCount + (material == CoreBoosterMaterial.DIAMOND ? 1 : 0);
        boolean nextReversed = reversed || material == CoreBoosterMaterial.NETHERITE;

        float addedWidth = material == CoreBoosterMaterial.GLASS ? 0.35F : 0.25F;
        float nextWidth = Math.min(2.0F, widthScale + addedWidth);
        float nextOuter = Math.min(2.20F, nextWidth * (1.0F + nextGlass * 0.06F));
        float nextInner = Math.max(0.60F, nextWidth * (1.0F - nextDiamond * 0.10F));
        float nextBrightness = Math.min(1.60F, 1.0F + nextQuartz * 0.15F);
        float nextRotation = Math.min(2.0F, (float) Math.pow(1.25D, nextAmethyst));

        return new BeaconRelayState(
                effectiveBoosters + 1,
                nextGlass,
                nextQuartz,
                nextAmethyst,
                nextDiamond,
                nextReversed,
                nextWidth,
                nextInner,
                nextOuter,
                nextBrightness,
                nextRotation
        );
    }

    public boolean modified() {
        return effectiveBoosters > 0;
    }

    public float innerRadius(float vanillaRadius) {
        return Mth.clamp(vanillaRadius * innerRadiusScale, 0.01F, vanillaRadius * 2.0F);
    }

    public float outerRadius(float vanillaRadius) {
        return Mth.clamp(vanillaRadius * outerRadiusScale, 0.01F, vanillaRadius * 2.25F);
    }

    public float signedRotationSpeed() {
        return (reversed ? -1.0F : 1.0F) * rotationSpeedScale;
    }

    /**
     * Reflected-light helpers deliberately keep the five Booster identities separate.
     * The incoming Beacon beam still uses the historical width/brightness/rotation fields,
     * while Crying Obsidian reflection maps materials to diffusion, radiance, resonance,
     * focus and inversion instead of collapsing every Booster into one generic light tier.
     */
    public int reflectedDiffusionTier() {
        return Mth.clamp(glassCount - diamondCount, 0, MAX_EFFECTIVE_BOOSTERS);
    }

    public int reflectedRadianceTier() {
        return Mth.clamp(quartzCount, 0, MAX_EFFECTIVE_BOOSTERS);
    }

    public int reflectedFocusTier() {
        return Mth.clamp(diamondCount, 0, MAX_EFFECTIVE_BOOSTERS);
    }

    public float reflectedBrightnessScale() {
        return brightnessScale;
    }

    public float reflectedInnerRadiusScale() {
        return Mth.clamp(1.0F + glassCount * 0.10F - diamondCount * 0.14F, 0.55F, 1.45F);
    }

    public float reflectedOuterRadiusScale() {
        return Mth.clamp(1.0F + glassCount * 0.20F - diamondCount * 0.08F, 0.65F, 1.70F);
    }

    public float reflectedLengthScale() {
        return Mth.clamp(
                1.0F + quartzCount * 0.10F + diamondCount * 0.06F + glassCount * 0.03F,
                1.0F,
                1.45F
        );
    }

    public float reflectedExcitationScale() {
        float radiance = quartzCount * 0.18F;
        float diffusion = glassCount * 0.08F;
        float resonance = Math.max(0.0F, rotationSpeedScale - 1.0F);
        float focus = diamondCount * 0.05F;
        return Mth.clamp(1.0F + radiance + diffusion + resonance + focus, 1.0F, 2.0F);
    }
}

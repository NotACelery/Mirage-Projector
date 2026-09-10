package celerbi.mirageprojector.crying;

public enum CryingObsidianCrystalStage {

    SMALL(1, 0, 0.75F, 0.34F, 480, 24, 4.5F / 16.0F),
    MEDIUM(2, 0, 0.50F, 0.68F, 240, 16, 6.5F / 16.0F),
    LARGE(3, 0, 0.25F, 1.02F, 160, 12, 8.5F / 16.0F),
    MATURE(4, 15, 0.0F, 1.36F, 120, 8, 1.0F);

    private final int shardDrops;
    private final int vanillaPoweredLight;
    private final float verticalTransmission;
    private final float residualMaxLength;
    private final int residualCycleTicks;
    private final int growthChanceDenominator;
    private final float verticalBeamResumeOffset;

    CryingObsidianCrystalStage(
            int shardDrops,
            int vanillaPoweredLight,
            float verticalTransmission,
            float residualMaxLength,
            int residualCycleTicks,
            int growthChanceDenominator,
            float verticalBeamResumeOffset
    ) {
        this.shardDrops = shardDrops;
        this.vanillaPoweredLight = vanillaPoweredLight;
        this.verticalTransmission = verticalTransmission;
        this.residualMaxLength = residualMaxLength;
        this.residualCycleTicks = residualCycleTicks;
        this.growthChanceDenominator = growthChanceDenominator;
        this.verticalBeamResumeOffset = verticalBeamResumeOffset;
    }

    public int shardDrops() {
        return shardDrops;
    }

    public int vanillaPoweredLight() {
        return vanillaPoweredLight;
    }

    public float verticalTransmission() {
        return verticalTransmission;
    }

    public float absorbedFraction() {
        return 1.0F - verticalTransmission;
    }

    public float residualMaxLength() {
        return residualMaxLength;
    }

    public int residualCycleTicks() {
        return residualCycleTicks;
    }

    public float residualScale() {
        return switch (this) {
            case SMALL -> 0.25F;
            case MEDIUM -> 0.50F;
            case LARGE -> 0.75F;
            case MATURE -> 1.00F;
        };
    }

    public int growthChanceDenominator() {
        return growthChanceDenominator;
    }

    public float verticalBeamResumeOffset() {
        return verticalBeamResumeOffset;
    }

    public boolean isMature() {
        return this == MATURE;
    }
}

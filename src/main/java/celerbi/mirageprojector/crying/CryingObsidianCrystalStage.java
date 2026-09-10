package celerbi.mirageprojector.crying;

public enum CryingObsidianCrystalStage {
    // dev.56 optics contract:
    // - buds never emit block light;
    // - each stage below MATURE scales residual ray size/length/frequency by -25%;
    // - MATURE remains the full-power light thief/source.
    SMALL(1, 0, 0.75F, 0.34F, 480, 24),
    MEDIUM(2, 0, 0.50F, 0.68F, 240, 16),
    LARGE(3, 0, 0.25F, 1.02F, 160, 12),
    MATURE(4, 15, 0.0F, 1.36F, 120, 8);

    private final int shardDrops;
    private final int vanillaPoweredLight;
    private final float verticalTransmission;
    private final float residualMaxLength;
    private final int residualCycleTicks;
    private final int growthChanceDenominator;

    CryingObsidianCrystalStage(
            int shardDrops,
            int vanillaPoweredLight,
            float verticalTransmission,
            float residualMaxLength,
            int residualCycleTicks,
            int growthChanceDenominator
    ) {
        this.shardDrops = shardDrops;
        this.vanillaPoweredLight = vanillaPoweredLight;
        this.verticalTransmission = verticalTransmission;
        this.residualMaxLength = residualMaxLength;
        this.residualCycleTicks = residualCycleTicks;
        this.growthChanceDenominator = growthChanceDenominator;
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

    public boolean isMature() {
        return this == MATURE;
    }
}

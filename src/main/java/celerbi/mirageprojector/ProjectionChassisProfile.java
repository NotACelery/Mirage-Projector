package celerbi.mirageprojector;

/**
 * Physical/efficiency contract for each Mirage Projector body.
 *
 * <p>Since dev.38 the geometry values are <strong>nominal efficiency targets</strong>,
 * not absolute hard caps. A sufficiently strong Core may overdrive a chassis beyond
 * its nominal Scale/Lift/Float, but the PU cost rises progressively. This prevents
 * unused Core power from becoming dead capacity while still keeping larger chassis
 * materially more efficient for large projections.</p>
 */
public enum ProjectionChassisProfile {
    COMPACT("Compact", Geometry.PLANE, 10, 10, 32, 4, 1, 1.00F, 5, 3.5F, 2, 3, 2),
    DISPLAY("Display", Geometry.PLANE, 32, 32, 48, 12, 1, 1.50F, 6, 4.5F, 4, 3, 4),
    WIDE("Wide", Geometry.PLANE, 80, 32, 64, 12, 4, 2.00F, 6, 4.5F, 4, 3, 4),
    TALL("Tall", Geometry.PLANE, 32, 80, 96, 16, 4, 2.00F, 8, 5.5F, 4, 5, 4),
    FIELD("Field", Geometry.PLANE, 128, 128, 144, 24, 1, 4.00F, 7, 5.0F, 4, 4, 4),
    PRISM("Prism", Geometry.PRISM, 48, 48, 96, 12, 4, 2.00F, 7, 5.0F, 4, 4, 4),
    // Future chassis values are provisional architecture targets only.
    EFFIGY("Effigy", Geometry.EFFIGY, 96, 160, 128, 16, 8, 3.00F, 8, 5.5F, 4, 5, 4),
    COLOSSAL("Colossal", Geometry.VOLUMETRIC, 160, 160, 160, 32, 16, 5.00F, 10, 7.0F, 6, 6, 6);

    private final String displayName;
    private final Geometry geometry;
    private final int nominalWidthPixels;
    private final int nominalHeightPixels;
    private final int nominalLiftPixels;
    private final int nominalFloatPixels;
    private final int sourceCapacity;
    private final float powerMultiplier;
    private final int physicalTopPixels;
    private final float coreCenterYPixels;
    private final int coreWidthPixels;
    private final int coreHeightPixels;
    private final int coreDepthPixels;

    ProjectionChassisProfile(
            String displayName,
            Geometry geometry,
            int nominalWidthPixels,
            int nominalHeightPixels,
            int nominalLiftPixels,
            int nominalFloatPixels,
            int sourceCapacity,
            float powerMultiplier,
            int physicalTopPixels,
            float coreCenterYPixels,
            int coreWidthPixels,
            int coreHeightPixels,
            int coreDepthPixels
    ) {
        this.displayName = displayName;
        this.geometry = geometry;
        this.nominalWidthPixels = nominalWidthPixels;
        this.nominalHeightPixels = nominalHeightPixels;
        this.nominalLiftPixels = nominalLiftPixels;
        this.nominalFloatPixels = nominalFloatPixels;
        this.sourceCapacity = sourceCapacity;
        this.powerMultiplier = powerMultiplier;
        this.physicalTopPixels = physicalTopPixels;
        this.coreCenterYPixels = coreCenterYPixels;
        this.coreWidthPixels = coreWidthPixels;
        this.coreHeightPixels = coreHeightPixels;
        this.coreDepthPixels = coreDepthPixels;
    }

    public String displayName() {
        return displayName;
    }

    public Geometry geometry() {
        return geometry;
    }

    public int nominalWidthPixels() {
        return nominalWidthPixels;
    }

    public int nominalHeightPixels() {
        return nominalHeightPixels;
    }

    /** Largest nominal dimension, useful for a generic Scale reference. */
    public int nominalScalePixels() {
        return Math.max(nominalWidthPixels, nominalHeightPixels);
    }

    public int nominalLiftPixels() {
        return nominalLiftPixels;
    }

    public int nominalFloatPixels() {
        return nominalFloatPixels;
    }

    public int sourceCapacity() {
        return sourceCapacity;
    }

    /** Multiplies Core base PU before projection costs are validated. */
    public float powerMultiplier() {
        return powerMultiplier;
    }

    /**
     * Chassis that optionally divide one Plane into four independent image cells.
     * Wide/Tall may switch between one continuous image and a 4-source strip/stack.
     * Field deliberately remains one continuous large Plane; the old dev.33 3x3
     * behaviour was never part of the intended chassis contract.
     */
    public boolean supportsMultiSourceImageLayout() {
        return this == WIDE || this == TALL;
    }

    public int imageLayoutColumns() {
        return this == WIDE ? 4 : 1;
    }

    public int imageLayoutRows() {
        return this == TALL ? 4 : 1;
    }

    public int imageLayoutSlots() {
        return supportsMultiSourceImageLayout() ? 4 : 1;
    }

    /** Height of the physical projector body/emitter above the block floor. */
    public int physicalTopPixels() {
        return physicalTopPixels;
    }

    public float coreCenterYPixels() {
        return coreCenterYPixels;
    }

    public int coreWidthPixels() {
        return coreWidthPixels;
    }

    public int coreHeightPixels() {
        return coreHeightPixels;
    }

    public int coreDepthPixels() {
        return coreDepthPixels;
    }

    public enum Geometry {
        PLANE,
        PRISM,
        EFFIGY,
        VOLUMETRIC
    }
}

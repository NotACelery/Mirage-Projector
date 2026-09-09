package celerbi.mirageprojector;

/**
 * Physical/efficiency contract for the six currently implemented Mirage Projector bodies.
 *
 * <p>Future chassis must not be added here as placeholder enum values. Add a profile only when
 * a real registered block/menu/render contract exists; this keeps ordinal-based menu transport
 * aligned with actual gameplay chassis.</p>
 *
 * <p>Since dev.38 the geometry values are <strong>nominal efficiency targets</strong>,
 * not absolute hard caps. A sufficiently strong Core may overdrive a chassis beyond
 * its nominal Scale/Lift/Float, but the PU cost rises progressively. This prevents
 * unused Core power from becoming dead capacity while still keeping larger chassis
 * materially more efficient for large projections.</p>
 */
public enum ProjectionChassisProfile {
    COMPACT("Compact", Geometry.PLANE, 10, 10, 32, 4, 1.00F, 5, 3.5F, 2, 3, 2),
    DISPLAY("Display", Geometry.PLANE, 32, 32, 48, 12, 1.50F, 6, 4.5F, 4, 3, 4),
    WIDE("Wide", Geometry.PLANE, 80, 32, 64, 12, 2.00F, 6, 4.5F, 4, 3, 4),
    TALL("Tall", Geometry.PLANE, 32, 80, 96, 16, 2.00F, 8, 5.5F, 4, 5, 4),
    FIELD("Field", Geometry.PLANE, 128, 128, 144, 24, 4.00F, 7, 5.0F, 4, 4, 4),
    PRISM("Prism", Geometry.PRISM, 48, 48, 96, 12, 2.00F, 7, 5.0F, 4, 4, 4);

    private final String displayName;
    private final Geometry geometry;
    private final int nominalWidthPixels;
    private final int nominalHeightPixels;
    private final int nominalLiftPixels;
    private final int nominalFloatPixels;
    private final float powerMultiplier;
    private final int physicalTopPixels;
    // Temporary dimensions for the dev.40 material-block Core visual. Remove when Core Chamber lands.
    private final float legacyCoreCenterYPixels;
    private final int legacyCoreWidthPixels;
    private final int legacyCoreHeightPixels;
    private final int legacyCoreDepthPixels;

    ProjectionChassisProfile(
            String displayName,
            Geometry geometry,
            int nominalWidthPixels,
            int nominalHeightPixels,
            int nominalLiftPixels,
            int nominalFloatPixels,
            float powerMultiplier,
            int physicalTopPixels,
            float legacyCoreCenterYPixels,
            int legacyCoreWidthPixels,
            int legacyCoreHeightPixels,
            int legacyCoreDepthPixels
    ) {
        this.displayName = displayName;
        this.geometry = geometry;
        this.nominalWidthPixels = nominalWidthPixels;
        this.nominalHeightPixels = nominalHeightPixels;
        this.nominalLiftPixels = nominalLiftPixels;
        this.nominalFloatPixels = nominalFloatPixels;
        this.powerMultiplier = powerMultiplier;
        this.physicalTopPixels = physicalTopPixels;
        this.legacyCoreCenterYPixels = legacyCoreCenterYPixels;
        this.legacyCoreWidthPixels = legacyCoreWidthPixels;
        this.legacyCoreHeightPixels = legacyCoreHeightPixels;
        this.legacyCoreDepthPixels = legacyCoreDepthPixels;
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

    public float legacyCoreCenterYPixels() {
        return legacyCoreCenterYPixels;
    }

    public int legacyCoreWidthPixels() {
        return legacyCoreWidthPixels;
    }

    public int legacyCoreHeightPixels() {
        return legacyCoreHeightPixels;
    }

    public int legacyCoreDepthPixels() {
        return legacyCoreDepthPixels;
    }

    public enum Geometry {
        PLANE,
        PRISM
    }
}

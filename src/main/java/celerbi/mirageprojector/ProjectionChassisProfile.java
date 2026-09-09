package celerbi.mirageprojector;

/**
 * Hard geometric contract for each Mirage Projector body.
 *
 * <p>The chassis defines the physical emission height, the maximum projection
 * envelope and how many sources the body is designed to host. The removable
 * Projection Core remains a separate power/soft-limit system.</p>
 */
public enum ProjectionChassisProfile {
    COMPACT("Compact", Geometry.PLANE, 10, 10, 32, 4, 1, 5, 3.5F, 2, 3, 2),
    DISPLAY("Display", Geometry.PLANE, 16, 16, 48, 6, 1, 6, 4.5F, 4, 3, 4),
    WIDE("Wide", Geometry.PLANE, 80, 32, 64, 8, 4, 6, 4.5F, 4, 3, 4),
    TALL("Tall", Geometry.PLANE, 32, 80, 96, 12, 4, 8, 5.5F, 4, 5, 4),
    FIELD("Field", Geometry.PLANE, 80, 80, 96, 16, 9, 7, 5.0F, 4, 4, 4),
    PRISM("Prism", Geometry.PRISM, 48, 48, 96, 12, 4, 7, 5.0F, 4, 4, 4),
    EFFIGY("Effigy", Geometry.EFFIGY, 96, 160, 128, 16, 8, 8, 5.5F, 4, 5, 4),
    COLOSSAL("Colossal", Geometry.VOLUMETRIC, 160, 160, 160, 32, 16, 10, 7.0F, 6, 6, 6);

    private final String displayName;
    private final Geometry geometry;
    private final int maxWidthPixels;
    private final int maxHeightPixels;
    private final int maxLiftPixels;
    private final int maxFloatPixels;
    private final int sourceCapacity;
    private final int physicalTopPixels;
    private final float coreCenterYPixels;
    private final int coreWidthPixels;
    private final int coreHeightPixels;
    private final int coreDepthPixels;

    ProjectionChassisProfile(
            String displayName,
            Geometry geometry,
            int maxWidthPixels,
            int maxHeightPixels,
            int maxLiftPixels,
            int maxFloatPixels,
            int sourceCapacity,
            int physicalTopPixels,
            float coreCenterYPixels,
            int coreWidthPixels,
            int coreHeightPixels,
            int coreDepthPixels
    ) {
        this.displayName = displayName;
        this.geometry = geometry;
        this.maxWidthPixels = maxWidthPixels;
        this.maxHeightPixels = maxHeightPixels;
        this.maxLiftPixels = maxLiftPixels;
        this.maxFloatPixels = maxFloatPixels;
        this.sourceCapacity = sourceCapacity;
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

    public int maxWidthPixels() {
        return maxWidthPixels;
    }

    public int maxHeightPixels() {
        return maxHeightPixels;
    }

    /** Largest single dimension retained for generic/debug UI and Core comparisons. */
    public int maxScalePixels() {
        return Math.max(maxWidthPixels, maxHeightPixels);
    }

    public int maxLiftPixels() {
        return maxLiftPixels;
    }

    public int maxFloatPixels() {
        return maxFloatPixels;
    }

    public int sourceCapacity() {
        return sourceCapacity;
    }

    /** Plane chassis that divide one physical projection surface into a source bank. */
    public boolean hasMultiSourceImageLayout() {
        return this == WIDE || this == TALL || this == FIELD;
    }

    public int imageLayoutColumns() {
        return switch (this) {
            case WIDE -> 4;
            case TALL -> 1;
            case FIELD -> 3;
            default -> 1;
        };
    }

    public int imageLayoutRows() {
        return switch (this) {
            case WIDE -> 1;
            case TALL -> 4;
            case FIELD -> 3;
            default -> 1;
        };
    }

    public int imageLayoutSlots() {
        return hasMultiSourceImageLayout() ? imageLayoutColumns() * imageLayoutRows() : 1;
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

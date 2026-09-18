package celerbi.mirageprojector;

import java.util.EnumSet;

public enum ProjectionChassisProfile {
    COMPACT("Compact", Geometry.PLANE, 10, 10, 32, 4, 1.00F, 5, 3.0F, Anchor.FLOOR_UPRIGHT, false,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING),
    DISPLAY("Display", Geometry.PLANE, 32, 32, 48, 12, 1.50F, 6, 4.0F, Anchor.FLOOR_UPRIGHT, false,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING),
    WIDE("Wide", Geometry.PLANE, 80, 32, 64, 12, 2.00F, 6, 4.0F, Anchor.FLOOR_UPRIGHT, false,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING),
    TALL("Tall", Geometry.PLANE, 32, 80, 96, 16, 2.00F, 8, 6.0F, Anchor.FLOOR_UPRIGHT, false,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING),
    FIELD("Field", Geometry.PLANE, 128, 128, 144, 24, 4.00F, 7, 5.0F, Anchor.FLOOR_UPRIGHT, true,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING),
    PRISM("Prism", Geometry.PRISM, 48, 48, 96, 12, 2.00F, 7, 5.0F, Anchor.FLOOR_UPRIGHT, true,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING,
            PlacementCapability.PRISM_DISTANCE),

    // 1.0.25 1.1-foundation chassis. Appended after the original six so historical ordinal
    // values sent through workspace menus remain stable. Final Survival recipes/balance are still
    // intentionally provisional; these entries freeze the runtime anchor semantics first.
    TABLE("Table", Geometry.PLANE, 48, 48, 64, 12, 1.50F, 6, 4.0F, Anchor.TABLE_HORIZONTAL, true,
            PlacementCapability.LIFT, PlacementCapability.TILT, PlacementCapability.ROTATION, PlacementCapability.FLOATING,
            PlacementCapability.TABLE_XZ_OFFSET),
    WALL("Wall", Geometry.PLANE, 64, 36, 0, 0, 1.50F, 6, 3.0F, Anchor.WALL_TARGET, false,
            PlacementCapability.WALL_XY_OFFSET);

    private final String displayName;
    private final Geometry geometry;
    private final int nominalWidthPixels;
    private final int nominalHeightPixels;
    private final int nominalLiftPixels;
    private final int nominalFloatPixels;
    private final float powerMultiplier;
    private final int physicalTopPixels;
    private final float coreChamberCenterYPixels;
    private final Anchor anchor;
    private final boolean endResonanceCapable;
    private final EnumSet<PlacementCapability> placementCapabilities;

    ProjectionChassisProfile(
            String displayName,
            Geometry geometry,
            int nominalWidthPixels,
            int nominalHeightPixels,
            int nominalLiftPixels,
            int nominalFloatPixels,
            float powerMultiplier,
            int physicalTopPixels,
            float coreChamberCenterYPixels,
            Anchor anchor,
            boolean endResonanceCapable,
            PlacementCapability... placementCapabilities
    ) {
        this.displayName = displayName;
        this.geometry = geometry;
        this.nominalWidthPixels = nominalWidthPixels;
        this.nominalHeightPixels = nominalHeightPixels;
        this.nominalLiftPixels = nominalLiftPixels;
        this.nominalFloatPixels = nominalFloatPixels;
        this.powerMultiplier = powerMultiplier;
        this.physicalTopPixels = physicalTopPixels;
        this.coreChamberCenterYPixels = coreChamberCenterYPixels;
        this.anchor = anchor == null ? Anchor.FLOOR_UPRIGHT : anchor;
        this.endResonanceCapable = endResonanceCapable;
        this.placementCapabilities = EnumSet.noneOf(PlacementCapability.class);
        if (placementCapabilities != null) {
            for (PlacementCapability capability : placementCapabilities) {
                if (capability != null) {
                    this.placementCapabilities.add(capability);
                }
            }
        }
    }

    public String displayName() { return displayName; }
    public Geometry geometry() { return geometry; }
    public int nominalWidthPixels() { return nominalWidthPixels; }
    public int nominalHeightPixels() { return nominalHeightPixels; }
    public int nominalScalePixels() { return Math.max(nominalWidthPixels, nominalHeightPixels); }
    public int nominalLiftPixels() { return nominalLiftPixels; }
    public int nominalFloatPixels() { return nominalFloatPixels; }
    public float powerMultiplier() { return powerMultiplier; }
    public int physicalTopPixels() { return physicalTopPixels; }
    public float coreChamberCenterYPixels() { return coreChamberCenterYPixels; }
    public Anchor anchor() { return anchor; }
    public boolean supportsEndResonance() { return endResonanceCapable; }

    public boolean supportsProjectionSource(ProjectionSettings.SourceMode source) {
        return ProjectionSourceRegistry.isCompatible(source, this);
    }

    public boolean supportsMultiSourceImageLayout() { return this == WIDE || this == TALL; }
    public int imageLayoutColumns() { return this == WIDE ? 4 : 1; }
    public int imageLayoutRows() { return this == TALL ? 4 : 1; }
    public int imageLayoutSlots() { return supportsMultiSourceImageLayout() ? 4 : 1; }

    public boolean supports(PlacementCapability capability) {
        return capability != null && placementCapabilities.contains(capability);
    }

    public boolean supportsLift() { return supports(PlacementCapability.LIFT); }
    public boolean supportsTilt() { return supports(PlacementCapability.TILT); }
    public boolean supportsRotation() { return supports(PlacementCapability.ROTATION); }
    public boolean supportsFloating() { return supports(PlacementCapability.FLOATING); }
    public boolean supportsPrismDistance() { return supports(PlacementCapability.PRISM_DISTANCE); }
    public boolean supportsWallXyOffset() { return supports(PlacementCapability.WALL_XY_OFFSET); }
    public boolean supportsTableXzOffset() { return supports(PlacementCapability.TABLE_XZ_OFFSET); }
    public boolean supportsPresentationDeck() { return this == TABLE || this == WALL; }

    /**
     * Plane sources use the table surface orientation. Volumetric Item/Entity sources remain
     * upright above the same anchor so a table projector can act as a miniature display plinth.
     */
    public boolean usesHorizontalPlaneFor(ProjectionSettings.SourceMode source) {
        return anchor == Anchor.TABLE_HORIZONTAL
                && (source == ProjectionSettings.SourceMode.IMAGE || source == ProjectionSettings.SourceMode.BANNER);
    }

    public enum Geometry { PLANE, PRISM }

    public enum Anchor {
        /** Existing six fixed chassis: content rises from the top of a floor-standing machine. */
        FLOOR_UPRIGHT,
        /** Floor/table appliance: planar sources lie parallel to the supporting surface. */
        TABLE_HORIZONTAL,
        /** Data-show appliance: the machine sits on a full flat support and targets a real wall ahead. */
        WALL_TARGET
    }

    public enum PlacementCapability {
        LIFT,
        TILT,
        ROTATION,
        FLOATING,
        PRISM_DISTANCE,
        WALL_XY_OFFSET,
        TABLE_XZ_OFFSET
    }
}

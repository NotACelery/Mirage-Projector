package celerbi.mirageprojector;

import net.minecraft.util.Mth;

/** Shared Projection Power and geometric validation for every chassis/core combination. */
public final class ProjectionPower {
    private ProjectionPower() {
    }

    public static Status evaluate(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem
    ) {
        return evaluate(settings, core, chassis, hasProjectedItem, hasProjectedItem ? 1 : 0);
    }

    public static Status evaluate(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            int projectedSourceCount
    ) {
        ProjectionSettings s = settings.sanitized();
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;

        int used = calculateUsedPower(s, hasProjectedItem, safeChassis, projectedSourceCount);
        if (!safeCore.present()) {
            return new Status(false, used, 0, Failure.NO_CORE);
        }

        if (s.scalePixels() > safeCore.maxScalePixels()) {
            return new Status(false, used, safeCore.power(), Failure.CORE_SCALE_LIMIT);
        }
        if (s.liftPixels() > safeCore.maxLiftPixels()) {
            return new Status(false, used, safeCore.power(), Failure.CORE_LIFT_LIMIT);
        }
        if (s.floatAmplitudePixels() > safeCore.maxFloatPixels()) {
            return new Status(false, used, safeCore.power(), Failure.CORE_FLOAT_LIMIT);
        }

        if (s.floatingEnabled() && s.floatAmplitudePixels() > s.liftPixels()) {
            return new Status(false, used, safeCore.power(), Failure.PHYSICAL_FLOAT_LIMIT);
        }

        if (!s.debugChassisOverride()) {
            Dimensions dimensions = dimensions(s, hasProjectedItem, safeChassis);
            if (dimensions.widthPixels() > safeChassis.maxWidthPixels()
                    || dimensions.heightPixels() > safeChassis.maxHeightPixels()) {
                return new Status(false, used, safeCore.power(), Failure.CHASSIS_ENVELOPE_LIMIT);
            }
            if (s.liftPixels() > safeChassis.maxLiftPixels()) {
                return new Status(false, used, safeCore.power(), Failure.CHASSIS_LIFT_LIMIT);
            }
            if (s.floatAmplitudePixels() > safeChassis.maxFloatPixels()) {
                return new Status(false, used, safeCore.power(), Failure.CHASSIS_FLOAT_LIMIT);
            }
        }

        if (used > safeCore.power()) {
            return new Status(false, used, safeCore.power(), Failure.POWER_EXCEEDED);
        }
        return new Status(true, used, safeCore.power(), Failure.NONE);
    }

    /** Compatibility helper for old call sites that still mean the Compact body. */
    public static Status evaluate(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            boolean hasProjectedItem
    ) {
        return evaluate(settings, core, ProjectionChassisProfile.COMPACT, hasProjectedItem);
    }

    public static Dimensions dimensions(ProjectionSettings s, boolean hasProjectedItem) {
        return dimensions(s, hasProjectedItem, ProjectionChassisProfile.COMPACT);
    }

    /**
     * Actual projected dimensions, in Minecraft pixels, after preserving source
     * aspect ratio. Plane keeps Front/Back semantics; Prism uses the largest
     * independent North/East/South/West face.
     */
    public static Dimensions dimensions(
            ProjectionSettings s,
            boolean hasProjectedItem,
            ProjectionChassisProfile chassis
    ) {
        ProjectionSettings safe = s.sanitized();
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        if (safe.sourceMode() == ProjectionSettings.SourceMode.ITEM) {
            int side = hasProjectedItem ? safe.scalePixels() : 0;
            return new Dimensions(side, side);
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.ENTITY) {
            int side = hasProjectedItem ? safe.scalePixels() : 0;
            return new Dimensions(side, side);
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.BANNER) {
            return hasProjectedItem ? bannerDimensions(safe.scalePixels()) : new Dimensions(0, 0);
        }

        if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && safeChassis.hasMultiSourceImageLayout()) {
            return hasProjectedItem ? multiSourceLayoutDimensions(safe.scalePixels(), safeChassis) : new Dimensions(0, 0);
        }

        if (safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM) {
            Dimensions result = new Dimensions(0, 0);
            result = includeImage(result, safe.hasImage(), safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
            result = includeImage(result, safe.hasEastImage(), safe.eastImageWidth(), safe.eastImageHeight(), safe.scalePixels());
            result = includeImage(result, safe.hasBackImage(), safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
            result = includeImage(result, safe.hasWestImage(), safe.westImageWidth(), safe.westImageHeight(), safe.scalePixels());
            return result;
        }

        return switch (safe.backFaceMode()) {
            case FRONT, MIRRORED, READABLE -> safe.hasImage()
                    ? imageDimensions(safe.imageWidth(), safe.imageHeight(), safe.scalePixels())
                    : new Dimensions(0, 0);
            case BACK -> safe.hasBackImage()
                    ? imageDimensions(safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels())
                    : new Dimensions(0, 0);
            case INDEPENDENT -> {
                Dimensions result = new Dimensions(0, 0);
                result = includeImage(result, safe.hasImage(), safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
                result = includeImage(result, safe.hasBackImage(), safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
                yield result;
            }
        };
    }

    private static Dimensions includeImage(Dimensions current, boolean present, int width, int height, int scalePixels) {
        return present ? merge(current, imageDimensions(width, height, scalePixels)) : current;
    }

    private static Dimensions merge(Dimensions a, Dimensions b) {
        return new Dimensions(
                Math.max(a.widthPixels(), b.widthPixels()),
                Math.max(a.heightPixels(), b.heightPixels())
        );
    }

    private static Dimensions imageDimensions(int imageWidth, int imageHeight, int scalePixels) {
        int largest = Math.max(1, scalePixels);
        if (imageWidth >= imageHeight) {
            return new Dimensions(
                    largest,
                    Math.max(1, Math.round(largest * imageHeight / (float) imageWidth))
            );
        }
        return new Dimensions(
                Math.max(1, Math.round(largest * imageWidth / (float) imageHeight)),
                largest
        );
    }

    public static int calculateUsedPower(ProjectionSettings s, boolean hasProjectedItem) {
        return calculateUsedPower(s, hasProjectedItem, ProjectionChassisProfile.COMPACT);
    }

    public static int calculateUsedPower(
            ProjectionSettings s,
            boolean hasProjectedItem,
            ProjectionChassisProfile chassis
    ) {
        return calculateUsedPower(s, hasProjectedItem, chassis, hasProjectedItem ? 1 : 0);
    }

    public static int calculateUsedPower(
            ProjectionSettings s,
            boolean hasProjectedItem,
            ProjectionChassisProfile chassis,
            int projectedSourceCount
    ) {
        ProjectionSettings safe = s.sanitized();
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        int safeSourceCount = Math.max(0, projectedSourceCount);
        boolean hasRenderableContent = switch (safe.sourceMode()) {
            case ITEM, ENTITY -> hasProjectedItem;
            case BANNER -> hasProjectedItem && safeSourceCount > 0;
            case IMAGE -> safeChassis.hasMultiSourceImageLayout()
                    ? hasProjectedItem && safeSourceCount > 0
                    : safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                    ? safe.hasAnyImage()
                    : hasPlaneImageContent(safe);
        };
        if (!hasRenderableContent) {
            return 0;
        }

        int power = 1;
        Dimensions dimensions = dimensions(safe, hasProjectedItem, safeChassis);
        if (safe.sourceMode() == ProjectionSettings.SourceMode.ITEM && hasProjectedItem) {
            int side = Math.max(1, dimensions.widthPixels());
            power += Math.max(1, Mth.ceil((side * side) / 256.0D));
            power += 2;
        } else if (safe.sourceMode() == ProjectionSettings.SourceMode.ENTITY && hasProjectedItem) {
            int side = Math.max(1, dimensions.widthPixels());
            power += Math.max(1, Mth.ceil((side * side) / 256.0D));
            power += 4;
        } else if (safe.sourceMode() == ProjectionSettings.SourceMode.BANNER && hasProjectedItem) {
            Dimensions banner = bannerDimensions(safe.scalePixels());
            int faceCost = Math.max(1, Mth.ceil((banner.widthPixels() * banner.heightPixels()) / 256.0D));
            power += faceCost * Math.max(1, safeSourceCount);
            power += safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM ? 2 : 1;
        } else if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && safeChassis.hasMultiSourceImageLayout()
                && hasProjectedItem && safeSourceCount > 0) {
            Dimensions layout = multiSourceLayoutDimensions(safe.scalePixels(), safeChassis);
            int columns = Math.max(1, safeChassis.imageLayoutColumns());
            int rows = Math.max(1, safeChassis.imageLayoutRows());
            int cellWidth = Math.max(1, Mth.ceil(layout.widthPixels() / (double) columns));
            int cellHeight = Math.max(1, Mth.ceil(layout.heightPixels() / (double) rows));
            int cellCost = Math.max(1, Mth.ceil((cellWidth * cellHeight) / 256.0D));
            power += cellCost * safeSourceCount;
            power += safeSourceCount > 1 ? 1 : 0;
        } else if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && safe.hasAnyImage()) {
            power += imagePower(safe.hasImage(), safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
            power += imagePower(safe.hasEastImage(), safe.eastImageWidth(), safe.eastImageHeight(), safe.scalePixels());
            power += imagePower(safe.hasBackImage(), safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
            power += imagePower(safe.hasWestImage(), safe.westImageWidth(), safe.westImageHeight(), safe.scalePixels());
            power += 2;
        } else if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE && hasPlaneImageContent(safe)) {
            if (safe.backFaceMode() == ProjectionSettings.BackFaceMode.BACK) {
                power += imagePower(true, safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
            } else if (safe.backFaceMode() == ProjectionSettings.BackFaceMode.INDEPENDENT) {
                if (safe.hasImage()) {
                    power += imagePower(true, safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
                    if (safe.hasBackImage()) {
                        // Separate Back remains a second texture/source but keeps
                        // the historical lightweight Independent surcharge.
                        power += 1;
                    }
                } else {
                    power += imagePower(true, safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
                }
            } else {
                power += imagePower(true, safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
            }
        } else {
            power += 1;
        }

        if (safe.liftPixels() > 0) {
            power += Mth.ceil(safe.liftPixels() / 16.0D);
        }
        if (safe.rotationEnabled()) {
            power += 1;
        }
        if (safe.floatingEnabled() && safe.floatAmplitudePixels() > 0) {
            power += Math.max(1, Mth.ceil(safe.floatAmplitudePixels() / 2.0D));
            if (safe.floatMode() == ProjectionSettings.FloatMode.ROTATION_SYNCED) {
                power += 1;
            }
        }
        if (safe.fullbright()) {
            power += 1;
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE && safe.scanlines()) {
            power += 1;
        }
        return power;
    }

    /** Overall multi-source Plane footprint. Scale controls the longest chassis axis. */
    private static Dimensions multiSourceLayoutDimensions(int scalePixels, ProjectionChassisProfile chassis) {
        int longest = Math.max(1, scalePixels);
        int maxW = Math.max(1, chassis.maxWidthPixels());
        int maxH = Math.max(1, chassis.maxHeightPixels());
        if (maxW >= maxH) {
            return new Dimensions(longest, Math.max(1, Math.round(longest * maxH / (float) maxW)));
        }
        return new Dimensions(Math.max(1, Math.round(longest * maxW / (float) maxH)), longest);
    }

    private static Dimensions bannerDimensions(int scalePixels) {
        int height = Math.max(1, scalePixels);
        int width = Math.max(1, Math.round(height * 0.5F));
        return new Dimensions(width, height);
    }

    private static boolean hasPlaneImageContent(ProjectionSettings safe) {
        return switch (safe.backFaceMode()) {
            case FRONT, MIRRORED, READABLE -> safe.hasImage();
            case BACK -> safe.hasBackImage();
            case INDEPENDENT -> safe.hasImage() || safe.hasBackImage();
        };
    }

    private static int imagePower(boolean present, int imageWidth, int imageHeight, int scalePixels) {
        if (!present) {
            return 0;
        }
        Dimensions face = imageDimensions(imageWidth, imageHeight, scalePixels);
        int width = Math.max(1, face.widthPixels());
        int height = Math.max(1, face.heightPixels());
        return Math.max(1, Mth.ceil((width * height) / 256.0D));
    }

    public enum Failure {
        NONE("Ready"),
        NO_CORE("No Projection Core installed"),
        CORE_SCALE_LIMIT("Scale exceeds this Core's limit"),
        CORE_LIFT_LIMIT("Projection Lift exceeds this Core's limit"),
        CORE_FLOAT_LIMIT("Float amplitude exceeds this Core's limit"),
        PHYSICAL_FLOAT_LIMIT("Float amplitude would intersect the projector"),
        CHASSIS_ENVELOPE_LIMIT("Projection aspect ratio exceeds this chassis envelope"),
        CHASSIS_LIFT_LIMIT("Lift exceeds this chassis envelope"),
        CHASSIS_FLOAT_LIMIT("Float amplitude exceeds this chassis envelope"),
        POWER_EXCEEDED("Projection Power budget exceeded");

        private final String message;

        Failure(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }

    public record Dimensions(int widthPixels, int heightPixels) {
        public boolean empty() {
            return widthPixels <= 0 || heightPixels <= 0;
        }

        public String summary() {
            return widthPixels + "×" + heightPixels + "px";
        }
    }

    public record Status(boolean active, int usedPower, int availablePower, Failure failure) {
        public int freePower() {
            return Math.max(0, availablePower - usedPower);
        }

        public float fillRatio() {
            if (availablePower <= 0) {
                return 0.0F;
            }
            return Mth.clamp(usedPower / (float) availablePower, 0.0F, 1.0F);
        }

        public String summary() {
            if (active) {
                return "Power: " + usedPower + " / " + availablePower;
            }
            return failure.message() + " · " + usedPower + " / " + availablePower;
        }
    }
}

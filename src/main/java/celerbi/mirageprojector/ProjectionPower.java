package celerbi.mirageprojector;

import net.minecraft.util.Mth;

public final class ProjectionPower {
    private ProjectionPower() {
    }

    public static final int GEOMETRY_PIXELS_PER_PU = 256;

    public static final int LIFT_PIXELS_PER_PU = 16;

    public static final int FLOAT_PIXELS_PER_PU = 2;

    public static final int BASE_EMITTER_COST = 2;

    public static final int GHOST_REBATE_DIVISOR = 3000;

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
        ProjectionSettings safe = settings.sanitized();
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        Breakdown breakdown = calculateBreakdown(safe, hasProjectedItem, safeChassis, projectedSourceCount);
        int available = effectiveCapacity(safeCore, safeChassis);

        if (!safeCore.present()) {
            return new Status(false, breakdown.totalPower(), 0, Failure.NO_CORE);
        }

        if (safe.floatingEnabled() && safe.floatAmplitudePixels() > safe.liftPixels()) {
            return new Status(false, breakdown.totalPower(), available, Failure.PHYSICAL_FLOAT_LIMIT);
        }

        if (breakdown.totalPower() > available) {
            return new Status(false, breakdown.totalPower(), available, Failure.POWER_EXCEEDED);
        }
        return new Status(true, breakdown.totalPower(), available, Failure.NONE);
    }

    public static int effectiveCapacity(ProjectionCoreProfile core, ProjectionChassisProfile chassis) {
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        if (!safeCore.present()) {
            return 0;
        }
        return Math.max(1, Mth.floor(
                safeCore.basePower()
                        * safeChassis.powerMultiplier()
                        * safeCore.amplificationMultiplier()
        ));
    }

    public static int maximumStructuralScale(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem
    ) {
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        if (!safeCore.present() || !hasProjectedItem) {
            return Math.min(ProjectionSettings.DEBUG_MAX_SCALE_PIXELS, safeChassis.nominalScalePixels());
        }
        return ProjectionSettings.DEBUG_MAX_SCALE_PIXELS;
    }

    public static int maximumStructuralLift(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis
    ) {
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        if (!safeCore.present()) {
            return Math.min(ProjectionSettings.DEBUG_MAX_LIFT_PIXELS, safeChassis.nominalLiftPixels());
        }
        return ProjectionSettings.DEBUG_MAX_LIFT_PIXELS;
    }

    public static int maximumStructuralFloat(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis
    ) {
        ProjectionSettings safe = settings.sanitized();
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        int max = !safeCore.present()
                ? Math.min(ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS, safeChassis.nominalFloatPixels())
                : ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS;
        if (safe.floatingEnabled()) {
            max = Math.min(max, safe.liftPixels());
        }
        return Math.max(0, max);
    }

    public static int maximumFeasibleScale(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            int projectedSourceCount
    ) {
        ProjectionSettings safe = settings.sanitized();
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        if (!safeCore.present() || !hasProjectedItem) {
            return maximumStructuralScale(safe, safeCore, safeChassis, hasProjectedItem);
        }
        for (int candidate = ProjectionSettings.DEBUG_MAX_SCALE_PIXELS;
            candidate >= ProjectionSettings.DEBUG_MIN_SCALE_PIXELS; candidate--) {
            if (evaluate(safe.withScalePixels(candidate), safeCore, safeChassis,
                    hasProjectedItem, projectedSourceCount).active()) {
                return candidate;
            }
        }
        return ProjectionSettings.DEBUG_MIN_SCALE_PIXELS;
    }

    public static int maximumFeasibleLift(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            int projectedSourceCount
    ) {
        ProjectionSettings safe = settings.sanitized();
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        if (!safeCore.present() || !hasProjectedItem) {
            return Math.min(ProjectionSettings.DEBUG_MAX_LIFT_PIXELS, safeChassis.nominalLiftPixels());
        }
        for (int candidate = ProjectionSettings.DEBUG_MAX_LIFT_PIXELS; candidate >= 0; candidate--) {
            if (evaluate(safe.withLiftPixels(candidate), safeCore, safeChassis,
                    hasProjectedItem, projectedSourceCount).active()) {
                return candidate;
            }
        }
        return 0;
    }

    public static int maximumFeasibleFloat(
            ProjectionSettings settings,
            ProjectionCoreProfile core,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            int projectedSourceCount
    ) {
        ProjectionSettings safe = settings.sanitized();
        ProjectionCoreProfile safeCore = core == null ? ProjectionCoreProfile.NONE : core;
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        int technical = safe.floatingEnabled()
                ? Math.min(ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS, safe.liftPixels())
                : ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS;
        if (!safeCore.present() || !hasProjectedItem) {
            return Math.min(technical, safeChassis.nominalFloatPixels());
        }
        for (int candidate = technical; candidate >= 0; candidate--) {
            if (evaluate(safe.withFloatAmplitudePixels(candidate), safeCore, safeChassis,
                    hasProjectedItem, projectedSourceCount).active()) {
                return candidate;
            }
        }
        return 0;
    }

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
                && safeChassis.supportsMultiSourceImageLayout()
                && safe.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
            return hasProjectedItem
                    ? multiSourceLayoutDimensions(safe.scalePixels(), safeChassis)
                    : new Dimensions(0, 0);
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
        ProjectionImageSizing.Size size = ProjectionImageSizing.size(imageWidth, imageHeight, scalePixels);
        return new Dimensions(size.widthPixels(), size.heightPixels());
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
        return calculateBreakdown(s, hasProjectedItem, chassis, projectedSourceCount).totalPower();
    }

    public static Breakdown calculateBreakdown(
            ProjectionSettings s,
            boolean hasProjectedItem,
            ProjectionChassisProfile chassis,
            int projectedSourceCount
    ) {
        ProjectionSettings safe = s.sanitized();
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        int safeSourceCount = Math.max(0, projectedSourceCount);
        boolean hasRenderableContent = hasRenderableContent(safe, safeChassis, hasProjectedItem, safeSourceCount);
        if (!hasRenderableContent) {
            return Breakdown.EMPTY;
        }

        Dimensions dims = dimensions(safe, hasProjectedItem, safeChassis);
        int sourceCost = sourceComplexityCost(safe, safeChassis, safeSourceCount);
        int rawGeometryCost = rawGeometryCost(safe, safeChassis, hasProjectedItem, safeSourceCount);
        int geometryCost = safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && !safe.debugChassisOverride()
                ? prismGeometryCost(safe)
                : applyOverdrive(rawGeometryCost,
                geometryOverdriveRatio(safe, dims, safeChassis, safe.debugChassisOverride()));
        int liftCost = axisCost(safe.liftPixels(), safeChassis.nominalLiftPixels(),
                LIFT_PIXELS_PER_PU, safe.debugChassisOverride());
        int floatCost = safe.floatingEnabled()
                ? axisCost(safe.floatAmplitudePixels(), safeChassis.nominalFloatPixels(),
                FLOAT_PIXELS_PER_PU, safe.debugChassisOverride())
                : 0;

        int featureCost = 0;
        if (safe.rotationEnabled()) {
            featureCost += 1;
        }
        if (safe.floatingEnabled()
                && safe.floatAmplitudePixels() > 0
                && safe.floatMode() == ProjectionSettings.FloatMode.ROTATION_SYNCED) {
            featureCost += 1;
        }
        if (safe.fullbright()) {
            featureCost += 1;
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE && safe.scanlines()) {
            featureCost += 1;
        }

        int gross = BASE_EMITTER_COST + geometryCost + sourceCost + liftCost + floatCost + featureCost;
        int ghostSavings = calculateGhostSavings(gross, safe.transparencyPercent());
        int total = Math.max(1, gross - ghostSavings);
        return new Breakdown(
                BASE_EMITTER_COST,
                geometryCost,
                sourceCost,
                liftCost,
                floatCost,
                featureCost,
                ghostSavings,
                total
        );
    }

    private static boolean hasRenderableContent(
            ProjectionSettings safe,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            int sourceCount
    ) {
        return switch (safe.sourceMode()) {
            case ITEM, ENTITY -> hasProjectedItem;
            case BANNER -> hasProjectedItem && sourceCount > 0;
            case IMAGE -> chassis.supportsMultiSourceImageLayout()
                    && safe.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI
                    ? hasProjectedItem && sourceCount > 0
                    : chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                    ? safe.hasAnyImage()
                    : hasPlaneImageContent(safe);
        };
    }

    private static int rawGeometryCost(
            ProjectionSettings safe,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            int sourceCount
    ) {
        if (safe.sourceMode() == ProjectionSettings.SourceMode.ITEM && hasProjectedItem) {
            return squareAreaPower(safe.scalePixels());
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.ENTITY && hasProjectedItem) {
            return squareAreaPower(safe.scalePixels());
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.BANNER && hasProjectedItem) {
            Dimensions banner = bannerDimensions(safe.scalePixels());
            return areaPower(banner.widthPixels(), banner.heightPixels()) * Math.max(1, sourceCount);
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && chassis.supportsMultiSourceImageLayout()
                && safe.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI
                && hasProjectedItem && sourceCount > 0) {
            Dimensions layout = multiSourceLayoutDimensions(safe.scalePixels(), chassis);
            int columns = Math.max(1, chassis.imageLayoutColumns());
            int rows = Math.max(1, chassis.imageLayoutRows());
            int cellWidth = Math.max(1, Mth.ceil(layout.widthPixels() / (double) columns));
            int cellHeight = Math.max(1, Mth.ceil(layout.heightPixels() / (double) rows));
            return areaPower(cellWidth, cellHeight) * sourceCount;
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && safe.hasAnyImage()) {
            int total = 0;
            total += imagePower(safe.hasImage(), safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
            total += imagePower(safe.hasEastImage(), safe.eastImageWidth(), safe.eastImageHeight(), safe.scalePixels());
            total += imagePower(safe.hasBackImage(), safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
            total += imagePower(safe.hasWestImage(), safe.westImageWidth(), safe.westImageHeight(), safe.scalePixels());
            return total;
        }
        if (safe.sourceMode() == ProjectionSettings.SourceMode.IMAGE && hasPlaneImageContent(safe)) {
            if (safe.backFaceMode() == ProjectionSettings.BackFaceMode.BACK) {
                return imagePower(true, safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
            }
            if (safe.hasImage()) {

                return imagePower(true, safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
            }
            return imagePower(true, safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
        }
        return 1;
    }

    private static int prismGeometryCost(ProjectionSettings safe) {
        int total = 0;
        total += prismFaceGeometryCost(safe.hasImage(), safe.imageWidth(), safe.imageHeight(), safe.scalePixels());
        total += prismFaceGeometryCost(safe.hasEastImage(), safe.eastImageWidth(), safe.eastImageHeight(), safe.scalePixels());
        total += prismFaceGeometryCost(safe.hasBackImage(), safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels());
        total += prismFaceGeometryCost(safe.hasWestImage(), safe.westImageWidth(), safe.westImageHeight(), safe.scalePixels());
        return Math.max(1, total);
    }

    private static int prismFaceGeometryCost(boolean present, int sourceW, int sourceH, int scalePixels) {
        if (!present) {
            return 0;
        }
        ProjectionImageSizing.Size size = ProjectionImageSizing.size(sourceW, sourceH, scalePixels);
        ProjectionImageSizing.NominalEnvelope nominal = ProjectionImageSizing.prismNominal(sourceW, sourceH);
        int base = areaPower(size.widthPixels(), size.heightPixels());
        double wr = size.widthPixels() / (double)Math.max(1, nominal.widthPixels());
        double hr = size.heightPixels() / (double)Math.max(1, nominal.heightPixels());
        return applyOverdrive(base, Math.max(1.0D, Math.max(wr, hr)));
    }

    private static int sourceComplexityCost(
            ProjectionSettings safe,
            ProjectionChassisProfile chassis,
            int sourceCount
    ) {
        return switch (safe.sourceMode()) {
            case IMAGE -> {
                if (chassis.supportsMultiSourceImageLayout()
                        && safe.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
                    yield sourceCount > 1 ? 1 : 0;
                }
                if (chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM) {
                    yield 2;
                }
                yield safe.backFaceMode() == ProjectionSettings.BackFaceMode.INDEPENDENT
                        && safe.hasImage() && safe.hasBackImage() ? 1 : 0;
            }
            case ITEM -> 2;
            case ENTITY -> 4;
            case BANNER -> chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM ? 2 : 1;
        };
    }

    private static int applyOverdrive(int baseCost, double ratio) {
        if (baseCost <= 0) {
            return 0;
        }
        double safeRatio = Math.max(1.0D, ratio);
        return Math.max(baseCost, Mth.ceil(baseCost * safeRatio * safeRatio));
    }

    private static int axisCost(int pixels, int nominalPixels, int pixelsPerPu, boolean debugChassisOverride) {
        if (pixels <= 0) {
            return 0;
        }
        int base = Math.max(1, Mth.ceil(pixels / (double) Math.max(1, pixelsPerPu)));
        if (debugChassisOverride || nominalPixels <= 0 || pixels <= nominalPixels) {
            return base;
        }
        double ratio = pixels / (double) nominalPixels;
        return applyOverdrive(base, ratio);
    }

    private static double geometryOverdriveRatio(
            ProjectionSettings settings,
            Dimensions dims,
            ProjectionChassisProfile chassis,
            boolean debugChassisOverride
    ) {
        if (debugChassisOverride || dims == null || dims.empty()) {
            return 1.0D;
        }
        if (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM) {
            double worst = 1.0D;
            worst = Math.max(worst, prismFaceOverdrive(settings.hasImage(), settings.imageWidth(), settings.imageHeight(), settings.scalePixels()));
            worst = Math.max(worst, prismFaceOverdrive(settings.hasEastImage(), settings.eastImageWidth(), settings.eastImageHeight(), settings.scalePixels()));
            worst = Math.max(worst, prismFaceOverdrive(settings.hasBackImage(), settings.backImageWidth(), settings.backImageHeight(), settings.scalePixels()));
            worst = Math.max(worst, prismFaceOverdrive(settings.hasWestImage(), settings.westImageWidth(), settings.westImageHeight(), settings.scalePixels()));
            return worst;
        }
        double widthRatio = dims.widthPixels() / (double) Math.max(1, chassis.nominalWidthPixels());
        double heightRatio = dims.heightPixels() / (double) Math.max(1, chassis.nominalHeightPixels());
        return Math.max(1.0D, Math.max(widthRatio, heightRatio));
    }

    private static double prismFaceOverdrive(boolean present, int sourceW, int sourceH, int scalePixels) {
        if (!present) {
            return 1.0D;
        }
        ProjectionImageSizing.Size size = ProjectionImageSizing.size(sourceW, sourceH, scalePixels);
        ProjectionImageSizing.NominalEnvelope nominal = ProjectionImageSizing.prismNominal(sourceW, sourceH);
        double wr = size.widthPixels() / (double)Math.max(1, nominal.widthPixels());
        double hr = size.heightPixels() / (double)Math.max(1, nominal.heightPixels());
        return Math.max(1.0D, Math.max(wr, hr));
    }

    public static Overdrive overdrive(
            ProjectionSettings settings,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem
    ) {
        ProjectionSettings safe = settings.sanitized();
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        if (safe.debugChassisOverride()) {
            return new Overdrive(1.0D, 1.0D, 1.0D);
        }
        Dimensions dims = dimensions(safe, hasProjectedItem, safeChassis);
        double geometry = geometryOverdriveRatio(safe, dims, safeChassis, false);
        double lift = Math.max(1.0D, safe.liftPixels() / (double) Math.max(1, safeChassis.nominalLiftPixels()));
        double floating = safe.floatingEnabled()
                ? Math.max(1.0D, safe.floatAmplitudePixels() / (double) Math.max(1, safeChassis.nominalFloatPixels()))
                : 1.0D;
        return new Overdrive(geometry, lift, floating);
    }

    private static int calculateGhostSavings(int grossPower, int ghostPercent) {
        int eligible = Math.max(0, grossPower - BASE_EMITTER_COST);
        int safeGhost = Mth.clamp(ghostPercent, 0, 90);
        int savings = Mth.floor(eligible * safeGhost / (double) GHOST_REBATE_DIVISOR);
        return Math.min(Math.max(0, grossPower - 1), Math.max(0, savings));
    }

    private static Dimensions multiSourceLayoutDimensions(int scalePixels, ProjectionChassisProfile chassis) {
        int longest = Math.max(1, scalePixels);
        int cross = Math.max(1, Mth.ceil(longest / 4.0D));
        return chassis == ProjectionChassisProfile.TALL
                ? new Dimensions(cross, longest)
                : new Dimensions(longest, cross);
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
        return areaPower(face.widthPixels(), face.heightPixels());
    }

    private static int squareAreaPower(int side) {
        int safeSide = Math.max(1, side);
        return areaPower(safeSide, safeSide);
    }

    private static int areaPower(int width, int height) {
        long area = (long) Math.max(1, width) * Math.max(1, height);
        return Math.max(1, Mth.ceil(area / (double) GEOMETRY_PIXELS_PER_PU));
    }

    public enum Failure {
        NONE("Ready"),
        NO_CORE("No Projection Core installed"),
        PHYSICAL_FLOAT_LIMIT("Float amplitude would intersect the projector"),
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
            return widthPixels + "x" + heightPixels + "px";
        }
    }

    public record Breakdown(
            int baseCost,
            int geometryCost,
            int sourceCost,
            int liftCost,
            int floatCost,
            int featureCost,
            int ghostSavings,
            int totalPower
    ) {
        public static final Breakdown EMPTY = new Breakdown(0, 0, 0, 0, 0, 0, 0, 0);

        public int grossPower() {
            return baseCost + geometryCost + sourceCost + liftCost + floatCost + featureCost;
        }
    }

    public record Overdrive(double geometryRatio, double liftRatio, double floatRatio) {
        public double maximumRatio() {
            return Math.max(geometryRatio, Math.max(liftRatio, floatRatio));
        }

        public boolean active() {
            return maximumRatio() > 1.0001D;
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

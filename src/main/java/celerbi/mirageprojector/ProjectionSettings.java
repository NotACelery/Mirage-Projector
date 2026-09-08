package celerbi.mirageprojector;

import net.minecraft.util.Mth;

public record ProjectionSettings(
        String imageId,
        int imageWidth,
        int imageHeight,
        String backImageId,
        int backImageWidth,
        int backImageHeight,
        SourceMode sourceMode,
        int scalePixels,
        int liftPixels,
        boolean rotationEnabled,
        int rotationPeriodTicks,
        boolean clockwise,
        float rotationOffsetDegrees,
        boolean floatingEnabled,
        FloatMode floatMode,
        int floatAmplitudePixels,
        int floatCycleTicks,
        int floatIntervalDegrees,
        BackFaceMode backFaceMode,
        boolean flipVertical,
        boolean debugChassisOverride
) {
    public static final int DEBUG_MIN_SCALE_PIXELS = 2;
    public static final int DEBUG_MAX_SCALE_PIXELS = 160;
    public static final int DEBUG_MAX_LIFT_PIXELS = 160;
    public static final int DEBUG_MAX_FLOAT_PIXELS = 32;

    public static final ProjectionSettings DEFAULT = new ProjectionSettings(
            "", 0, 0,
            "", 0, 0,
            SourceMode.IMAGE,
            10,
            1,
            true,
            80,
            true,
            0.0F,
            true,
            FloatMode.TIME,
            1,
            40,
            90,
            BackFaceMode.MIRRORED,
            false,
            false
    );

    public ProjectionSettings sanitized() {
        String safeImageId = sanitizeAssetId(imageId);
        String safeBackImageId = sanitizeAssetId(backImageId);

        return new ProjectionSettings(
                safeImageId,
                Mth.clamp(imageWidth, 0, 8192),
                Mth.clamp(imageHeight, 0, 8192),
                safeBackImageId,
                Mth.clamp(backImageWidth, 0, 8192),
                Mth.clamp(backImageHeight, 0, 8192),
                sourceMode == null ? SourceMode.IMAGE : sourceMode,
                Mth.clamp(scalePixels, DEBUG_MIN_SCALE_PIXELS, DEBUG_MAX_SCALE_PIXELS),
                Mth.clamp(liftPixels, 0, DEBUG_MAX_LIFT_PIXELS),
                rotationEnabled,
                Mth.clamp(rotationPeriodTicks, 5, 20 * 60),
                clockwise,
                Mth.wrapDegrees(rotationOffsetDegrees),
                floatingEnabled,
                floatMode == null ? FloatMode.TIME : floatMode,
                Mth.clamp(floatAmplitudePixels, 0, DEBUG_MAX_FLOAT_PIXELS),
                Mth.clamp(floatCycleTicks, 5, 20 * 60),
                Mth.clamp(floatIntervalDegrees, 1, 360),
                backFaceMode == null ? BackFaceMode.MIRRORED : backFaceMode,
                flipVertical,
                debugChassisOverride
        );
    }

    private static String sanitizeAssetId(String id) {
        String safe = id == null ? "" : id.trim();
        if (safe.isBlank()) {
            return "";
        }
        return ProjectionAssetRules.isValidAssetId(safe) ? safe : "";
    }

    public boolean hasImage() {
        return !imageId.isBlank() && imageWidth > 0 && imageHeight > 0;
    }

    public boolean hasBackImage() {
        return !backImageId.isBlank() && backImageWidth > 0 && backImageHeight > 0;
    }

    public ProjectionSettings withImage(String id, int width, int height) {
        return new ProjectionSettings(
                id, width, height,
                backImageId, backImageWidth, backImageHeight,
                sourceMode,
                scalePixels, liftPixels,
                rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, debugChassisOverride
        ).sanitized();
    }

    public ProjectionSettings withBackImage(String id, int width, int height) {
        return new ProjectionSettings(
                imageId, imageWidth, imageHeight,
                id, width, height,
                sourceMode,
                scalePixels, liftPixels,
                rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, debugChassisOverride
        ).sanitized();
    }

    public ProjectionSettings withDebugChassisOverride(boolean enabled) {
        return new ProjectionSettings(
                imageId, imageWidth, imageHeight,
                backImageId, backImageWidth, backImageHeight,
                sourceMode,
                scalePixels, liftPixels,
                rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, enabled
        ).sanitized();
    }

    public enum SourceMode {
        IMAGE,
        ITEM;

        public static SourceMode fromOrdinal(int ordinal) {
            SourceMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : IMAGE;
        }
    }

    public enum FloatMode {
        TIME,
        ROTATION_SYNCED;

        public static FloatMode fromOrdinal(int ordinal) {
            FloatMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : TIME;
        }
    }

    public enum BackFaceMode {
        MIRRORED,
        READABLE,
        INDEPENDENT;

        public static BackFaceMode fromOrdinal(int ordinal) {
            BackFaceMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MIRRORED;
        }
    }
}

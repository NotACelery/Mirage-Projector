package celerbi.mirageprojector;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;

public record ProjectionSettings(
        String imageId,
        int imageWidth,
        int imageHeight,
        String backImageId,
        int backImageWidth,
        int backImageHeight,
        String eastImageId,
        int eastImageWidth,
        int eastImageHeight,
        String westImageId,
        int westImageWidth,
        int westImageHeight,
        SourceMode sourceMode,
        ImageLayoutMode imageLayoutMode,
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
        boolean fullbright,
        int opacityPercent,
        int tintRgb,
        boolean scanlines,
        boolean debugChassisOverride
) {
    public static final int DEBUG_MIN_SCALE_PIXELS = 2;

    public static final int DEBUG_MAX_SCALE_PIXELS = 512;
    public static final int DEBUG_MAX_LIFT_PIXELS = 512;
    public static final int DEBUG_MAX_FLOAT_PIXELS = 128;

    public static final ProjectionSettings DEFAULT = new ProjectionSettings(
            "", 0, 0,
            "", 0, 0,
            "", 0, 0,
            "", 0, 0,
            SourceMode.IMAGE,
            ImageLayoutMode.SINGLE,
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
            BackFaceMode.FRONT,
            false,
            true,
            100,
            0xFFFFFF,
            false,
            false
    );

    public ProjectionSettings sanitized() {
        String safeImageId = sanitizeAssetId(imageId);
        String safeBackImageId = sanitizeAssetId(backImageId);
        String safeEastImageId = sanitizeAssetId(eastImageId);
        String safeWestImageId = sanitizeAssetId(westImageId);

        return new ProjectionSettings(
                safeImageId,
                Mth.clamp(imageWidth, 0, 8192),
                Mth.clamp(imageHeight, 0, 8192),
                safeBackImageId,
                Mth.clamp(backImageWidth, 0, 8192),
                Mth.clamp(backImageHeight, 0, 8192),
                safeEastImageId,
                Mth.clamp(eastImageWidth, 0, 8192),
                Mth.clamp(eastImageHeight, 0, 8192),
                safeWestImageId,
                Mth.clamp(westImageWidth, 0, 8192),
                Mth.clamp(westImageHeight, 0, 8192),
                sourceMode == null ? SourceMode.IMAGE : sourceMode,
                imageLayoutMode == null ? ImageLayoutMode.SINGLE : imageLayoutMode,
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
                backFaceMode == null ? BackFaceMode.FRONT : backFaceMode,
                flipVertical,
                fullbright,
                Mth.clamp(opacityPercent, 10, 100),
                tintRgb & 0xFFFFFF,
                scanlines,
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
        return hasAsset(imageId, imageWidth, imageHeight);
    }

    public boolean hasBackImage() {
        return hasAsset(backImageId, backImageWidth, backImageHeight);
    }

    public boolean hasEastImage() {
        return hasAsset(eastImageId, eastImageWidth, eastImageHeight);
    }

    public boolean hasWestImage() {
        return hasAsset(westImageId, westImageWidth, westImageHeight);
    }

    public boolean hasAnyImage() {
        return hasImage() || hasBackImage() || hasEastImage() || hasWestImage();
    }

    private static boolean hasAsset(String id, int width, int height) {
        return id != null && !id.isBlank() && width > 0 && height > 0;
    }

    public int transparencyPercent() {
        return 100 - opacityPercent;
    }

    public float opacity() {
        return opacityPercent / 100.0F;
    }

    public float tintRed() {
        return ((tintRgb >> 16) & 0xFF) / 255.0F;
    }

    public float tintGreen() {
        return ((tintRgb >> 8) & 0xFF) / 255.0F;
    }

    public float tintBlue() {
        return (tintRgb & 0xFF) / 255.0F;
    }

    public ProjectionSettings withImage(String id, int width, int height) {
        return copy(id, width, height, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride);
    }

    public ProjectionSettings withBackImage(String id, int width, int height) {
        return copy(imageId, imageWidth, imageHeight, id, width, height,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride);
    }

    public ProjectionSettings withDebugChassisOverride(boolean enabled) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, enabled);
    }

    public ProjectionSettings withSourceMode(SourceMode mode) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                mode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride);
    }

    public ProjectionSettings withImageWorkspace(
            String frontId, int frontWidth, int frontHeight,
            String backId, int backWidth, int backHeight,
            String eastId, int eastWidth, int eastHeight,
            String westId, int westWidth, int westHeight,
            ImageLayoutMode newImageLayoutMode, BackFaceMode newBackFaceMode, boolean newFlipVertical, boolean newScanlines
    ) {
        return copy(frontId, frontWidth, frontHeight, backId, backWidth, backHeight,
                eastId, eastWidth, eastHeight, westId, westWidth, westHeight,
                SourceMode.IMAGE, newImageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, newBackFaceMode, newFlipVertical, fullbright, opacityPercent, tintRgb,
                newScanlines, debugChassisOverride);
    }

    public ProjectionSettings withImageLayoutMode(ImageLayoutMode mode) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, mode == null ? ImageLayoutMode.SINGLE : mode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride);
    }

    public ProjectionSettings withScalePixels(int value) {
        return withPresentation(value, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks, floatIntervalDegrees,
                fullbright, opacityPercent, tintRgb, debugChassisOverride);
    }

    public ProjectionSettings withLiftPixels(int value) {
        return withPresentation(scalePixels, value, rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks, floatIntervalDegrees,
                fullbright, opacityPercent, tintRgb, debugChassisOverride);
    }

    public ProjectionSettings withFloatAmplitudePixels(int value) {
        return withPresentation(scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, value, floatCycleTicks, floatIntervalDegrees,
                fullbright, opacityPercent, tintRgb, debugChassisOverride);
    }

    public ProjectionSettings withPresentation(
            int newScalePixels,
            int newLiftPixels,
            boolean newRotationEnabled,
            int newRotationPeriodTicks,
            boolean newClockwise,
            float newRotationOffsetDegrees,
            boolean newFloatingEnabled,
            FloatMode newFloatMode,
            int newFloatAmplitudePixels,
            int newFloatCycleTicks,
            int newFloatIntervalDegrees,
            boolean newFullbright,
            int newOpacityPercent,
            int newTintRgb,
            boolean newDebugChassisOverride
    ) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, newScalePixels, newLiftPixels, newRotationEnabled, newRotationPeriodTicks, newClockwise,
                newRotationOffsetDegrees, newFloatingEnabled, newFloatMode, newFloatAmplitudePixels, newFloatCycleTicks,
                newFloatIntervalDegrees, backFaceMode, flipVertical, newFullbright, newOpacityPercent, newTintRgb,
                scanlines, newDebugChassisOverride);
    }

    private static ProjectionSettings copy(
            String imageId, int imageWidth, int imageHeight,
            String backImageId, int backImageWidth, int backImageHeight,
            String eastImageId, int eastImageWidth, int eastImageHeight,
            String westImageId, int westImageWidth, int westImageHeight,
            SourceMode sourceMode, ImageLayoutMode imageLayoutMode, int scalePixels, int liftPixels,
            boolean rotationEnabled, int rotationPeriodTicks, boolean clockwise, float rotationOffsetDegrees,
            boolean floatingEnabled, FloatMode floatMode, int floatAmplitudePixels, int floatCycleTicks,
            int floatIntervalDegrees, BackFaceMode backFaceMode, boolean flipVertical,
            boolean fullbright, int opacityPercent, int tintRgb, boolean scanlines,
            boolean debugChassisOverride
    ) {
        return new ProjectionSettings(
                imageId, imageWidth, imageHeight,
                backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight,
                westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels,
                rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical,
                fullbright, opacityPercent, tintRgb, scanlines, debugChassisOverride
        ).sanitized();
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        ProjectionSettings s = sanitized();
        writeAsset(buffer, s.imageId(), s.imageWidth(), s.imageHeight());
        writeAsset(buffer, s.backImageId(), s.backImageWidth(), s.backImageHeight());
        writeAsset(buffer, s.eastImageId(), s.eastImageWidth(), s.eastImageHeight());
        writeAsset(buffer, s.westImageId(), s.westImageWidth(), s.westImageHeight());
        buffer.writeVarInt(s.sourceMode().ordinal());
        buffer.writeVarInt(s.imageLayoutMode().ordinal());
        buffer.writeVarInt(s.scalePixels());
        buffer.writeVarInt(s.liftPixels());
        buffer.writeBoolean(s.rotationEnabled());
        buffer.writeVarInt(s.rotationPeriodTicks());
        buffer.writeBoolean(s.clockwise());
        buffer.writeFloat(s.rotationOffsetDegrees());
        buffer.writeBoolean(s.floatingEnabled());
        buffer.writeVarInt(s.floatMode().ordinal());
        buffer.writeVarInt(s.floatAmplitudePixels());
        buffer.writeVarInt(s.floatCycleTicks());
        buffer.writeVarInt(s.floatIntervalDegrees());
        buffer.writeVarInt(s.backFaceMode().ordinal());
        buffer.writeBoolean(s.flipVertical());
        buffer.writeBoolean(s.fullbright());
        buffer.writeVarInt(s.opacityPercent());
        buffer.writeInt(s.tintRgb());
        buffer.writeBoolean(s.scanlines());
        buffer.writeBoolean(s.debugChassisOverride());
    }

    public static ProjectionSettings read(RegistryFriendlyByteBuf buffer) {
        Asset front = readAsset(buffer);
        Asset back = readAsset(buffer);
        Asset east = readAsset(buffer);
        Asset west = readAsset(buffer);
        return new ProjectionSettings(
                front.id(), front.width(), front.height(),
                back.id(), back.width(), back.height(),
                east.id(), east.width(), east.height(),
                west.id(), west.width(), west.height(),
                SourceMode.fromOrdinal(buffer.readVarInt()),
                ImageLayoutMode.fromOrdinal(buffer.readVarInt()),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readBoolean(),
                FloatMode.fromOrdinal(buffer.readVarInt()),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                BackFaceMode.fromOrdinal(buffer.readVarInt()),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        ).sanitized();
    }

    public void save(CompoundTag tag) {
        ProjectionSettings s = sanitized();
        tag.putString("ImageId", s.imageId());
        tag.putInt("ImageWidth", s.imageWidth());
        tag.putInt("ImageHeight", s.imageHeight());
        tag.putString("BackImageId", s.backImageId());
        tag.putInt("BackImageWidth", s.backImageWidth());
        tag.putInt("BackImageHeight", s.backImageHeight());
        tag.putString("EastImageId", s.eastImageId());
        tag.putInt("EastImageWidth", s.eastImageWidth());
        tag.putInt("EastImageHeight", s.eastImageHeight());
        tag.putString("WestImageId", s.westImageId());
        tag.putInt("WestImageWidth", s.westImageWidth());
        tag.putInt("WestImageHeight", s.westImageHeight());
        tag.putInt("SourceMode", s.sourceMode().ordinal());
        tag.putInt("ImageLayoutMode", s.imageLayoutMode().ordinal());
        tag.putInt("ScalePixels", s.scalePixels());
        tag.putInt("LiftPixels", s.liftPixels());
        tag.putBoolean("RotationEnabled", s.rotationEnabled());
        tag.putInt("RotationPeriodTicks", s.rotationPeriodTicks());
        tag.putBoolean("Clockwise", s.clockwise());
        tag.putFloat("RotationOffsetDegrees", s.rotationOffsetDegrees());
        tag.putBoolean("FloatingEnabled", s.floatingEnabled());
        tag.putInt("FloatMode", s.floatMode().ordinal());
        tag.putInt("FloatAmplitudePixels", s.floatAmplitudePixels());
        tag.putInt("FloatCycleTicks", s.floatCycleTicks());
        tag.putInt("FloatIntervalDegrees", s.floatIntervalDegrees());
        tag.putInt("BackFaceMode", s.backFaceMode().ordinal());
        tag.putBoolean("FlipVertical", s.flipVertical());
        tag.putBoolean("Fullbright", s.fullbright());
        tag.putInt("OpacityPercent", s.opacityPercent());
        tag.putInt("TintRgb", s.tintRgb());
        tag.putBoolean("Scanlines", s.scanlines());
        tag.putBoolean("DebugChassisOverride", s.debugChassisOverride());
    }

    public static ProjectionSettings load(CompoundTag tag) {
        ProjectionSettings d = DEFAULT;
        return new ProjectionSettings(
                tag.contains("ImageId") ? tag.getString("ImageId") : d.imageId(),
                tag.contains("ImageWidth") ? tag.getInt("ImageWidth") : d.imageWidth(),
                tag.contains("ImageHeight") ? tag.getInt("ImageHeight") : d.imageHeight(),
                tag.contains("BackImageId") ? tag.getString("BackImageId") : d.backImageId(),
                tag.contains("BackImageWidth") ? tag.getInt("BackImageWidth") : d.backImageWidth(),
                tag.contains("BackImageHeight") ? tag.getInt("BackImageHeight") : d.backImageHeight(),
                tag.contains("EastImageId") ? tag.getString("EastImageId") : d.eastImageId(),
                tag.contains("EastImageWidth") ? tag.getInt("EastImageWidth") : d.eastImageWidth(),
                tag.contains("EastImageHeight") ? tag.getInt("EastImageHeight") : d.eastImageHeight(),
                tag.contains("WestImageId") ? tag.getString("WestImageId") : d.westImageId(),
                tag.contains("WestImageWidth") ? tag.getInt("WestImageWidth") : d.westImageWidth(),
                tag.contains("WestImageHeight") ? tag.getInt("WestImageHeight") : d.westImageHeight(),
                SourceMode.fromOrdinal(tag.contains("SourceMode") ? tag.getInt("SourceMode") : d.sourceMode().ordinal()),
                ImageLayoutMode.fromOrdinal(tag.contains("ImageLayoutMode") ? tag.getInt("ImageLayoutMode") : d.imageLayoutMode().ordinal()),
                tag.contains("ScalePixels") ? tag.getInt("ScalePixels") : d.scalePixels(),
                tag.contains("LiftPixels") ? tag.getInt("LiftPixels") : d.liftPixels(),
                tag.contains("RotationEnabled") ? tag.getBoolean("RotationEnabled") : d.rotationEnabled(),
                tag.contains("RotationPeriodTicks") ? tag.getInt("RotationPeriodTicks") : d.rotationPeriodTicks(),
                tag.contains("Clockwise") ? tag.getBoolean("Clockwise") : d.clockwise(),
                tag.contains("RotationOffsetDegrees") ? tag.getFloat("RotationOffsetDegrees") : d.rotationOffsetDegrees(),
                tag.contains("FloatingEnabled") ? tag.getBoolean("FloatingEnabled") : d.floatingEnabled(),
                FloatMode.fromOrdinal(tag.contains("FloatMode") ? tag.getInt("FloatMode") : d.floatMode().ordinal()),
                tag.contains("FloatAmplitudePixels") ? tag.getInt("FloatAmplitudePixels") : d.floatAmplitudePixels(),
                tag.contains("FloatCycleTicks") ? tag.getInt("FloatCycleTicks") : d.floatCycleTicks(),
                tag.contains("FloatIntervalDegrees") ? tag.getInt("FloatIntervalDegrees") : d.floatIntervalDegrees(),
                BackFaceMode.fromOrdinal(tag.contains("BackFaceMode") ? tag.getInt("BackFaceMode") : d.backFaceMode().ordinal()),
                tag.contains("FlipVertical") ? tag.getBoolean("FlipVertical") : d.flipVertical(),
                tag.contains("Fullbright") ? tag.getBoolean("Fullbright") : d.fullbright(),
                tag.contains("OpacityPercent") ? tag.getInt("OpacityPercent") : d.opacityPercent(),
                tag.contains("TintRgb") ? tag.getInt("TintRgb") : d.tintRgb(),
                tag.contains("Scanlines") ? tag.getBoolean("Scanlines") : d.scanlines(),
                tag.contains("DebugChassisOverride") ? tag.getBoolean("DebugChassisOverride") : d.debugChassisOverride()
        ).sanitized();
    }

    private static void writeAsset(RegistryFriendlyByteBuf buffer, String id, int width, int height) {
        buffer.writeUtf(id, 128);
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
    }

    private static Asset readAsset(RegistryFriendlyByteBuf buffer) {
        return new Asset(buffer.readUtf(128), buffer.readVarInt(), buffer.readVarInt());
    }

    private record Asset(String id, int width, int height) {
    }

    public enum SourceMode {
        IMAGE,
        ITEM,
        ENTITY,
        BANNER;

        public static SourceMode fromOrdinal(int ordinal) {
            SourceMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : IMAGE;
        }
    }

    public enum ImageLayoutMode {
        SINGLE,
        MULTI;

        public static ImageLayoutMode fromOrdinal(int ordinal) {
            ImageLayoutMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SINGLE;
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
        INDEPENDENT,
        FRONT,
        BACK;

        public static BackFaceMode fromOrdinal(int ordinal) {
            BackFaceMode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FRONT;
        }
    }
}

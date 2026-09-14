package celerbi.mirageprojector;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
        float orientationX,
        float orientationY,
        float orientationZ,
        float orientationW,
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
        boolean debugChassisOverride,
        int horizontalOffsetPixels, // legacy v3 wire/NBT slot; always sanitized to zero
        int verticalOffsetPixels,   // legacy v3 wire/NBT slot; positive values migrate into Lift, then zero
        int distanceOffsetPixels
) {
    public static final int SERIALIZATION_VERSION = 3;

    public static final int DEBUG_MIN_SCALE_PIXELS = 2;

    public static final int DEBUG_MAX_SCALE_PIXELS = 512;
    public static final int DEBUG_MAX_LIFT_PIXELS = 512;
    public static final int DEBUG_MAX_FLOAT_PIXELS = 128;
    public static final int MAX_PLACEMENT_OFFSET_PIXELS = 256;
    public static final int MAX_TILT_DEGREES = 90;

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
            0.0F,
            0.0F,
            0.0F,
            1.0F,
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
            false,
            0,
            0,
            0
    );

    public ProjectionSettings sanitized() {
        String safeImageId = sanitizeAssetId(imageId);
        String safeBackImageId = sanitizeAssetId(backImageId);
        String safeEastImageId = sanitizeAssetId(eastImageId);
        String safeWestImageId = sanitizeAssetId(westImageId);
        ProjectionTransform.Orientation safeOrientation = ProjectionTransform.normalizeOrientation(
                orientationX, orientationY, orientationZ, orientationW
        );

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
                Mth.clamp(liftPixels + Math.max(0, verticalOffsetPixels), 0, DEBUG_MAX_LIFT_PIXELS),
                rotationEnabled,
                Mth.clamp(rotationPeriodTicks, 5, 20 * 60),
                clockwise,
                Mth.wrapDegrees(rotationOffsetDegrees),
                safeOrientation.x(),
                safeOrientation.y(),
                safeOrientation.z(),
                safeOrientation.w(),
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
                debugChassisOverride,
                0,
                0,
                Mth.clamp(distanceOffsetPixels, 0, PrismProjectionSpacing.MAX_DISTANCE_PIXELS)
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

    public ProjectionTransform transform() {
        return ProjectionTransform.fromSettings(this);
    }

    public ProjectionSettings withTransform(ProjectionTransform transform) {
        ProjectionTransform safe = transform == null ? ProjectionTransform.fromSettings(this) : transform.sanitized();
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, safe.scalePixels(), safe.liftPixels(), safe.rotationEnabled(),
                safe.rotationPeriodTicks(), safe.clockwise(), safe.rotationOffsetDegrees(),
                safe.orientationX(), safe.orientationY(), safe.orientationZ(), safe.orientationW(),
                safe.floatingEnabled(), safe.floatMode(), safe.floatAmplitudePixels(), safe.floatCycleTicks(),
                safe.floatIntervalDegrees(), backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride, 0, 0, safe.distanceOffsetPixels());
    }

    public ProjectionSettings withPlacement(int prismDistancePixels, float tiltDegrees) {
        ProjectionTransform.Orientation orientation = ProjectionTransform.orientationFromTiltDegrees(tiltDegrees);
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientation.x(), orientation.y(), orientation.z(), orientation.w(),
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks, floatIntervalDegrees,
                backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb, scanlines, debugChassisOverride,
                0, 0, prismDistancePixels);
    }

    public float tiltDegrees() {
        return ProjectionTransform.tiltDegrees(orientationX, orientationY, orientationZ, orientationW);
    }

    public ProjectionSettings withImage(String id, int width, int height) {
        return copy(id, width, height, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
    }

    public ProjectionSettings withBackImage(String id, int width, int height) {
        return copy(imageId, imageWidth, imageHeight, id, width, height,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
    }

    public ProjectionSettings withDebugChassisOverride(boolean enabled) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, enabled, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
    }

    public ProjectionSettings withSourceMode(SourceMode mode) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                mode, imageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
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
                sourceMode, newImageLayoutMode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, newBackFaceMode, newFlipVertical, fullbright, opacityPercent, tintRgb,
                newScanlines, debugChassisOverride, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
    }

    public ProjectionSettings withImageLayoutMode(ImageLayoutMode mode) {
        return copy(imageId, imageWidth, imageHeight, backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight, westImageId, westImageWidth, westImageHeight,
                sourceMode, mode == null ? ImageLayoutMode.SINGLE : mode, scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise,
                rotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical, fullbright, opacityPercent, tintRgb,
                scanlines, debugChassisOverride, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
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
                newRotationOffsetDegrees, orientationX, orientationY, orientationZ, orientationW, newFloatingEnabled, newFloatMode, newFloatAmplitudePixels, newFloatCycleTicks,
                newFloatIntervalDegrees, backFaceMode, flipVertical, newFullbright, newOpacityPercent, newTintRgb,
                scanlines, newDebugChassisOverride, horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels);
    }

    private static ProjectionSettings copy(
            String imageId, int imageWidth, int imageHeight,
            String backImageId, int backImageWidth, int backImageHeight,
            String eastImageId, int eastImageWidth, int eastImageHeight,
            String westImageId, int westImageWidth, int westImageHeight,
            SourceMode sourceMode, ImageLayoutMode imageLayoutMode, int scalePixels, int liftPixels,
            boolean rotationEnabled, int rotationPeriodTicks, boolean clockwise, float rotationOffsetDegrees,
            float orientationX, float orientationY, float orientationZ, float orientationW,
            boolean floatingEnabled, FloatMode floatMode, int floatAmplitudePixels, int floatCycleTicks,
            int floatIntervalDegrees, BackFaceMode backFaceMode, boolean flipVertical,
            boolean fullbright, int opacityPercent, int tintRgb, boolean scanlines,
            boolean debugChassisOverride, int horizontalOffsetPixels, int verticalOffsetPixels, int distanceOffsetPixels
    ) {
        return new ProjectionSettings(
                imageId, imageWidth, imageHeight,
                backImageId, backImageWidth, backImageHeight,
                eastImageId, eastImageWidth, eastImageHeight,
                westImageId, westImageWidth, westImageHeight,
                sourceMode, imageLayoutMode, scalePixels, liftPixels,
                rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                orientationX, orientationY, orientationZ, orientationW,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks,
                floatIntervalDegrees, backFaceMode, flipVertical,
                fullbright, opacityPercent, tintRgb, scanlines, debugChassisOverride,
                horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels
        ).sanitized();
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        ProjectionSettings s = sanitized();
        buffer.writeVarInt(SERIALIZATION_VERSION);
        writeAsset(buffer, s.imageId(), s.imageWidth(), s.imageHeight());
        writeAsset(buffer, s.backImageId(), s.backImageWidth(), s.backImageHeight());
        writeAsset(buffer, s.eastImageId(), s.eastImageWidth(), s.eastImageHeight());
        writeAsset(buffer, s.westImageId(), s.westImageWidth(), s.westImageHeight());
        buffer.writeUtf(s.sourceMode().serializedName(), 128);
        buffer.writeVarInt(s.imageLayoutMode().ordinal());
        buffer.writeVarInt(s.scalePixels());
        buffer.writeVarInt(s.liftPixels());
        buffer.writeBoolean(s.rotationEnabled());
        buffer.writeVarInt(s.rotationPeriodTicks());
        buffer.writeBoolean(s.clockwise());
        buffer.writeFloat(s.rotationOffsetDegrees());
        buffer.writeFloat(s.orientationX());
        buffer.writeFloat(s.orientationY());
        buffer.writeFloat(s.orientationZ());
        buffer.writeFloat(s.orientationW());
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
        buffer.writeVarInt(s.horizontalOffsetPixels());
        buffer.writeVarInt(s.verticalOffsetPixels());
        buffer.writeVarInt(s.distanceOffsetPixels());
    }

    public static ProjectionSettings read(RegistryFriendlyByteBuf buffer) {
        int serializationVersion = buffer.readVarInt();
        if (serializationVersion < 2 || serializationVersion > SERIALIZATION_VERSION) {
            throw new IllegalArgumentException("Unsupported ProjectionSettings network format: " + serializationVersion);
        }
        Asset front = readAsset(buffer);
        Asset back = readAsset(buffer);
        Asset east = readAsset(buffer);
        Asset west = readAsset(buffer);
        return new ProjectionSettings(
                front.id(), front.width(), front.height(),
                back.id(), back.width(), back.height(),
                east.id(), east.width(), east.height(),
                west.id(), west.width(), west.height(),
                SourceMode.parse(buffer.readUtf(128)),
                ImageLayoutMode.fromOrdinal(buffer.readVarInt()),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
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
                buffer.readBoolean(),
                serializationVersion >= 3 ? buffer.readVarInt() : 0,
                serializationVersion >= 3 ? buffer.readVarInt() : 0,
                serializationVersion >= 3 ? buffer.readVarInt() : 0
        ).sanitized();
    }

    public void save(CompoundTag tag) {
        ProjectionSettings s = sanitized();
        tag.putInt("ProjectionSettingsVersion", SERIALIZATION_VERSION);
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
        tag.putString("SourceId", s.sourceMode().serializedName());
        int legacySourceOrdinal = s.sourceMode().legacyOrdinal();
        if (legacySourceOrdinal >= 0) {
            tag.putInt("SourceMode", legacySourceOrdinal);
        }
        tag.putInt("ImageLayoutMode", s.imageLayoutMode().ordinal());
        tag.putInt("ScalePixels", s.scalePixels());
        tag.putInt("LiftPixels", s.liftPixels());
        tag.putBoolean("RotationEnabled", s.rotationEnabled());
        tag.putInt("RotationPeriodTicks", s.rotationPeriodTicks());
        tag.putBoolean("Clockwise", s.clockwise());
        tag.putFloat("RotationOffsetDegrees", s.rotationOffsetDegrees());
        tag.putFloat("OrientationX", s.orientationX());
        tag.putFloat("OrientationY", s.orientationY());
        tag.putFloat("OrientationZ", s.orientationZ());
        tag.putFloat("OrientationW", s.orientationW());
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
        tag.putInt("HorizontalOffsetPixels", s.horizontalOffsetPixels());
        tag.putInt("VerticalOffsetPixels", s.verticalOffsetPixels());
        tag.putInt("DistanceOffsetPixels", s.distanceOffsetPixels());
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
                loadSourceMode(tag, d.sourceMode()),
                ImageLayoutMode.fromOrdinal(tag.contains("ImageLayoutMode") ? tag.getInt("ImageLayoutMode") : d.imageLayoutMode().ordinal()),
                tag.contains("ScalePixels") ? tag.getInt("ScalePixels") : d.scalePixels(),
                tag.contains("LiftPixels") ? tag.getInt("LiftPixels") : d.liftPixels(),
                tag.contains("RotationEnabled") ? tag.getBoolean("RotationEnabled") : d.rotationEnabled(),
                tag.contains("RotationPeriodTicks") ? tag.getInt("RotationPeriodTicks") : d.rotationPeriodTicks(),
                tag.contains("Clockwise") ? tag.getBoolean("Clockwise") : d.clockwise(),
                tag.contains("RotationOffsetDegrees") ? tag.getFloat("RotationOffsetDegrees") : d.rotationOffsetDegrees(),
                tag.contains("OrientationX") ? tag.getFloat("OrientationX") : d.orientationX(),
                tag.contains("OrientationY") ? tag.getFloat("OrientationY") : d.orientationY(),
                tag.contains("OrientationZ") ? tag.getFloat("OrientationZ") : d.orientationZ(),
                tag.contains("OrientationW") ? tag.getFloat("OrientationW") : d.orientationW(),
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
                tag.contains("DebugChassisOverride") ? tag.getBoolean("DebugChassisOverride") : d.debugChassisOverride(),
                tag.contains("HorizontalOffsetPixels") ? tag.getInt("HorizontalOffsetPixels") : d.horizontalOffsetPixels(),
                tag.contains("VerticalOffsetPixels") ? tag.getInt("VerticalOffsetPixels") : d.verticalOffsetPixels(),
                tag.contains("DistanceOffsetPixels") ? tag.getInt("DistanceOffsetPixels") : d.distanceOffsetPixels()
        ).sanitized();
    }

    private static SourceMode loadSourceMode(CompoundTag tag, SourceMode fallback) {
        if (tag.contains("SourceId")) {
            return SourceMode.parseOrDefault(tag.getString("SourceId"), fallback);
        }
        if (tag.contains("SourceMode")) {
            return SourceMode.fromLegacyOrdinal(tag.getInt("SourceMode"));
        }
        return fallback == null ? SourceMode.IMAGE : fallback;
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

    /**
     * Extensible projection-source identity.
     *
     * <p>This intentionally keeps the historical SourceMode type name so the existing 1.0
     * call sites stay compact, but it is no longer a closed Java enum. Values are interned
     * by stable namespaced ResourceLocation, allowing addons/future versions to introduce
     * new sources without changing save/network ordinal layouts.</p>
     */
    public static final class SourceMode {
        private static final Map<ResourceLocation, SourceMode> INTERN = new ConcurrentHashMap<>();

        public static final SourceMode IMAGE = of(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "image"));
        public static final SourceMode ITEM = of(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "item"));
        public static final SourceMode ENTITY = of(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "entity"));
        public static final SourceMode BANNER = of(ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "banner"));

        private final ResourceLocation id;

        private SourceMode(ResourceLocation id) {
            this.id = id;
        }

        public static SourceMode of(ResourceLocation id) {
            Objects.requireNonNull(id, "Projection source id");
            return INTERN.computeIfAbsent(id, SourceMode::new);
        }

        public static SourceMode parse(String serialized) {
            return parseOrDefault(serialized, IMAGE);
        }

        public static SourceMode parseOrDefault(String serialized, SourceMode fallback) {
            ResourceLocation parsed = serialized == null ? null : ResourceLocation.tryParse(serialized.trim());
            return parsed == null ? (fallback == null ? IMAGE : fallback) : of(parsed);
        }

        public static SourceMode fromLegacyOrdinal(int ordinal) {
            return switch (ordinal) {
                case 1 -> ITEM;
                case 2 -> ENTITY;
                case 3 -> BANNER;
                default -> IMAGE;
            };
        }

        public int legacyOrdinal() {
            if (this == IMAGE) return 0;
            if (this == ITEM) return 1;
            if (this == ENTITY) return 2;
            if (this == BANNER) return 3;
            return -1;
        }

        public ResourceLocation id() {
            return id;
        }

        public String serializedName() {
            return id.toString();
        }

        public boolean isBuiltin() {
            return legacyOrdinal() >= 0;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof SourceMode that && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return serializedName();
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

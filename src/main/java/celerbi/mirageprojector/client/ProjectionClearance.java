package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.entity.HumanoidPosePreset;
import celerbi.mirageprojector.entity.EntityProjectionState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class ProjectionClearance {
    private static final double PIXEL = 1.0D / 16.0D;
    private static final int MAX_PREVIEW_BLOCKS = 128;

    private ProjectionClearance() {
    }

    public static Result scan(
            Level level,
            BlockPos projectorPos,
            ProjectionSettings settings,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem
    ) {
        return scan(level, projectorPos, settings, chassis, hasProjectedItem, null);
    }

    public static Result scan(
            Level level,
            BlockPos projectorPos,
            ProjectionSettings settings,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            HumanoidPosePreset humanoidPose
    ) {
        return scan(level, projectorPos, settings, chassis, hasProjectedItem, humanoidPose, null);
    }

    public static Result scan(
            Level level,
            BlockPos projectorPos,
            ProjectionSettings settings,
            ProjectionChassisProfile chassis,
            boolean hasProjectedItem,
            HumanoidPosePreset humanoidPose,
            EntityProjectionState entityState
    ) {
        if (level == null) {
            return Result.UNKNOWN;
        }

        ProjectionSettings s = settings.sanitized();
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        ProjectionPower.Dimensions dimensions = ProjectionPower.dimensions(s, hasProjectedItem, safeChassis);
        if (dimensions.empty()) {
            return Result.EMPTY;
        }

        double width = dimensions.widthPixels() * PIXEL;
        double height = dimensions.heightPixels() * PIXEL;
        if (s.sourceMode() == ProjectionSettings.SourceMode.ENTITY && entityState != null) {
            EntityProjectionBounds.Bounds bounds = EntityProjectionBounds.projected(entityState, s);
            width = Math.max(PIXEL, bounds.widthPixels() * PIXEL);
            height = Math.max(PIXEL, bounds.heightPixels() * PIXEL);
        } else if (s.sourceMode() == ProjectionSettings.SourceMode.ENTITY && humanoidPose != null) {
            double targetHeight = Math.max(PIXEL, s.scalePixels() * PIXEL);
            width = targetHeight * HumanoidPoseController.horizontalExtentMultiplier(humanoidPose);
            height = targetHeight * HumanoidPoseController.verticalExtentMultiplier(humanoidPose);
        }
        double centerX = projectorPos.getX() + 0.5D;
        double centerZ = projectorPos.getZ() + 0.5D;
        double bottom = projectorPos.getY()
                + safeChassis.physicalTopPixels() * PIXEL
                + s.liftPixels() * PIXEL;
        double minY = bottom - (s.floatingEnabled() ? s.floatAmplitudePixels() * PIXEL : 0.0D);
        double maxY = bottom + height;

        double halfX;
        double halfZ;
        boolean prism = safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (s.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || s.sourceMode() == ProjectionSettings.SourceMode.BANNER);
        if (s.sourceMode() == ProjectionSettings.SourceMode.ITEM
                || s.sourceMode() == ProjectionSettings.SourceMode.ENTITY) {
            double radius = Math.max(0.05D, width * 0.5D);
            halfX = radius;
            halfZ = radius;
        } else if (prism) {
            // Prism is an open four-sided square of quads. Static clearance follows
            // its current rotation; animated rotation reserves the complete sweep.
            double radius = Math.max(0.05D, width * 0.5D);
            if (s.rotationEnabled()) {
                double swept = radius * Math.sqrt(2.0D);
                halfX = swept;
                halfZ = swept;
            } else {
                double radians = Math.toRadians(s.rotationOffsetDegrees());
                double axisExtent = radius * (Math.abs(Math.cos(radians)) + Math.abs(Math.sin(radians)));
                halfX = axisExtent;
                halfZ = axisExtent;
            }
        } else if (s.rotationEnabled()) {
            // A rotating Plane sweeps around the Y axis.
            double radius = Math.max(0.05D, width * 0.5D);
            halfX = radius;
            halfZ = radius;
        } else {
            // A stationary 2D plane uses a thin oriented envelope to avoid false positives.
            double halfWidth = width * 0.5D;
            double radians = Math.toRadians(s.rotationOffsetDegrees());
            double thickness = 0.01D;
            halfX = Math.max(thickness,
                    Math.abs(Math.cos(radians)) * halfWidth + Math.abs(Math.sin(radians)) * thickness);
            halfZ = Math.max(thickness,
                    Math.abs(Math.sin(radians)) * halfWidth + Math.abs(Math.cos(radians)) * thickness);
        }

        AABB envelope = new AABB(
                centerX - halfX,
                minY,
                centerZ - halfZ,
                centerX + halfX,
                maxY,
                centerZ + halfZ
        );

        BlockPos min = BlockPos.containing(envelope.minX, envelope.minY, envelope.minZ);
        BlockPos max = BlockPos.containing(envelope.maxX, envelope.maxY, envelope.maxZ);

        int checked = 0;
        int blocked = 0;
        List<BlockPos> blockedPreview = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.equals(projectorPos)) {
                continue;
            }
            checked++;
            if (!level.getBlockState(pos).isAir()) {
                blocked++;
                if (blockedPreview.size() < MAX_PREVIEW_BLOCKS) {
                    blockedPreview.add(pos.immutable());
                }
            }
        }
        return new Result(blocked, checked, envelope, List.copyOf(blockedPreview));
    }

    /** Compatibility overload retained for Compact-only callers. */
    public static Result scan(Level level, BlockPos projectorPos, ProjectionSettings settings, boolean hasProjectedItem) {
        return scan(level, projectorPos, settings, ProjectionChassisProfile.COMPACT, hasProjectedItem);
    }

    public record Result(int blockedBlocks, int checkedBlocks, AABB envelope, List<BlockPos> blockedPreview) {
        public static final Result UNKNOWN = new Result(-1, 0, null, List.of());
        public static final Result EMPTY = new Result(0, 0, null, List.of());

        public boolean known() {
            return blockedBlocks >= 0;
        }

        public boolean clear() {
            return blockedBlocks == 0;
        }

        public boolean hasEnvelope() {
            return envelope != null;
        }
    }
}

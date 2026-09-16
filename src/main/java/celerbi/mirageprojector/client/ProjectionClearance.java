package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.PrismProjectionSpacing;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.HumanoidPosePreset;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

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
        if (safeChassis == ProjectionChassisProfile.WALL) {
            // The data-show validates only the real aspect-correct image rectangle against its
            // target wall. Generic hologram clearance would incorrectly inspect unused envelope.
            return Result.EMPTY;
        }
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
        double minY = bottom - (safeChassis.supportsFloating() && s.floatingEnabled() ? s.floatAmplitudePixels() * PIXEL : 0.0D);
        double tiltRadians = Math.toRadians(Math.abs(s.tiltDegrees()));
        boolean horizontalPlane = safeChassis.usesHorizontalPlaneFor(s.sourceMode());
        double verticalReach = horizontalPlane
                ? Math.max(width, height) * Math.abs(Math.sin(tiltRadians)) * 0.5D + 0.05D
                : height * Math.max(0.0D, Math.cos(tiltRadians));
        if (horizontalPlane) {
            minY -= verticalReach;
        }
        double maxY = bottom + verticalReach;

        double halfX;
        double halfZ;
        boolean prism = safeChassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (s.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || s.sourceMode() == ProjectionSettings.SourceMode.BANNER);
        double orientationDegrees = s.rotationOffsetDegrees()
                + placementFacingAngle(level, projectorPos, safeChassis, s);
        if (s.sourceMode() == ProjectionSettings.SourceMode.ITEM
                || s.sourceMode() == ProjectionSettings.SourceMode.ENTITY) {
            double radius = Math.max(0.05D, width * 0.5D);
            halfX = radius;
            halfZ = radius;
        } else if (prism) {
            double outwardTiltReach = s.tiltDegrees() > 0.0F
                    ? height * Math.sin(Math.toRadians(s.tiltDegrees()))
                    : 0.0D;
            double outerRadius = PrismProjectionSpacing.effectiveDistancePixels(s) * PIXEL
                    + width * 0.5D + outwardTiltReach;
            // The four faces form a carousel around the machine center. A circular/square envelope
            // is intentionally conservative and remains correct for every rotation angle.
            halfX = Math.max(0.05D, outerRadius);
            halfZ = Math.max(0.05D, outerRadius);
        } else if (horizontalPlane) {
            // A table plane occupies both horizontal axes. Rotation simply spins that rectangle on
            // the table, so a circular envelope is conservative and independent of yaw.
            double radius = Math.max(0.05D, Math.hypot(width, height) * 0.5D);
            halfX = radius;
            halfZ = radius;
        } else if (safeChassis.supportsRotation() && s.rotationEnabled()) {

            double radius = Math.max(0.05D, width * 0.5D);
            halfX = radius;
            halfZ = radius;
        } else {

            double halfWidth = width * 0.5D;
            double radians = Math.toRadians(orientationDegrees);
            double thickness = 0.01D;
            halfX = Math.max(thickness,
                    Math.abs(Math.cos(radians)) * halfWidth + Math.abs(Math.sin(radians)) * thickness);
            halfZ = Math.max(thickness,
                    Math.abs(Math.sin(radians)) * halfWidth + Math.abs(Math.cos(radians)) * thickness);
        }

        if (!prism && !horizontalPlane) {
            double tiltReach = height * Math.abs(Math.sin(Math.toRadians(s.tiltDegrees())));
            halfX += tiltReach;
            halfZ += tiltReach;
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

    private static double placementFacingAngle(
            Level level,
            BlockPos projectorPos,
            ProjectionChassisProfile chassis,
            ProjectionSettings settings
    ) {

        if (chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || settings.sourceMode() == ProjectionSettings.SourceMode.BANNER)) {
            return 0.0D;
        }
        return projectorFacingAngle(level, projectorPos);
    }

    private static double projectorFacingAngle(Level level, BlockPos projectorPos) {
        var state = level.getBlockState(projectorPos);
        if (!state.hasProperty(MirageProjectorBlock.FACING)) {
            return 0.0D;
        }
        return switch (state.getValue(MirageProjectorBlock.FACING)) {
            case SOUTH -> 0.0D;
            case EAST -> 90.0D;
            case NORTH -> 180.0D;
            case WEST -> 270.0D;
            default -> 0.0D;
        };
    }

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

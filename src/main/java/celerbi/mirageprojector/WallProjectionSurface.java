package celerbi.mirageprojector;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Resolves the real wall used by the Mirage Wall Projector (data-show chassis).
 *
 * <p>The important invariant is that validity is checked against the <em>actual image rectangle</em>
 * after aspect-ratio sizing, scale and X/Y offsets are applied. No square/nominal projector envelope
 * is used for wall regularity, so unused area around a tall or wide image may be irregular without
 * blocking the projection.</p>
 */
public final class WallProjectionSurface {
    public static final int MAX_SEARCH_BLOCKS = 32;
    public static final int LENS_CENTER_Y_PIXELS = 3;
    private static final double PIXEL = 1.0D / 16.0D;
    private static final double SURFACE_EPSILON = 0.002D;

    private WallProjectionSurface() {
    }

    public static Result resolve(
            Level level,
            BlockPos projectorPos,
            Direction facing,
            ProjectionSettings settings,
            ImageSourceBank.Asset image,
            ProjectionCoreProfile core
    ) {
        if (level == null || projectorPos == null || facing == null || facing.getAxis() == Direction.Axis.Y) {
            return Result.invalid(Failure.NO_WALL);
        }
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (image == null || !image.present()) {
            return Result.invalid(Failure.NO_IMAGE);
        }

        // Search candidate wall planes along the projector's optical axis, but never use the
        // unused nominal/square envelope as a validator. The only cells that matter are those
        // intersected by the aspect-correct image rectangle after scale + X/Y offsets.
        ProjectionPower.Status lastPower = null;
        BlockPos lastCandidate = projectorPos.relative(facing, MAX_SEARCH_BLOCKS);
        int lastDistance = distancePixels(MAX_SEARCH_BLOCKS);
        boolean encounteredIrregularOrObstructedSurface = false;
        boolean foundRegularRectangle = false;
        int requestedScale = Math.max(ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, safe.scalePixels());

        for (int step = 1; step <= MAX_SEARCH_BLOCKS; step++) {
            Target target = new Target(true, Failure.NONE, projectorPos.relative(facing, step), step, distancePixels(step));
            lastCandidate = target.wallBlock();
            lastDistance = target.distancePixels();

            boolean regularAtThisPlane = false;
            for (int scale = requestedScale; scale >= ProjectionSettings.DEBUG_MIN_SCALE_PIXELS; scale--) {
                ProjectionImageSizing.Size size = ProjectionImageSizing.size(image.width(), image.height(), scale);
                Rectangle rectangle = rectangle(projectorPos, target, facing, safe, size);
                Validation validation = validateRectangle(level, projectorPos, target, facing, rectangle);
                if (!validation.valid()) {
                    encounteredIrregularOrObstructedSurface |= validation.encounteredSolid();
                    continue;
                }

                foundRegularRectangle = true;
                regularAtThisPlane = true;
                ProjectionSettings candidate = safe
                        .withImage(image.id(), image.width(), image.height())
                        .withScalePixels(scale);
                ProjectionPower.Status power = ProjectionPower.evaluateWallProjection(
                        candidate, core, true, 1, target.distancePixels()
                );
                lastPower = power;
                if (power.active()) {
                    return new Result(
                            true, Failure.NONE, target.wallBlock(), facing, target.distancePixels(),
                            scale, size.widthPixels(), size.heightPixels(),
                            rectangle.centerX(), rectangle.centerY(), rectangle.centerZ(),
                            rectangle.envelope(), power
                    );
                }
            }

            // Once a complete regular image rectangle exists at this depth, it is the physical
            // target wall. A power failure must not cause the resolver to look through that wall.
            if (regularAtThisPlane) {
                break;
            }

            // If the real image rays already encountered geometry before or on this candidate
            // plane, looking farther cannot magically pass through it. Stop instead of treating a
            // deeper wall as valid through an obstruction. Geometry outside the image rectangle
            // never reaches this branch and therefore never blocks the projection.
            if (encounteredIrregularOrObstructedSurface) {
                break;
            }
        }

        if (foundRegularRectangle && lastPower != null) {
            Failure failure = lastPower.failure() == ProjectionPower.Failure.NO_CORE
                    ? Failure.NO_CORE
                    : Failure.POWER_EXCEEDED;
            return Result.invalid(failure, lastCandidate, facing, lastDistance, lastPower);
        }
        return Result.invalid(
                encounteredIrregularOrObstructedSurface ? Failure.IRREGULAR_SURFACE : Failure.NO_WALL,
                lastCandidate, facing, lastDistance, lastPower
        );
    }

    private static boolean isRegularWallBlock(Level level, BlockPos pos, BlockState state, Direction projectorFacing) {
        if (state.isAir() || !state.isCollisionShapeFullBlock(level, pos)) {
            return false;
        }
        return state.isFaceSturdy(level, pos, projectorFacing.getOpposite());
    }

    private static Rectangle rectangle(
            BlockPos projectorPos,
            Target target,
            Direction facing,
            ProjectionSettings settings,
            ProjectionImageSizing.Size size
    ) {
        Direction right = facing.getClockWise();
        double xOffset = settings.horizontalOffsetPixels() * PIXEL;
        double yOffset = settings.verticalOffsetPixels() * PIXEL;
        double centerX = projectorPos.getX() + 0.5D + right.getStepX() * xOffset;
        double centerZ = projectorPos.getZ() + 0.5D + right.getStepZ() * xOffset;
        double centerY = projectorPos.getY() + LENS_CENTER_Y_PIXELS * PIXEL + yOffset;

        BlockPos wall = target.wallBlock();
        switch (facing) {
            case NORTH -> centerZ = wall.getZ() + 1.0D + SURFACE_EPSILON;
            case SOUTH -> centerZ = wall.getZ() - SURFACE_EPSILON;
            case EAST -> centerX = wall.getX() - SURFACE_EPSILON;
            case WEST -> centerX = wall.getX() + 1.0D + SURFACE_EPSILON;
            default -> {
            }
        }

        double width = Math.max(1, size.widthPixels()) * PIXEL;
        double height = Math.max(1, size.heightPixels()) * PIXEL;
        double halfW = width * 0.5D;
        double halfH = height * 0.5D;
        double thickness = 0.01D;
        AABB envelope;
        if (facing.getAxis() == Direction.Axis.Z) {
            envelope = new AABB(
                    centerX - halfW, centerY - halfH, centerZ - thickness,
                    centerX + halfW, centerY + halfH, centerZ + thickness
            );
        } else {
            envelope = new AABB(
                    centerX - thickness, centerY - halfH, centerZ - halfW,
                    centerX + thickness, centerY + halfH, centerZ + halfW
            );
        }
        return new Rectangle(centerX, centerY, centerZ, width, height, envelope);
    }

    private static Validation validateRectangle(
            Level level,
            BlockPos projectorPos,
            Target target,
            Direction facing,
            Rectangle rectangle
    ) {
        final double epsilon = 1.0E-6D;
        int minY = (int) Math.floor(rectangle.centerY() - rectangle.height() * 0.5D + epsilon);
        int maxY = (int) Math.floor(rectangle.centerY() + rectangle.height() * 0.5D - epsilon);
        if (maxY < minY) {
            maxY = minY;
        }

        if (facing.getAxis() == Direction.Axis.Z) {
            int minLateral = (int) Math.floor(rectangle.centerX() - rectangle.width() * 0.5D + epsilon);
            int maxLateral = (int) Math.floor(rectangle.centerX() + rectangle.width() * 0.5D - epsilon);
            if (maxLateral < minLateral) maxLateral = minLateral;
            int wallZ = target.wallBlock().getZ();
            for (int x = minLateral; x <= maxLateral; x++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos wallPos = new BlockPos(x, y, wallZ);
                    Validation cell = validateCell(level, projectorPos, wallPos, target.steps(), facing);
                    if (!cell.valid()) {
                        return cell;
                    }
                }
            }
        } else {
            int minLateral = (int) Math.floor(rectangle.centerZ() - rectangle.width() * 0.5D + epsilon);
            int maxLateral = (int) Math.floor(rectangle.centerZ() + rectangle.width() * 0.5D - epsilon);
            if (maxLateral < minLateral) maxLateral = minLateral;
            int wallX = target.wallBlock().getX();
            for (int z = minLateral; z <= maxLateral; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos wallPos = new BlockPos(wallX, y, z);
                    Validation cell = validateCell(level, projectorPos, wallPos, target.steps(), facing);
                    if (!cell.valid()) {
                        return cell;
                    }
                }
            }
        }
        return Validation.VALID;
    }

    private static Validation validateCell(
            Level level,
            BlockPos projectorPos,
            BlockPos wallPos,
            int targetSteps,
            Direction facing
    ) {
        BlockState wallState = level.getBlockState(wallPos);
        if (!isRegularWallBlock(level, wallPos, wallState, facing)) {
            return wallState.isAir() ? Validation.NO_SURFACE : Validation.IRREGULAR;
        }

        for (int step = 1; step < targetSteps; step++) {
            BlockPos path;
            if (facing.getAxis() == Direction.Axis.Z) {
                path = new BlockPos(wallPos.getX(), wallPos.getY(), projectorPos.getZ() + facing.getStepZ() * step);
            } else {
                path = new BlockPos(projectorPos.getX() + facing.getStepX() * step, wallPos.getY(), wallPos.getZ());
            }
            if (!level.getBlockState(path).isAir()) {
                return Validation.IRREGULAR;
            }
        }
        return Validation.VALID;
    }

    private static int distancePixels(int steps) {
        return Math.max(1, steps * 16 - 8);
    }

    public enum Failure {
        NONE,
        NO_IMAGE,
        NO_WALL,
        IRREGULAR_SURFACE,
        NO_CORE,
        POWER_EXCEEDED;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record Result(
            boolean valid,
            Failure failure,
            BlockPos wallBlock,
            Direction facing,
            int distancePixels,
            int resolvedScalePixels,
            int widthPixels,
            int heightPixels,
            double centerX,
            double centerY,
            double centerZ,
            AABB envelope,
            ProjectionPower.Status powerStatus
    ) {
        public static Result invalid(Failure failure) {
            return invalid(failure, BlockPos.ZERO, Direction.NORTH, 0, null);
        }

        public static Result invalid(
                Failure failure,
                BlockPos wallBlock,
                Direction facing,
                int distancePixels,
                ProjectionPower.Status powerStatus
        ) {
            return new Result(false, failure == null ? Failure.NO_WALL : failure,
                    wallBlock == null ? BlockPos.ZERO : wallBlock,
                    facing == null ? Direction.NORTH : facing,
                    Math.max(0, distancePixels), 0, 0, 0,
                    0.0D, 0.0D, 0.0D, null, powerStatus);
        }

        public boolean hasEnvelope() {
            return envelope != null;
        }
    }

    private record Validation(boolean valid, boolean encounteredSolid) {
        private static final Validation VALID = new Validation(true, false);
        private static final Validation NO_SURFACE = new Validation(false, false);
        private static final Validation IRREGULAR = new Validation(false, true);
    }

    private record Target(boolean valid, Failure failure, BlockPos wallBlock, int steps, int distancePixels) {
    }

    private record Rectangle(
            double centerX,
            double centerY,
            double centerZ,
            double width,
            double height,
            AABB envelope
    ) {
    }
}

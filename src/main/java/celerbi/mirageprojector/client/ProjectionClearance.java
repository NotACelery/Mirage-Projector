package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class ProjectionClearance {
    private static final double PIXEL = 1.0D / 16.0D;
    private static final double PHYSICAL_TOP = 5.0D * PIXEL;

    private ProjectionClearance() {
    }

    public static Result scan(Level level, BlockPos projectorPos, ProjectionSettings settings, boolean hasProjectedItem) {
        if (level == null) {
            return Result.UNKNOWN;
        }

        double width;
        double height;
        if (settings.sourceMode() == ProjectionSettings.SourceMode.ITEM) {
            if (!hasProjectedItem) {
                return new Result(0, 0);
            }
            width = settings.scalePixels() * PIXEL;
            height = width;
        } else {
            if (!settings.hasImage()) {
                return new Result(0, 0);
            }
            double largest = settings.scalePixels() * PIXEL;
            if (settings.imageWidth() >= settings.imageHeight()) {
                width = largest;
                height = largest * settings.imageHeight() / (double) settings.imageWidth();
            } else {
                height = largest;
                width = largest * settings.imageWidth() / (double) settings.imageHeight();
            }
        }

        double radius = Math.max(0.05D, width * 0.5D);
        double centerX = projectorPos.getX() + 0.5D;
        double centerZ = projectorPos.getZ() + 0.5D;
        double bottom = projectorPos.getY() + PHYSICAL_TOP + settings.liftPixels() * PIXEL;
        double minY = bottom - (settings.floatingEnabled() ? settings.floatAmplitudePixels() * PIXEL : 0.0D);
        double maxY = bottom + height;

        BlockPos min = BlockPos.containing(centerX - radius, minY, centerZ - radius);
        BlockPos max = BlockPos.containing(centerX + radius, maxY, centerZ + radius);

        int checked = 0;
        int blocked = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.equals(projectorPos)) {
                continue;
            }
            checked++;
            if (!level.getBlockState(pos).isAir()) {
                blocked++;
            }
        }
        return new Result(blocked, checked);
    }

    public record Result(int blockedBlocks, int checkedBlocks) {
        public static final Result UNKNOWN = new Result(-1, 0);

        public boolean known() {
            return blockedBlocks >= 0;
        }

        public boolean clear() {
            return blockedBlocks == 0;
        }
    }
}

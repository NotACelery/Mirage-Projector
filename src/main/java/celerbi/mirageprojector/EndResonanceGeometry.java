package celerbi.mirageprojector;

import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** Fixed world-space geometry owned by End Resonance rather than ProjectionSettings. */
public final class EndResonanceGeometry {
    public static final double FIELD_WIDTH = 2.0D;
    public static final double FIELD_HEIGHT = 3.0D;
    public static final double PRISM_WIDTH = 2.0D;
    public static final double PRISM_HEIGHT = 3.0D;
    public static final double PRISM_DEPTH = 2.0D;
    public static final double TABLE_SIZE = 3.0D;
    public static final double PLANE_TRIGGER_THICKNESS = 0.125D;

    private EndResonanceGeometry() {
    }

    public static double baseY(MirageProjectorBlockEntity projector) {
        return projector.getBlockPos().getY() + projector.chassisProfile().physicalTopPixels() / 16.0D;
    }

    public static Direction facing(MirageProjectorBlockEntity projector) {
        return projector.getBlockState().hasProperty(MirageProjectorBlock.FACING)
                ? projector.getBlockState().getValue(MirageProjectorBlock.FACING)
                : Direction.SOUTH;
    }

    public static AABB resonanceBounds(MirageProjectorBlockEntity projector) {
        BlockPos pos = projector.getBlockPos();
        double cx = pos.getX() + 0.5D;
        double cz = pos.getZ() + 0.5D;
        double minY = baseY(projector);
        if (projector.chassisProfile() == ProjectionChassisProfile.TABLE) {
            double half = TABLE_SIZE * 0.5D;
            return new AABB(cx - half, minY - 0.12D, cz - half,
                    cx + half, minY + 0.20D, cz + half);
        }
        if (projector.chassisProfile() == ProjectionChassisProfile.PRISM) {
            return new AABB(
                    cx - PRISM_WIDTH * 0.5D, minY, cz - PRISM_DEPTH * 0.5D,
                    cx + PRISM_WIDTH * 0.5D, minY + PRISM_HEIGHT, cz + PRISM_DEPTH * 0.5D
            );
        }

        Direction facing = facing(projector);
        double halfWidth = FIELD_WIDTH * 0.5D;
        double halfDepth = PLANE_TRIGGER_THICKNESS * 0.5D;
        if (facing.getAxis() == Direction.Axis.Z) {
            return new AABB(cx - halfWidth, minY, cz - halfDepth,
                    cx + halfWidth, minY + FIELD_HEIGHT, cz + halfDepth);
        }
        return new AABB(cx - halfDepth, minY, cz - halfWidth,
                cx + halfDepth, minY + FIELD_HEIGHT, cz + halfWidth);
    }

    public static AABB renderBounds(MirageProjectorBlockEntity projector) {
        AABB machine = new AABB(projector.getBlockPos()).inflate(0.25D);
        AABB resonance = resonanceBounds(projector).inflate(0.08D);
        return new AABB(
                Math.min(machine.minX, resonance.minX), Math.min(machine.minY, resonance.minY), Math.min(machine.minZ, resonance.minZ),
                Math.max(machine.maxX, resonance.maxX), Math.max(machine.maxY, resonance.maxY), Math.max(machine.maxZ, resonance.maxZ)
        );
    }
}

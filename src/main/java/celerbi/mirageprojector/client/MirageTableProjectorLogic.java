package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectorVisualLayout;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.ProjectionTransform;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Runtime rules owned exclusively by the Mirage Table Projector.
 *
 * <p>The Table is not an upright projector rotated ninety degrees. Image/Banner content is a
 * horizontal presentation surface whose in-plane Rotation only changes how the content is read;
 * it must never decide whether the surface exists. Item/Entity content is volumetric and remains
 * upright above the same X/Y/Z anchor.</p>
 *
 * <p>Most importantly, front/back classification derives its normal from the exact quaternion
 * chain used by {@link #applyPlanarPlacement}. This prevents render visibility and render geometry
 * from drifting apart as Rotation/Tilt evolve.</p>
 */
public final class MirageTableProjectorLogic {
    private static final float PIXEL = 1.0F / 16.0F;

    private MirageTableProjectorLogic() {
    }

    public static float projectionAngle(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            double gameTime
    ) {
        float angle = blockFacingAngle(blockEntity);
        if (!settings.rotationEnabled()) {
            return angle + settings.rotationOffsetDegrees();
        }
        double cycles = gameTime / settings.rotationPeriodTicks();
        float direction = settings.clockwise() ? 1.0F : -1.0F;
        return angle + settings.rotationOffsetDegrees()
                + (float) ((cycles * 360.0D) % 360.0D) * direction;
    }

    public static float bobOffset(ProjectionSettings settings, double gameTime) {
        if (!settings.floatingEnabled() || settings.floatAmplitudePixels() <= 0) {
            return 0.0F;
        }

        double phase;
        if (settings.floatMode() == ProjectionSettings.FloatMode.ROTATION_SYNCED) {
            if (!settings.rotationEnabled()) {
                return 0.0F;
            }
            double travelledDegrees = gameTime * 360.0D / settings.rotationPeriodTicks();
            phase = travelledDegrees / (settings.floatIntervalDegrees() * 2.0D);
        } else {
            phase = gameTime / settings.floatCycleTicks();
        }

        double smoothDownAndBack = (1.0D - Math.cos(phase * Math.PI * 2.0D)) * 0.5D;
        return (float) (-smoothDownAndBack * settings.floatAmplitudePixels() * PIXEL);
    }

    public static void applyPlanarPlacement(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            double baseY,
            float finalYawDegrees
    ) {
        applyAnchorAndOffsets(blockEntity, settings, poseStack, baseY);
        poseStack.mulPose(Axis.YP.rotationDegrees(finalYawDegrees));
        // Built-in Image/Banner planes are authored in XY with +Z as their front normal.
        // Flatten them onto the tabletop before applying user Tilt.
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        ProjectionTransform.Orientation orientation = settings.transform().orientation();
        poseStack.mulPose(new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w()));
    }

    public static void applyVolumetricPlacement(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            double baseY,
            float finalYawDegrees
    ) {
        applyAnchorAndOffsets(blockEntity, settings, poseStack, baseY);
        poseStack.mulPose(Axis.YP.rotationDegrees(finalYawDegrees));
        ProjectionTransform.Orientation orientation = settings.transform().orientation();
        poseStack.mulPose(new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w()));
    }

    /**
     * Determines the physical side of a planar Table projection that contains the camera.
     * Rotation is intentionally not approximated with yaw/sine formulae; the exact render
     * transform is composed and applied to local +Z, so zero Tilt stays +Y for every yaw.
     */
    public static boolean isCameraOnFrontSide(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            float finalYawDegrees,
            double baseY
    ) {
        Vector3f normal = planarFrontNormal(settings, finalYawDegrees);
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos pos = blockEntity.getBlockPos();

        double originX = pos.getX() + 0.5D + settings.horizontalOffsetPixels() * PIXEL;
        double originY = pos.getY() + baseY;
        double originZ = pos.getZ() + 0.5D + settings.verticalOffsetPixels() * PIXEL;
        double toCameraX = camera.x - originX;
        double toCameraY = camera.y - originY;
        double toCameraZ = camera.z - originZ;

        return toCameraX * normal.x + toCameraY * normal.y + toCameraZ * normal.z >= 0.0D;
    }

    static Vector3f planarFrontNormal(ProjectionSettings settings, float finalYawDegrees) {
        Quaternionf world = new Quaternionf().rotationY((float) Math.toRadians(finalYawDegrees));
        world.mul(new Quaternionf().rotationX((float) Math.toRadians(-90.0F)));
        ProjectionTransform.Orientation orientation = settings.transform().orientation();
        world.mul(new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w()));

        Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F);
        world.transform(normal);
        if (normal.lengthSquared() > 0.000001F) {
            normal.normalize();
        }
        return normal;
    }

    /**
     * Conservative Table-only BER envelope. Includes signed X/Z offsets, full in-plane rotation,
     * Tilt, Lift and Float so vanilla frustum culling never depends on the tiny physical chassis.
     */
    public static AABB renderBoundingBox(MirageProjectorBlockEntity blockEntity) {
        ProjectionSettings settings = blockEntity.settings();
        BlockPos pos = blockEntity.getBlockPos();
        ProjectionPower.Status power = ProjectionPower.evaluate(
                settings,
                blockEntity.coreProfile(),
                ProjectionChassisProfile.TABLE,
                blockEntity.hasProjectedSourceContent(),
                blockEntity.projectedSourceCount()
        );
        if (!blockEntity.projectionEnabled() || !power.active()) {
            return new AABB(pos).inflate(0.5D);
        }

        ProjectionPower.Dimensions dimensions = ProjectionPower.dimensions(
                settings,
                blockEntity.hasProjectedSourceContent(),
                ProjectionChassisProfile.TABLE
        );
        double width = Math.max(PIXEL, dimensions.widthPixels() * PIXEL);
        double height = Math.max(PIXEL, dimensions.heightPixels() * PIXEL);
        double planarRadius = Math.hypot(width, height) * 0.5D;
        double tiltReach = Math.max(width, height)
                * Math.abs(Math.sin(Math.toRadians(settings.tiltDegrees()))) * 0.5D;
        double radius = Math.max(1.0D, planarRadius + tiltReach + 0.5D);

        double centerX = pos.getX() + 0.5D + settings.horizontalOffsetPixels() * PIXEL;
        double centerZ = pos.getZ() + 0.5D + settings.verticalOffsetPixels() * PIXEL;
        double projectionY = pos.getY()
                + ProjectorVisualLayout.forState(blockEntity.getBlockState(), ProjectionChassisProfile.TABLE)
                .projectionTopPixels() * PIXEL
                + settings.liftPixels() * PIXEL;
        double floatReach = settings.floatingEnabled() ? settings.floatAmplitudePixels() * PIXEL : 0.0D;
        // Volumetric Item/Entity can stand upright; planar Image/Banner can tilt. Use the larger
        // reach so one Table envelope safely covers every workspace without source-dependent pop.
        double verticalReach = Math.max(1.0D, Math.max(width, height) + tiltReach + floatReach + 0.5D);

        AABB projection = new AABB(
                centerX - radius,
                projectionY - verticalReach,
                centerZ - radius,
                centerX + radius,
                projectionY + verticalReach,
                centerZ + radius
        );
        AABB machine = new AABB(pos).inflate(0.25D);
        return new AABB(
                Math.min(machine.minX, projection.minX),
                Math.min(machine.minY, projection.minY),
                Math.min(machine.minZ, projection.minZ),
                Math.max(machine.maxX, projection.maxX),
                Math.max(machine.maxY, projection.maxY),
                Math.max(machine.maxZ, projection.maxZ)
        );
    }

    private static void applyAnchorAndOffsets(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            double baseY
    ) {
        poseStack.translate(
                0.5D + settings.horizontalOffsetPixels() * PIXEL,
                baseY,
                0.5D + settings.verticalOffsetPixels() * PIXEL
        );
    }

    private static float blockFacingAngle(MirageProjectorBlockEntity blockEntity) {
        if (blockEntity == null || !blockEntity.getBlockState().hasProperty(MirageProjectorBlock.FACING)) {
            return 0.0F;
        }
        return switch (blockEntity.getBlockState().getValue(MirageProjectorBlock.FACING)) {
            case SOUTH -> 0.0F;
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}

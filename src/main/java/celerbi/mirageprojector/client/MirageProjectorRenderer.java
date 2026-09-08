package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class MirageProjectorRenderer implements BlockEntityRenderer<MirageProjectorBlockEntity> {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float PHYSICAL_TOP = 5.0F * PIXEL;

    public MirageProjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            MirageProjectorBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ProjectionSettings settings = blockEntity.settings();
        ProjectionCoreProfile core = blockEntity.coreProfile();
        renderCore(blockEntity, core, poseStack, bufferSource);

        ProjectionPower.Status power = ProjectionPower.evaluate(settings, core, !blockEntity.projectedStack().isEmpty());
        if (!power.active()) {
            return;
        }

        double gameTime = blockEntity.getLevel() == null ? 0.0D : blockEntity.getLevel().getGameTime() + partialTick;
        float angle = rotationAngle(settings, gameTime);
        float bob = bobOffset(settings, gameTime);

        if (settings.sourceMode() == ProjectionSettings.SourceMode.ITEM) {
            if (blockEntity.projectedStack().isEmpty()) {
                renderBook(blockEntity, poseStack, bufferSource, angle, bob);
            } else {
                renderProjectedItem(blockEntity, settings, poseStack, bufferSource, angle, bob);
            }
            return;
        }

        if (!settings.hasImage()) {
            renderBook(blockEntity, poseStack, bufferSource, angle, bob);
            return;
        }

        ProjectionTextureCache.get(settings.imageId()).ifPresentOrElse(
                frontTexture -> renderImage(blockEntity, settings, frontTexture, poseStack, bufferSource, angle, bob),
                () -> renderMissingAsset(blockEntity, poseStack, bufferSource, angle, bob)
        );
    }


    private static void renderCore(
            MirageProjectorBlockEntity blockEntity,
            ProjectionCoreProfile core,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        if (core == null || !core.present()) {
            return;
        }

        poseStack.pushPose();
        // Reference core occupies X/Z 7..9 and Y 2..5: 2x3x2 Minecraft pixels.
        // ItemDisplayContext.NONE keeps vanilla block-item geometry untransformed so a
        // non-uniform scale can reproduce that exact little pillar.
        poseStack.translate(0.5D, 3.5F * PIXEL, 0.5D);
        poseStack.scale(2.0F * PIXEL, 3.0F * PIXEL, 2.0F * PIXEL);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                core.visualStack(),
                ItemDisplayContext.NONE,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }


    private static void renderProjectedItem(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob
    ) {
        float scale = settings.scalePixels() * PIXEL;
        float bottom = PHYSICAL_TOP + settings.liftPixels() * PIXEL + bob;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom + scale * 0.5F, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                blockEntity.projectedStack(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private static void renderBook(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, PHYSICAL_TOP + 4.0F * PIXEL + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                Items.BOOK.getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private static void renderMissingAsset(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, PHYSICAL_TOP + 4.0F * PIXEL + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.35F, 0.35F, 0.35F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                Items.PAPER.getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private static void renderImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            ResourceLocation frontTexture,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob
    ) {
        float bottom = PHYSICAL_TOP + settings.liftPixels() * PIXEL + bob;
        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        FaceSize frontSize = faceSize(settings.imageWidth(), settings.imageHeight(), settings.scalePixels());

        // A translucent hologram must behave like a physical two-sided sheet: from any
        // camera position only the face that actually points toward the camera is drawn.
        // Rendering both translucent quads at once makes transparent pixels reveal the
        // opposite image and produces the doubled/ghosted silhouettes seen in dev.5.
        boolean viewingFront = isCameraOnFrontSide(blockEntity, angle);
        if (viewingFront) {
            renderFrontFace(frontTexture, frontSize, topV, bottomV, matrix, pose, bufferSource);
        } else {
            switch (settings.backFaceMode()) {
                case MIRRORED -> renderBackFace(
                        frontTexture,
                        frontSize,
                        topV,
                        bottomV,
                        false,
                        matrix,
                        pose,
                        bufferSource
                );
                case READABLE -> renderBackFace(
                        frontTexture,
                        frontSize,
                        topV,
                        bottomV,
                        true,
                        matrix,
                        pose,
                        bufferSource
                );
                case INDEPENDENT -> {
                    ResourceLocation backTexture = settings.hasBackImage()
                            ? ProjectionTextureCache.get(settings.backImageId()).orElse(frontTexture)
                            : frontTexture;
                    FaceSize backSize = settings.hasBackImage()
                            ? faceSize(settings.backImageWidth(), settings.backImageHeight(), settings.scalePixels())
                            : frontSize;
                    renderBackFace(
                            backTexture,
                            backSize,
                            topV,
                            bottomV,
                            true,
                            matrix,
                            pose,
                            bufferSource
                    );
                }
            }
        }

        poseStack.popPose();
    }

    private static boolean isCameraOnFrontSide(MirageProjectorBlockEntity blockEntity, float angle) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        BlockPos pos = blockEntity.getBlockPos();

        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double radians = Math.toRadians(angle);

        // The local front normal is +Z. Rotating the projection around Y turns that
        // into (sin(angle), 0, cos(angle)) in world space. Only the face on the
        // camera's side of the plane is rendered, preventing translucent pixels from
        // exposing the opposite face and creating doubled silhouettes.
        double normalX = Math.sin(radians);
        double normalZ = Math.cos(radians);
        double toCameraX = camera.x - centerX;
        double toCameraZ = camera.z - centerZ;
        double dot = toCameraX * normalX + toCameraZ * normalZ;
        return dot >= 0.0D;
    }

    private static FaceSize faceSize(int imageWidth, int imageHeight, int scalePixels) {
        float largest = scalePixels * PIXEL;
        if (imageWidth >= imageHeight) {
            return new FaceSize(largest, largest * imageHeight / (float) imageWidth);
        }
        return new FaceSize(largest * imageWidth / (float) imageHeight, largest);
    }

    private static void renderFrontFace(
            ResourceLocation texture,
            FaceSize size,
            float topV,
            float bottomV,
            Matrix4f matrix,
            PoseStack.Pose pose,
            MultiBufferSource bufferSource
    ) {
        float minX = -size.width() * 0.5F;
        float maxX = size.width() * 0.5F;
        float maxY = size.height();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        vertex(consumer, matrix, pose, minX, 0.0F, 0.0F, 0.0F, bottomV, 0.0F, 0.0F, 1.0F);
        vertex(consumer, matrix, pose, maxX, 0.0F, 0.0F, 1.0F, bottomV, 0.0F, 0.0F, 1.0F);
        vertex(consumer, matrix, pose, maxX, maxY, 0.0F, 1.0F, topV, 0.0F, 0.0F, 1.0F);
        vertex(consumer, matrix, pose, minX, maxY, 0.0F, 0.0F, topV, 0.0F, 0.0F, 1.0F);
    }

    private static void renderBackFace(
            ResourceLocation texture,
            FaceSize size,
            float topV,
            float bottomV,
            boolean readable,
            Matrix4f matrix,
            PoseStack.Pose pose,
            MultiBufferSource bufferSource
    ) {
        float minX = -size.width() * 0.5F;
        float maxX = size.width() * 0.5F;
        float maxY = size.height();
        float leftU = readable ? 1.0F : 0.0F;
        float rightU = readable ? 0.0F : 1.0F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        vertex(consumer, matrix, pose, minX, maxY, 0.0F, leftU, topV, 0.0F, 0.0F, -1.0F);
        vertex(consumer, matrix, pose, maxX, maxY, 0.0F, rightU, topV, 0.0F, 0.0F, -1.0F);
        vertex(consumer, matrix, pose, maxX, 0.0F, 0.0F, rightU, bottomV, 0.0F, 0.0F, -1.0F);
        vertex(consumer, matrix, pose, minX, 0.0F, 0.0F, leftU, bottomV, 0.0F, 0.0F, -1.0F);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    private static float rotationAngle(ProjectionSettings settings, double gameTime) {
        if (!settings.rotationEnabled()) {
            return settings.rotationOffsetDegrees();
        }
        double cycles = gameTime / settings.rotationPeriodTicks();
        float direction = settings.clockwise() ? 1.0F : -1.0F;
        return settings.rotationOffsetDegrees() + (float) ((cycles * 360.0D) % 360.0D) * direction;
    }

    private static float bobOffset(ProjectionSettings settings, double gameTime) {
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

    @Override
    public AABB getRenderBoundingBox(MirageProjectorBlockEntity blockEntity) {
        ProjectionSettings settings = blockEntity.settings();
        BlockPos pos = blockEntity.getBlockPos();

        ProjectionPower.Status power = ProjectionPower.evaluate(
                settings,
                blockEntity.coreProfile(),
                !blockEntity.projectedStack().isEmpty()
        );
        if (!power.active()) {
            return new AABB(pos).inflate(0.5D, 0.5D, 0.5D);
        }

        boolean itemProjection = settings.sourceMode() == ProjectionSettings.SourceMode.ITEM
                && !blockEntity.projectedStack().isEmpty();
        if (!settings.hasImage() && !itemProjection) {
            return new AABB(pos).inflate(0.5D, 1.0D, 0.5D);
        }

        double largest = settings.scalePixels() * PIXEL;
        double radius = Math.max(0.75D, largest * 0.5D + 0.25D);
        double minY = pos.getY() + PHYSICAL_TOP + settings.liftPixels() * PIXEL
                - settings.floatAmplitudePixels() * PIXEL - 0.25D;
        double maxY = pos.getY() + PHYSICAL_TOP + settings.liftPixels() * PIXEL
                + largest + 0.25D;
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        return new AABB(
                centerX - radius,
                minY,
                centerZ - radius,
                centerX + radius,
                maxY,
                centerZ + radius
        );
    }

    @Override
    public boolean shouldRenderOffScreen(MirageProjectorBlockEntity blockEntity) {
        // Large and/or highly lifted projections can extend far beyond the physical
        // block. NeoForge's frustum decisions can otherwise drop the renderer when the
        // pedestal itself leaves the camera frustum (most noticeable while standing
        // close and looking diagonally upward). Keep rendering inside getViewDistance;
        // a projection-aware visibility/LOD pass can replace this debug-safe behavior
        // once the renderer is feature-complete.
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private record FaceSize(float width, float height) {
    }
}

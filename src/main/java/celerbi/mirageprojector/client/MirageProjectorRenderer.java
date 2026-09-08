package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class MirageProjectorRenderer implements BlockEntityRenderer<MirageProjectorBlockEntity> {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final UUID BODYLESS_CACHE_ID = new UUID(0L, 1L);

    private final Map<MirageProjectorBlockEntity, EntityCacheEntry> entityCache = new WeakHashMap<>();
    private final Map<MirageProjectorBlockEntity, EquippedItemCacheEntry> equippedItemCache = new WeakHashMap<>();

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
        renderCore(blockEntity, core, poseStack, bufferSource, packedLight);

        ProjectionPower.Status power = ProjectionPower.evaluate(
                settings,
                core,
                blockEntity.chassisProfile(),
                !blockEntity.projectedStack().isEmpty()
        );
        if (!power.active()) {
            return;
        }

        double gameTime = blockEntity.getLevel() == null ? 0.0D : blockEntity.getLevel().getGameTime() + partialTick;
        float angle = rotationAngle(settings, gameTime);
        float bob = bobOffset(settings, gameTime);
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        if (settings.sourceMode() == ProjectionSettings.SourceMode.ITEM) {
            if (blockEntity.projectedStack().isEmpty()) {
                equippedItemCache.remove(blockEntity);
                renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            } else {
                LivingEntity equippedPiece = resolveEquippedItemEntity(blockEntity);
                if (equippedPiece != null) {
                    renderProjectedEntity(
                            blockEntity,
                            settings,
                            equippedPiece,
                            partialTick,
                            poseStack,
                            bufferSource,
                            angle,
                            bob,
                            projectionLight,
                            false
                    );
                } else {
                    renderProjectedItem(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
                }
            }
            return;
        }

        if (settings.sourceMode() == ProjectionSettings.SourceMode.ENTITY) {
            LivingEntity entity = resolveProjectedEntity(blockEntity);
            if (entity == null) {
                renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            } else {
                renderProjectedEntity(
                        blockEntity,
                        settings,
                        entity,
                        partialTick,
                        poseStack,
                        bufferSource,
                        angle,
                        bob,
                        projectionLight,
                        blockEntity.entityProjectionState().activeKind() == celerbi.mirageprojector.entity.EntityScanData.Kind.HUMANOID
                                || !blockEntity.entityProjectionState().hasActiveEntity()
                );
                renderProjectionNameplate(blockEntity, settings, poseStack, bufferSource, bob, projectionLight);
            }
            return;
        }

        if (blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM) {
            if (!settings.hasAnyImage()) {
                renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
                return;
            }
            boolean rendered = renderPrismImage(
                    blockEntity,
                    settings,
                    poseStack,
                    bufferSource,
                    angle,
                    bob,
                    projectionLight
            );
            if (!rendered) {
                renderMissingAsset(blockEntity, poseStack, bufferSource, angle, bob, projectionLight);
            }
            return;
        }

        if (!settings.hasImage()) {
            renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            return;
        }

        ProjectionTextureCache.get(settings.imageId()).ifPresentOrElse(
                frontTexture -> renderImage(
                        blockEntity,
                        settings,
                        frontTexture,
                        poseStack,
                        bufferSource,
                        angle,
                        bob,
                        projectionLight
                ),
                () -> renderMissingAsset(blockEntity, poseStack, bufferSource, angle, bob, projectionLight)
        );
    }

    private LivingEntity resolveEquippedItemEntity(MirageProjectorBlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ClientLevel clientLevel)) {
            equippedItemCache.remove(blockEntity);
            return null;
        }
        if (EntityProjectionClientEntityFactory.standaloneEquipmentSlot(blockEntity.projectedStack()) == null) {
            equippedItemCache.remove(blockEntity);
            return null;
        }

        UUID snapshotId = blockEntity.projectionSnapshotId();
        if (snapshotId == null) {
            equippedItemCache.remove(blockEntity);
            return null;
        }
        EquippedItemCacheEntry cached = equippedItemCache.get(blockEntity);
        if (cached != null && cached.level() == clientLevel && cached.snapshotId().equals(snapshotId)) {
            return cached.entity();
        }

        LivingEntity entity = EntityProjectionClientEntityFactory.createEquippedItem(
                blockEntity.projectedStack(),
                clientLevel
        );
        if (entity == null) {
            equippedItemCache.remove(blockEntity);
            return null;
        }
        equippedItemCache.put(blockEntity, new EquippedItemCacheEntry(clientLevel, snapshotId, entity));
        return entity;
    }

    private LivingEntity resolveProjectedEntity(MirageProjectorBlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ClientLevel clientLevel)) {
            entityCache.remove(blockEntity);
            return null;
        }

        var active = blockEntity.entityProjectionState().activeEntity();
        if (active.isEmpty()) {
            if (!blockEntity.entityProjectionState().hasProjectedHumanoidEquipment()) {
                entityCache.remove(blockEntity);
                return null;
            }

            String fingerprint = EntityProjectionClientEntityFactory.equipmentFingerprint(
                    blockEntity.entityProjectionState(),
                    celerbi.mirageprojector.entity.EntityScanData.Kind.HUMANOID
            );
            EntityCacheEntry cached = entityCache.get(blockEntity);
            if (cached != null
                    && cached.level() == clientLevel
                    && BODYLESS_CACHE_ID.equals(cached.scanId())
                    && cached.equipmentFingerprint().equals(fingerprint)) {
                return cached.entity();
            }

            LivingEntity mannequin = EntityProjectionClientEntityFactory.createBodylessHumanoid(
                    blockEntity.entityProjectionState(),
                    clientLevel
            );
            if (mannequin == null) {
                entityCache.remove(blockEntity);
                return null;
            }
            entityCache.put(blockEntity, new EntityCacheEntry(clientLevel, BODYLESS_CACHE_ID, fingerprint, mannequin));
            return mannequin;
        }

        var scan = active.get();
        String equipmentFingerprint = EntityProjectionClientEntityFactory.equipmentFingerprint(
                blockEntity.entityProjectionState(),
                scan.kind()
        );
        EntityCacheEntry cached = entityCache.get(blockEntity);
        if (cached != null
                && cached.level() == clientLevel
                && cached.scanId().equals(scan.scanId())
                && cached.equipmentFingerprint().equals(equipmentFingerprint)) {
            return cached.entity();
        }

        LivingEntity entity = EntityProjectionClientEntityFactory.create(
                scan,
                blockEntity.entityProjectionState(),
                clientLevel
        );
        if (entity == null) {
            entityCache.remove(blockEntity);
            return null;
        }
        entityCache.put(blockEntity, new EntityCacheEntry(clientLevel, scan.scanId(), equipmentFingerprint, entity));
        return entity;
    }

    private static void renderProjectedEntity(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            LivingEntity entity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight,
            boolean humanoidPose
    ) {
        HumanoidPoseController.bind(
                entity,
                humanoidPose ? blockEntity.entityProjectionState().humanoidPose() : null
        );
        EntityProjectionClientEntityFactory.prepareVisualFrame(
                entity,
                blockEntity.getLevel() == null ? 0 : (int) (blockEntity.getLevel().getGameTime() & Integer.MAX_VALUE)
        );

        float nativeLargest = Math.max(0.1F, Math.max(entity.getBbWidth(), entity.getBbHeight()));
        float targetLargest = settings.scalePixels() * PIXEL;
        float entityScale = targetLargest / nativeLargest;
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(entityScale, entityScale, entityScale);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(bufferSource, settings);
        dispatcher.setRenderShadow(false);
        try {
            dispatcher.render(
                    entity,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0F,
                    partialTick,
                    poseStack,
                    projectionBuffers,
                    projectionLight
            );
        } finally {
            dispatcher.setRenderShadow(true);
            poseStack.popPose();
        }
    }

    private static void renderProjectionNameplate(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float bob,
            int projectionLight
    ) {
        String text = blockEntity.entityProjectionState().projectionNameplate();
        if (text == null || text.isBlank()) {
            return;
        }

        float baseTop = physicalTop(blockEntity);
        float entityBottom = baseTop + settings.liftPixels() * PIXEL + bob;
        float gap = Math.max(0.0F, entityBottom - baseTop);
        float labelY = baseTop + (gap >= 2.0F * PIXEL ? gap * 0.5F : 0.5F * PIXEL);

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component label = Component.literal(text);

        poseStack.pushPose();
        poseStack.translate(0.5D, labelY, 0.5D);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        float textX = -font.width(label) / 2.0F;
        font.drawInBatch(
                label,
                textX,
                0.0F,
                ProjectionRenderBuffers.tintedArgb(settings, 0xFFFFFF),
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                projectionLight
        );
        poseStack.popPose();
    }

    private static void renderCore(
            MirageProjectorBlockEntity blockEntity,
            ProjectionCoreProfile core,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (core == null || !core.present()) {
            return;
        }

        ProjectionChassisProfile chassis = blockEntity.chassisProfile();
        poseStack.pushPose();
        poseStack.translate(0.5D, chassis.coreCenterYPixels() * PIXEL, 0.5D);
        poseStack.scale(
                chassis.coreWidthPixels() * PIXEL,
                chassis.coreHeightPixels() * PIXEL,
                chassis.coreDepthPixels() * PIXEL
        );
        Minecraft.getInstance().getItemRenderer().renderStatic(
                core.visualStack(),
                ItemDisplayContext.NONE,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private static float physicalTop(MirageProjectorBlockEntity blockEntity) {
        return blockEntity.chassisProfile().physicalTopPixels() * PIXEL;
    }

    private static void renderProjectedItem(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        float scale = settings.scalePixels() * PIXEL;
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom + scale * 0.5F, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(scale, scale, scale);
        MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(bufferSource, settings);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                blockEntity.projectedStack(),
                ItemDisplayContext.FIXED,
                projectionLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                projectionBuffers,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private static void renderBook(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, physicalTop(blockEntity) + 4.0F * PIXEL + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                Items.BOOK.getDefaultInstance(),
                ItemDisplayContext.FIXED,
                projectionLight,
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
            float bob,
            int projectionLight
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, physicalTop(blockEntity) + 4.0F * PIXEL + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.35F, 0.35F, 0.35F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                Items.PAPER.getDefaultInstance(),
                ItemDisplayContext.FIXED,
                projectionLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    /**
     * Renders the Prism as four independent lateral quads around an open square.
     * North/East/South/West rotate as one unit; there is deliberately no top or
     * bottom quad, so the result never becomes a solid cube.
     */
    private static boolean renderPrismImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;
        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;
        ProjectionPower.Dimensions dimensions = ProjectionPower.dimensions(
                settings,
                false,
                blockEntity.chassisProfile()
        );
        float radius = Math.max(PIXEL, dimensions.widthPixels() * PIXEL * 0.5F);

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        boolean rendered = false;
        rendered |= renderPrismFace(
                poseStack,
                settings.imageId(), settings.imageWidth(), settings.imageHeight(), settings.hasImage(),
                180.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );
        rendered |= renderPrismFace(
                poseStack,
                settings.eastImageId(), settings.eastImageWidth(), settings.eastImageHeight(), settings.hasEastImage(),
                90.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );
        rendered |= renderPrismFace(
                poseStack,
                settings.backImageId(), settings.backImageWidth(), settings.backImageHeight(), settings.hasBackImage(),
                0.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );
        rendered |= renderPrismFace(
                poseStack,
                settings.westImageId(), settings.westImageWidth(), settings.westImageHeight(), settings.hasWestImage(),
                -90.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );

        poseStack.popPose();
        return rendered;
    }

    private static boolean renderPrismFace(
            PoseStack poseStack,
            String assetId,
            int imageWidth,
            int imageHeight,
            boolean present,
            float faceRotationDegrees,
            float radius,
            float topV,
            float bottomV,
            ProjectionSettings settings,
            MultiBufferSource bufferSource,
            int projectionLight
    ) {
        if (!present) {
            return false;
        }
        var texture = ProjectionTextureCache.get(assetId);
        if (texture.isEmpty()) {
            return false;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(faceRotationDegrees));
        poseStack.translate(0.0D, 0.0D, radius);
        PoseStack.Pose pose = poseStack.last();
        renderFrontFace(
                texture.get(),
                faceSize(imageWidth, imageHeight, settings.scalePixels()),
                topV,
                bottomV,
                settings,
                projectionLight,
                pose.pose(),
                pose,
                bufferSource
        );
        poseStack.popPose();
        return true;
    }

    private static void renderImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            ResourceLocation frontTexture,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;
        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        FaceSize frontSize = faceSize(settings.imageWidth(), settings.imageHeight(), settings.scalePixels());

        boolean viewingFront = isCameraOnFrontSide(blockEntity, angle);
        if (viewingFront) {
            renderFrontFace(frontTexture, frontSize, topV, bottomV, settings, projectionLight, matrix, pose, bufferSource);
        } else {
            switch (settings.backFaceMode()) {
                case MIRRORED -> renderBackFace(
                        frontTexture,
                        frontSize,
                        topV,
                        bottomV,
                        false,
                        settings,
                        projectionLight,
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
                        settings,
                        projectionLight,
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
                            settings,
                            projectionLight,
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
            ProjectionSettings settings,
            int projectionLight,
            Matrix4f matrix,
            PoseStack.Pose pose,
            MultiBufferSource bufferSource
    ) {
        float minX = -size.width() * 0.5F;
        float maxX = size.width() * 0.5F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        if (!settings.scanlines()) {
            emitFrontQuad(consumer, matrix, pose, minX, maxX, 0.0F, size.height(), bottomV, topV, settings, projectionLight);
            return;
        }
        emitFrontScanlines(consumer, matrix, pose, minX, maxX, size.height(), bottomV, topV, settings, projectionLight);
    }

    private static void renderBackFace(
            ResourceLocation texture,
            FaceSize size,
            float topV,
            float bottomV,
            boolean readable,
            ProjectionSettings settings,
            int projectionLight,
            Matrix4f matrix,
            PoseStack.Pose pose,
            MultiBufferSource bufferSource
    ) {
        float minX = -size.width() * 0.5F;
        float maxX = size.width() * 0.5F;
        float leftU = readable ? 1.0F : 0.0F;
        float rightU = readable ? 0.0F : 1.0F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        if (!settings.scanlines()) {
            emitBackQuad(consumer, matrix, pose, minX, maxX, 0.0F, size.height(), bottomV, topV,
                    leftU, rightU, settings, projectionLight);
            return;
        }
        emitBackScanlines(consumer, matrix, pose, minX, maxX, size.height(), bottomV, topV,
                leftU, rightU, settings, projectionLight);
    }

    private static void emitFrontScanlines(
            VertexConsumer consumer,
            Matrix4f matrix,
            PoseStack.Pose pose,
            float minX,
            float maxX,
            float height,
            float bottomV,
            float topV,
            ProjectionSettings settings,
            int projectionLight
    ) {
        int segments = scanlineSegments(height);
        float segmentHeight = height / segments;
        float visibleRatio = 0.72F;
        for (int i = 0; i < segments; i++) {
            float y0 = i * segmentHeight;
            float y1 = y0 + segmentHeight * visibleRatio;
            float t0 = y0 / height;
            float t1 = y1 / height;
            float v0 = lerp(bottomV, topV, t0);
            float v1 = lerp(bottomV, topV, t1);
            emitFrontQuad(consumer, matrix, pose, minX, maxX, y0, y1, v0, v1, settings, projectionLight);
        }
    }

    private static void emitBackScanlines(
            VertexConsumer consumer,
            Matrix4f matrix,
            PoseStack.Pose pose,
            float minX,
            float maxX,
            float height,
            float bottomV,
            float topV,
            float leftU,
            float rightU,
            ProjectionSettings settings,
            int projectionLight
    ) {
        int segments = scanlineSegments(height);
        float segmentHeight = height / segments;
        float visibleRatio = 0.72F;
        for (int i = 0; i < segments; i++) {
            float y0 = i * segmentHeight;
            float y1 = y0 + segmentHeight * visibleRatio;
            float t0 = y0 / height;
            float t1 = y1 / height;
            float v0 = lerp(bottomV, topV, t0);
            float v1 = lerp(bottomV, topV, t1);
            emitBackQuad(consumer, matrix, pose, minX, maxX, y0, y1, v0, v1,
                    leftU, rightU, settings, projectionLight);
        }
    }

    private static int scanlineSegments(float heightBlocks) {
        int projectedPixels = Math.max(1, Math.round(heightBlocks / PIXEL));
        return Math.max(8, Math.min(64, projectedPixels * 2));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static void emitFrontQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            PoseStack.Pose pose,
            float minX,
            float maxX,
            float minY,
            float maxY,
            float bottomV,
            float topV,
            ProjectionSettings settings,
            int projectionLight
    ) {
        vertex(consumer, matrix, pose, minX, minY, 0.0F, 0.0F, bottomV, 0.0F, 0.0F, 1.0F, settings, projectionLight);
        vertex(consumer, matrix, pose, maxX, minY, 0.0F, 1.0F, bottomV, 0.0F, 0.0F, 1.0F, settings, projectionLight);
        vertex(consumer, matrix, pose, maxX, maxY, 0.0F, 1.0F, topV, 0.0F, 0.0F, 1.0F, settings, projectionLight);
        vertex(consumer, matrix, pose, minX, maxY, 0.0F, 0.0F, topV, 0.0F, 0.0F, 1.0F, settings, projectionLight);
    }

    private static void emitBackQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            PoseStack.Pose pose,
            float minX,
            float maxX,
            float minY,
            float maxY,
            float bottomV,
            float topV,
            float leftU,
            float rightU,
            ProjectionSettings settings,
            int projectionLight
    ) {
        vertex(consumer, matrix, pose, minX, maxY, 0.0F, leftU, topV, 0.0F, 0.0F, -1.0F, settings, projectionLight);
        vertex(consumer, matrix, pose, maxX, maxY, 0.0F, rightU, topV, 0.0F, 0.0F, -1.0F, settings, projectionLight);
        vertex(consumer, matrix, pose, maxX, minY, 0.0F, rightU, bottomV, 0.0F, 0.0F, -1.0F, settings, projectionLight);
        vertex(consumer, matrix, pose, minX, minY, 0.0F, leftU, bottomV, 0.0F, 0.0F, -1.0F, settings, projectionLight);
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
            float nz,
            ProjectionSettings settings,
            int projectionLight
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(settings.tintRed(), settings.tintGreen(), settings.tintBlue(), settings.opacity())
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(projectionLight)
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
                blockEntity.chassisProfile(),
                !blockEntity.projectedStack().isEmpty()
        );
        if (!power.active()) {
            return new AABB(pos).inflate(0.5D, 0.5D, 0.5D);
        }

        boolean itemProjection = settings.sourceMode() == ProjectionSettings.SourceMode.ITEM
                && !blockEntity.projectedStack().isEmpty();
        boolean entityProjection = settings.sourceMode() == ProjectionSettings.SourceMode.ENTITY
                && (blockEntity.entityProjectionState().hasActiveEntity()
                || blockEntity.entityProjectionState().hasProjectedHumanoidEquipment());
        boolean imageProjection = settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && (blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                ? settings.hasAnyImage()
                : settings.hasImage());
        if (!imageProjection && !itemProjection && !entityProjection) {
            return new AABB(pos).inflate(0.5D, 1.0D, 0.5D);
        }

        ProjectionPower.Dimensions dimensions = ProjectionPower.dimensions(
                settings,
                !blockEntity.projectedStack().isEmpty(),
                blockEntity.chassisProfile()
        );
        double width = Math.max(PIXEL, dimensions.widthPixels() * PIXEL);
        double height = Math.max(PIXEL, dimensions.heightPixels() * PIXEL);

        boolean humanoidProjection = settings.sourceMode() == ProjectionSettings.SourceMode.ENTITY
                && (blockEntity.entityProjectionState().hasActiveEntity()
                ? blockEntity.entityProjectionState().activeKind()
                == celerbi.mirageprojector.entity.EntityScanData.Kind.HUMANOID
                : blockEntity.entityProjectionState().hasProjectedHumanoidEquipment());
        if (humanoidProjection) {
            double targetHeight = Math.max(PIXEL, settings.scalePixels() * PIXEL);
            width = Math.max(
                    width,
                    targetHeight * HumanoidPoseController.horizontalExtentMultiplier(
                            blockEntity.entityProjectionState().humanoidPose()
                    )
            );
            height = Math.max(
                    height,
                    targetHeight * HumanoidPoseController.verticalExtentMultiplier(
                            blockEntity.entityProjectionState().humanoidPose()
                    )
            );
        }

        double radius = blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                && settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                ? Math.max(0.75D, width * Math.sqrt(2.0D) * 0.5D + 0.25D)
                : Math.max(0.75D, width * 0.5D + 0.25D);
        double minY = pos.getY() + physicalTop(blockEntity) + settings.liftPixels() * PIXEL
                - settings.floatAmplitudePixels() * PIXEL - 0.25D;
        double maxY = pos.getY() + physicalTop(blockEntity) + settings.liftPixels() * PIXEL
                + height + 0.25D;
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
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private record EntityCacheEntry(
            ClientLevel level,
            UUID scanId,
            String equipmentFingerprint,
            LivingEntity entity
    ) {
    }

    private record EquippedItemCacheEntry(ClientLevel level, UUID snapshotId, LivingEntity entity) {
    }

    private record FaceSize(float width, float height) {
    }
}

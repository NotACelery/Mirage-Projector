package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionImageSizing;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class MirageProjectorRenderer implements BlockEntityRenderer<MirageProjectorBlockEntity> {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final UUID BODYLESS_CACHE_ID = new UUID(0L, 1L);
    /**
     * Entity projections are deferred until NeoForge AFTER_TRIPWIRE_BLOCKS.
     * At that point opaque/translucent world geometry and physical projector
     * bodies have already populated the frame/depth buffer, so a hologram can
     * depth-test against water and other projectors according to real distance
     * instead of submission order.
     *
     * The deferred pass owns its own BufferSource. Never call endBatch() on
     * Minecraft's shared renderBuffers().bufferSource() here: doing so flushes
     * unrelated Image/Banner/BlockEntity batches and was the dev.36 regression
     * that corrupted previously-correct image projection ordering.
     */
    private static final List<DeferredEntityProjection> DEFERRED_ENTITY_PROJECTIONS = new ArrayList<>();
    private static final ByteBufferBuilder DEFERRED_ENTITY_BUFFER = new ByteBufferBuilder(1 << 20);
    private static final MultiBufferSource.BufferSource DEFERRED_ENTITY_BUFFERS =
            MultiBufferSource.immediate(DEFERRED_ENTITY_BUFFER);

    private final Map<MirageProjectorBlockEntity, EntityCacheEntry> entityCache = new WeakHashMap<>();
    private final Map<MirageProjectorBlockEntity, EquippedItemCacheEntry> equippedItemCache = new WeakHashMap<>();
    private final ModelPart bannerFlag;

    public MirageProjectorRenderer(BlockEntityRendererProvider.Context context) {
        bannerFlag = context.bakeLayer(ModelLayers.BANNER).getChild(BannerRenderer.FLAG);
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

        double gameTime = blockEntity.getLevel() == null ? 0.0D : blockEntity.getLevel().getGameTime() + partialTick;
        float angle = projectionBaseAngle(blockEntity, settings) + rotationAngle(settings, gameTime);
        float bob = bobOffset(settings, gameTime);
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        // Idle identity marker: every current chassis shows the same floating
        // vanilla book when there is no renderable source configured. This must
        // not depend on having a Core installed; otherwise only Compact (which
        // historically starts with Glass) gets the idle book while the other five
        // chassis appear dead/unfinished.
        boolean hasProjectedContent = blockEntity.hasProjectedSourceContent();
        if (!hasProjectedContent) {
            equippedItemCache.remove(blockEntity);
            renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            return;
        }

        ProjectionPower.Status power = ProjectionPower.evaluate(
                settings,
                core,
                blockEntity.chassisProfile(),
                true,
                blockEntity.projectedSourceCount()
        );
        if (!power.active()) {
            return;
        }

        if (settings.sourceMode() == ProjectionSettings.SourceMode.BANNER) {
            renderProjectedBanners(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight, gameTime);
            return;
        }

        if (settings.sourceMode() == ProjectionSettings.SourceMode.ITEM) {
            if (blockEntity.projectedStack().isEmpty()) {
                equippedItemCache.remove(blockEntity);
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
            deferEntityProjection(this, blockEntity, partialTick, packedLight);
            return;
        }

        if (blockEntity.chassisProfile().supportsMultiSourceImageLayout()
                && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
            if (!blockEntity.imageSourceBank().hasAny(blockEntity.chassisProfile().imageLayoutSlots())) {
                return;
            }
            renderMultiSourceImageLayout(
                    blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight
            );
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

        if (!hasPlaneImageContent(settings)) {
            renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            return;
        }

        renderImage(
                blockEntity,
                settings,
                poseStack,
                bufferSource,
                angle,
                bob,
                projectionLight
        );
    }

    private void renderProjectedBanners(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight,
            double gameTime
    ) {
        if (!blockEntity.hasAnyBannerSnapshot()) {
            return;
        }

        MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(bufferSource, settings);
        float bannerHeight = settings.scalePixels() * PIXEL;
        float modelScale = Math.max(PIXEL, settings.scalePixels() / 40.0F);
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom + bannerHeight, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        if (blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM) {
            float bannerWidth = bannerHeight * 0.5F;
            float radius = Math.max(PIXEL, bannerWidth * 0.5F);
            renderBannerFace(blockEntity.bannerSnapshot(0), 180.0F, radius, 0, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight);
            renderBannerFace(blockEntity.bannerSnapshot(1), 90.0F, radius, 1, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight);
            renderBannerFace(blockEntity.bannerSnapshot(2), 0.0F, radius, 2, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight);
            renderBannerFace(blockEntity.bannerSnapshot(3), -90.0F, radius, 3, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight);
        } else {
            renderBannerFace(blockEntity.bannerSnapshot(0), 0.0F, 0.0F, 0, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight);
        }

        poseStack.popPose();
    }

    private void renderBannerFace(
            ItemStack stack,
            float faceRotationDegrees,
            float radius,
            int faceIndex,
            double gameTime,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float modelScale,
            int projectionLight
    ) {
        if (!(stack.getItem() instanceof BannerItem bannerItem)) {
            return;
        }

        PartPose previous = bannerFlag.storePose();
        poseStack.pushPose();
        try {
            poseStack.mulPose(Axis.YP.rotationDegrees(faceRotationDegrees));
            poseStack.translate(0.0D, 0.0D, radius);

            // The baked vanilla flag is 20x40 model pixels. Remove the standing-
            // banner pivot and scale only the cloth, deliberately omitting pole/bar.
            bannerFlag.setPos(0.0F, 0.0F, 0.0F);
            float wave = (float) (-0.035D + Math.sin(gameTime * 0.08D + faceIndex * 0.7D) * 0.025D);
            bannerFlag.setRotation(wave, 0.0F, 0.0F);

            poseStack.scale(modelScale, -modelScale, -modelScale);
            BannerPatternLayers patterns = stack.getOrDefault(
                    DataComponents.BANNER_PATTERNS,
                    BannerPatternLayers.EMPTY
            );
            BannerRenderer.renderPatterns(
                    poseStack,
                    bufferSource,
                    projectionLight,
                    OverlayTexture.NO_OVERLAY,
                    bannerFlag,
                    ModelBakery.BANNER_BASE,
                    true,
                    bannerItem.getColor(),
                    patterns,
                    stack.hasFoil()
            );
        } finally {
            bannerFlag.loadPose(previous);
            poseStack.popPose();
        }
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

    private static void deferEntityProjection(
            MirageProjectorRenderer renderer,
            MirageProjectorBlockEntity blockEntity,
            float partialTick,
            int packedLight
    ) {
        // A BER may be visited more than once by compatibility render paths. Keep
        // one entry per physical projector for the current frame.
        DEFERRED_ENTITY_PROJECTIONS.removeIf(entry -> entry.blockEntity() == blockEntity);
        DEFERRED_ENTITY_PROJECTIONS.add(new DeferredEntityProjection(renderer, blockEntity, partialTick, packedLight));
    }

    /** Called from ClientRuntimeEvents at NeoForge AFTER_TRIPWIRE_BLOCKS. */
    public static void flushDeferredEntityProjections(PoseStack poseStack, Vec3 cameraPosition) {
        if (poseStack == null || cameraPosition == null || DEFERRED_ENTITY_PROJECTIONS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            DEFERRED_ENTITY_PROJECTIONS.clear();
            return;
        }

        List<DeferredEntityProjection> frame = new ArrayList<>(DEFERRED_ENTITY_PROJECTIONS);
        DEFERRED_ENTITY_PROJECTIONS.clear();

        // Transparent holograms need the same far-to-near ordering users expect
        // from normal translucent geometry. Opaque projections are also safe in
        // this order because they still use normal depth writes.
        frame.sort(Comparator.comparingDouble((DeferredEntityProjection entry) -> {
            BlockPos pos = entry.blockEntity().getBlockPos();
            double dx = pos.getX() + 0.5D - cameraPosition.x;
            double dy = pos.getY() + 0.5D - cameraPosition.y;
            double dz = pos.getZ() + 0.5D - cameraPosition.z;
            return dx * dx + dy * dy + dz * dz;
        }).reversed());

        MultiBufferSource.BufferSource buffers = DEFERRED_ENTITY_BUFFERS;
        for (DeferredEntityProjection entry : frame) {
            MirageProjectorBlockEntity blockEntity = entry.blockEntity();
            if (blockEntity.isRemoved() || blockEntity.getLevel() != minecraft.level) {
                continue;
            }

            BlockPos pos = blockEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(
                    pos.getX() - cameraPosition.x,
                    pos.getY() - cameraPosition.y,
                    pos.getZ() - cameraPosition.z
            );
            entry.renderer().renderDeferredEntityProjection(
                    blockEntity,
                    entry.partialTick(),
                    poseStack,
                    buffers,
                    entry.packedLight()
            );
            poseStack.popPose();
        }

        // Flush only Mirage's private deferred source. Physical projectors,
        // images, banners and vanilla/modded renderers keep their own global
        // batching lifecycle untouched.
        buffers.endBatch();
    }

    public static void clearDeferredEntityProjections() {
        DEFERRED_ENTITY_PROJECTIONS.clear();
    }

    private void renderDeferredEntityProjection(
            MirageProjectorBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        ProjectionSettings settings = blockEntity.settings();
        if (settings.sourceMode() != ProjectionSettings.SourceMode.ENTITY) {
            return;
        }

        ProjectionPower.Status power = ProjectionPower.evaluate(
                settings,
                blockEntity.coreProfile(),
                blockEntity.chassisProfile(),
                blockEntity.hasProjectedSourceContent(),
                blockEntity.projectedSourceCount()
        );
        if (!power.active()) {
            return;
        }

        LivingEntity entity = resolveProjectedEntity(blockEntity);
        if (entity == null) {
            return;
        }

        double gameTime = blockEntity.getLevel() == null
                ? 0.0D
                : blockEntity.getLevel().getGameTime() + partialTick;
        float angle = blockFacingAngle(blockEntity) + rotationAngle(settings, gameTime);
        float bob = bobOffset(settings, gameTime);
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

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
        renderProjectionNameplate(blockEntity, poseStack, bufferSource, projectionLight);
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
        try (ProjectionRenderContext.Scope ignored = ProjectionRenderContext.push(entity, settings)) {
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
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int projectionLight
    ) {
        String text = blockEntity.entityProjectionState().projectionNameplate();
        if (text == null || text.isBlank()) {
            return;
        }

        // The projection name belongs to the projector/base, not to the scaled
        // hologram envelope. Keeping it here makes a nametag readable even when
        // Scale is enormous or Lift moves the entity many blocks away.
        float labelY = physicalTop(blockEntity) + 3.0F * PIXEL;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component label = Component.literal(text);

        poseStack.pushPose();
        poseStack.translate(0.5D, labelY, 0.5D);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        float textX = -font.width(label) / 2.0F;
        int textColor = 0xFFFFFFFF;
        int backgroundColor = 0x60000000;

        // First pass guarantees readability through the translucent projection;
        // second pass keeps normal depth behaviour against opaque world geometry.
        font.drawInBatch(
                label, textX, 0.0F, 0x60FFFFFF, false, matrix, bufferSource,
                Font.DisplayMode.SEE_THROUGH, backgroundColor, projectionLight
        );
        font.drawInBatch(
                label, textX, 0.0F, textColor, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, projectionLight
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
        poseStack.translate(0.5D, chassis.legacyCoreCenterYPixels() * PIXEL, 0.5D);
        poseStack.scale(
                chassis.legacyCoreWidthPixels() * PIXEL,
                chassis.legacyCoreHeightPixels() * PIXEL,
                chassis.legacyCoreDepthPixels() * PIXEL
        );
        Minecraft.getInstance().getItemRenderer().renderStatic(
                core.legacyBlockVisualStack(),
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
     * Multi-source Plane layouts are one physical surface divided into virtual cells.
     * Wide = 4x1 and Tall = 1x4. Field is deliberately a single continuous Plane.
     * Each source preserves its own aspect ratio inside the cell while global Scale
     * controls the complete layout envelope.
     */
    private static void renderMultiSourceImageLayout(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        ProjectionChassisProfile chassis = blockEntity.chassisProfile();
        int columns = Math.max(1, chassis.imageLayoutColumns());
        int rows = Math.max(1, chassis.imageLayoutRows());
        int slots = Math.min(chassis.imageLayoutSlots(), ImageSourceBank.ACTIVE_MULTI_SLOTS);
        ProjectionPower.Dimensions layout = ProjectionPower.dimensions(settings, true, chassis);
        float layoutWidth = Math.max(PIXEL, layout.widthPixels() * PIXEL);
        float layoutHeight = Math.max(PIXEL, layout.heightPixels() * PIXEL);
        float cellWidth = layoutWidth / columns;
        float cellHeight = layoutHeight / rows;
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;
        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;
        boolean viewingFront = isCameraOnFrontSide(blockEntity, angle);
        ProjectionSettings.BackFaceMode planeMode = settings.backFaceMode();
        // Wide/Tall MULTI currently have one source bank. FRONT is the new
        // one-sided default; MIRRORED/READABLE may reuse that bank on the rear.
        // BACK/INDEPENDENT require a real rear bank and therefore render no
        // fabricated fallback here.
        if ((viewingFront && planeMode == ProjectionSettings.BackFaceMode.BACK)
                || (!viewingFront && (planeMode == ProjectionSettings.BackFaceMode.FRONT
                || planeMode == ProjectionSettings.BackFaceMode.BACK
                || planeMode == ProjectionSettings.BackFaceMode.INDEPENDENT))) {
            return;
        }
        boolean readableBack = planeMode == ProjectionSettings.BackFaceMode.READABLE;

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        long animationSampleMs = (System.nanoTime() / 1_000_000L);

        for (int slot = 0; slot < slots; slot++) {
            ImageSourceBank.Asset asset = blockEntity.imageSourceBank().get(slot);
            if (!asset.present()) continue;
            var texture = ProjectionTextureCache.get(asset.id(), animationSampleMs);
            if (texture.isEmpty()) continue;

            int column = slot % columns;
            int row = slot / columns;
            FaceSize fitted = fitFaceToCell(asset.width(), asset.height(), cellWidth, cellHeight);
            float cellLeft = -layoutWidth * 0.5F + column * cellWidth;
            float cellBottom = layoutHeight - (row + 1) * cellHeight;
            float sourceLeft = cellLeft + (cellWidth - fitted.width()) * 0.5F;
            float sourceBottom = cellBottom + (cellHeight - fitted.height()) * 0.5F;

            poseStack.pushPose();
            poseStack.translate(sourceLeft + fitted.width() * 0.5F, sourceBottom, 0.0D);
            PoseStack.Pose pose = poseStack.last();
            if (viewingFront) {
                renderFrontFace(texture.get(), fitted, topV, bottomV, settings, projectionLight, pose.pose(), pose, bufferSource);
            } else {
                renderBackFace(texture.get(), fitted, topV, bottomV, readableBack, settings, projectionLight, pose.pose(), pose, bufferSource);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static FaceSize fitFaceToCell(int imageWidth, int imageHeight, float cellWidth, float cellHeight) {
        float safeW = Math.max(PIXEL, cellWidth * 0.94F);
        float safeH = Math.max(PIXEL, cellHeight * 0.94F);
        float ratio = Math.max(1, imageWidth) / (float) Math.max(1, imageHeight);
        float width = safeW;
        float height = width / ratio;
        if (height > safeH) {
            height = safeH;
            width = height * ratio;
        }
        return new FaceSize(width, height);
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
        long animationSampleMs = (System.nanoTime() / 1_000_000L);

        boolean rendered = false;
        rendered |= renderPrismFace(
                poseStack,
                settings.imageId(), settings.imageWidth(), settings.imageHeight(), settings.hasImage(), animationSampleMs,
                180.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );
        rendered |= renderPrismFace(
                poseStack,
                settings.eastImageId(), settings.eastImageWidth(), settings.eastImageHeight(), settings.hasEastImage(), animationSampleMs,
                90.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );
        rendered |= renderPrismFace(
                poseStack,
                settings.backImageId(), settings.backImageWidth(), settings.backImageHeight(), settings.hasBackImage(), animationSampleMs,
                0.0F, radius, topV, bottomV, settings, bufferSource, projectionLight
        );
        rendered |= renderPrismFace(
                poseStack,
                settings.westImageId(), settings.westImageWidth(), settings.westImageHeight(), settings.hasWestImage(), animationSampleMs,
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
            long animationSampleMs,
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
        var texture = ProjectionTextureCache.get(assetId, animationSampleMs);
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

    private static boolean hasPlaneImageContent(ProjectionSettings settings) {
        return switch (settings.backFaceMode()) {
            case FRONT, MIRRORED, READABLE -> settings.hasImage();
            case BACK -> settings.hasBackImage();
            case INDEPENDENT -> settings.hasImage() || settings.hasBackImage();
        };
    }

    /**
     * Plane source presentation. FRONT/BACK are truly one-sided. MIRRORED and
     * READABLE reuse Front on the rear. INDEPENDENT uses the stored Back asset
     * and deliberately does not fall back to Front when Back is missing.
     */
    private static void renderImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        boolean viewingFront = isCameraOnFrontSide(blockEntity, angle);
        ProjectionSettings.BackFaceMode mode = settings.backFaceMode();

        String assetId;
        int imageWidth;
        int imageHeight;
        boolean renderAsBack;
        boolean readableBack;

        if (viewingFront) {
            if (mode == ProjectionSettings.BackFaceMode.BACK || !settings.hasImage()) {
                return;
            }
            assetId = settings.imageId();
            imageWidth = settings.imageWidth();
            imageHeight = settings.imageHeight();
            renderAsBack = false;
            readableBack = false;
        } else {
            switch (mode) {
                case FRONT -> {
                    return;
                }
                case BACK -> {
                    if (!settings.hasBackImage()) return;
                    assetId = settings.backImageId();
                    imageWidth = settings.backImageWidth();
                    imageHeight = settings.backImageHeight();
                    renderAsBack = true;
                    readableBack = true;
                }
                case MIRRORED -> {
                    if (!settings.hasImage()) return;
                    assetId = settings.imageId();
                    imageWidth = settings.imageWidth();
                    imageHeight = settings.imageHeight();
                    renderAsBack = true;
                    readableBack = false;
                }
                case READABLE -> {
                    if (!settings.hasImage()) return;
                    assetId = settings.imageId();
                    imageWidth = settings.imageWidth();
                    imageHeight = settings.imageHeight();
                    renderAsBack = true;
                    readableBack = true;
                }
                case INDEPENDENT -> {
                    if (!settings.hasBackImage()) return;
                    assetId = settings.backImageId();
                    imageWidth = settings.backImageWidth();
                    imageHeight = settings.backImageHeight();
                    renderAsBack = true;
                    readableBack = true;
                }
                default -> throw new IllegalStateException("Unhandled plane mode: " + mode);
            }
        }

        var texture = ProjectionTextureCache.get(assetId);
        if (texture.isEmpty()) {
            renderMissingAsset(blockEntity, poseStack, bufferSource, angle, bob, projectionLight);
            return;
        }

        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;
        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;
        FaceSize size = faceSize(imageWidth, imageHeight, settings.scalePixels());

        poseStack.pushPose();
        poseStack.translate(0.5D, bottom, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        if (renderAsBack) {
            renderBackFace(texture.get(), size, topV, bottomV, readableBack, settings, projectionLight, matrix, pose, bufferSource);
        } else {
            renderFrontFace(texture.get(), size, topV, bottomV, settings, projectionLight, matrix, pose, bufferSource);
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
        ProjectionImageSizing.Size size = ProjectionImageSizing.size(imageWidth, imageHeight, scalePixels);
        return new FaceSize(size.widthPixels() * PIXEL, size.heightPixels() * PIXEL);
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
        RenderType projectionType = settings.opacityPercent() < 100
                ? ProjectionRenderTypes.ghostEntity(texture)
                : RenderType.entityTranslucent(texture);
        VertexConsumer consumer = bufferSource.getBuffer(projectionType);
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
        RenderType projectionType = settings.opacityPercent() < 100
                ? ProjectionRenderTypes.ghostEntity(texture)
                : RenderType.entityTranslucent(texture);
        VertexConsumer consumer = bufferSource.getBuffer(projectionType);
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

    /**
     * Plane-like projections inherit the furnace-style placement facing. Prism Image/Banner
     * assignments are true world-cardinal North/East/South/West, so rotating the physical
     * symmetric Prism block on placement must not remap those four source slots.
     */
    private static float projectionBaseAngle(MirageProjectorBlockEntity blockEntity, ProjectionSettings settings) {
        if (blockEntity != null
                && blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || settings.sourceMode() == ProjectionSettings.SourceMode.BANNER)) {
            return 0.0F;
        }
        return blockFacingAngle(blockEntity);
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
                blockEntity.hasProjectedSourceContent(),
                blockEntity.projectedSourceCount()
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
                && (blockEntity.chassisProfile().supportsMultiSourceImageLayout()
                && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI
                ? blockEntity.imageSourceBank().hasAny(blockEntity.chassisProfile().imageLayoutSlots())
                : blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                ? settings.hasAnyImage()
                : hasPlaneImageContent(settings));
        boolean bannerProjection = settings.sourceMode() == ProjectionSettings.SourceMode.BANNER
                && blockEntity.hasAnyBannerSnapshot();
        if (!imageProjection && !itemProjection && !entityProjection && !bannerProjection) {
            return new AABB(pos).inflate(0.5D, 1.0D, 0.5D);
        }

        ProjectionPower.Dimensions dimensions = ProjectionPower.dimensions(
                settings,
                blockEntity.hasProjectedSourceContent(),
                blockEntity.chassisProfile()
        );
        double width = Math.max(PIXEL, dimensions.widthPixels() * PIXEL);
        double height = Math.max(PIXEL, dimensions.heightPixels() * PIXEL);

        if (settings.sourceMode() == ProjectionSettings.SourceMode.ENTITY) {
            EntityProjectionBounds.Bounds entityBounds = EntityProjectionBounds.projected(
                    blockEntity.entityProjectionState(), settings);
            width = Math.max(PIXEL, entityBounds.widthPixels() * PIXEL);
            height = Math.max(PIXEL, entityBounds.heightPixels() * PIXEL);
        }

        double radius = blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || settings.sourceMode() == ProjectionSettings.SourceMode.BANNER)
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

    private record DeferredEntityProjection(
            MirageProjectorRenderer renderer,
            MirageProjectorBlockEntity blockEntity,
            float partialTick,
            int packedLight
    ) {
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

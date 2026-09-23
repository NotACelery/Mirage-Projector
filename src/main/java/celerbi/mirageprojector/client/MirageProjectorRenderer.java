package celerbi.mirageprojector.client;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.EndResonanceGeometry;
import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectorVisualLayout;
import celerbi.mirageprojector.ProjectionImageSizing;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.PrismProjectionSpacing;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.ProjectionTransform;
import celerbi.mirageprojector.WallProjectionSurface;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MirageProjectorRenderer implements BlockEntityRenderer<MirageProjectorBlockEntity> {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float ENTITY_NAMEPLATE_GAP = 4.0F * PIXEL;
    private static final float ENTITY_NAMEPLATE_BOUND_HEIGHT = 0.5F;
    private static final UUID BODYLESS_CACHE_ID = new UUID(0L, 1L);
    private static final ResourceLocation PROJECTION_CANCELLATION_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "mirage_projector", "textures/misc/projection_cancellation.png"
    );

    private static final List<DeferredEntityProjection> DEFERRED_ENTITY_PROJECTIONS = new ArrayList<>();
    private static final MultiBufferSource.BufferSource DEFERRED_ENTITY_BUFFERS = createDeferredEntityBuffers();

    static {
        ProjectionSourceRenderRegistry.registerBuiltin(
                ProjectionSettings.SourceMode.IMAGE,
                context -> context.renderer().renderImageSource(context)
        );
        ProjectionSourceRenderRegistry.registerBuiltin(
                ProjectionSettings.SourceMode.ITEM,
                context -> context.renderer().renderItemSource(context)
        );
        ProjectionSourceRenderRegistry.registerBuiltin(
                ProjectionSettings.SourceMode.ENTITY,
                context -> context.renderer().renderEntitySource(context)
        );
        ProjectionSourceRenderRegistry.registerBuiltin(
                ProjectionSettings.SourceMode.BANNER,
                context -> context.renderer().renderBannerSource(context)
        );
    }

    private final Map<MirageProjectorBlockEntity, EntityCacheEntry> entityCache = new WeakHashMap<>();
    private final Map<MirageProjectorBlockEntity, EquippedItemCacheEntry> equippedItemCache = new WeakHashMap<>();
    private final ModelPart bannerFlag;

    private static MultiBufferSource.BufferSource createDeferredEntityBuffers() {
        LinkedHashMap<RenderType, ByteBufferBuilder> fixedBuffers = new LinkedHashMap<>();
        addDeferredBuffer(fixedBuffers, Sheets.solidBlockSheet());
        addDeferredBuffer(fixedBuffers, Sheets.cutoutBlockSheet());
        addDeferredBuffer(fixedBuffers, Sheets.bannerSheet());
        addDeferredBuffer(fixedBuffers, Sheets.translucentCullBlockSheet());
        addDeferredBuffer(fixedBuffers, Sheets.translucentItemSheet());
        addDeferredBuffer(fixedBuffers, Sheets.shieldSheet());
        addDeferredBuffer(fixedBuffers, Sheets.bedSheet());
        addDeferredBuffer(fixedBuffers, Sheets.shulkerBoxSheet());
        addDeferredBuffer(fixedBuffers, Sheets.signSheet());
        addDeferredBuffer(fixedBuffers, Sheets.hangingSignSheet());
        addDeferredBuffer(fixedBuffers, Sheets.chestSheet());
        addDeferredBuffer(fixedBuffers, ProjectionRenderTypes.lateGhostItem(InventoryMenu.BLOCK_ATLAS));
        addDeferredBuffer(fixedBuffers, ProjectionRenderTypes.lateGhostEntity(Sheets.SHIELD_SHEET));
        addDeferredBuffer(fixedBuffers, ProjectionRenderTypes.lateGhostEntity(Sheets.BANNER_SHEET));
        addDeferredBuffer(fixedBuffers, ProjectionRenderTypes.lateGhostEntity(Sheets.ARMOR_TRIMS_SHEET));
        addDeferredBuffer(fixedBuffers, RenderType.armorEntityGlint());
        addDeferredBuffer(fixedBuffers, RenderType.glint());
        addDeferredBuffer(fixedBuffers, RenderType.glintTranslucent());
        addDeferredBuffer(fixedBuffers, RenderType.entityGlint());
        addDeferredBuffer(fixedBuffers, RenderType.entityGlintDirect());
        addDeferredBuffer(fixedBuffers, RenderType.waterMask());
        return MultiBufferSource.immediateWithBuffers(fixedBuffers, new ByteBufferBuilder(1 << 20));
    }

    private static void addDeferredBuffer(
            LinkedHashMap<RenderType, ByteBufferBuilder> fixedBuffers,
            RenderType renderType
    ) {
        fixedBuffers.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
    }

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
        double gameTime = blockEntity.getLevel() == null ? 0.0D : blockEntity.getLevel().getGameTime() + partialTick;
        renderCoreItem(blockEntity, poseStack, bufferSource, gameTime);
        renderDockedPresentationRemote(blockEntity, poseStack, bufferSource, packedLight);
        if (blockEntity.endResonanceActive()) {
            renderEndResonance(blockEntity, poseStack, bufferSource, gameTime);
            equippedItemCache.remove(blockEntity);
            return;
        }
        if (!blockEntity.projectionEnabled()) {
            equippedItemCache.remove(blockEntity);
            return;
        }

        ProjectionChassisProfile chassis = blockEntity.chassisProfile();
        if (chassis == ProjectionChassisProfile.TABLE) {
            renderTableProjectorRuntime(
                    blockEntity, settings, partialTick, poseStack, bufferSource, packedLight, gameTime
            );
            return;
        }
        float angle = projectionBaseAngle(blockEntity, settings)
                + (chassis.supportsRotation() ? rotationAngle(settings, gameTime) : 0.0F);
        float bob = chassis.supportsFloating() ? bobOffset(settings, gameTime) : 0.0F;
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        boolean hasProjectedContent = blockEntity.hasProjectedSourceContent();
        if (!hasProjectedContent) {
            equippedItemCache.remove(blockEntity);
            if (chassis != ProjectionChassisProfile.WALL) {
                renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            }
            return;
        }

        ProjectionPower.Status power = blockEntity.powerStatus();
        if (!power.active()) {
            return;
        }

        ProjectionSourceRenderRegistry.renderer(settings.sourceMode()).ifPresent(renderer -> renderer.render(
                new ProjectionSourceRenderRegistry.RenderContext(
                        this,
                        blockEntity,
                        settings,
                        partialTick,
                        poseStack,
                        bufferSource,
                        packedLight,
                        angle,
                        bob,
                        projectionLight,
                        gameTime
                )
        ));
    }

    /** Renders the Table chassis through its dedicated placement rules. */
    private void renderTableProjectorRuntime(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            double gameTime
    ) {
        if (!blockEntity.projectionEnabled()) {
            equippedItemCache.remove(blockEntity);
            return;
        }

        float angle = MirageTableProjectorLogic.projectionAngle(blockEntity, settings, gameTime);
        float bob = MirageTableProjectorLogic.bobOffset(settings, gameTime);
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        if (!blockEntity.hasProjectedSourceContent()) {
            equippedItemCache.remove(blockEntity);
            renderBook(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight);
            return;
        }
        if (!blockEntity.powerStatus().active()) {
            return;
        }

        ProjectionSourceRenderRegistry.RenderContext context = new ProjectionSourceRenderRegistry.RenderContext(
                this,
                blockEntity,
                settings,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                angle,
                bob,
                projectionLight,
                gameTime
        );
        ProjectionSourceRenderRegistry.renderer(settings.sourceMode()).ifPresent(renderer -> renderer.render(context));
    }

    private void renderEndResonance(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            double gameTime
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, blockEntity.chassisProfile().physicalTopPixels() * PIXEL, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(blockFacingAngle(blockEntity)));
        if (blockEntity.chassisProfile() == ProjectionChassisProfile.PRISM) {
            renderEndResonancePrism(poseStack, bufferSource, gameTime);
        } else if (blockEntity.chassisProfile() == ProjectionChassisProfile.TABLE) {
            renderEndResonanceTable(poseStack, bufferSource, gameTime);
        } else {
            renderEndResonanceField(poseStack, bufferSource, gameTime);
        }
        poseStack.popPose();
    }

    private static void renderEndResonanceField(
            PoseStack poseStack, MultiBufferSource bufferSource, double gameTime
    ) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endPortal());
        float uShift = (float) ((gameTime * 0.003D) % 1.0D);
        emitResonanceQuad(consumer, matrix, pose, -1.0F, 1.0F, 0.0F, 3.0F, 0.002F, uShift, false);
        emitResonanceQuad(consumer, matrix, pose, -1.0F, 1.0F, 0.0F, 3.0F, -0.002F, uShift, true);
    }

    private static void renderEndResonanceTable(
            PoseStack poseStack, MultiBufferSource bufferSource, double gameTime
    ) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endPortal());
        float shift = (float) ((gameTime * 0.003D) % 1.0D);
        // Separate the top and bottom sheets by a few microunits so a close camera never makes
        // the two translucent faces contend for the same depth value.
        emitHorizontalResonanceFace(poseStack, consumer, 0.002F, 1.5F, shift, true);
        emitHorizontalResonanceFace(poseStack, consumer, -0.002F, 1.5F, shift, false);
    }

    private static void renderEndResonancePrism(
            PoseStack poseStack, MultiBufferSource bufferSource, double gameTime
    ) {
        float half = 1.0F;
        float height = 3.0F;
        float shift = (float) ((gameTime * 0.003D) % 1.0D);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endPortal());

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, half);
        emitResonanceQuad(consumer, poseStack.last().pose(), poseStack.last(), -half, half, 0.0F, height, 0.0F, shift, false);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, 0.0F, half);
        emitResonanceQuad(consumer, poseStack.last().pose(), poseStack.last(), -half, half, 0.0F, height, 0.0F, shift, false);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, 0.0F, half);
        emitResonanceQuad(consumer, poseStack.last().pose(), poseStack.last(), -half, half, 0.0F, height, 0.0F, shift, false);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, 0.0F, half);
        emitResonanceQuad(consumer, poseStack.last().pose(), poseStack.last(), -half, half, 0.0F, height, 0.0F, shift, false);
        poseStack.popPose();

        // Ceiling/floor close the anomaly visually so it reads as a volume rather than four flat portals.
        emitHorizontalResonanceFace(poseStack, consumer, 0.0F, half, shift, true);
        emitHorizontalResonanceFace(poseStack, consumer, height, half, shift, false);
    }

    private static void emitHorizontalResonanceFace(
            PoseStack poseStack, VertexConsumer consumer, float y, float half, float shift, boolean upward
    ) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        float ny = upward ? 1.0F : -1.0F;
        resonanceVertex(consumer, matrix, pose, -half, y, -half, shift, 0.0F, 0.0F, ny, 0.0F);
        resonanceVertex(consumer, matrix, pose, -half, y, half, shift, 1.0F, 0.0F, ny, 0.0F);
        resonanceVertex(consumer, matrix, pose, half, y, half, shift + 1.0F, 1.0F, 0.0F, ny, 0.0F);
        resonanceVertex(consumer, matrix, pose, half, y, -half, shift + 1.0F, 0.0F, 0.0F, ny, 0.0F);
    }

    private static void emitResonanceQuad(
            VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
            float minX, float maxX, float minY, float maxY, float z, float uShift, boolean back
    ) {
        float nz = back ? -1.0F : 1.0F;
        if (!back) {
            resonanceVertex(consumer, matrix, pose, minX, minY, z, uShift, 1.0F, 0.0F, 0.0F, nz);
            resonanceVertex(consumer, matrix, pose, maxX, minY, z, uShift + 1.0F, 1.0F, 0.0F, 0.0F, nz);
            resonanceVertex(consumer, matrix, pose, maxX, maxY, z, uShift + 1.0F, 0.0F, 0.0F, 0.0F, nz);
            resonanceVertex(consumer, matrix, pose, minX, maxY, z, uShift, 0.0F, 0.0F, 0.0F, nz);
        } else {
            resonanceVertex(consumer, matrix, pose, minX, maxY, z, uShift, 0.0F, 0.0F, 0.0F, nz);
            resonanceVertex(consumer, matrix, pose, maxX, maxY, z, uShift + 1.0F, 0.0F, 0.0F, 0.0F, nz);
            resonanceVertex(consumer, matrix, pose, maxX, minY, z, uShift + 1.0F, 1.0F, 0.0F, 0.0F, nz);
            resonanceVertex(consumer, matrix, pose, minX, minY, z, uShift, 1.0F, 0.0F, 0.0F, nz);
        }
    }

    private static void resonanceVertex(
            VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
            float x, float y, float z, float u, float v, float nx, float ny, float nz
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(0.80F, 0.38F, 1.0F, 0.94F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    /** Renders only the hologram carried by a Hand Projector. */
    public void renderPortableProjection(
            MirageProjectorBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            Vec3 cameraPosition,
            Vec3 worldAnchor,
            float outerYawDegrees
    ) {
        if (blockEntity == null || !blockEntity.projectionEnabled() || !blockEntity.hasProjectedSourceContent()) {
            return;
        }

        ProjectionSettings settings = blockEntity.settings();
        double gameTime = blockEntity.getLevel() == null
                ? 0.0D
                : blockEntity.getLevel().getGameTime() + partialTick;
        float angle = projectionBaseAngle(blockEntity, settings) + rotationAngle(settings, gameTime);
        float bob = bobOffset(settings, gameTime);
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;

        // Entity projections normally enter a deferred world pass so they can be depth-sorted
        // against fixed projector blocks. A Hand Projector is already being rendered in the
        // player's interpolated pose stack; deferring it would throw that pose away and snap the
        // projection back to the temporary block entity's integer coordinates.
        if (settings.sourceMode() == ProjectionSettings.SourceMode.ENTITY) {
            LivingEntity entity = resolveProjectedEntity(blockEntity);
            if (entity != null) {
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
                        blockEntity.entityProjectionState().activeKind()
                                == celerbi.mirageprojector.entity.EntityScanData.Kind.HUMANOID
                                || !blockEntity.entityProjectionState().hasActiveEntity(),
                        settings.opacityPercent() < 100
                );
                renderProjectionNameplate(blockEntity, settings, bob, poseStack, bufferSource, projectionLight);
            }
            return;
        }

        if (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE) {
            // The held renderer supplies the real world-space anchor/yaw.  The temporary block
            // entity itself is always cardinal, so its fixed-projector side test would otherwise
            // flip FRONT/MIRRORED/READABLE when the player crosses north or south.
            renderImage(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight,
                    cameraOnPortableImageFront(cameraPosition, worldAnchor, outerYawDegrees + angle, settings));
            return;
        }

        ProjectionSourceRenderRegistry.renderer(settings.sourceMode()).ifPresent(renderer -> renderer.render(
                new ProjectionSourceRenderRegistry.RenderContext(
                        this,
                        blockEntity,
                        settings,
                        partialTick,
                        poseStack,
                        bufferSource,
                        packedLight,
                        angle,
                        bob,
                        projectionLight,
                        gameTime
                )
        ));
    }

    /** Renders the Hand Projector's two-dimensional image directly on an aimed block face. */
    public void renderPortableSurfaceImage(
            MirageProjectorBlockEntity blockEntity,
            Vec3 surfacePoint,
            Direction surfaceNormal,
            Vec3 cameraPosition,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (blockEntity == null || surfacePoint == null || surfaceNormal == null) return;
        ProjectionSettings settings = blockEntity.settings();
        if (!blockEntity.projectionEnabled() || settings.sourceMode() != ProjectionSettings.SourceMode.IMAGE || !settings.hasImage()) {
            return;
        }
        var texture = ProjectionTextureCache.get(settings.imageId());
        if (texture.isEmpty()) return;

        FaceSize size = faceSize(settings.imageWidth(), settings.imageHeight(), settings.scalePixels());
        Vec3 normal = Vec3.atLowerCornerOf(surfaceNormal.getNormal());
        int projectionLight = settings.fullbright() ? LightTexture.FULL_BRIGHT : packedLight;
        poseStack.pushPose();
        poseStack.translate(
                surfacePoint.x - cameraPosition.x + normal.x * 0.003D,
                surfacePoint.y - cameraPosition.y + normal.y * 0.003D,
                surfacePoint.z - cameraPosition.z + normal.z * 0.003D
        );
        Vec3 normalVector = Vec3.atLowerCornerOf(surfaceNormal.getNormal());
        Quaternionf orientation = new Quaternionf().rotationTo(0.0F, 0.0F, 1.0F,
                surfaceNormal.getStepX(), surfaceNormal.getStepY(), surfaceNormal.getStepZ());
        // A vertical wall always keeps world-up. For floor and ceiling faces, roll the plane so
        // its image-up vector points toward the viewer; this makes the artwork turn with the
        // camera rather than being locked to north/south.
        if (Math.abs(normalVector.y) > 0.9D) {
            Vec3 towardCamera = cameraPosition.subtract(surfacePoint);
            towardCamera = towardCamera.subtract(normalVector.scale(towardCamera.dot(normalVector)));
            if (towardCamera.lengthSqr() > 1.0E-6D) {
                Vector3f currentUp = orientation.transform(new Vector3f(0.0F, 1.0F, 0.0F));
                Vec3 desiredUp = towardCamera.normalize();
                // A floor is viewed from the opposite side of its front-face basis. Reverse its
                // in-plane up direction once; ceilings already use the correct handedness.
                if (surfaceNormal == Direction.UP) {
                    desiredUp = desiredUp.scale(-1.0D);
                }
                Quaternionf twist = new Quaternionf().rotationTo(currentUp,
                        new Vector3f((float) desiredUp.x, (float) desiredUp.y, (float) desiredUp.z));
                orientation = twist.mul(orientation);
            }
        }
        poseStack.mulPose(orientation);
        poseStack.translate(0.0D, -size.height() * 0.5D, 0.0D);
        PoseStack.Pose pose = poseStack.last();
        renderFrontFace(texture.get(), size,
                settings.flipVertical() ? 1.0F : 0.0F,
                settings.flipVertical() ? 0.0F : 1.0F,
                settings, projectionLight, pose.pose(), pose, bufferSource);
        poseStack.popPose();
    }

    public void renderPortableWarBanner(
            ItemStack banner,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int projectionLight,
            float yawDegrees,
            float modelScale,
            double gameTime
    ) {
        if (!(banner.getItem() instanceof BannerItem) || modelScale <= 0.0F) {
            return;
        }
        MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(bufferSource, settings);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDegrees));
        renderBannerFace(
                banner,
                0.0F,
                0.0F,
                0,
                gameTime,
                poseStack,
                projectionBuffers,
                modelScale,
                projectionLight,
                null
        );
        poseStack.popPose();
    }

    private void renderBannerSource(ProjectionSourceRenderRegistry.RenderContext context) {
        renderProjectedBanners(
                context.blockEntity(),
                context.settings(),
                context.poseStack(),
                context.bufferSource(),
                context.angle(),
                context.bob(),
                context.projectionLight(),
                context.gameTime()
        );
    }

    private void renderItemSource(ProjectionSourceRenderRegistry.RenderContext context) {
        MirageProjectorBlockEntity blockEntity = context.blockEntity();
        ProjectionSettings settings = context.settings();
        if (blockEntity.projectedStack().isEmpty()) {
            equippedItemCache.remove(blockEntity);
            return;
        }
        LivingEntity equippedPiece = resolveEquippedItemEntity(blockEntity);
        if (equippedPiece != null) {
            renderProjectedEntity(
                    blockEntity,
                    settings,
                    equippedPiece,
                    context.partialTick(),
                    context.poseStack(),
                    context.bufferSource(),
                    context.angle(),
                    context.bob(),
                    context.projectionLight(),
                    false,
                    false
            );
            return;
        }
        renderProjectedItem(
                blockEntity,
                settings,
                context.poseStack(),
                context.bufferSource(),
                context.angle(),
                context.bob(),
                context.projectionLight()
        );
    }

    private void renderEntitySource(ProjectionSourceRenderRegistry.RenderContext context) {
        deferEntityProjection(this, context.blockEntity(), context.partialTick(), context.packedLight());
    }

    private void renderImageSource(ProjectionSourceRenderRegistry.RenderContext context) {
        MirageProjectorBlockEntity blockEntity = context.blockEntity();
        ProjectionSettings settings = context.settings();
        if (blockEntity.chassisProfile() == ProjectionChassisProfile.WALL) {
            renderWallDataShowImage(
                    blockEntity, settings, context.poseStack(), context.bufferSource(), context.projectionLight()
            );
            return;
        }
        if (blockEntity.chassisProfile().supportsMultiSourceImageLayout()
                && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
            if (!blockEntity.imageSourceBank().hasAny(blockEntity.chassisProfile().imageLayoutSlots())) {
                return;
            }
            renderMultiSourceImageLayout(
                    blockEntity,
                    settings,
                    context.poseStack(),
                    context.bufferSource(),
                    context.angle(),
                    context.bob(),
                    context.projectionLight()
            );
            return;
        }

        if (blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM) {
            if (!settings.hasAnyImage()) {
                renderBook(
                        blockEntity,
                        settings,
                        context.poseStack(),
                        context.bufferSource(),
                        context.angle(),
                        context.bob(),
                        context.projectionLight()
                );
                return;
            }
            boolean rendered = renderPrismImage(
                    blockEntity,
                    settings,
                    context.poseStack(),
                    context.bufferSource(),
                    context.angle(),
                    context.bob(),
                    context.projectionLight()
            );
            if (!rendered) {
                renderMissingAsset(
                        blockEntity,
                        context.poseStack(),
                        context.bufferSource(),
                        context.angle(),
                        context.bob(),
                        context.projectionLight()
                );
            }
            return;
        }

        if (!hasPlaneImageContent(settings)) {
            renderBook(
                    blockEntity,
                    settings,
                    context.poseStack(),
                    context.bufferSource(),
                    context.angle(),
                    context.bob(),
                    context.projectionLight()
            );
            return;
        }

        renderImage(
                blockEntity,
                settings,
                context.poseStack(),
                context.bufferSource(),
                context.angle(),
                context.bob(),
                context.projectionLight()
        );
    }

    private static void renderWallDataShowImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int projectionLight
    ) {
        WallProjectionSurface.Result surface = blockEntity.wallProjectionSurface();
        ImageSourceBank.Asset image = blockEntity.activeWallImage();
        if (!image.present()) {
            return;
        }
        if (!surface.valid()) {
            // The deck index is still authoritative even when the selected slide cannot be
            // projected. Show the transparent red prohibition marker for every actionable
            // Data-show failure that has a wall/target context, so Previous/Next/automatic
            // playback can continue through an invalid slide instead of appearing frozen.
            if (surface.failure() == WallProjectionSurface.Failure.IRREGULAR_SURFACE
                    || surface.failure() == WallProjectionSurface.Failure.NO_CORE
                    || surface.failure() == WallProjectionSurface.Failure.POWER_EXCEEDED) {
                renderWallCancellation(blockEntity, poseStack, bufferSource);
            }
            return;
        }
        var texture = ProjectionTextureCache.get(image.id(), System.nanoTime() / 1_000_000L);
        if (texture.isEmpty()) {
            return;
        }

        BlockPos origin = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(
                surface.centerX() - origin.getX(),
                surface.centerY() - origin.getY() - surface.heightPixels() * PIXEL * 0.5D,
                surface.centerZ() - origin.getZ()
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(wallPlaneYaw(surface.facing())));
        PoseStack.Pose pose = poseStack.last();
        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;
        renderFrontFace(
                texture.get(),
                new FaceSize(surface.widthPixels() * PIXEL, surface.heightPixels() * PIXEL),
                topV, bottomV, settings, projectionLight, pose.pose(), pose, bufferSource
        );
        poseStack.popPose();
    }

    private static void renderWallCancellation(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        WallCancellationTarget target = wallCancellationTarget(blockEntity);
        if (target == null) {
            return;
        }
        BlockPos origin = blockEntity.getBlockPos();
        float size = 12.0F * PIXEL;
        poseStack.pushPose();
        poseStack.translate(
                target.centerX() - origin.getX(),
                target.centerY() - origin.getY() - size * 0.5D,
                target.centerZ() - origin.getZ()
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(wallPlaneYaw(target.facing())));
        PoseStack.Pose pose = poseStack.last();
        renderFrontFace(
                PROJECTION_CANCELLATION_TEXTURE, new FaceSize(size, size),
                0.0F, 1.0F, ProjectionSettings.DEFAULT, LightTexture.FULL_BRIGHT,
                pose.pose(), pose, bufferSource
        );
        poseStack.popPose();
    }

    private static WallCancellationTarget wallCancellationTarget(MirageProjectorBlockEntity blockEntity) {
        if (blockEntity.getLevel() == null) {
            return null;
        }
        Direction facing = blockEntity.getBlockState().hasProperty(MirageProjectorBlock.FACING)
                ? blockEntity.getBlockState().getValue(MirageProjectorBlock.FACING)
                : Direction.NORTH;
        BlockPos origin = blockEntity.getBlockPos();
        for (int step = 1; step <= WallProjectionSurface.MAX_SEARCH_BLOCKS; step++) {
            BlockPos wall = origin.relative(facing, step);
            var state = blockEntity.getLevel().getBlockState(wall);
            if (state.isAir() || !state.isCollisionShapeFullBlock(blockEntity.getLevel(), wall)
                    || !state.isFaceSturdy(blockEntity.getLevel(), wall, facing.getOpposite())) {
                continue;
            }
            double x = wall.getX() + 0.5D;
            double y = wall.getY() + 0.5D;
            double z = wall.getZ() + 0.5D;
            double epsilon = 0.002D;
            switch (facing) {
                case NORTH -> z = wall.getZ() + 1.0D + epsilon;
                case SOUTH -> z = wall.getZ() - epsilon;
                case EAST -> x = wall.getX() - epsilon;
                case WEST -> x = wall.getX() + 1.0D + epsilon;
                default -> { }
            }
            return new WallCancellationTarget(x, y, z, facing);
        }
        return null;
    }

    private static float wallPlaneYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 270.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
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
        boolean prism = blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM;
        if (prism) {
            applyPrismCarouselPlacement(blockEntity, settings, poseStack, bottom + bannerHeight, angle);
            float radius = Math.max(PIXEL, PrismProjectionSpacing.effectiveDistancePixels(settings) * PIXEL);
            ProjectionTransform.Orientation faceTilt = settings.transform().orientation();
            renderBannerFace(blockEntity.bannerSnapshot(0), 180.0F, radius, 0, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight, faceTilt);
            renderBannerFace(blockEntity.bannerSnapshot(1), 90.0F, radius, 1, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight, faceTilt);
            renderBannerFace(blockEntity.bannerSnapshot(2), 0.0F, radius, 2, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight, faceTilt);
            renderBannerFace(blockEntity.bannerSnapshot(3), -90.0F, radius, 3, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight, faceTilt);
        } else {
            applyPlaneProjectionPlacement(blockEntity, settings, poseStack, bottom + bannerHeight, angle);
            renderBannerFace(blockEntity.bannerSnapshot(0), 0.0F, 0.0F, 0, gameTime,
                    poseStack, projectionBuffers, modelScale, projectionLight, null);
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
            int projectionLight,
            ProjectionTransform.Orientation faceTilt
    ) {
        if (!(stack.getItem() instanceof BannerItem bannerItem)) {
            return;
        }

        PartPose previous = bannerFlag.storePose();
        poseStack.pushPose();
        try {
            poseStack.mulPose(Axis.YP.rotationDegrees(faceRotationDegrees));
            poseStack.translate(0.0D, 0.0D, radius);
            if (faceTilt != null) {
                poseStack.mulPose(new Quaternionf(faceTilt.x(), faceTilt.y(), faceTilt.z(), faceTilt.w()));
            }

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
        ) + (scan.playerSource() ? "|layers=" + blockEntity.entityProjectionState().playerAllLayers() : "");
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

        DEFERRED_ENTITY_PROJECTIONS.removeIf(entry -> entry.blockEntity() == blockEntity);
        DEFERRED_ENTITY_PROJECTIONS.add(new DeferredEntityProjection(
                renderer, blockEntity, partialTick, packedLight, blockEntity.settings().opacityPercent() < 100
        ));
    }

    public static void flushDeferredEntityProjections(
            PoseStack poseStack,
            Vec3 cameraPosition,
            boolean lateGhostPass
    ) {
        if (poseStack == null || cameraPosition == null || DEFERRED_ENTITY_PROJECTIONS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            DEFERRED_ENTITY_PROJECTIONS.clear();
            return;
        }

        List<DeferredEntityProjection> frame = new ArrayList<>(DEFERRED_ENTITY_PROJECTIONS.stream()
                .filter(entry -> entry.lateGhostPass() == lateGhostPass)
                .toList());
        DEFERRED_ENTITY_PROJECTIONS.removeIf(entry -> entry.lateGhostPass() == lateGhostPass);
        if (frame.isEmpty()) {
            return;
        }

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
            try {
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
            } finally {
                poseStack.popPose();
                buffers.endBatch();
            }
        }
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
        ProjectionChassisProfile chassis = blockEntity.chassisProfile();
        float angle = projectionBaseAngle(blockEntity, settings)
                + (chassis.supportsRotation() ? rotationAngle(settings, gameTime) : 0.0F);
        float bob = chassis.supportsFloating() ? bobOffset(settings, gameTime) : 0.0F;
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
                        || !blockEntity.entityProjectionState().hasActiveEntity(),
                settings.opacityPercent() < 100
        );
        renderProjectionNameplate(blockEntity, settings, bob, poseStack, bufferSource, projectionLight);
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
            boolean humanoidPose,
            boolean lateDepthStableGhost
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
        applyVolumetricProjectionPlacement(blockEntity, settings, poseStack, bottom, angle);
        poseStack.scale(entityScale, entityScale, entityScale);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(
                bufferSource,
                settings,
                lateDepthStableGhost
        );
        dispatcher.setRenderShadow(false);
        try (ProjectionRenderContext.Scope ignored = ProjectionRenderContext.push(
                entity,
                settings,
                lateDepthStableGhost
        )) {
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
            float bob,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int projectionLight
    ) {
        String text = blockEntity.entityProjectionState().projectionNameplate();
        if (text == null || text.isBlank()) {
            return;
        }

        EntityProjectionBounds.Bounds bounds = EntityProjectionBounds.projected(
                blockEntity.entityProjectionState(),
                settings
        );
        float projectionBottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;
        float labelY = projectionBottom + bounds.heightPixels() * PIXEL + ENTITY_NAMEPLATE_GAP;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component label = Component.literal(text);

        poseStack.pushPose();
        applyAnchorTranslation(blockEntity, poseStack, labelY);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        float textX = -font.width(label) / 2.0F;
        int textColor = ProjectionRenderBuffers.tintedArgb(settings, 0xFFFFFF);
        int alpha = (textColor >>> 24) & 0xFF;
        int seeThroughAlpha = Math.max(1, Math.round(alpha * (96.0F / 255.0F)));
        int seeThroughColor = (seeThroughAlpha << 24) | (textColor & 0x00FFFFFF);
        int backgroundAlpha = Math.max(0, Math.round(96.0F * settings.opacity()));
        int backgroundColor = backgroundAlpha << 24;

        font.drawInBatch(
                label, textX, 0.0F, seeThroughColor, false, matrix, bufferSource,
                Font.DisplayMode.SEE_THROUGH, backgroundColor, projectionLight
        );
        font.drawInBatch(
                label, textX, 0.0F, textColor, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, projectionLight
        );
        poseStack.popPose();
    }

    private static void renderDockedPresentationRemote(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (blockEntity.chassisProfile() != ProjectionChassisProfile.WALL
                || !blockEntity.hasDockedPresentationRemote()) {
            return;
        }
        ItemStack remote = blockEntity.presentationRemote().getStackInSlot(0);
        if (remote.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        // Physical pairing dock on the top shell of the low-profile data-show. The remote lies
        // flat instead of floating as a hologram.
        poseStack.translate(0.5D, 6.35D * PIXEL, 0.53D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.30F, 0.30F, 0.30F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                remote, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, blockEntity.getLevel(), (int) blockEntity.getBlockPos().asLong()
        );
        poseStack.popPose();
    }

    private static void renderCoreItem(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            double gameTime
    ) {
        ItemStack coreStack = blockEntity.coreStack();
        if (coreStack.isEmpty()) {
            return;
        }

        ProjectorVisualLayout visual = ProjectorVisualLayout.forState(
                blockEntity.getBlockState(), blockEntity.chassisProfile()
        );
        long positionSeed = blockEntity.getBlockPos().asLong();
        float rotation = (float) ((gameTime * 3.0D + Math.floorMod(positionSeed, 360L)) % 360.0D);
        float bobPixels = (float) (Math.sin(gameTime * 0.10D + Math.floorMod(positionSeed, 97L)) * 0.20D);

        poseStack.pushPose();
        poseStack.translate(
                0.5D,
                (visual.coreCenterYPixels() + bobPixels) * PIXEL,
                0.5D
        );

        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        // The Table uses Wall's full core scale, but keeps it upright and raised enough to
        // clear the low frame throughout the intentional floating movement.
        if (blockEntity.chassisProfile() != ProjectionChassisProfile.TABLE) {
            poseStack.mulPose(Axis.XP.rotationDegrees(18.0F));
        }
        poseStack.scale(visual.coreRenderScale(), visual.coreRenderScale(), visual.coreRenderScale());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                coreStack,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                (int) positionSeed
        );

        CoreBoosterMaterial boosterMaterial = CoreBoosterBlockEntity.materialFromStack(coreStack);
        if (boosterMaterial.present()) {
            ItemStack boostedCore = boosterMaterial.centerStack();
            poseStack.pushPose();
            poseStack.scale(0.42F, 0.42F, 0.42F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    boostedCore,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    (int) (positionSeed ^ 0x5A17L)
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static float physicalTop(MirageProjectorBlockEntity blockEntity) {
        return ProjectorVisualLayout.forState(
                blockEntity.getBlockState(), blockEntity.chassisProfile()
        ).projectionTopPixels() * PIXEL;
    }

    private static float idleBookCenter(MirageProjectorBlockEntity blockEntity) {
        return ProjectorVisualLayout.forState(
                blockEntity.getBlockState(), blockEntity.chassisProfile()
        ).idleBookCenterYPixels() * PIXEL;
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
        applyVolumetricProjectionPlacement(blockEntity, settings, poseStack, bottom + scale * 0.5F, angle);
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
        applyAnchorTranslation(blockEntity, poseStack, idleBookCenter(blockEntity) + bob);
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
        applyAnchorTranslation(blockEntity, poseStack, idleBookCenter(blockEntity) + bob);
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
        boolean viewingFront = isCameraOnFrontSide(blockEntity, settings, angle, bottom);
        ProjectionSettings.BackFaceMode planeMode = settings.backFaceMode();

        if ((viewingFront && planeMode == ProjectionSettings.BackFaceMode.BACK)
                || (!viewingFront && (planeMode == ProjectionSettings.BackFaceMode.FRONT
                || planeMode == ProjectionSettings.BackFaceMode.BACK
                || planeMode == ProjectionSettings.BackFaceMode.INDEPENDENT))) {
            return;
        }
        boolean readableBack = planeMode == ProjectionSettings.BackFaceMode.READABLE;

        poseStack.pushPose();
        applyPlaneProjectionPlacement(blockEntity, settings, poseStack, bottom, angle);
        long animationSampleMs = (System.nanoTime() / 1_000_000L);

        for (int slot = 0; slot < slots; slot++) {
            ImageSourceBank.Asset asset = blockEntity.imageSourceBank().get(slot);
            if (!asset.present()) {
                continue;
            }
            var texture = ProjectionTextureCache.get(asset.id(), animationSampleMs);
            if (texture.isEmpty()) {
                continue;
            }

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
        float radius = Math.max(PIXEL, PrismProjectionSpacing.effectiveDistancePixels(settings) * PIXEL);

        poseStack.pushPose();
        applyPrismCarouselPlacement(blockEntity, settings, poseStack, bottom, angle);
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
        ProjectionTransform.Orientation faceTilt = settings.transform().orientation();
        poseStack.mulPose(new Quaternionf(faceTilt.x(), faceTilt.y(), faceTilt.z(), faceTilt.w()));
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

    private static void renderImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight
    ) {
        renderImage(blockEntity, settings, poseStack, bufferSource, angle, bob, projectionLight, null);
    }

    private static void renderImage(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float angle,
            float bob,
            int projectionLight,
            Boolean viewingFrontOverride
    ) {
        float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;
        boolean viewingFront = viewingFrontOverride == null
                ? isCameraOnFrontSide(blockEntity, settings, angle, bottom)
                : viewingFrontOverride;
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
                    if (!settings.hasBackImage()) {
                        return;
                    }
                    assetId = settings.backImageId();
                    imageWidth = settings.backImageWidth();
                    imageHeight = settings.backImageHeight();
                    renderAsBack = true;
                    readableBack = true;
                }
                case MIRRORED -> {
                    if (!settings.hasImage()) {
                        return;
                    }
                    assetId = settings.imageId();
                    imageWidth = settings.imageWidth();
                    imageHeight = settings.imageHeight();
                    renderAsBack = true;
                    readableBack = false;
                }
                case READABLE -> {
                    if (!settings.hasImage()) {
                        return;
                    }
                    assetId = settings.imageId();
                    imageWidth = settings.imageWidth();
                    imageHeight = settings.imageHeight();
                    renderAsBack = true;
                    readableBack = true;
                }
                case INDEPENDENT -> {
                    if (!settings.hasBackImage()) {
                        return;
                    }
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

        float topV = settings.flipVertical() ? 1.0F : 0.0F;
        float bottomV = settings.flipVertical() ? 0.0F : 1.0F;
        FaceSize size = faceSize(imageWidth, imageHeight, settings.scalePixels());

        poseStack.pushPose();
        applyPlaneProjectionPlacement(blockEntity, settings, poseStack, bottom, angle);
        if (blockEntity.chassisProfile() == ProjectionChassisProfile.TABLE) {
            // Table images rotate/tilt around their geometric center rather than around the near
            // image edge. X is already centered by the face mesh; center the local Y axis too.
            poseStack.translate(0.0D, -size.height() * 0.5D, 0.0D);
        }
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        if (renderAsBack) {
            renderBackFace(texture.get(), size, topV, bottomV, readableBack, settings, projectionLight, matrix, pose, bufferSource);
        } else {
            renderFrontFace(texture.get(), size, topV, bottomV, settings, projectionLight, matrix, pose, bufferSource);
        }
        poseStack.popPose();
    }

    private static boolean cameraOnPortableImageFront(
            Vec3 cameraPosition,
            Vec3 anchor,
            float worldYawDegrees,
            ProjectionSettings settings
    ) {
        if (cameraPosition == null || anchor == null) return true;
        double yawRadians = Math.toRadians(worldYawDegrees);
        double tiltRadians = Math.toRadians(settings.tiltDegrees());
        Vec3 normal = new Vec3(Math.sin(yawRadians) * Math.cos(tiltRadians), -Math.sin(tiltRadians),
                Math.cos(yawRadians) * Math.cos(tiltRadians));
        return cameraPosition.subtract(anchor).dot(normal) >= 0.0D;
    }

    private static boolean isCameraOnFrontSide(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            float angle,
            double baseY
    ) {
        if (blockEntity.chassisProfile() == ProjectionChassisProfile.TABLE) {
            return MirageTableProjectorLogic.isCameraOnFrontSide(blockEntity, settings, angle, baseY);
        }
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        BlockPos pos = blockEntity.getBlockPos();
        double originX = pos.getX() + 0.5D;
        double originY = pos.getY() + baseY;
        double originZ = pos.getZ() + 0.5D;
        double yawRadians = Math.toRadians(angle);
        double tiltRadians = Math.toRadians(settings.tiltDegrees());
        double sinTilt = Math.sin(tiltRadians);
        double cosTilt = Math.cos(tiltRadians);

        // Canonical fixed projectors are upright planes. Table never reaches this path: its
        // physical side classification is owned by MirageTableProjectorLogic.
        double normalX = Math.sin(yawRadians) * cosTilt;
        double normalY = -sinTilt;
        double normalZ = Math.cos(yawRadians) * cosTilt;

        double toCameraX = camera.x - originX;
        double toCameraY = camera.y - originY;
        double toCameraZ = camera.z - originZ;
        double dot = toCameraX * normalX + toCameraY * normalY + toCameraZ * normalZ;
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

    private static float projectionBaseAngle(MirageProjectorBlockEntity blockEntity, ProjectionSettings settings) {
        if (blockEntity != null
                && blockEntity.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || settings.sourceMode() == ProjectionSettings.SourceMode.BANNER)) {
            return 0.0F;
        }
        return blockFacingAngle(blockEntity);
    }

    private static void applyPlaneProjectionPlacement(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            double baseY,
            float finalYawDegrees
    ) {
        if (blockEntity != null && blockEntity.chassisProfile() == ProjectionChassisProfile.TABLE) {
            MirageTableProjectorLogic.applyPlanarPlacement(blockEntity, settings, poseStack, baseY, finalYawDegrees);
            return;
        }
        applyAnchorTranslation(blockEntity, poseStack, baseY);
        poseStack.mulPose(Axis.YP.rotationDegrees(finalYawDegrees));
        ProjectionTransform.Orientation orientation = settings.transform().orientation();
        poseStack.mulPose(new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w()));
    }

    private static void applyVolumetricProjectionPlacement(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            double baseY,
            float finalYawDegrees
    ) {
        if (blockEntity != null && blockEntity.chassisProfile() == ProjectionChassisProfile.TABLE) {
            MirageTableProjectorLogic.applyVolumetricPlacement(blockEntity, settings, poseStack, baseY, finalYawDegrees);
            return;
        }
        applyAnchorTranslation(blockEntity, poseStack, baseY);
        poseStack.mulPose(Axis.YP.rotationDegrees(finalYawDegrees));
        ProjectionTransform.Orientation orientation = settings.transform().orientation();
        poseStack.mulPose(new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w()));
    }

    private static void applyAnchorTranslation(
            MirageProjectorBlockEntity blockEntity,
            PoseStack poseStack,
            double baseY
    ) {
        poseStack.translate(0.5D, baseY, 0.5D);
    }

    private static void applyPrismCarouselPlacement(
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            PoseStack poseStack,
            double baseY,
            float carouselYawDegrees
    ) {
        poseStack.translate(0.5D, baseY, 0.5D);
        // Prism rotation is a carousel: rotate the entire four-face cross around the machine center.
        poseStack.mulPose(Axis.YP.rotationDegrees(carouselYawDegrees));
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

        if (blockEntity.endResonanceActive()) {
            return EndResonanceGeometry.renderBounds(blockEntity);
        }

        if (blockEntity.chassisProfile() == ProjectionChassisProfile.TABLE) {
            return MirageTableProjectorLogic.renderBoundingBox(blockEntity);
        }

        if (blockEntity.chassisProfile() == ProjectionChassisProfile.WALL) {
            AABB machine = new AABB(pos).inflate(0.25D);
            WallProjectionSurface.Result wallSurface = blockEntity.wallProjectionSurface();
            if (!blockEntity.projectionEnabled()) {
                return machine;
            }
            if (!wallSurface.valid() || !wallSurface.hasEnvelope()) {
                if (wallSurface.failure() == WallProjectionSurface.Failure.IRREGULAR_SURFACE) {
                    WallCancellationTarget cancellation = wallCancellationTarget(blockEntity);
                    if (cancellation != null) {
                        AABB marker = new AABB(
                                cancellation.centerX() - 0.5D, cancellation.centerY() - 0.5D, cancellation.centerZ() - 0.5D,
                                cancellation.centerX() + 0.5D, cancellation.centerY() + 0.5D, cancellation.centerZ() + 0.5D
                        );
                        return new AABB(
                                Math.min(machine.minX, marker.minX), Math.min(machine.minY, marker.minY), Math.min(machine.minZ, marker.minZ),
                                Math.max(machine.maxX, marker.maxX), Math.max(machine.maxY, marker.maxY), Math.max(machine.maxZ, marker.maxZ)
                        );
                    }
                }
                return machine;
            }
            AABB projection = wallSurface.envelope().inflate(0.05D);
            return new AABB(
                    Math.min(machine.minX, projection.minX), Math.min(machine.minY, projection.minY), Math.min(machine.minZ, projection.minZ),
                    Math.max(machine.maxX, projection.maxX), Math.max(machine.maxY, projection.maxY), Math.max(machine.maxZ, projection.maxZ)
            );
        }

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
                || blockEntity.entityProjectionState().hasVisibleProjectedHumanoidEquipment());
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

        ProjectionChassisProfile chassis = blockEntity.chassisProfile();
        boolean prismFaces = chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || settings.sourceMode() == ProjectionSettings.SourceMode.BANNER);
        boolean horizontalPlane = chassis.usesHorizontalPlaneFor(settings.sourceMode());
        double tiltReach = height * Math.abs(Math.sin(Math.toRadians(settings.tiltDegrees())));
        double prismOutwardTiltReach = settings.tiltDegrees() > 0.0F
                ? height * Math.sin(Math.toRadians(settings.tiltDegrees()))
                : 0.0D;
        double radius = prismFaces
                ? Math.max(0.75D, PrismProjectionSpacing.effectiveDistancePixels(settings) * PIXEL
                + width * 0.5D + prismOutwardTiltReach + 0.25D)
                : horizontalPlane
                ? Math.max(0.75D, Math.hypot(width, height) * 0.5D + 0.25D)
                : Math.max(0.75D, width * 0.5D + tiltReach + 0.25D);
        double floatReach = chassis.supportsFloating()
                ? settings.floatAmplitudePixels() * PIXEL
                : 0.0D;
        double projectionBaseY = pos.getY() + physicalTop(blockEntity) + settings.liftPixels() * PIXEL;
        double horizontalPlaneVerticalReach = horizontalPlane
                ? Math.max(width, height) * Math.abs(Math.sin(Math.toRadians(settings.tiltDegrees()))) * 0.5D + 0.25D
                : 0.0D;
        double projectionMinY = horizontalPlane
                ? projectionBaseY - horizontalPlaneVerticalReach - floatReach
                : projectionBaseY - floatReach - 0.25D;
        double projectionVerticalReach = horizontalPlane
                ? horizontalPlaneVerticalReach
                : height * Math.max(0.0D, Math.cos(Math.toRadians(Math.abs(settings.tiltDegrees()))));
        double projectionMaxY = projectionBaseY + projectionVerticalReach + 0.25D;

        if (entityProjection) {
            String nameplate = blockEntity.entityProjectionState().projectionNameplate();
            if (nameplate != null && !nameplate.isBlank()) {
                double labelHalfWidth = Minecraft.getInstance().font.width(nameplate) * 0.025D * 0.5D + 0.25D;
                radius = Math.max(radius, labelHalfWidth);
                projectionMaxY += ENTITY_NAMEPLATE_GAP + ENTITY_NAMEPLATE_BOUND_HEIGHT;
            }
        }

        // A BER culling box must cover both the physical machine and its displaced projection.
        // Otherwise a high-Lift projection can disappear just because the chassis itself left the frustum.
        double minY = Math.min(pos.getY(), projectionMinY);
        double maxY = Math.max(pos.getY() + 1.0D, projectionMaxY);
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        return new AABB(
                Math.min(pos.getX(), centerX - radius),
                minY,
                Math.min(pos.getZ(), centerZ - radius),
                Math.max(pos.getX() + 1.0D, centerX + radius),
                maxY,
                Math.max(pos.getZ() + 1.0D, centerZ + radius)
        );
    }

    @Override
    public boolean shouldRenderOffScreen(MirageProjectorBlockEntity blockEntity) {
        // getRenderBoundingBox() now describes the full projector + projection envelope, so vanilla frustum
        // culling is safe and avoids submitting giant deferred entities that cannot contribute to the frame.
        return false;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private record DeferredEntityProjection(
            MirageProjectorRenderer renderer,
            MirageProjectorBlockEntity blockEntity,
            float partialTick,
            int packedLight,
            boolean lateGhostPass
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
    private record WallCancellationTarget(double centerX, double centerY, double centerZ, Direction facing) { }

}

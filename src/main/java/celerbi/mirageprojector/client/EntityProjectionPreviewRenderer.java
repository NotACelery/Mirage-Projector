package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EntityScanData;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

/**
 * Client-only reconstruction and GUI rendering of one frozen entity projection.
 *
 * <p>The preview entity is never added to the world. It exists only as a local
 * render object rebuilt from the frozen scan and the projector's projected
 * equipment snapshots. The preview owns a small copy of vanilla's inventory
 * entity transform so Mirage can inject its projection-local tint/alpha buffer
 * without touching global shader state.</p>
 */
public final class EntityProjectionPreviewRenderer {
    private static final UUID BODYLESS_CACHE_ID = new UUID(0L, 1L);
    private ClientLevel cachedLevel;
    private UUID cachedScanId;
    private String cachedEquipmentFingerprint = "";
    private LivingEntity cachedEntity;
    private boolean creationFailed;

    /**
     * Renders the active entity into a bounded GUI viewport.
     *
     * @return true when a real reconstructed entity was rendered
     */
    public boolean render(
            GuiGraphics graphics,
            EntityProjectionState state,
            ProjectionSettings settings,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        LivingEntity entity = resolve(state);
        if (entity == null || width < 8 || height < 8) {
            return false;
        }

        boolean humanoidPose = state.activeEntity()
                .map(scan -> scan.kind() == EntityScanData.Kind.HUMANOID)
                .orElse(state.hasProjectedHumanoidEquipment());

        float neutralHeight = Math.max(entity.getBbHeight(), 0.1F);
        float entityWidth = Math.max(entity.getBbWidth(), 0.1F);
        float entityHeight = neutralHeight;
        if (humanoidPose) {
            entityWidth = Math.max(
                    entityWidth,
                    neutralHeight * HumanoidPoseController.horizontalExtentMultiplier(state.humanoidPose())
            );
            entityHeight = Math.max(
                    entityHeight,
                    neutralHeight * HumanoidPoseController.verticalExtentMultiplier(state.humanoidPose())
            );
        }

        float fitWidth = Math.max(8.0F, width - 16.0F);
        float fitHeight = Math.max(8.0F, height - 18.0F);

        int scale = Mth.clamp(
                (int) Math.floor(Math.min(fitWidth / entityWidth, fitHeight / entityHeight)),
                4,
                72
        );

        HumanoidPoseController.bind(entity, humanoidPose ? state.humanoidPose() : null);
        EntityProjectionClientEntityFactory.prepareVisualFrame(
                entity,
                (int) ((System.currentTimeMillis() / 50L) & Integer.MAX_VALUE)
        );

        renderEntityInViewportFollowsMouse(
                graphics,
                x,
                y,
                x + width,
                y + height,
                scale,
                0.0F,
                mouseX,
                mouseY,
                entity,
                settings == null ? ProjectionSettings.DEFAULT : settings
        );
        return true;
    }

    public void invalidate() {
        cachedLevel = null;
        cachedScanId = null;
        cachedEquipmentFingerprint = "";
        cachedEntity = null;
        creationFailed = false;
    }

    /**
     * Vanilla inventory-style camera tracking with a projection-local render
     * buffer. Kept here instead of calling InventoryScreen directly because the
     * vanilla helper hardcodes GuiGraphics.bufferSource() and therefore cannot
     * preview Ghost Effect/Tint safely.
     */
    public static void renderEntityInViewportFollowsMouse(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int scale,
            float yOffset,
            float mouseX,
            float mouseY,
            LivingEntity entity,
            ProjectionSettings settings
    ) {
        float centerX = (left + right) / 2.0F;
        float centerY = (top + bottom) / 2.0F;
        float angleX = (float) Math.atan((centerX - mouseX) / 40.0F);
        float angleY = (float) Math.atan((centerY - mouseY) / 40.0F);
        renderEntityInViewport(
                graphics,
                left,
                top,
                right,
                bottom,
                scale,
                yOffset,
                angleX,
                angleY,
                entity,
                settings
        );
    }

    private static void renderEntityInViewport(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int scale,
            float yOffset,
            float angleX,
            float angleY,
            LivingEntity entity,
            ProjectionSettings settings
    ) {
        float centerX = (left + right) / 2.0F;
        float centerY = (top + bottom) / 2.0F;
        graphics.enableScissor(left, top, right, bottom);

        Quaternionf orientation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf pitch = new Quaternionf().rotateX(angleY * 20.0F * (float) (Math.PI / 180.0));
        orientation.mul(pitch);

        float oldBodyRot = entity.yBodyRot;
        float oldYRot = entity.getYRot();
        float oldXRot = entity.getXRot();
        float oldHeadRotO = entity.yHeadRotO;
        float oldHeadRot = entity.yHeadRot;

        entity.yBodyRot = 180.0F + angleX * 20.0F;
        entity.setYRot(180.0F + angleX * 40.0F);
        entity.setXRot(-angleY * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        float nativeScale = entity.getScale();
        Vector3f translation = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + yOffset * nativeScale, 0.0F);
        float renderScale = scale / nativeScale;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 50.0D);
        graphics.pose().scale(renderScale, renderScale, -renderScale);
        graphics.pose().translate(translation.x, translation.y, translation.z);
        graphics.pose().mulPose(orientation);
        Lighting.setupForEntityInInventory();

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(pitch.conjugate(new Quaternionf()).rotateY((float) Math.PI));
        dispatcher.setRenderShadow(false);
        MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(graphics.bufferSource(), settings);
        try {
            RenderSystem.runAsFancy(() -> dispatcher.render(
                    entity,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0F,
                    1.0F,
                    graphics.pose(),
                    projectionBuffers,
                    15728880
            ));
            graphics.flush();
        } finally {
            dispatcher.setRenderShadow(true);
            graphics.pose().popPose();
            Lighting.setupFor3DItems();
            graphics.disableScissor();

            entity.yBodyRot = oldBodyRot;
            entity.setYRot(oldYRot);
            entity.setXRot(oldXRot);
            entity.yHeadRotO = oldHeadRotO;
            entity.yHeadRot = oldHeadRot;
        }
    }

    private LivingEntity resolve(EntityProjectionState state) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            invalidate();
            return null;
        }

        Optional<EntityScanData.View> active = state.activeEntity();
        if (active.isEmpty()) {
            if (!state.hasProjectedHumanoidEquipment()) {
                cachedEntity = null;
                cachedScanId = null;
                cachedEquipmentFingerprint = "";
                creationFailed = false;
                cachedLevel = level;
                return null;
            }

            String fingerprint = EntityProjectionClientEntityFactory.equipmentFingerprint(
                    state,
                    EntityScanData.Kind.HUMANOID
            );
            if (cachedLevel == level
                    && BODYLESS_CACHE_ID.equals(cachedScanId)
                    && fingerprint.equals(cachedEquipmentFingerprint)) {
                return creationFailed ? null : cachedEntity;
            }

            cachedLevel = level;
            cachedScanId = BODYLESS_CACHE_ID;
            cachedEquipmentFingerprint = fingerprint;
            cachedEntity = EntityProjectionClientEntityFactory.createBodylessHumanoid(state, level);
            creationFailed = cachedEntity == null;
            return cachedEntity;
        }

        EntityScanData.View scan = active.get();
        String equipmentFingerprint = EntityProjectionClientEntityFactory.equipmentFingerprint(state, scan.kind());
        if (cachedLevel == level
                && scan.scanId().equals(cachedScanId)
                && equipmentFingerprint.equals(cachedEquipmentFingerprint)) {
            return creationFailed ? null : cachedEntity;
        }

        cachedLevel = level;
        cachedScanId = scan.scanId();
        cachedEquipmentFingerprint = equipmentFingerprint;
        cachedEntity = EntityProjectionClientEntityFactory.create(scan, state, level);
        creationFailed = cachedEntity == null;
        return cachedEntity;
    }
}

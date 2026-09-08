package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * GUI preview for Item Mode.
 *
 * <p>Standalone armor is reconstructed on the same invisible humanoid rig used
 * by the world renderer. Ordinary items/blocks now use their FIXED 3D model in
 * the preview rather than an inventory icon, so blocks remain volumetric and
 * projection Tint/Ghost Effect can be previewed through the same local buffer
 * path as the world hologram.</p>
 */
public final class ItemProjectionPreviewRenderer {
    private ClientLevel cachedLevel;
    private UUID cachedSnapshotId;
    private LivingEntity cachedEquippedEntity;

    public void render(
            GuiGraphics graphics,
            ItemStack stack,
            UUID snapshotId,
            ProjectionSettings settings,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        ProjectionSettings safeSettings = settings == null ? ProjectionSettings.DEFAULT : settings;
        graphics.fill(x, y, x + width, y + height, 0xFF4B4453);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF0D1015);
        graphics.drawString(Minecraft.getInstance().font, "ITEM SNAPSHOT", x + 4, y + 4, 0xFFAFA7BC, false);

        if (stack == null || stack.isEmpty()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "empty",
                    x + width / 2,
                    y + height / 2 - 4,
                    0xFF6F6878
            );
            invalidate();
            return;
        }

        if (EntityProjectionClientEntityFactory.standaloneEquipmentSlot(stack) != null) {
            LivingEntity equipped = resolveEquipped(stack, snapshotId);
            if (equipped != null) {
                renderEquipped(
                        graphics,
                        equipped,
                        safeSettings,
                        x + 3,
                        y + 14,
                        width - 6,
                        height - 17,
                        mouseX,
                        mouseY
                );
                return;
            }
        } else {
            invalidate();
        }

        renderGenericItem(
                graphics,
                stack,
                safeSettings,
                x + 3,
                y + 14,
                width - 6,
                height - 17,
                mouseX,
                mouseY
        );
    }

    public void invalidate() {
        cachedLevel = null;
        cachedSnapshotId = null;
        cachedEquippedEntity = null;
    }

    private LivingEntity resolveEquipped(ItemStack stack, UUID snapshotId) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            invalidate();
            return null;
        }

        if (snapshotId != null
                && cachedLevel == level
                && snapshotId.equals(cachedSnapshotId)
                && cachedEquippedEntity != null) {
            return cachedEquippedEntity;
        }

        cachedLevel = level;
        cachedSnapshotId = snapshotId;
        cachedEquippedEntity = EntityProjectionClientEntityFactory.createEquippedItem(stack, level);
        return cachedEquippedEntity;
    }

    private static void renderEquipped(
            GuiGraphics graphics,
            LivingEntity entity,
            ProjectionSettings settings,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        float entityWidth = Math.max(entity.getBbWidth(), 0.1F);
        float entityHeight = Math.max(entity.getBbHeight(), 0.1F);
        float fitWidth = Math.max(8.0F, width - 4.0F);
        float fitHeight = Math.max(8.0F, height - 4.0F);
        int scale = Mth.clamp(
                (int) Math.floor(Math.min(fitWidth / entityWidth, fitHeight / entityHeight)),
                4,
                48
        );

        EntityProjectionClientEntityFactory.prepareVisualFrame(
                entity,
                (int) ((System.currentTimeMillis() / 50L) & Integer.MAX_VALUE)
        );
        EntityProjectionPreviewRenderer.renderEntityInViewportFollowsMouse(
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
                settings
        );
    }

    private static void renderGenericItem(
            GuiGraphics graphics,
            ItemStack stack,
            ProjectionSettings settings,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        if (width < 8 || height < 8) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float centerX = x + width * 0.5F;
        float centerY = y + height * 0.53F;
        float modelScale = Math.max(10.0F, Math.min(width, height) * 0.62F);
        float yaw = Mth.clamp((mouseX - centerX) * 0.55F, -50.0F, 50.0F);
        float pitch = Mth.clamp((mouseY - centerY) * 0.35F, -25.0F, 25.0F);

        graphics.enableScissor(x, y, x + width, y + height);
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(centerX, centerY, 100.0D);
            graphics.pose().scale(modelScale, -modelScale, modelScale);
            graphics.pose().mulPose(Axis.XP.rotationDegrees(22.0F + pitch));
            graphics.pose().mulPose(Axis.YP.rotationDegrees(35.0F + yaw));
            Lighting.setupFor3DItems();
            MultiBufferSource projectionBuffers = ProjectionRenderBuffers.wrap(graphics.bufferSource(), settings);
            minecraft.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    graphics.pose(),
                    projectionBuffers,
                    minecraft.level,
                    0
            );
            graphics.flush();
        } finally {
            graphics.pose().popPose();
            Lighting.setupFor3DItems();
            graphics.disableScissor();
        }
    }
}

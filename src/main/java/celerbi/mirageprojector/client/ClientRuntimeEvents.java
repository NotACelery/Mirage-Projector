package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT)
public final class ClientRuntimeEvents {
    private static final ResourceLocation MAGENTA_STAINED_GLASS = ResourceLocation.withDefaultNamespace("textures/block/magenta_stained_glass.png");
    private ClientRuntimeEvents() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                && ClientEntityScanner.isUsingScanner()
                && event.getItemStack().is(ModItems.ENTITY_SCANNER.get())) {
            event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-90.0F));
        }
        if (!(minecraft.screen instanceof DebugHandbookScreen)) {
            return;
        }
        if (event.getItemStack().is(ModItems.DEBUG_HANDBOOK.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            ClientHeldProjectors.renderVisiblePlayers(
                    Minecraft.getInstance(),
                    event.getPoseStack(),
                    event.getCamera().getPosition(),
                    partialTick
            );
            MirageProjectorRenderer.flushDeferredEntityProjections(
                    event.getPoseStack(),
                    event.getCamera().getPosition(),
                    false
            );
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            ClientHeldFlashlights.submitVisiblePlayers(
                    Minecraft.getInstance(),
                    event.getCamera().getPosition(),
                    partialTick
            );
            ClientShoulderEquipment.submitShoulderFlashlights(
                    Minecraft.getInstance(),
                    event.getCamera().getPosition(),
                    partialTick
            );
            ClientPlacedLightProjectors.submitNearby(
                    Minecraft.getInstance(),
                    event.getCamera().getPosition()
            );
            ClientDynamicMirageLightManager.tick(
                    Minecraft.getInstance(),
                    event.getCamera().getPosition()
            );
            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.mul(event.getModelViewMatrix());
            RenderSystem.applyModelViewMatrix();
            try {
                MirageProjectorRenderer.flushDeferredEntityProjections(
                        event.getPoseStack(),
                        event.getCamera().getPosition(),
                        true
                );
            } finally {
                modelViewStack.popMatrix();
                RenderSystem.applyModelViewMatrix();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!ClientEntityScanner.active() || !ClientEntityScanner.isUsingScanner()) {
            return;
        }
        var graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int y = graphics.guiHeight() - 54;
        int sideWidth = 30;
        int barWidth = 150;
        int height = 14;
        int x = (width - (sideWidth * 2 + barWidth)) / 2;
        drawScannerPanel(graphics, x, y, sideWidth, height, "0%");
        drawScannerPanel(graphics, x + sideWidth + barWidth, y, sideWidth, height, "100%");
        graphics.fill(x + sideWidth, y, x + sideWidth + barWidth, y + height, 0xFFF0F0F0);
        graphics.fill(x + sideWidth + 1, y + 1, x + sideWidth + barWidth - 1, y + height - 1, 0xFF101015);
        int fillWidth = Math.round((barWidth - 2) * (ClientEntityScanner.progress() / (float) ClientEntityScanner.totalTicks()));
        if (fillWidth > 0) {
            graphics.blit(MAGENTA_STAINED_GLASS, x + sideWidth + 1, y + 1, 0, 0, fillWidth, height - 2);
        }
    }

    private static void drawScannerPanel(net.minecraft.client.gui.GuiGraphics graphics, int x, int y, int width, int height, String text) {
        graphics.fill(x, y, x + width, y + height, 0xFFF0F0F0);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF101015);
        graphics.drawCenteredString(Minecraft.getInstance().font, text, x + width / 2, y + 3, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ProjectionTextureCache.clear();
        ClientAssetTransport.resetSession();
        ProjectionClearancePreviewRenderer.clear();
        MirageProjectorRenderer.clearDeferredEntityProjections();
        ClientHeldFlashlights.resetSession();
        ClientHeldProjectors.resetSession();
        ClientShoulderEquipment.resetSession();
        ClientPlacedLightProjectors.resetSession();
        ClientDynamicMirageLightManager.resetSession();
        ClientMirageLightSync.resetSession();
        ClientEntityScanner.reset();
    }
}

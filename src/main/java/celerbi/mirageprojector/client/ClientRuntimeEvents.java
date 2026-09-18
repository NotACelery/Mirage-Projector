package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT)
public final class ClientRuntimeEvents {
    private ClientRuntimeEvents() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
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
    }
}

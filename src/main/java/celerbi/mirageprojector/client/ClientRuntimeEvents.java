package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientRuntimeEvents {
    private ClientRuntimeEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ProjectionTextureCache.clear();
        ClientAssetTransport.resetSession();
        ProjectionClearancePreviewRenderer.clear();
    }
}

package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.network.RequestMirageLightChunkPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client handshake for the authoritative STATIC_WORLD Mirage section mirror. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientMirageLightLifecycleEvents {
    private ClientMirageLightLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel) {
            PacketDistributor.sendToServer(new RequestMirageLightChunkPayload(event.getChunk().getPos().toLong()));
        }
    }

    /*
     * Intentionally no destructive ChunkEvent.Unload cleanup.
     * During login/reload Minecraft can replace a chunk object and emit unload/load edges in
     * timing-dependent order. Clearing here can erase a newer authoritative snapshot. Stale
     * entries are harmless while the chunk is unloaded, and the next load atomically replaces
     * them. Session/world teardown still clears the complete client mirror.
     */
}

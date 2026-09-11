package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Client chunk geometry lifecycle for deterministic Mirage-light re-solves. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientMirageLightLifecycleEvents {
    private ClientMirageLightLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel) {
            ClientMirageLightSync.queueChunkGeometryChanged(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            ClientMirageLightSync.queueChunkGeometryChanged(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        ClientMirageLightSync.flushChunkGeometryChanges();
    }
}

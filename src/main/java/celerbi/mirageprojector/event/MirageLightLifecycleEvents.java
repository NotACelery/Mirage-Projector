package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.crying.CryingObsidianLightField;
import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.network.MirageLightNetwork;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Persistence, chunk lifecycle and player-tracking bridge for authoritative Mirage light.
 */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MirageLightLifecycleEvents {
    private static final Map<ServerLevel, Set<ChunkPos>> PENDING_LOADED_CHUNKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private MirageLightLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING_LOADED_CHUNKS.remove(level);
            CryingObsidianLightField.clearLevel(level);
            MirageLightEngine.clear(level);
        } else if (event.getLevel() instanceof net.minecraft.world.level.Level level) {
            MirageLightEngine.clear(level);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        // Do not mutate a chunk from inside its load callback. Discovery, legacy
        // cleanup and field re-solves are deferred/coalesced to LevelTickEvent.Post,
        // when the chunk is fully attached to the live level.
        PENDING_LOADED_CHUNKS.computeIfAbsent(level, ignored -> new HashSet<>())
                .add(event.getChunk().getPos());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Set<ChunkPos> pending = PENDING_LOADED_CHUNKS.get(level);
            if (pending != null) {
                pending.remove(event.getChunk().getPos());
                if (pending.isEmpty()) {
                    PENDING_LOADED_CHUNKS.remove(level);
                }
            }
            CryingObsidianLightField.removeSourcesInChunk(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Set<ChunkPos> loaded = PENDING_LOADED_CHUNKS.remove(level);
        if (loaded == null || loaded.isEmpty()) {
            return;
        }

        Set<ChunkPos> liveChunks = new HashSet<>();
        for (ChunkPos chunkPos : loaded) {
            var chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk == null) {
                continue;
            }
            int removedLegacy = CryingObsidianLightField.cleanupLegacyNodesInChunk(level, chunk);
            if (removedLegacy > 0) {
                MirageProjector.LOGGER.debug(
                        "Removed {} orphan legacy Mirage light nodes from chunk {}",
                        removedLegacy,
                        chunkPos
                );
            }
            CryingObsidianLightField.discoverSourcesInChunk(level, chunk);
            liveChunks.add(chunkPos);
        }
        if (!liveChunks.isEmpty()) {
            CryingObsidianLightField.refreshSourcesForLoadedChunks(level, liveChunks);
        }
    }

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        MirageLightNetwork.onChunkSent(event.getPlayer(), event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onChunkUnwatch(ChunkWatchEvent.UnWatch event) {
        MirageLightNetwork.onChunkUnwatch(event.getPlayer(), event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MirageLightNetwork.syncAll(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MirageLightNetwork.syncAll(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MirageLightNetwork.syncAll(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MirageLightNetwork.forgetPlayer(player);
        }
    }
}

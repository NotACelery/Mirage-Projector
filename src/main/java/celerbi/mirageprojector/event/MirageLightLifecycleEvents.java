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
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Persistence, chunk lifecycle and player-tracking bridge for authoritative Mirage light.
 */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MirageLightLifecycleEvents {
    private static final int STALE_SOURCE_AUDIT_TICKS = 20;
    private static final Map<ServerLevel, ChunkLoadBatch> PENDING_LOADED_CHUNKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private MirageLightLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING_LOADED_CHUNKS.remove(level);
            MirageLightNetwork.clearLevel(level);
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

        // ChunkEvent.Load may fire before the chunk is queryable through the live Level.
        // Keep the event pending until getChunkNow() confirms the exact same geometry that
        // MirageLightSolver will be able to cross. Never consume an unavailable load event.
        PENDING_LOADED_CHUNKS
                .computeIfAbsent(level, ignored -> new ChunkLoadBatch())
                .add(event.getChunk().getPos());
        CryingObsidianLightField.noteChunkChanged(level, event.getChunk().getPos());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CryingObsidianLightField.noteChunkChanged(level, event.getChunk().getPos());
            ChunkLoadBatch pending = PENDING_LOADED_CHUNKS.get(level);
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

        if (level.getGameTime() % STALE_SOURCE_AUDIT_TICKS == 0L) {
            int pruned = CryingObsidianLightField.pruneStaleSources(level);
            if (pruned > 0) {
                MirageProjector.LOGGER.debug("Pruned {} stale Mature Mirage light source(s)", pruned);
            }
        }

        // dev.76g: every energized Mature Cluster independently watches its local
        // chunk window and revalidates/resynchronizes itself when that window changes.
        CryingObsidianLightField.verifyActiveSources(level);

        // dev.76c: sources that could not see their complete dependency window are
        // deliberately unpublished (or keep their previous complete field). Retry them
        // every tick; a normal Cluster checks only about 25 chunk columns and never
        // force-loads any of them.
        CryingObsidianLightField.retryPendingSources(level);

        ChunkLoadBatch batch = PENDING_LOADED_CHUNKS.get(level);
        if (batch == null || batch.isEmpty()) {
            return;
        }

        // Only consume load events once the chunk is actually attached/queryable. This is
        // the critical guarantee: a directional load-order race must not permanently leave
        // one side of a Mirage field clipped at a chunk border.
        Set<ChunkPos> loaded = new HashSet<>();
        for (ChunkPos chunkPos : batch.snapshot()) {
            if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) != null) {
                loaded.add(chunkPos);
            }
        }
        if (loaded.isEmpty()) {
            return;
        }
        batch.removeAll(loaded);
        if (batch.isEmpty()) {
            PENDING_LOADED_CHUNKS.remove(level);
        }

        Set<ChunkPos> liveChunks = new HashSet<>();
        for (ChunkPos chunkPos : loaded) {
            var chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk == null) {
                // It can theoretically unload between readiness check and processing. Put
                // it back so a later tick retries instead of losing the lifecycle edge.
                PENDING_LOADED_CHUNKS
                        .computeIfAbsent(level, ignored -> new ChunkLoadBatch())
                        .add(chunkPos);
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


    private static final class ChunkLoadBatch {
        private final Set<ChunkPos> chunks = new HashSet<>();

        void add(ChunkPos chunkPos) {
            chunks.add(chunkPos);
        }

        void remove(ChunkPos chunkPos) {
            chunks.remove(chunkPos);
        }

        void removeAll(Set<ChunkPos> chunkPositions) {
            chunks.removeAll(chunkPositions);
        }

        boolean isEmpty() {
            return chunks.isEmpty();
        }

        Set<ChunkPos> snapshot() {
            return Set.copyOf(chunks);
        }
    }
}

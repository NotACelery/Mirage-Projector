package celerbi.mirageprojector.network;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Tracking-scoped source synchronization for the authoritative Mirage light backend.
 *
 * dev.75b no longer fans every source update to every player in a dimension. A client
 * receives a source descriptor only while at least one chunk touched by that source's
 * solved radius is actually being sent/watched by that player. We also remember which
 * descriptors were delivered so UnWatch/source removal can retract stale client fields.
 */
public final class MirageLightNetwork {
    private static final Map<ServerPlayer, PlayerTrackingState> TRACKING =
            Collections.synchronizedMap(new WeakHashMap<>());

    private MirageLightNetwork() {
    }

    public static void broadcastUpsert(ServerLevel level, MirageLightSource source) {
        if (level == null || source == null) {
            return;
        }
        MirageLightSourceSyncPayload payload = MirageLightSourceSyncPayload.upsert(source);
        for (ServerPlayer player : level.players()) {
            PlayerTrackingState state = state(player, level);
            boolean shouldHave = touchesAnyWatchedChunk(source, state.watchedChunks);
            if (shouldHave) {
                PacketDistributor.sendToPlayer(player, payload);
                state.deliveredSources.put(source.id(), source.origin());
            } else {
                retractIfDelivered(player, state, source.id());
            }
        }
    }

    public static void broadcastRemove(ServerLevel level, MirageLightSourceId sourceId, BlockPos origin) {
        if (level == null || sourceId == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            PlayerTrackingState state = state(player, level);
            BlockPos deliveredOrigin = state.deliveredSources.remove(sourceId);
            if (deliveredOrigin != null) {
                PacketDistributor.sendToPlayer(
                        player,
                        MirageLightSourceSyncPayload.remove(sourceId, deliveredOrigin)
                );
            }
        }
    }

    /**
     * Reset the client's Mirage-light session. When the player is still in the same
     * ServerLevel (notably same-dimension respawn), preserve the watched-chunk set and
     * immediately repopulate descriptors; those chunks may not be transmitted again.
     * Login/dimension switches start from an empty watch set and Chunk Sent events fill it.
     */
    public static void syncAll(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, MirageLightSourceSyncPayload.clear());
        ServerLevel level = player.serverLevel();
        PlayerTrackingState current = TRACKING.get(player);
        if (current != null && current.level == level) {
            current.deliveredSources.clear();
            reconcilePlayer(player, level, current);
            return;
        }
        TRACKING.put(player, new PlayerTrackingState(level));
    }

    /** Called after vanilla has transmitted a watched chunk to the client. */
    public static void onChunkSent(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        if (player == null || chunkPos == null) {
            return;
        }
        PlayerTrackingState state = state(player, level);
        state.watchedChunks.add(chunkPos.toLong());
        reconcilePlayer(player, level, state);
    }

    public static void onChunkUnwatch(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        if (player == null || chunkPos == null) {
            return;
        }
        PlayerTrackingState state = existingState(player, level);
        if (state == null) {
            return;
        }
        state.watchedChunks.remove(chunkPos.toLong());
        reconcilePlayer(player, level, state);
    }

    public static void forgetPlayer(ServerPlayer player) {
        if (player != null) {
            TRACKING.remove(player);
        }
    }

    private static void reconcilePlayer(ServerPlayer player, ServerLevel level, PlayerTrackingState state) {
        Map<MirageLightSourceId, MirageLightSource> desired = new HashMap<>();
        for (MirageLightSource source : MirageLightEngine.sources(level)) {
            if (touchesAnyWatchedChunk(source, state.watchedChunks)) {
                desired.put(source.id(), source);
            }
        }

        Set<MirageLightSourceId> stale = new HashSet<>(state.deliveredSources.keySet());
        stale.removeAll(desired.keySet());
        for (MirageLightSourceId sourceId : stale) {
            retractIfDelivered(player, state, sourceId);
        }

        for (MirageLightSource source : desired.values()) {
            if (!state.deliveredSources.containsKey(source.id())) {
                PacketDistributor.sendToPlayer(player, MirageLightSourceSyncPayload.upsert(source));
                state.deliveredSources.put(source.id(), source.origin());
            }
        }
    }

    private static void retractIfDelivered(
            ServerPlayer player,
            PlayerTrackingState state,
            MirageLightSourceId sourceId
    ) {
        BlockPos deliveredOrigin = state.deliveredSources.remove(sourceId);
        if (deliveredOrigin != null) {
            PacketDistributor.sendToPlayer(
                    player,
                    MirageLightSourceSyncPayload.remove(sourceId, deliveredOrigin)
            );
        }
    }

    private static boolean touchesAnyWatchedChunk(MirageLightSource source, Set<Long> watchedChunks) {
        if (watchedChunks.isEmpty()) {
            return false;
        }
        for (long packedChunk : watchedChunks) {
            if (MirageLightEngine.sourceTouchesChunk(source, new ChunkPos(packedChunk))) {
                return true;
            }
        }
        return false;
    }

    private static PlayerTrackingState state(ServerPlayer player, ServerLevel level) {
        PlayerTrackingState current = TRACKING.get(player);
        if (current == null || current.level != level) {
            current = new PlayerTrackingState(level);
            TRACKING.put(player, current);
        }
        return current;
    }

    private static PlayerTrackingState existingState(ServerPlayer player, ServerLevel level) {
        PlayerTrackingState current = TRACKING.get(player);
        return current != null && current.level == level ? current : null;
    }

    private static final class PlayerTrackingState {
        private final ServerLevel level;
        private final Set<Long> watchedChunks = new HashSet<>();
        private final Map<MirageLightSourceId, BlockPos> deliveredSources = new HashMap<>();

        private PlayerTrackingState(ServerLevel level) {
            this.level = level;
        }
    }
}

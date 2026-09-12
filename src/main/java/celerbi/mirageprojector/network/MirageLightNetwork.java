package celerbi.mirageprojector.network;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-authoritative STATIC_WORLD Mirage chunk transport. */
public final class MirageLightNetwork {
    public static final int MAX_CLIENT_SYNC_CHUNK_DISTANCE = 64;

    private static final Map<ServerLevel, Map<Long, Long>> CHUNK_REVISIONS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private MirageLightNetwork() {
    }

    /**
     * Any aggregate section change promotes the containing chunk to a new revision and sends
     * one complete atomic snapshot of that chunk. No per-section delta/clear packets remain.
     */
    public static void broadcastSections(ServerLevel level, Iterable<Long> sectionKeys) {
        if (level == null || sectionKeys == null) {
            return;
        }
        Set<Long> chunks = new HashSet<>();
        for (Long sectionKey : sectionKeys) {
            if (sectionKey == null) {
                continue;
            }
            SectionPos section = SectionPos.of(sectionKey);
            chunks.add(ChunkPos.asLong(section.x(), section.z()));
        }
        for (long chunkKey : chunks) {
            ChunkPos chunkPos = new ChunkPos(chunkKey);
            long revision = bumpRevision(level, chunkKey);
            MirageLightChunkSnapshotPayload payload = snapshot(level, chunkPos, revision);
            for (ServerPlayer player : level.players()) {
                if (isNearChunk(player, chunkPos)) {
                    PacketDistributor.sendToPlayer(player, payload);
                }
            }
        }
    }

    /** Called after vanilla transmits a watched chunk. */
    public static void onChunkSent(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        sendChunkSnapshot(player, level, chunkPos);
    }


    /**
     * dev.76g source-centric watchdog heartbeat.
     *
     * A Mature Cluster advertises only chunk revision numbers for its local watch
     * window. This is intentionally tiny compared with section snapshots. Clients
     * request a full snapshot only when their installed revision is missing/stale.
     */
    public static void broadcastRevisionManifestForSource(ServerLevel level, MirageLightSource source) {
        if (level == null || source == null || !source.active()) {
            return;
        }
        int chunkRadius = Math.max(2, (source.profile().maxRadius() + 15) / 16);
        ChunkPos sourceChunk = new ChunkPos(source.origin());
        List<MirageLightChunkRevisionManifestPayload.Entry> entries = new ArrayList<>();
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkPos chunkPos = new ChunkPos(sourceChunk.x + dx, sourceChunk.z + dz);
                if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
                    continue;
                }
                long chunkKey = chunkPos.toLong();
                entries.add(new MirageLightChunkRevisionManifestPayload.Entry(
                        chunkKey,
                        currentRevision(level, chunkKey),
                        MirageLightEngine.aggregateSectionKeysForChunk(level, chunkPos).size()
                ));
            }
        }
        if (entries.isEmpty()) {
            return;
        }
        MirageLightChunkRevisionManifestPayload payload = new MirageLightChunkRevisionManifestPayload(
                source.origin().asLong(),
                List.copyOf(entries)
        );
        for (ServerPlayer player : level.players()) {
            if (isNearChunk(player, sourceChunk)) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }


    public static void clearLevel(ServerLevel level) {
        synchronized (CHUNK_REVISIONS) {
            CHUNK_REVISIONS.remove(level);
        }
    }

    public static void sendChunkSnapshot(ServerPlayer player, ServerLevel level, ChunkPos chunkPos) {
        if (player == null || level == null || chunkPos == null || player.serverLevel() != level) {
            return;
        }
        if (!isNearChunk(player, chunkPos)) {
            return;
        }
        long chunkKey = chunkPos.toLong();
        PacketDistributor.sendToPlayer(player, snapshot(level, chunkPos, currentRevision(level, chunkKey)));
    }

    private static MirageLightChunkSnapshotPayload snapshot(ServerLevel level, ChunkPos chunkPos, long revision) {
        List<MirageLightChunkSnapshotPayload.SectionData> sections = new ArrayList<>();
        List<Long> keys = new ArrayList<>(MirageLightEngine.aggregateSectionKeysForChunk(level, chunkPos));
        keys.sort(Long::compare);
        for (long sectionKey : keys) {
            byte[] levels = MirageLightEngine.copyAggregateSectionLevels(level, sectionKey);
            if (levels == null) {
                continue;
            }
            sections.add(MirageLightChunkSnapshotPayload.section(SectionPos.of(sectionKey).y(), levels));
        }
        return new MirageLightChunkSnapshotPayload(chunkPos.toLong(), revision, List.copyOf(sections));
    }

    private static long currentRevision(ServerLevel level, long chunkKey) {
        synchronized (CHUNK_REVISIONS) {
            return CHUNK_REVISIONS
                    .computeIfAbsent(level, ignored -> new HashMap<>())
                    .getOrDefault(chunkKey, 0L);
        }
    }

    private static long bumpRevision(ServerLevel level, long chunkKey) {
        synchronized (CHUNK_REVISIONS) {
            Map<Long, Long> revisions = CHUNK_REVISIONS.computeIfAbsent(level, ignored -> new HashMap<>());
            long next = revisions.getOrDefault(chunkKey, 0L) + 1L;
            revisions.put(chunkKey, next);
            return next;
        }
    }

    private static boolean isNearChunk(ServerPlayer player, ChunkPos chunkPos) {
        ChunkPos playerChunk = player.chunkPosition();
        return Math.abs(chunkPos.x - playerChunk.x) <= MAX_CLIENT_SYNC_CHUNK_DISTANCE
                && Math.abs(chunkPos.z - playerChunk.z) <= MAX_CLIENT_SYNC_CHUNK_DISTANCE;
    }
}

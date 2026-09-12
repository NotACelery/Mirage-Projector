package celerbi.mirageprojector.client;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.network.MirageLightChunkSnapshotPayload;
import celerbi.mirageprojector.network.MirageLightChunkRevisionManifestPayload;
import celerbi.mirageprojector.network.RequestMirageLightChunkPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;

/** Client mirror for server-authoritative STATIC_WORLD Mirage light. */
public final class ClientMirageLightSync {
    private static ClientLevel revisionLevel;
    private static final Map<Long, Long> CHUNK_REVISIONS = new HashMap<>();
    private static final Map<Long, PendingRequest> PENDING_REQUESTS = new HashMap<>();
    private static final long REQUEST_RETRY_TICKS = 40L;

    private ClientMirageLightSync() {
    }

    /**
     * Atomically replace every Mirage section for one chunk from a single authoritative packet.
     * Older snapshots are ignored so a delayed login/watch response cannot erase newer light.
     */
    public static void applyChunkSnapshot(MirageLightChunkSnapshotPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || payload == null) {
            return;
        }
        ensureRevisionLevel(level);

        long chunkKey = payload.packedChunkPos();
        long installedRevision = CHUNK_REVISIONS.getOrDefault(chunkKey, Long.MIN_VALUE);
        if (payload.revision() < installedRevision) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(chunkKey);
        Map<Long, byte[]> replacement = new HashMap<>();
        for (MirageLightChunkSnapshotPayload.SectionData section : payload.sections()) {
            long sectionKey = SectionPos.asLong(chunkPos.x, section.sectionY(), chunkPos.z);
            replacement.put(sectionKey, section.unpackLevels());
        }

        Set<Long> dirty = MirageLightEngine.replaceAuthoritativeChunk(level, chunkPos, replacement);
        CHUNK_REVISIONS.put(chunkKey, payload.revision());
        PendingRequest pendingRequest = PENDING_REQUESTS.get(chunkKey);
        if (pendingRequest != null && payload.revision() >= pendingRequest.revision()) {
            PENDING_REQUESTS.remove(chunkKey);
        }

        /*
         * dev.76h: invalidate every section carried by the authoritative snapshot even
         * when its bytes are identical to the mirror we already have. A chunk-load
         * handshake can arrive after an external light consumer cached vanilla-only
         * values; byte equality in Mirage storage does not mean downstream caches are
         * current. The exact changed set is still included so removed sections refresh.
         */
        java.util.HashSet<Long> refresh = new java.util.HashSet<>(dirty);
        refresh.addAll(replacement.keySet());
        invalidateSections(minecraft, refresh);
    }

    /**
     * dev.76g source-centric revision heartbeat. Only request a full chunk snapshot when
     * the authoritative server revision is newer than the client's installed mirror.
     */
    public static void applyRevisionManifest(MirageLightChunkRevisionManifestPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || payload == null) {
            return;
        }
        ensureRevisionLevel(level);

        for (MirageLightChunkRevisionManifestPayload.Entry entry : payload.entries()) {
            long chunkKey = entry.packedChunkPos();
            ChunkPos chunkPos = new ChunkPos(chunkKey);
            if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
                continue;
            }
            long installed = CHUNK_REVISIONS.getOrDefault(chunkKey, Long.MIN_VALUE);
            int localSectionCount = MirageLightEngine.authoritativeSectionKeysForChunk(level, chunkPos).size();
            boolean staleRevision = installed < entry.revision();
            boolean shapeMismatch = localSectionCount != entry.sectionCount();
            if (!staleRevision && !shapeMismatch) {
                continue;
            }

            long now = level.getGameTime();
            PendingRequest pending = PENDING_REQUESTS.get(chunkKey);
            if (pending != null
                    && pending.revision() >= entry.revision()
                    && now - pending.requestTick() < REQUEST_RETRY_TICKS) {
                continue;
            }
            PENDING_REQUESTS.put(chunkKey, new PendingRequest(entry.revision(), now));
            PacketDistributor.sendToServer(new RequestMirageLightChunkPayload(chunkKey));
        }
    }

    public static void resetSession() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != null) {
            Set<Long> dirty = MirageLightEngine.sectionKeys(level);
            MirageLightEngine.clear(level);
            invalidateSections(minecraft, dirty);
        }
        revisionLevel = null;
        CHUNK_REVISIONS.clear();
        PENDING_REQUESTS.clear();
    }

    private static void ensureRevisionLevel(ClientLevel level) {
        if (revisionLevel == level) {
            return;
        }
        revisionLevel = level;
        CHUNK_REVISIONS.clear();
        PENDING_REQUESTS.clear();
    }

    private static void invalidateSections(Minecraft minecraft, Set<Long> sectionKeys) {
        if (minecraft == null || sectionKeys == null || sectionKeys.isEmpty()) {
            return;
        }
        ClientLevel level = minecraft.level;
        java.util.HashSet<Long> refreshKeys = new java.util.HashSet<>();
        for (long sectionKey : sectionKeys) {
            SectionPos section = SectionPos.of(sectionKey);
            refreshKeys.add(sectionKey);
            /*
             * A light value in section Y can illuminate the top faces / spawn-label
             * samples whose floor block lives in section Y-1. Vanilla block-change
             * invalidation handles this boundary explicitly; virtual Mirage light must
             * do the same. This is especially visible on test floors at Y=79/80.
             */
            refreshKeys.add(section.offset(0, -1, 0).asLong());
        }
        for (long refreshKey : refreshKeys) {
            SectionPos section = SectionPos.of(refreshKey);
            if (level != null) {
                level.getChunkSource().onLightUpdate(LightLayer.BLOCK, section);
            }
            if (minecraft.levelRenderer != null) {
                minecraft.levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            }
        }
    }

    private record PendingRequest(long revision, long requestTick) {
    }

}

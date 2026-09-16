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
         * Invalidate every section carried by the authoritative snapshot even
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
     * Source-centric revision heartbeat. Only request a full chunk snapshot when
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

    static void invalidateSections(Minecraft minecraft, Set<Long> sectionKeys) {
        if (minecraft == null || sectionKeys == null || sectionKeys.isEmpty()) {
            return;
        }

        /*
         * Static/authoritative updates are comparatively rare, so invalidate the complete
         * one-section halo. Chunk meshes can sample light from the one-block neighborhood
         * outside their own section (faces, AO edges/corners). Dirtifying only the section
         * that owns the changed light leaves adjacent wall/floor meshes baked with stale
         * packed light until an unrelated block update happens.
         */
        java.util.HashSet<Long> refreshKeys = new java.util.HashSet<>();
        for (long sectionKey : sectionKeys) {
            SectionPos section = SectionPos.of(sectionKey);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        refreshKeys.add(section.offset(dx, dy, dz).asLong());
                    }
                }
            }
        }
        invalidateExact(minecraft, refreshKeys);
    }

    /**
     * Optimized invalidation for moving DYNAMIC_VISUAL emitters.
     *
     * <p>A moving Lantern may rebuild every two ticks, so the full 3x3x3 halo above would be
     * needlessly expensive. Instead, always rebuild sections whose Mirage bytes changed and
     * rebuild a neighboring render section only when a changed voxel lies on the matching
     * section face/edge/corner that the neighbor can sample.</p>
     */
    static void invalidateDynamicSections(
            Minecraft minecraft,
            Set<Long> changedSections,
            Map<Long, byte[]> beforeLevels
    ) {
        if (minecraft == null || changedSections == null || changedSections.isEmpty()) {
            return;
        }
        ClientLevel level = minecraft.level;
        java.util.HashSet<Long> refreshKeys = new java.util.HashSet<>(changedSections);
        Map<Long, byte[]> safeBefore = beforeLevels == null ? Map.of() : beforeLevels;

        for (long sectionKey : changedSections) {
            SectionPos section = SectionPos.of(sectionKey);
            byte[] before = safeBefore.get(sectionKey);
            byte[] after = level == null ? null : MirageLightEngine.copyAggregateSectionLevels(level, sectionKey);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        if (boundaryChanged(before, after, dx, dy, dz)) {
                            refreshKeys.add(section.offset(dx, dy, dz).asLong());
                        }
                    }
                }
            }
        }
        invalidateExact(minecraft, refreshKeys);
    }

    private static boolean boundaryChanged(
            byte[] before,
            byte[] after,
            int sectionDx,
            int sectionDy,
            int sectionDz
    ) {
        for (int y = 0; y < 16; y++) {
            if (sectionDy < 0 && y != 0) continue;
            if (sectionDy > 0 && y != 15) continue;
            for (int z = 0; z < 16; z++) {
                if (sectionDz < 0 && z != 0) continue;
                if (sectionDz > 0 && z != 15) continue;
                for (int x = 0; x < 16; x++) {
                    if (sectionDx < 0 && x != 0) continue;
                    if (sectionDx > 0 && x != 15) continue;
                    int index = (y << 8) | (z << 4) | x;
                    int oldValue = before == null ? 0 : Byte.toUnsignedInt(before[index]);
                    int newValue = after == null ? 0 : Byte.toUnsignedInt(after[index]);
                    if (oldValue != newValue) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void invalidateExact(Minecraft minecraft, Set<Long> refreshKeys) {
        ClientLevel level = minecraft.level;
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

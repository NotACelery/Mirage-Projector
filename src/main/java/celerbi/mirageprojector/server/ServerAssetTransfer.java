package celerbi.mirageprojector.server;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
import celerbi.mirageprojector.network.AssetUploadAckPayload;
import celerbi.mirageprojector.network.DownloadAssetChunkPayload;
import celerbi.mirageprojector.network.UploadAssetChunkPayload;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ServerAssetTransfer {
    private static final long SESSION_TIMEOUT_MS = 30_000L;
    private static final int MAX_ACTIVE_UPLOADS_PER_PLAYER = 4;
    private static final int MAX_ACTIVE_UPLOADS_GLOBAL = 64;
    private static final Map<UploadKey, UploadSession> UPLOADS = new HashMap<>();

    private ServerAssetTransfer() {
    }

    public static void receiveUploadChunk(ServerPlayer player, UploadAssetChunkPayload payload) {
        MinecraftServer server = player.getServer();
        if (server == null || !validEnvelope(payload.assetId(), payload.chunkIndex(), payload.totalChunks(), payload.totalBytes(), payload.data())) {
            return;
        }

        if (ServerAssetStore.exists(server, payload.assetId())) {
            UPLOADS.remove(new UploadKey(player.getUUID(), payload.assetId()));
            sendUploadAck(player, payload.assetId(), true, "Already stored");
            return;
        }

        purgeExpired();
        UploadKey key = new UploadKey(player.getUUID(), payload.assetId());
        if (!UPLOADS.containsKey(key)) {
            long playerSessions = UPLOADS.keySet().stream()
                    .filter(existing -> existing.playerId().equals(player.getUUID()))
                    .count();
            if (playerSessions >= MAX_ACTIVE_UPLOADS_PER_PLAYER || UPLOADS.size() >= MAX_ACTIVE_UPLOADS_GLOBAL) {
                sendUploadAck(player, payload.assetId(), false, "Too many active uploads");
                return;
            }
        }
        UploadSession session = UPLOADS.computeIfAbsent(key,
                ignored -> new UploadSession(payload.totalChunks(), payload.totalBytes()));

        if (!session.matches(payload.totalChunks(), payload.totalBytes())) {
            UPLOADS.remove(key);
            sendUploadAck(player, payload.assetId(), false, "Upload envelope changed during transfer");
            return;
        }

        if (!session.accept(payload.chunkIndex(), payload.data())) {
            UPLOADS.remove(key);
            sendUploadAck(player, payload.assetId(), false, "Conflicting upload chunk");
            return;
        }

        if (!session.complete()) {
            return;
        }

        UPLOADS.remove(key);
        try {
            byte[] bytes = session.assemble();
            ServerAssetStore.store(server, payload.assetId(), bytes);
            sendUploadAck(player, payload.assetId(), true, "Stored on server");
        } catch (IOException exception) {
            sendUploadAck(player, payload.assetId(), false, "Server rejected the projection asset");
            MirageProjector.LOGGER.warn("Rejected Mirage asset upload {} from {}", payload.assetId(), player.getGameProfile().getName(), exception);
        }
    }

    public static void sendAsset(ServerPlayer player, String assetId) {
        MinecraftServer server = player.getServer();
        if (server == null || !ProjectionAssetRules.isValidAssetId(assetId)) {
            return;
        }

        ServerAssetStore.read(server, assetId).ifPresent(bytes -> {
            int totalChunks = (bytes.length + ProjectionAssetRules.NETWORK_CHUNK_BYTES - 1)
                    / ProjectionAssetRules.NETWORK_CHUNK_BYTES;
            for (int index = 0; index < totalChunks; index++) {
                int start = index * ProjectionAssetRules.NETWORK_CHUNK_BYTES;
                int end = Math.min(bytes.length, start + ProjectionAssetRules.NETWORK_CHUNK_BYTES);
                byte[] chunk = Arrays.copyOfRange(bytes, start, end);
                PacketDistributor.sendToPlayer(player, new DownloadAssetChunkPayload(
                        assetId,
                        index,
                        totalChunks,
                        bytes.length,
                        chunk
                ));
            }
        });
    }

    private static void sendUploadAck(ServerPlayer player, String assetId, boolean accepted, String message) {
        PacketDistributor.sendToPlayer(player, new AssetUploadAckPayload(assetId, accepted, message));
    }

    private static boolean validEnvelope(String assetId, int index, int totalChunks, int totalBytes, byte[] data) {
        if (!ProjectionAssetRules.isValidAssetId(assetId)) {
            return false;
        }
        if (totalBytes <= 0 || totalBytes > ProjectionAssetRules.MAX_ASSET_BYTES) {
            return false;
        }
        if (totalChunks <= 0 || totalChunks > ProjectionAssetRules.MAX_CHUNKS) {
            return false;
        }
        if (index < 0 || index >= totalChunks) {
            return false;
        }
        if (data == null || data.length <= 0 || data.length > ProjectionAssetRules.NETWORK_CHUNK_BYTES) {
            return false;
        }
        int expectedChunks = (totalBytes + ProjectionAssetRules.NETWORK_CHUNK_BYTES - 1)
                / ProjectionAssetRules.NETWORK_CHUNK_BYTES;
        return totalChunks == expectedChunks;
    }

    private static void purgeExpired() {
        long now = System.currentTimeMillis();
        UPLOADS.entrySet().removeIf(entry -> now - entry.getValue().lastTouchedMs > SESSION_TIMEOUT_MS);
    }

    private record UploadKey(UUID playerId, String assetId) {
    }

    private static final class UploadSession {
        private final byte[][] chunks;
        private final boolean[] received;
        private final int totalBytes;
        private int receivedCount;
        private long lastTouchedMs = System.currentTimeMillis();

        private UploadSession(int totalChunks, int totalBytes) {
            this.chunks = new byte[totalChunks][];
            this.received = new boolean[totalChunks];
            this.totalBytes = totalBytes;
        }

        private boolean matches(int totalChunks, int totalBytes) {
            return chunks.length == totalChunks && this.totalBytes == totalBytes;
        }

        private boolean accept(int index, byte[] data) {
            lastTouchedMs = System.currentTimeMillis();
            if (received[index]) {
                return Arrays.equals(chunks[index], data);
            }
            chunks[index] = Arrays.copyOf(data, data.length);
            received[index] = true;
            receivedCount++;
            return true;
        }

        private boolean complete() {
            return receivedCount == chunks.length;
        }

        private byte[] assemble() throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream(totalBytes);
            for (byte[] chunk : chunks) {
                if (chunk == null) {
                    throw new IOException("Missing upload chunk");
                }
                output.write(chunk);
            }
            byte[] bytes = output.toByteArray();
            if (bytes.length != totalBytes) {
                throw new IOException("Upload byte count does not match envelope");
            }
            return bytes;
        }
    }
}

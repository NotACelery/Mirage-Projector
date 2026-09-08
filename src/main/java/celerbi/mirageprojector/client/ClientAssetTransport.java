package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
import celerbi.mirageprojector.network.AssetUploadAckPayload;
import celerbi.mirageprojector.network.DownloadAssetChunkPayload;
import celerbi.mirageprojector.network.RequestAssetPayload;
import celerbi.mirageprojector.network.UploadAssetChunkPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public final class ClientAssetTransport {
    private static final long REQUEST_RETRY_MS = 5_000L;
    private static final long SESSION_TIMEOUT_MS = 30_000L;
    private static final Map<String, Long> REQUESTED = new HashMap<>();
    private static final Set<String> UPLOADED_THIS_SESSION = new HashSet<>();
    private static final Map<String, String> UPLOAD_STATUS = new HashMap<>();
    private static final Map<String, DownloadSession> DOWNLOADS = new HashMap<>();

    private ClientAssetTransport() {
    }

    public static void uploadIfPresent(String assetId) {
        if (!ProjectionAssetRules.isValidAssetId(assetId) || UPLOADED_THIS_SESSION.contains(assetId)) {
            return;
        }

        Path path = cachePath(assetId);
        if (!Files.isRegularFile(path)) {
            UPLOAD_STATUS.put(assetId, "Upload failed · local cache file missing");
            MirageProjector.LOGGER.warn("Cannot upload Mirage asset {} because it is missing from the local cache", assetId);
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length <= 0 || bytes.length > ProjectionAssetRules.MAX_NORMALIZED_BYTES) {
                UPLOAD_STATUS.put(assetId, "Upload failed · invalid normalized size");
                MirageProjector.LOGGER.warn("Cannot upload Mirage asset {} because its size is invalid", assetId);
                return;
            }
            if (!ProjectionAssetRules.sha256(bytes).equals(assetId)) {
                UPLOAD_STATUS.put(assetId, "Upload failed · local SHA-256 mismatch");
                MirageProjector.LOGGER.warn("Cannot upload Mirage asset {} because its local SHA-256 does not match", assetId);
                return;
            }

            int totalChunks = (bytes.length + ProjectionAssetRules.NETWORK_CHUNK_BYTES - 1)
                    / ProjectionAssetRules.NETWORK_CHUNK_BYTES;
            UPLOAD_STATUS.put(assetId, "Uploading 0%");
            for (int index = 0; index < totalChunks; index++) {
                int start = index * ProjectionAssetRules.NETWORK_CHUNK_BYTES;
                int end = Math.min(bytes.length, start + ProjectionAssetRules.NETWORK_CHUNK_BYTES);
                PacketDistributor.sendToServer(new UploadAssetChunkPayload(
                        assetId,
                        index,
                        totalChunks,
                        bytes.length,
                        Arrays.copyOfRange(bytes, start, end)
                ));
                int percent = Math.round((index + 1) * 100.0F / totalChunks);
                UPLOAD_STATUS.put(assetId, "Upload sent " + percent + "% · awaiting server");
            }
            UPLOADED_THIS_SESSION.add(assetId);
        } catch (IOException exception) {
            UPLOAD_STATUS.put(assetId, "Upload failed · local I/O error");
            MirageProjector.LOGGER.warn("Could not upload Mirage asset {}", assetId, exception);
        }
    }

    public static void receiveUploadAck(AssetUploadAckPayload payload) {
        if (!ProjectionAssetRules.isValidAssetId(payload.assetId())) {
            return;
        }
        if (payload.accepted()) {
            UPLOADED_THIS_SESSION.add(payload.assetId());
            UPLOAD_STATUS.put(payload.assetId(), "Upload complete · " + payload.message());
        } else {
            UPLOADED_THIS_SESSION.remove(payload.assetId());
            UPLOAD_STATUS.put(payload.assetId(), "Upload failed · " + payload.message());
        }
    }

    public static String statusSummary(String assetId) {
        if (!ProjectionAssetRules.isValidAssetId(assetId)) {
            return "";
        }
        DownloadSession download = DOWNLOADS.get(assetId);
        if (download != null && download.chunks.length > 0) {
            int percent = Math.round(download.receivedCount * 100.0F / download.chunks.length);
            return "Download " + percent + "%";
        }
        return UPLOAD_STATUS.getOrDefault(assetId, "");
    }

    public static void retryUpload(String assetId) {
        if (!ProjectionAssetRules.isValidAssetId(assetId)) {
            return;
        }
        UPLOADED_THIS_SESSION.remove(assetId);
        UPLOAD_STATUS.remove(assetId);
        uploadIfPresent(assetId);
    }

    public static void requestIfMissing(String assetId) {
        if (!ProjectionAssetRules.isValidAssetId(assetId) || Files.isRegularFile(cachePath(assetId))) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = REQUESTED.get(assetId);
        if (last != null && now - last < REQUEST_RETRY_MS) {
            return;
        }
        REQUESTED.put(assetId, now);
        PacketDistributor.sendToServer(new RequestAssetPayload(assetId));
    }

    public static void receiveDownloadChunk(DownloadAssetChunkPayload payload) {
        if (!validEnvelope(payload)) {
            return;
        }

        purgeExpired();
        DownloadSession session = DOWNLOADS.computeIfAbsent(
                payload.assetId(),
                ignored -> new DownloadSession(payload.totalChunks(), payload.totalBytes())
        );
        if (!session.matches(payload.totalChunks(), payload.totalBytes())) {
            DOWNLOADS.remove(payload.assetId());
            return;
        }
        if (!session.accept(payload.chunkIndex(), payload.data())) {
            DOWNLOADS.remove(payload.assetId());
            return;
        }
        if (!session.complete()) {
            return;
        }

        DOWNLOADS.remove(payload.assetId());
        try {
            byte[] bytes = session.assemble();
            if (!ProjectionAssetRules.sha256(bytes).equals(payload.assetId())) {
                throw new IOException("Downloaded asset hash mismatch");
            }
            Path target = cachePath(payload.assetId());
            Files.createDirectories(target.getParent());
            Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            Files.write(temp, bytes);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicFailure) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            ProjectionTextureCache.invalidate(payload.assetId());
            REQUESTED.remove(payload.assetId());
            MirageProjector.LOGGER.info("Downloaded Mirage projection asset {} ({} bytes)", payload.assetId(), bytes.length);
        } catch (IOException exception) {
            REQUESTED.remove(payload.assetId());
            MirageProjector.LOGGER.warn("Could not complete Mirage asset download {}", payload.assetId(), exception);
        }
    }

    public static void resetSession() {
        REQUESTED.clear();
        DOWNLOADS.clear();
        UPLOADED_THIS_SESSION.clear();
        UPLOAD_STATUS.clear();
    }

    public static Path cachePath(String assetId) {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("mirage_projector")
                .resolve("cache")
                .resolve(assetId + ".png");
    }

    private static boolean validEnvelope(DownloadAssetChunkPayload payload) {
        if (!ProjectionAssetRules.isValidAssetId(payload.assetId())) {
            return false;
        }
        if (payload.totalBytes() <= 0 || payload.totalBytes() > ProjectionAssetRules.MAX_NORMALIZED_BYTES) {
            return false;
        }
        if (payload.totalChunks() <= 0 || payload.totalChunks() > ProjectionAssetRules.MAX_CHUNKS) {
            return false;
        }
        if (payload.chunkIndex() < 0 || payload.chunkIndex() >= payload.totalChunks()) {
            return false;
        }
        if (payload.data() == null || payload.data().length <= 0
                || payload.data().length > ProjectionAssetRules.NETWORK_CHUNK_BYTES) {
            return false;
        }
        int expectedChunks = (payload.totalBytes() + ProjectionAssetRules.NETWORK_CHUNK_BYTES - 1)
                / ProjectionAssetRules.NETWORK_CHUNK_BYTES;
        return expectedChunks == payload.totalChunks();
    }

    private static void purgeExpired() {
        long now = System.currentTimeMillis();
        DOWNLOADS.entrySet().removeIf(entry -> now - entry.getValue().lastTouchedMs > SESSION_TIMEOUT_MS);
    }

    private static final class DownloadSession {
        private final byte[][] chunks;
        private final boolean[] received;
        private final int totalBytes;
        private int receivedCount;
        private long lastTouchedMs = System.currentTimeMillis();

        private DownloadSession(int totalChunks, int totalBytes) {
            chunks = new byte[totalChunks][];
            received = new boolean[totalChunks];
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
                    throw new IOException("Missing download chunk");
                }
                output.write(chunk);
            }
            byte[] bytes = output.toByteArray();
            if (bytes.length != totalBytes) {
                throw new IOException("Downloaded byte count does not match envelope");
            }
            return bytes;
        }
    }
}

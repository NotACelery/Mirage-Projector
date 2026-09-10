package celerbi.mirageprojector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ProjectionAssetRules {

    public static final int MAX_ASSET_BYTES = 8 * 1024 * 1024;

    public static final int MAX_NORMALIZED_BYTES = MAX_ASSET_BYTES;
    public static final int NETWORK_CHUNK_BYTES = 32 * 1024;
    public static final int MAX_CHUNKS = (MAX_ASSET_BYTES + NETWORK_CHUNK_BYTES - 1) / NETWORK_CHUNK_BYTES;

    public static final int MAX_GIF_DIMENSION = 1024;
    public static final int MAX_GIF_FRAMES = 128;
    public static final long MAX_GIF_FRAME_PIXELS = 16_777_216L;
    public static final int MIN_GIF_DELAY_MS = 20;
    public static final int ZERO_GIF_DELAY_MS = 100;
    public static final int MAX_GIF_DELAY_MS = 10_000;
    public static final long MAX_GIF_LOOP_MS = 5L * 60L * 1000L;

    private ProjectionAssetRules() {
    }

    public static boolean isValidAssetId(String id) {
        if (id == null || id.length() != 64) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if (!hex) {
                return false;
            }
        }
        return true;
    }

    public static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}

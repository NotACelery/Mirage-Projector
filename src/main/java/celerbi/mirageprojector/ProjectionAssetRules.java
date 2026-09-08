package celerbi.mirageprojector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ProjectionAssetRules {
    public static final int MAX_NORMALIZED_BYTES = 4 * 1024 * 1024;
    public static final int NETWORK_CHUNK_BYTES = 32 * 1024;
    public static final int MAX_CHUNKS = (MAX_NORMALIZED_BYTES + NETWORK_CHUNK_BYTES - 1) / NETWORK_CHUNK_BYTES;

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

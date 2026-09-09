package celerbi.mirageprojector;

import java.nio.charset.StandardCharsets;

/** Content-based image sniffing. File extensions are advisory only and never select a decoder. */
public final class ImageAssetFormat {
    private static final byte[] PNG = new byte[]{(byte)0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A};

    private ImageAssetFormat() {}

    public enum Kind {
        PNG_STATIC("PNG", false, true),
        APNG_ANIMATED("APNG", true, false),
        JPEG_STATIC("JPEG", false, true),
        BMP_STATIC("BMP", false, true),
        WEBP_STATIC("WebP", false, true),
        WEBP_ANIMATED("Animated WebP", true, false),
        GIF_ANIMATED("GIF", true, true),
        UNKNOWN("Unknown", false, false);

        private final String displayName;
        private final boolean animated;
        private final boolean supported;
        Kind(String displayName, boolean animated, boolean supported) {
            this.displayName = displayName;
            this.animated = animated;
            this.supported = supported;
        }
        public String displayName() { return displayName; }
        public boolean animated() { return animated; }
        public boolean supported() { return supported; }
    }

    public static Kind detect(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return Kind.UNKNOWN;
        if (startsWith(bytes, "GIF87a") || startsWith(bytes, "GIF89a")) return Kind.GIF_ANIMATED;
        if (isPng(bytes)) return containsPngChunk(bytes, "acTL") ? Kind.APNG_ANIMATED : Kind.PNG_STATIC;
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) return Kind.JPEG_STATIC;
        if (bytes.length >= 2 && bytes[0] == 'B' && bytes[1] == 'M') return Kind.BMP_STATIC;
        if (isWebP(bytes)) return isAnimatedWebP(bytes) ? Kind.WEBP_ANIMATED : Kind.WEBP_STATIC;
        return Kind.UNKNOWN;
    }

    public static boolean isTransportAsset(byte[] bytes) {
        Kind kind = detect(bytes);
        return kind == Kind.PNG_STATIC || kind == Kind.GIF_ANIMATED;
    }

    private static boolean isPng(byte[] bytes) {
        if (bytes.length < PNG.length) return false;
        for (int i = 0; i < PNG.length; i++) if (bytes[i] != PNG[i]) return false;
        return true;
    }

    private static boolean isWebP(byte[] bytes) {
        return bytes.length >= 12 && ascii(bytes, 0, 4).equals("RIFF") && ascii(bytes, 8, 4).equals("WEBP");
    }

    private static boolean isAnimatedWebP(byte[] bytes) {
        // VP8X feature flags: animation = bit 1 (0x02) in byte 20 when the extended header is present.
        if (bytes.length >= 21 && ascii(bytes, 12, 4).equals("VP8X") && (bytes[20] & 0x02) != 0) return true;
        int offset = 12;
        while (offset + 8 <= bytes.length) {
            String chunk = ascii(bytes, offset, 4);
            long size = uint32le(bytes, offset + 4);
            if (chunk.equals("ANIM") || chunk.equals("ANMF")) return true;
            long next = offset + 8L + size + (size & 1L);
            if (next <= offset || next > bytes.length) break;
            offset = (int) next;
        }
        return false;
    }

    private static boolean containsPngChunk(byte[] bytes, String wanted) {
        int offset = 8;
        while (offset + 12 <= bytes.length) {
            long length = uint32be(bytes, offset);
            if (length < 0 || length > Integer.MAX_VALUE) return false;
            String type = ascii(bytes, offset + 4, 4);
            if (wanted.equals(type)) return true;
            long next = offset + 12L + length;
            if (next <= offset || next > bytes.length) return false;
            offset = (int) next;
            if ("IEND".equals(type)) return false;
        }
        return false;
    }

    private static boolean startsWith(byte[] bytes, String magic) {
        byte[] expected = magic.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++) if (bytes[i] != expected[i]) return false;
        return true;
    }

    private static String ascii(byte[] bytes, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > bytes.length) return "";
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }

    private static long uint32le(byte[] bytes, int off) {
        if (off < 0 || off + 4 > bytes.length) return -1;
        return ((long) bytes[off] & 0xff) | (((long) bytes[off+1] & 0xff) << 8)
                | (((long) bytes[off+2] & 0xff) << 16) | (((long) bytes[off+3] & 0xff) << 24);
    }

    private static long uint32be(byte[] bytes, int off) {
        if (off < 0 || off + 4 > bytes.length) return -1;
        return (((long) bytes[off] & 0xff) << 24) | (((long) bytes[off+1] & 0xff) << 16)
                | (((long) bytes[off+2] & 0xff) << 8) | ((long) bytes[off+3] & 0xff);
    }
}

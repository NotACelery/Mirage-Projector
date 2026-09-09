package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ImageAssetFormat;
import celerbi.mirageprojector.ProjectionAssetRules;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bounded GIF compositor used by Mirage animated Image sources.
 * Frames are fully composed once at load time, including offsets, transparency and disposal.
 */
public final class GifAssetDecoder {
    private GifAssetDecoder() {}

    public static DecodedGif decode(byte[] bytes) throws IOException {
        if (ImageAssetFormat.detect(bytes) != ImageAssetFormat.Kind.GIF_ANIMATED) {
            throw new IOException("Asset is not a GIF.");
        }
        if (bytes.length <= 0 || bytes.length > ProjectionAssetRules.MAX_ASSET_BYTES) {
            throw new IOException("GIF is larger than Mirage's 8 MiB asset limit.");
        }

        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (stream == null) throw new IOException("Could not open GIF stream.");
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) throw new IOException("No GIF decoder is available.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, false, false);
                int frameCount = reader.getNumImages(true);
                if (frameCount <= 0 || frameCount > ProjectionAssetRules.MAX_GIF_FRAMES) {
                    throw new IOException("GIF frame count is outside Mirage limits (1-" + ProjectionAssetRules.MAX_GIF_FRAMES + ").");
                }

                int canvasWidth = -1;
                int canvasHeight = -1;
                IIOMetadata streamMetadata = reader.getStreamMetadata();
                if (streamMetadata != null) {
                    Node root = safeTree(streamMetadata, "javax_imageio_gif_stream_1.0");
                    Node descriptor = child(root, "LogicalScreenDescriptor");
                    if (descriptor != null) {
                        canvasWidth = intAttr(descriptor, "logicalScreenWidth", -1);
                        canvasHeight = intAttr(descriptor, "logicalScreenHeight", -1);
                    }
                }
                if (canvasWidth <= 0 || canvasHeight <= 0) {
                    canvasWidth = reader.getWidth(0);
                    canvasHeight = reader.getHeight(0);
                }
                validateCanvas(canvasWidth, canvasHeight);
                long framePixels = (long) canvasWidth * canvasHeight * frameCount;
                if (framePixels > ProjectionAssetRules.MAX_GIF_FRAME_PIXELS) {
                    throw new IOException("GIF expands beyond Mirage's decoded frame-pixel limit.");
                }

                BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                List<BufferedImage> frames = new ArrayList<>(frameCount);
                int[] delays = new int[frameCount];
                long totalDuration = 0L;

                Disposal previousDisposal = Disposal.NONE;
                Rectangle previousRect = null;
                BufferedImage restorePrevious = null;

                for (int i = 0; i < frameCount; i++) {
                    FrameInfo info = frameInfo(reader.getImageMetadata(i), reader.getWidth(i), reader.getHeight(i));
                    validateFrameRect(info, canvasWidth, canvasHeight);

                    // Apply the previous frame disposal before drawing the current frame.
                    if (i > 0 && previousRect != null) {
                        if (previousDisposal == Disposal.BACKGROUND) {
                            Graphics2D clear = canvas.createGraphics();
                            try {
                                clear.setComposite(AlphaComposite.Clear);
                                clear.fillRect(previousRect.x, previousRect.y, previousRect.width, previousRect.height);
                            } finally {
                                clear.dispose();
                            }
                        } else if (previousDisposal == Disposal.PREVIOUS && restorePrevious != null) {
                            Graphics2D restore = canvas.createGraphics();
                            try {
                                restore.setComposite(AlphaComposite.Src);
                                restore.drawImage(restorePrevious, 0, 0, null);
                            } finally {
                                restore.dispose();
                            }
                        }
                    }

                    BufferedImage beforeCurrent = null;
                    if (info.disposal == Disposal.PREVIOUS) beforeCurrent = deepCopy(canvas);

                    BufferedImage raw = reader.read(i);
                    Graphics2D draw = canvas.createGraphics();
                    try {
                        draw.setComposite(AlphaComposite.SrcOver);
                        draw.drawImage(raw, info.left, info.top, null);
                    } finally {
                        draw.dispose();
                    }
                    frames.add(deepCopy(canvas));

                    int delayMs = info.delayCentiseconds <= 0
                            ? ProjectionAssetRules.ZERO_GIF_DELAY_MS
                            : info.delayCentiseconds * 10;
                    delayMs = Math.max(ProjectionAssetRules.MIN_GIF_DELAY_MS,
                            Math.min(ProjectionAssetRules.MAX_GIF_DELAY_MS, delayMs));
                    delays[i] = delayMs;
                    totalDuration += delayMs;
                    if (totalDuration > ProjectionAssetRules.MAX_GIF_LOOP_MS) {
                        throw new IOException("GIF loop duration exceeds Mirage's 5 minute limit.");
                    }

                    previousDisposal = info.disposal;
                    previousRect = new Rectangle(info.left, info.top, info.width, info.height);
                    restorePrevious = beforeCurrent;
                }
                return new DecodedGif(canvasWidth, canvasHeight, List.copyOf(frames), delays, totalDuration);
            } finally {
                reader.dispose();
            }
        }
    }

    private static void validateCanvas(int width, int height) throws IOException {
        if (width <= 0 || height <= 0 || width > ProjectionAssetRules.MAX_GIF_DIMENSION || height > ProjectionAssetRules.MAX_GIF_DIMENSION) {
            throw new IOException("GIF canvas exceeds Mirage's 1024x1024 limit.");
        }
    }

    private static void validateFrameRect(FrameInfo info, int canvasWidth, int canvasHeight) throws IOException {
        if (info.width <= 0 || info.height <= 0 || info.left < 0 || info.top < 0
                || info.left > canvasWidth - info.width || info.top > canvasHeight - info.height) {
            throw new IOException("GIF frame rectangle is outside its declared canvas.");
        }
        if (info.width > ProjectionAssetRules.MAX_GIF_DIMENSION || info.height > ProjectionAssetRules.MAX_GIF_DIMENSION) {
            throw new IOException("GIF frame exceeds Mirage dimensions.");
        }
    }

    private static FrameInfo frameInfo(IIOMetadata metadata, int fallbackW, int fallbackH) {
        Node root = safeTree(metadata, "javax_imageio_gif_image_1.0");
        Node descriptor = child(root, "ImageDescriptor");
        int left = descriptor == null ? 0 : intAttr(descriptor, "imageLeftPosition", 0);
        int top = descriptor == null ? 0 : intAttr(descriptor, "imageTopPosition", 0);
        int width = descriptor == null ? fallbackW : intAttr(descriptor, "imageWidth", fallbackW);
        int height = descriptor == null ? fallbackH : intAttr(descriptor, "imageHeight", fallbackH);
        Node gce = child(root, "GraphicControlExtension");
        int delay = gce == null ? 0 : intAttr(gce, "delayTime", 0);
        String disposal = gce == null ? "none" : stringAttr(gce, "disposalMethod", "none");
        return new FrameInfo(left, top, width, height, delay, Disposal.from(disposal));
    }

    private static Node safeTree(IIOMetadata metadata, String format) {
        try { return metadata == null ? null : metadata.getAsTree(format); }
        catch (RuntimeException ignored) { return null; }
    }

    private static Node child(Node root, String name) {
        if (root == null) return null;
        for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (name.equals(node.getNodeName())) return node;
        }
        return null;
    }

    private static int intAttr(Node node, String name, int fallback) {
        try { return Integer.parseInt(stringAttr(node, name, Integer.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static String stringAttr(Node node, String name, String fallback) {
        if (node == null) return fallback;
        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs == null ? null : attrs.getNamedItem(name);
        return attr == null ? fallback : attr.getNodeValue();
    }

    private static BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(source, 0, 0, null);
        } finally { g.dispose(); }
        return copy;
    }

    private enum Disposal {
        NONE, BACKGROUND, PREVIOUS;
        static Disposal from(String value) {
            if ("restoreToBackgroundColor".equals(value)) return BACKGROUND;
            if ("restoreToPrevious".equals(value)) return PREVIOUS;
            return NONE;
        }
    }

    private record FrameInfo(int left, int top, int width, int height, int delayCentiseconds, Disposal disposal) {}

    public record DecodedGif(int width, int height, List<BufferedImage> frames, int[] delaysMs, long loopDurationMs) {
        public int frameIndexAt(long timeMs) {
            if (frames.isEmpty() || loopDurationMs <= 0) return 0;
            long cursor = Math.floorMod(timeMs, loopDurationMs);
            long elapsed = 0L;
            for (int i = 0; i < delaysMs.length; i++) {
                elapsed += delaysMs[i];
                if (cursor < elapsed) return i;
            }
            return delaysMs.length - 1;
        }
    }
}

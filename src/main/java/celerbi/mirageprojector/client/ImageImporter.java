package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ImageAssetFormat;
import celerbi.mirageprojector.ProjectionAssetRules;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Content-sniffed import pipeline. Extensions never select the decoder. */
public final class ImageImporter {
    public static final int MAX_DIMENSION = 2048;
    public static final long MAX_NORMALIZED_BYTES = ProjectionAssetRules.MAX_ASSET_BYTES;
    public static final long MAX_SOURCE_BYTES = 32L * 1024L * 1024L;
    private static volatile boolean imageIoPluginsScanned;

    private ImageImporter() {}

    public static ImportedImage importFile(Path source) throws IOException {
        if (!Files.isRegularFile(source)) throw new IOException("The selected file does not exist.");
        long sourceBytes = Files.size(source);
        if (sourceBytes <= 0 || sourceBytes > MAX_SOURCE_BYTES) throw new IOException("Source file is larger than 32 MiB.");

        byte[] bytes = Files.readAllBytes(source);
        ImageAssetFormat.Kind detected = ImageAssetFormat.detect(bytes);
        if (detected == ImageAssetFormat.Kind.WEBP_ANIMATED) {
            throw new IOException("This file is actually an animated WebP. Animated WebP is not supported yet; convert it to GIF or a static PNG/JPG/WebP/BMP image.");
        }
        if (detected == ImageAssetFormat.Kind.APNG_ANIMATED) {
            throw new IOException("This file is actually an animated PNG (APNG). APNG is not supported yet; convert it to GIF or a static image.");
        }
        if (detected == ImageAssetFormat.Kind.UNKNOWN) {
            throw new IOException("Unsupported image format. Supported static formats: PNG, JPG/JPEG, WebP and BMP. Supported animated format: GIF.");
        }

        if (detected == ImageAssetFormat.Kind.GIF_ANIMATED) {
            if (bytes.length > ProjectionAssetRules.MAX_ASSET_BYTES) throw new IOException("GIF is larger than 8 MiB.");
            GifAssetDecoder.DecodedGif gif = GifAssetDecoder.decode(bytes); // full bounded validation before persistence
            String hash = ProjectionAssetRules.sha256(bytes);
            Path target = cacheDir().resolve(hash + ".asset");
            if (!Files.exists(target)) Files.write(target, bytes);
            return new ImportedImage(hash, gif.width(), gif.height(), target, bytes.length, detected, true, gif.frames().size());
        }

        ensureImageIoPlugins();
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
        if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0) {
            if (detected == ImageAssetFormat.Kind.WEBP_STATIC) {
                throw new IOException("Static WebP was detected, but no WebP decoder could read it. Check the bundled WebP decoder/build.");
            }
            throw new IOException("The detected " + detected.displayName() + " image could not be decoded.");
        }

        BufferedImage normalized = toArgb(decoded);
        if (Math.max(normalized.getWidth(), normalized.getHeight()) > MAX_DIMENSION) normalized = resizeToMaxDimension(normalized, MAX_DIMENSION);
        byte[] png = encodePng(normalized);
        while (png.length > MAX_NORMALIZED_BYTES && Math.max(normalized.getWidth(), normalized.getHeight()) > 128) {
            int nextMax = Math.max(128, (int)(Math.max(normalized.getWidth(), normalized.getHeight()) * 0.85D));
            normalized = resizeToMaxDimension(normalized, nextMax);
            png = encodePng(normalized);
        }
        if (png.length > MAX_NORMALIZED_BYTES) throw new IOException("Normalized image remains larger than 8 MiB.");

        String hash = ProjectionAssetRules.sha256(png);
        Path target = cacheDir().resolve(hash + ".asset");
        if (!Files.exists(target)) Files.write(target, png);
        return new ImportedImage(hash, normalized.getWidth(), normalized.getHeight(), target, png.length, detected, false, 1);
    }

    private static Path cacheDir() throws IOException {
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("mirage_projector").resolve("cache");
        Files.createDirectories(dir);
        return dir;
    }

    /** Discover embedded ImageIO providers (notably WebP) once per client process. */
    private static void ensureImageIoPlugins() {
        if (imageIoPluginsScanned) return;
        synchronized (ImageImporter.class) {
            if (!imageIoPluginsScanned) {
                ImageIO.scanForPlugins();
                imageIoPluginsScanned = true;
            }
        }
    }

    private static BufferedImage toArgb(BufferedImage source) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        try { graphics.drawImage(source, 0, 0, null); }
        finally { graphics.dispose(); }
        return target;
    }

    private static BufferedImage resizeToMaxDimension(BufferedImage source, int maxDimension) {
        int sourceMax = Math.max(source.getWidth(), source.getHeight());
        if (sourceMax <= maxDimension) return source;
        double factor = maxDimension / (double)sourceMax;
        int width = Math.max(1, (int)Math.round(source.getWidth() * factor));
        int height = Math.max(1, (int)Math.round(source.getHeight() * factor));
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally { graphics.dispose(); }
        return target;
    }

    private static byte[] encodePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", output)) throw new IOException("No PNG encoder is available.");
        return output.toByteArray();
    }

    public record ImportedImage(String hash, int width, int height, Path cachedFile, long normalizedBytes,
                                ImageAssetFormat.Kind detectedFormat, boolean animated, int frameCount) {}
}

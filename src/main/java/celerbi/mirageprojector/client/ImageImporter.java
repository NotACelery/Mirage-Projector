package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionAssetRules;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ImageImporter {
    public static final int MAX_DIMENSION = 2048;
    public static final long MAX_NORMALIZED_BYTES = ProjectionAssetRules.MAX_NORMALIZED_BYTES;
    public static final long MAX_SOURCE_BYTES = 32L * 1024L * 1024L;
    private static volatile boolean imageIoPluginsScanned;

    private ImageImporter() {
    }

    public static ImportedImage importFile(Path source) throws IOException {
        if (!Files.isRegularFile(source)) {
            throw new IOException("The selected file does not exist.");
        }
        if (Files.size(source) > MAX_SOURCE_BYTES) {
            throw new IOException("Source file is larger than 32 MiB.");
        }

        ensureImageIoPlugins();
        BufferedImage decoded = ImageIO.read(source.toFile());
        if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0) {
            throw new IOException("Unsupported or invalid image. Mirage supports PNG/JPG/JPEG/WebP.");
        }

        BufferedImage normalized = toArgb(decoded);
        if (Math.max(normalized.getWidth(), normalized.getHeight()) > MAX_DIMENSION) {
            normalized = resizeToMaxDimension(normalized, MAX_DIMENSION);
        }

        byte[] png = encodePng(normalized);
        while (png.length > MAX_NORMALIZED_BYTES && Math.max(normalized.getWidth(), normalized.getHeight()) > 128) {
            int nextMax = Math.max(128, (int) (Math.max(normalized.getWidth(), normalized.getHeight()) * 0.85D));
            normalized = resizeToMaxDimension(normalized, nextMax);
            png = encodePng(normalized);
        }

        if (png.length > MAX_NORMALIZED_BYTES) {
            throw new IOException("Normalized image remains larger than 4 MiB.");
        }

        String hash = sha256(png);
        Path cacheDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("mirage_projector")
                .resolve("cache");
        Files.createDirectories(cacheDir);
        Path target = cacheDir.resolve(hash + ".png");
        if (!Files.exists(target)) {
            Files.write(target, png);
        }

        return new ImportedImage(hash, normalized.getWidth(), normalized.getHeight(), target, png.length);
    }

    /** Discover embedded ImageIO providers (notably WebP) once per client process. */
    private static void ensureImageIoPlugins() {
        if (imageIoPluginsScanned) {
            return;
        }
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
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private static BufferedImage resizeToMaxDimension(BufferedImage source, int maxDimension) {
        int sourceMax = Math.max(source.getWidth(), source.getHeight());
        if (sourceMax <= maxDimension) {
            return source;
        }

        double factor = maxDimension / (double) sourceMax;
        int width = Math.max(1, (int) Math.round(source.getWidth() * factor));
        int height = Math.max(1, (int) Math.round(source.getHeight() * factor));
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private static byte[] encodePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", output)) {
            throw new IOException("No PNG encoder is available.");
        }
        return output.toByteArray();
    }

    private static String sha256(byte[] bytes) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("SHA-256 is unavailable.", exception);
        }
    }

    public record ImportedImage(String hash, int width, int height, Path cachedFile, long normalizedBytes) {
    }
}

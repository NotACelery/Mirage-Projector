package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ImageAssetFormat;
import celerbi.mirageprojector.MirageProjector;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public final class ProjectionTextureCache {
    private static final Map<String, CachedAsset> REGISTERED = new HashMap<>();

    private ProjectionTextureCache() {
    }

    public static Optional<ResourceLocation> get(String imageId) {
        return get(imageId, (System.nanoTime() / 1_000_000L));
    }

    public static Optional<ResourceLocation> get(String imageId, long sampleTimeMs) {
        if (imageId == null || imageId.isBlank()) {
            return Optional.empty();
        }
        CachedAsset cached = REGISTERED.get(imageId);
        if (cached == null) {
            cached = load(imageId);
            if (cached == null) {
                return Optional.empty();
            }
            REGISTERED.put(imageId, cached);
        }
        return Optional.of(cached.textureAt(sampleTimeMs));
    }

    public static boolean isAnimated(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return false;
        }
        CachedAsset cached = REGISTERED.get(imageId);
        if (cached == null) {
            cached = load(imageId);
            if (cached == null) {
                return false;
            }
            REGISTERED.put(imageId, cached);
        }
        return cached.animated();
    }

    private static CachedAsset load(String imageId) {
        Path file = ClientAssetTransport.existingCachePath(imageId);
        if (file == null) {
            ClientAssetTransport.requestIfMissing(imageId);
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(file);
            ImageAssetFormat.Kind kind = ImageAssetFormat.detect(bytes);
            CachedAsset loaded;
            if (kind == ImageAssetFormat.Kind.GIF_ANIMATED) {
                GifAssetDecoder.DecodedGif gif = GifAssetDecoder.decode(bytes);
                List<ResourceLocation> frames = new ArrayList<>(gif.frames().size());
                for (int i = 0; i < gif.frames().size(); i++) {
                    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "dynamic/" + imageId + "/" + i);
                    Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(toNative(gif.frames().get(i))));
                    frames.add(location);
                }
                loaded = new AnimatedAsset(List.copyOf(frames), gif.delaysMs().clone(), gif.loopDurationMs());
            } else if (kind == ImageAssetFormat.Kind.PNG_STATIC) {
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "dynamic/" + imageId);
                Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(image));
                loaded = new StaticAsset(location);
            } else {
                throw new IOException("Cached Mirage asset has unsupported content type: " + kind.displayName());
            }
            ClientAssetTransport.uploadIfPresent(imageId);
            return loaded;
        } catch (IOException | RuntimeException exception) {
            MirageProjector.LOGGER.warn("Could not load Mirage image {} from local cache", imageId, exception);
            try {
                Files.deleteIfExists(file);
            } catch (IOException deleteFailure) {
                MirageProjector.LOGGER.debug("Could not delete invalid Mirage cache {}", file, deleteFailure);
            }
            ClientAssetTransport.requestIfMissing(imageId);
            return null;
        }
    }

    private static NativeImage toNative(BufferedImage frame) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(frame, "PNG", out)) {
            throw new IOException("No PNG encoder for GIF frame texture");
        }
        return NativeImage.read(new ByteArrayInputStream(out.toByteArray()));
    }

    public static void clear() {
        for (CachedAsset asset : REGISTERED.values()) {
            asset.release();
        }
        REGISTERED.clear();
    }

    public static void invalidate(String imageId) {
        CachedAsset asset = REGISTERED.remove(imageId);
        if (asset != null) {
            asset.release();
        }
    }

    private interface CachedAsset {
        ResourceLocation textureAt(long sampleTimeMs);
        boolean animated();
        void release();
    }

    private record StaticAsset(ResourceLocation location) implements CachedAsset {
        @Override
        public ResourceLocation textureAt(long sampleTimeMs) {
            return location;
        }

        @Override
        public boolean animated() {
            return false;
        }

        @Override
        public void release() {
            Minecraft.getInstance().getTextureManager().release(location);
        }
    }

    private record AnimatedAsset(List<ResourceLocation> frames, int[] delays, long loopDurationMs) implements CachedAsset {
        private AnimatedAsset {
            if (frames.isEmpty()) {
                throw new IllegalArgumentException("Animated Mirage asset must contain at least one frame");
            }
        }

        @Override
        public ResourceLocation textureAt(long sampleTimeMs) {
            if (frames.size() == 1 || loopDurationMs <= 0) {
                return frames.getFirst();
            }
            long cursor = Math.floorMod(sampleTimeMs, loopDurationMs);
            long elapsed = 0L;
            for (int i = 0; i < delays.length; i++) {
                elapsed += delays[i];
                if (cursor < elapsed) {
                    return frames.get(Math.min(i, frames.size() - 1));
                }
            }
            return frames.getLast();
        }
        @Override
        public boolean animated() {
            return true;
        }

        @Override
        public void release() {
            for (ResourceLocation frame : frames) {
                Minecraft.getInstance().getTextureManager().release(frame);
            }
        }
    }
}

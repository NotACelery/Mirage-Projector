package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ProjectionTextureCache {
    private static final Map<String, ResourceLocation> REGISTERED = new HashMap<>();

    private ProjectionTextureCache() {
    }

    public static Optional<ResourceLocation> get(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return Optional.empty();
        }

        ResourceLocation existing = REGISTERED.get(imageId);
        if (existing != null) {
            return Optional.of(existing);
        }

        Path file = ClientAssetTransport.cachePath(imageId);
        if (!Files.isRegularFile(file)) {
            ClientAssetTransport.requestIfMissing(imageId);
            return Optional.empty();
        }

        try (InputStream input = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(input);
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                    MirageProjector.MOD_ID,
                    "dynamic/" + imageId
            );
            Minecraft.getInstance().getTextureManager().register(location, texture);
            REGISTERED.put(imageId, location);
            ClientAssetTransport.uploadIfPresent(imageId);
            return Optional.of(location);
        } catch (IOException | RuntimeException exception) {
            MirageProjector.LOGGER.warn("Could not load Mirage image {} from local cache", imageId, exception);
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
            ClientAssetTransport.requestIfMissing(imageId);
            return Optional.empty();
        }
    }

    public static void clear() {
        for (ResourceLocation location : REGISTERED.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        REGISTERED.clear();
    }

    public static void invalidate(String imageId) {
        ResourceLocation location = REGISTERED.remove(imageId);
        if (location != null) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
    }
}

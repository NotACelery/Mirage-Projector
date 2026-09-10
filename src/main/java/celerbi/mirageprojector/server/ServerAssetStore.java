package celerbi.mirageprojector.server;

import celerbi.mirageprojector.ImageAssetFormat;
import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class ServerAssetStore {
    private ServerAssetStore() {}

    public static Path assetsDirectory(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("mirage_projector").resolve("assets");
    }

    public static Path assetPath(MinecraftServer server, String assetId) {
        return assetsDirectory(server).resolve(assetId + ".asset");
    }

    public static Path existingAssetPath(MinecraftServer server, String assetId) {
        Path generic = assetPath(server, assetId);
        if (Files.isRegularFile(generic)) {
            return generic;
        }
        Path legacy = assetsDirectory(server).resolve(assetId + ".png");
        return Files.isRegularFile(legacy) ? legacy : null;
    }

    public static boolean exists(MinecraftServer server, String assetId) {
        return ProjectionAssetRules.isValidAssetId(assetId) && existingAssetPath(server, assetId) != null;
    }

    public static void store(MinecraftServer server, String assetId, byte[] bytes) throws IOException {
        if (!ProjectionAssetRules.isValidAssetId(assetId)) {
            throw new IOException("Invalid asset id");
        }
        if (bytes.length <= 0 || bytes.length > ProjectionAssetRules.MAX_ASSET_BYTES) {
            throw new IOException("Asset size is outside Mirage limits");
        }
        if (!ImageAssetFormat.isTransportAsset(bytes)) {
            throw new IOException("Uploaded asset is neither normalized PNG nor supported GIF");
        }
        if (!ProjectionAssetRules.sha256(bytes).equals(assetId)) {
            throw new IOException("Asset SHA-256 does not match its id");
        }

        Path dir = assetsDirectory(server);
        Files.createDirectories(dir);
        if (existingAssetPath(server, assetId) != null) {
            return;
        }
        Path target = assetPath(server, assetId);
        Path temp = dir.resolve(assetId + ".tmp");
        Files.write(temp, bytes);
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailure) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        MirageProjector.LOGGER.info("Stored Mirage projection asset {} ({} bytes)", assetId, bytes.length);
    }

    public static Optional<byte[]> read(MinecraftServer server, String assetId) {
        if (!exists(server, assetId)) {
            return Optional.empty();
        }
        try {
            Path path = existingAssetPath(server, assetId);
            if (path == null) {
                return Optional.empty();
            }
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length <= 0 || bytes.length > ProjectionAssetRules.MAX_ASSET_BYTES) {
                return Optional.empty();
            }
            if (!ImageAssetFormat.isTransportAsset(bytes)) {
                return Optional.empty();
            }
            if (!ProjectionAssetRules.sha256(bytes).equals(assetId)) {
                MirageProjector.LOGGER.warn("Mirage asset {} failed SHA-256 verification on disk", assetId);
                return Optional.empty();
            }
            return Optional.of(bytes);
        } catch (IOException exception) {
            MirageProjector.LOGGER.warn("Could not read Mirage asset {} from world store", assetId, exception);
            return Optional.empty();
        }
    }
}

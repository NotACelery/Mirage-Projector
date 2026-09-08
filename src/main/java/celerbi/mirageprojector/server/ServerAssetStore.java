package celerbi.mirageprojector.server;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionAssetRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class ServerAssetStore {
    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private ServerAssetStore() {
    }

    public static Path assetsDirectory(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("mirage_projector")
                .resolve("assets");
    }

    public static Path assetPath(MinecraftServer server, String assetId) {
        return assetsDirectory(server).resolve(assetId + ".png");
    }

    public static boolean exists(MinecraftServer server, String assetId) {
        return ProjectionAssetRules.isValidAssetId(assetId)
                && Files.isRegularFile(assetPath(server, assetId));
    }

    public static void store(MinecraftServer server, String assetId, byte[] bytes) throws IOException {
        if (!ProjectionAssetRules.isValidAssetId(assetId)) {
            throw new IOException("Invalid asset id");
        }
        if (bytes.length <= 0 || bytes.length > ProjectionAssetRules.MAX_NORMALIZED_BYTES) {
            throw new IOException("Asset size is outside Mirage limits");
        }
        if (!hasPngSignature(bytes)) {
            throw new IOException("Uploaded asset is not a normalized PNG");
        }
        String actual = ProjectionAssetRules.sha256(bytes);
        if (!actual.equals(assetId)) {
            throw new IOException("Asset SHA-256 does not match its id");
        }

        Path dir = assetsDirectory(server);
        Files.createDirectories(dir);
        Path target = assetPath(server, assetId);
        if (Files.isRegularFile(target)) {
            return;
        }

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
            byte[] bytes = Files.readAllBytes(assetPath(server, assetId));
            if (bytes.length <= 0 || bytes.length > ProjectionAssetRules.MAX_NORMALIZED_BYTES) {
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

    private static boolean hasPngSignature(byte[] bytes) {
        if (bytes.length < PNG_SIGNATURE.length) {
            return false;
        }
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (bytes[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }
}

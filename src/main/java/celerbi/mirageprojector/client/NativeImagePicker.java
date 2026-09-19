package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import java.nio.file.Path;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

final class NativeImagePicker {
    private NativeImagePicker() {
    }

    static Path choose(String title, String failureContext) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(7);
            for (String filter : new String[]{"*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif", "*.bmp", "*.*"}) {
                filters.put(stack.UTF8(filter));
            }
            filters.flip();
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    title,
                    System.getProperty("user.home", ""),
                    filters,
                    "PNG / JPG / JPEG / WebP / GIF / BMP / renamed image",
                    false
            );
            return path == null || path.isBlank() ? null : Path.of(path);
        } catch (RuntimeException exception) {
            MirageProjector.LOGGER.error("Could not open {}", failureContext, exception);
            return null;
        }
    }
}

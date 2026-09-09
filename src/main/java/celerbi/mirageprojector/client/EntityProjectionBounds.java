package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.HorsePosePreset;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

/** Species-aware projected dimensions derived from the scanned EntityType footprint. */
public final class EntityProjectionBounds {
    private EntityProjectionBounds() {
    }

    public static Bounds projected(EntityProjectionState state, ProjectionSettings settings) {
        int targetLargestPixels = Math.max(1, settings == null ? 1 : settings.scalePixels());
        if (state == null) {
            return new Bounds(targetLargestPixels, targetLargestPixels);
        }

        var scan = state.activeEntity();
        float nativeWidth = 0.6F;
        float nativeHeight = 1.8F;
        EntityScanData.Kind kind = EntityScanData.Kind.HUMANOID;

        if (scan.isPresent()) {
            EntityScanData.View view = scan.get();
            kind = view.kind();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(view.entityType());
            if (type != null) {
                nativeWidth = Math.max(0.05F, type.getWidth());
                nativeHeight = Math.max(0.05F, type.getHeight());
            }
        }

        float nativeLargest = Math.max(nativeWidth, nativeHeight);
        float scale = targetLargestPixels / nativeLargest;
        float width = nativeWidth * scale;
        float height = nativeHeight * scale;

        if (kind == EntityScanData.Kind.HUMANOID) {
            height = Math.max(height,
                    targetLargestPixels * HumanoidPoseController.verticalExtentMultiplier(state.humanoidPose()));
            width = Math.max(width,
                    targetLargestPixels * HumanoidPoseController.horizontalExtentMultiplier(state.humanoidPose()));
        } else if (kind == EntityScanData.Kind.HORSE && state.horsePose() == HorsePosePreset.REARING) {
            // Vanilla horse rearing raises the forequarters well beyond the standing
            // hitbox. Keep a conservative renderer envelope while preserving the
            // species width instead of reverting to a square Scale×Scale volume.
            height = Math.max(height, targetLargestPixels * 1.35F);
            width = Math.max(width, targetLargestPixels * 0.72F);
        }

        return new Bounds(Math.max(1, Math.round(width)), Math.max(1, Math.round(height)));
    }

    public record Bounds(int widthPixels, int heightPixels) {
    }
}

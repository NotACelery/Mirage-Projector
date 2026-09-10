package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.HorsePosePreset;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

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

            // Renderer/clearance envelope must include equipment that can extend beyond the vanilla body AABB.
            // These are intentionally conservative generic margins; mod-specific adapters can refine them later.
            boolean mainHand = visible(state, VirtualEquipmentSnapshots.Channel.MAIN_HAND);
            boolean offHand = visible(state, VirtualEquipmentSnapshots.Channel.OFF_HAND);
            if (mainHand || offHand) {
                float handWidth = mainHand && offHand ? 1.42F : 1.30F;
                width = Math.max(width, targetLargestPixels * handWidth);
                height = Math.max(height, targetLargestPixels * 1.18F);
            }
            if (visible(state, VirtualEquipmentSnapshots.Channel.CHEST)) {
                width = Math.max(width, targetLargestPixels * 1.20F);
            }
            if (visible(state, VirtualEquipmentSnapshots.Channel.HEAD)) {
                height = Math.max(height, targetLargestPixels * 1.10F);
            }
        } else if (kind == EntityScanData.Kind.HORSE) {
            if (state.horsePose() == HorsePosePreset.REARING) {
                height = Math.max(height, targetLargestPixels * 1.35F);
                width = Math.max(width, targetLargestPixels * 0.72F);
            }
            if (visible(state, VirtualEquipmentSnapshots.Channel.BODY)) {
                width = Math.max(width, targetLargestPixels * 1.08F);
            }
        }

        return new Bounds(Math.max(1, Math.round(width)), Math.max(1, Math.round(height)));
    }

    private static boolean visible(
            EntityProjectionState state,
            VirtualEquipmentSnapshots.Channel channel
    ) {
        return state != null
                && state.isEquipmentVisible(channel)
                && state.hasProjectedEquipment(channel);
    }

    public record Bounds(int widthPixels, int heightPixels) {
    }
}

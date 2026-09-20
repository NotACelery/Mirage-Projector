package celerbi.mirageprojector;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Static visual anchors for the canonical fixed and anchor-family projector models.
 *
 * <p>Anchors are chassis-defined and match the canonical runtime projector models.</p>
 */
public record ProjectorVisualLayout(
        int projectionTopPixels,
        float coreCenterYPixels,
        int idleBookCenterYPixels,
        float coreRenderScale
) {
    private static final float CORE_SCALE = 0.28F;

    public static ProjectorVisualLayout forState(BlockState state, ProjectionChassisProfile chassis) {
        return switch (chassis) {
            case DISPLAY -> new ProjectorVisualLayout(10, 6, 15, CORE_SCALE);
            case WIDE -> new ProjectorVisualLayout(9, 5, 14, CORE_SCALE);
            case TALL -> new ProjectorVisualLayout(12, 6, 15, CORE_SCALE);
            case FIELD -> new ProjectorVisualLayout(9, 6, 13, CORE_SCALE);
            case PRISM -> new ProjectorVisualLayout(12, 7, 16, CORE_SCALE);
            // Table shares Wall's physical core scale; its raised center clears the low frame
            // through the entire intentional bob without needing a glass chamber.
            case TABLE -> new ProjectorVisualLayout(9, 7, 11, CORE_SCALE);
            case WALL -> new ProjectorVisualLayout(6, 8, 12, CORE_SCALE);
            default -> new ProjectorVisualLayout(9, 5, 14, CORE_SCALE);
        };
    }
}

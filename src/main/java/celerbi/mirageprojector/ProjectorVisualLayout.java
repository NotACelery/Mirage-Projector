package celerbi.mirageprojector;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Static visual anchors for the six canonical projector models.
 *
 * <p>Anchors are chassis-defined and match the canonical runtime projector models.</p>
 */
public record ProjectorVisualLayout(
        int projectionTopPixels,
        int coreCenterYPixels,
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
            default -> new ProjectorVisualLayout(9, 5, 14, CORE_SCALE);
        };
    }
}

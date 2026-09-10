package celerbi.mirageprojector.light.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/** Vanilla-compatible edge transmission translated to Mirage fixed-point costs. */
public final class MirageLightOcclusion {
    public static final int BLOCKED = Integer.MAX_VALUE;

    private MirageLightOcclusion() {
    }

    public static int edgeCost(
            Level level,
            BlockPos fromPos,
            BlockPos toPos,
            Direction direction,
            MirageLightProfile profile
    ) {
        BlockState fromState = level.getBlockState(fromPos);
        BlockState toState = level.getBlockState(toPos);

        /*
         * Reuse vanilla's own face-shape + opacity decision. This is the key difference
         * from the old source-to-node ray tests: slabs, stairs and modded states can use
         * the same per-edge light-occlusion semantics as the real LightEngine.
         */
        int vanillaOpacity = LightEngine.getLightBlockInto(
                level,
                fromState,
                fromPos,
                toState,
                toPos,
                direction,
                1
        );
        if (vanillaOpacity >= 15) {
            return BLOCKED;
        }

        int additionalWholeLevelPenalty = Math.max(0, vanillaOpacity - 1);
        return profile.airStepCostUnits()
                + additionalWholeLevelPenalty * profile.substepsPerLightLevel();
    }
}

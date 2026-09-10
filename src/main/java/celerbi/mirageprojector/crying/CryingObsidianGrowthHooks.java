package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public final class CryingObsidianGrowthHooks {

    private static final int NUCLEATION_CHANCE_DENOMINATOR = 5;

    private CryingObsidianGrowthHooks() {
    }

    public static void tryNucleate(ServerLevel level, BlockPos sourcePos, RandomSource random) {
        if (!level.getBlockState(sourcePos).is(Blocks.CRYING_OBSIDIAN)) {
            return;
        }
        if (!hasLavaAbove(level, sourcePos.above())) {
            return;
        }
        if (random.nextInt(NUCLEATION_CHANCE_DENOMINATOR) != 0) {
            return;
        }

        BlockPos growthPos = sourcePos.below();
        BlockState existing = level.getBlockState(growthPos);
        if (!existing.isAir() && !existing.getFluidState().is(Fluids.WATER)) {
            return;
        }

        BlockState bud = ModBlocks.SMALL_CRYING_OBSIDIAN_BUD.get().defaultBlockState()
                .setValue(CryingObsidianCrystalBlock.FACING, Direction.DOWN)
                .setValue(CryingObsidianCrystalBlock.WATERLOGGED, existing.getFluidState().is(Fluids.WATER))
                .setValue(CryingObsidianCrystalBlock.ENERGIZED, false);
        level.setBlock(growthPos, bud, 3);
    }

    public static boolean isValidGenerator(ServerLevel level, BlockPos crystalPos, BlockState crystalState) {
        if (!(crystalState.getBlock() instanceof CryingObsidianCrystalBlock)) {
            return false;
        }
        if (crystalState.getValue(CryingObsidianCrystalBlock.FACING) != Direction.DOWN) {
            return false;
        }

        BlockPos sourcePos = crystalPos.above();
        return level.getBlockState(sourcePos).is(Blocks.CRYING_OBSIDIAN)
                && hasLavaAbove(level, sourcePos.above());
    }

    private static boolean hasLavaAbove(ServerLevel level, BlockPos pos) {

        return level.getFluidState(pos).is(FluidTags.LAVA);
    }
}

package celerbi.mirageprojector.block;

import celerbi.mirageprojector.crying.CryingObsidianCrystalOptics;
import celerbi.mirageprojector.crying.CryingObsidianCrystalStage;
import celerbi.mirageprojector.crying.CryingObsidianGrowthHooks;
import celerbi.mirageprojector.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class CryingObsidianCrystalBlock extends AmethystClusterBlock {
    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");
    private static final int OPTICS_RECHECK_TICKS = 20;

    private final CryingObsidianCrystalStage stage;

    public CryingObsidianCrystalBlock(
            CryingObsidianCrystalStage stage,
            float height,
            float aabbOffset,
            BlockBehaviour.Properties properties
    ) {
        super(height, aabbOffset, properties);
        this.stage = stage;
        registerDefaultState(defaultBlockState().setValue(ENERGIZED, false));
    }

    public CryingObsidianCrystalStage stage() {
        return stage;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENERGIZED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
            if (stage.isMature() && level.getBlockState(pos.below()).is(Blocks.BEACON)) {
                level.getLightEngine().checkBlock(pos.below());
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean energized = CryingObsidianCrystalOptics.isBeaconEnergized(level, pos);
        BlockState current = state;
        if (state.getValue(ENERGIZED) != energized) {
            current = state.setValue(ENERGIZED, energized);
            level.setBlock(pos, current, 3);
            if (stage.isMature() && level.getBlockState(pos.below()).is(Blocks.BEACON)) {
                level.getLightEngine().checkBlock(pos.below());
            }
        }
        level.scheduleTick(pos, current.getBlock(), OPTICS_RECHECK_TICKS);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && stage.isMature() && level.getBlockState(pos.below()).is(Blocks.BEACON)) {
            level.getLightEngine().checkBlock(pos.below());
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (stage.isMature()) {
            return;
        }
        if (!CryingObsidianGrowthHooks.isValidGenerator(level, pos, state)) {
            return;
        }
        if (random.nextInt(stage.growthChanceDenominator()) != 0) {
            return;
        }

        CryingObsidianCrystalBlock nextBlock = switch (stage) {
            case SMALL -> ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD.get();
            case MEDIUM -> ModBlocks.LARGE_CRYING_OBSIDIAN_BUD.get();
            case LARGE -> ModBlocks.CRYING_OBSIDIAN_CLUSTER.get();
            case MATURE -> this;
        };

        BlockState nextState = nextBlock.defaultBlockState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                .setValue(ENERGIZED, state.getValue(ENERGIZED));
        level.setBlock(pos, nextState, 3);
    }
}

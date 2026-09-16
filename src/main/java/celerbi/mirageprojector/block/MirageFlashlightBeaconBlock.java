package celerbi.mirageprojector.block;

import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import celerbi.mirageprojector.item.MirageFlashlightItem;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Temporary world form of the Mirage Flashlight; it never owns a separate inventory item. */
public final class MirageFlashlightBeaconBlock extends BaseEntityBlock {
    public static final MapCodec<MirageFlashlightBeaconBlock> CODEC = simpleCodec(MirageFlashlightBeaconBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
    // Low horizontal silhouette; the lens/head points along FACING.
    private static final VoxelShape NORTH = Shapes.or(
            box(6, 1, 7, 10, 5, 15),
            box(5, 1, 5, 11, 6, 8),
            box(4, 0.5, 1, 12, 6.5, 6)
    );
    private static final VoxelShape SOUTH = Shapes.or(
            box(6, 1, 1, 10, 5, 9),
            box(5, 1, 8, 11, 6, 11),
            box(4, 0.5, 10, 12, 6.5, 15)
    );
    private static final VoxelShape WEST = Shapes.or(
            box(7, 1, 6, 15, 5, 10),
            box(5, 1, 5, 8, 6, 11),
            box(1, 0.5, 4, 6, 6.5, 12)
    );
    private static final VoxelShape EAST = Shapes.or(
            box(1, 1, 6, 9, 5, 10),
            box(8, 1, 5, 11, 6, 11),
            box(10, 0.5, 4, 15, 6.5, 12)
    );

    public MirageFlashlightBeaconBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MirageLightProjectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(
                type,
                ModBlockEntities.MIRAGE_LIGHT_PROJECTOR.get(),
                MirageLightProjectorBlockEntity::serverTick
        );
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof MirageLightProjectorBlockEntity placed) {
            ItemStack flashlight = new ItemStack(ModItems.MIRAGE_FLASHLIGHT.get());
            MirageFlashlightItem.setMode(flashlight, placed.mode());
            ItemStack cell = placed.extractEnergyCell();
            if (!cell.isEmpty()) {
                MirageFlashlightItem.replaceEnergyCell(flashlight, cell, level.registryAccess());
            }
            Block.popResource(level, pos, flashlight);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

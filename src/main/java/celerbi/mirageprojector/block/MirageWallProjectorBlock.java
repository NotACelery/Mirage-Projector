package celerbi.mirageprojector.block;

import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
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

/**
 * Wall-mounted member of the Mirage illumination family.
 *
 * <p>FACING is the emission direction. The supporting wall therefore sits one block behind
 * {@code FACING.getOpposite()}. It intentionally reuses the Light Projector block entity/menu so
 * battery state, Focus/Flood/Ambient behavior and recharge semantics cannot drift between the two
 * chassis.</p>
 */
public final class MirageWallProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<MirageWallProjectorBlock> CODEC = simpleCodec(MirageWallProjectorBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;

    private static final VoxelShape NORTH = Shapes.or(
            box(3, 3, 11, 13, 13, 16),
            box(4, 4, 10, 12, 12, 11),
            box(5, 5, 9, 11, 11, 10)
    );
    private static final VoxelShape SOUTH = Shapes.or(
            box(3, 3, 0, 13, 13, 5),
            box(4, 4, 5, 12, 12, 6),
            box(5, 5, 6, 11, 11, 7)
    );
    private static final VoxelShape WEST = Shapes.or(
            box(11, 3, 3, 16, 13, 13),
            box(10, 4, 4, 11, 12, 12),
            box(9, 5, 5, 10, 11, 11)
    );
    private static final VoxelShape EAST = Shapes.or(
            box(0, 3, 3, 5, 13, 13),
            box(5, 4, 4, 6, 12, 12),
            box(6, 5, 5, 7, 11, 11)
    );

    public MirageWallProjectorBlock(BlockBehaviour.Properties properties) {
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
        Direction clicked = context.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) {
            return null;
        }
        BlockState state = defaultBlockState().setValue(FACING, clicked);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
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
        Direction supportDirection = state.getValue(FACING).getOpposite();
        if (direction == supportDirection && !state.canSurvive(level, pos)) {
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
                && level.getBlockEntity(pos) instanceof MirageLightProjectorBlockEntity projector) {
            var cell = projector.extractEnergyCell();
            if (!cell.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, cell);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

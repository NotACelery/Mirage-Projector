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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Physical directional light emitter for the 1.1 illumination family. */
public final class MirageLightProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<MirageLightProjectorBlock> CODEC = simpleCodec(MirageLightProjectorBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty AMBIENT = BooleanProperty.create("ambient");

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(3, 0, 3, 13, 2, 13),
            box(4, 2, 4, 12, 2.25, 12),
            box(4, 2, 6, 6, 9, 10),
            box(10, 2, 6, 12, 9, 10),
            box(4.5, 2.01, 5.75, 5.75, 8.75, 6.01),
            box(10, 2.01, 5.75, 11.5, 8.75, 6.01),
            box(3, 8, 3, 13, 15, 12),
            box(4, 15, 4, 12, 15.25, 11),
            box(4, 9, 2.75, 12, 14, 3),
            box(4, 9, 11.99, 12, 14, 12.25),
            box(2.75, 9, 4, 3.01, 14, 11),
            box(12.99, 9, 4, 13.25, 14, 11)
    );
    private static final VoxelShape EAST_SHAPE = rotateY(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateY(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateY(SOUTH_SHAPE);

    public MirageLightProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AMBIENT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AMBIENT);
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
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private static VoxelShape rotateY(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(
                        1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX
                ))
        );
        return rotated[0];
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof MirageLightProjectorBlockEntity projector) {
            ItemStackDrop.drop(level, pos, projector.extractEnergyCell());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static final class ItemStackDrop {
        private ItemStackDrop() {
        }

        private static void drop(Level level, BlockPos pos, net.minecraft.world.item.ItemStack stack) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
            }
        }
    }
}

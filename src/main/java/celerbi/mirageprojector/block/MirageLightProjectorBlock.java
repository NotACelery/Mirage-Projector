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
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            // Derived from the shared structural elements of the supplied Off/On/Ambient models.
            // Thin screens, trims and light panels remain non-colliding on purpose.
            box(0, 0, 0, 16, 1, 16),
            box(1, 1, 1, 15, 2, 15),
            box(0, 2, 6, 1, 11, 10),
            box(15, 2, 6, 16, 11, 10),
            box(2, 5, 4, 14, 13, 13)
    );
    private static final VoxelShape EAST_SHAPE = rotateY(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateY(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateY(SOUTH_SHAPE);
    // The Ambient export has its own low base, side pylons, latch bridges and
    // 12×10 housing.  Keep the collision footprint faithful to those structural
    // parts only; the shallow lens surface is already enclosed by the housing.
    private static final VoxelShape AMBIENT_NORTH_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 1, 16),
            box(1, 1, 1, 15, 2, 15),
            box(1, 2, 6, 5, 3, 10),
            box(11, 2, 6, 15, 3, 10),
            box(0, 2, 6, 1, 11, 10),
            box(15, 2, 6, 16, 11, 10),
            box(2, 4, 4, 14, 14, 12)
    );
    private static final VoxelShape AMBIENT_EAST_SHAPE = rotateY(AMBIENT_NORTH_SHAPE);
    private static final VoxelShape AMBIENT_SOUTH_SHAPE = rotateY(AMBIENT_EAST_SHAPE);
    private static final VoxelShape AMBIENT_WEST_SHAPE = rotateY(AMBIENT_SOUTH_SHAPE);

    public MirageLightProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AMBIENT, false)
                .setValue(ACTIVE, true));
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
        builder.add(FACING, AMBIENT, ACTIVE);
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
        if (state.getValue(AMBIENT)) {
            return switch (state.getValue(FACING)) {
                case EAST -> AMBIENT_EAST_SHAPE;
                case SOUTH -> AMBIENT_SOUTH_SHAPE;
                case WEST -> AMBIENT_WEST_SHAPE;
                default -> AMBIENT_NORTH_SHAPE;
            };
        }
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

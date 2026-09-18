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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Temporary world form of the Mirage Flashlight; it never owns a separate inventory item. */
public final class MirageFlashlightBeaconBlock extends BaseEntityBlock {
    public static final MapCodec<MirageFlashlightBeaconBlock> CODEC = simpleCodec(MirageFlashlightBeaconBlock::new);
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty AMBIENT = BooleanProperty.create("ambient");
    // Exact north-facing outline of block/mirage_flashlight.json.  This used to describe an
    // obsolete low model, leaving a visibly tall flashlight with a detached hitbox.
    private static final VoxelShape NORTH = shiftY(Shapes.or(
            box(4, 6, 1, 12, 14, 5),
            box(5, 7, 0.75, 11, 13, 1),
            box(5, 7, 5, 11, 13, 10),
            box(5.5, 7.5, 10, 10.5, 12.5, 15),
            box(5.25, 6, 9.5, 10.75, 7, 14),
            box(4.5, 2.5, 8, 6, 6, 10),
            box(10, 2.5, 8, 11.5, 6, 10),
            box(4.5, 1, 8, 11.5, 2.5, 13),
            box(11, 10.5, 8, 11.75, 12, 10)
    ), -1.0D / 16.0D);
    private static final VoxelShape EAST = rotateY(NORTH);
    private static final VoxelShape SOUTH = rotateY(EAST);
    private static final VoxelShape WEST = rotateY(SOUTH);
    // Ambient is an upright, centred flashlight without the horizontal iron stand. It has no
    // horizontal facing, so a single compact collision shape serves every state.
    private static final VoxelShape AMBIENT_SHAPE = box(4, 1, 4, 12, 15.25, 12);

    public MirageFlashlightBeaconBlock(BlockBehaviour.Properties properties) {
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
        if (state.getValue(AMBIENT)) {
            return AMBIENT_SHAPE;
        }
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private static VoxelShape rotateY(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX))
        );
        return rotated[0];
    }

    private static VoxelShape shiftY(VoxelShape shape, double amount) {
        VoxelShape[] shifted = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                shifted[0] = Shapes.or(shifted[0], Shapes.box(minX, minY + amount, minZ, maxX, maxY + amount, maxZ))
        );
        return shifted[0];
    }

    private static VoxelShape rotateX(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(minX, 1.0D - maxZ, minY, maxX, 1.0D - minZ, maxY))
        );
        return rotated[0];
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MirageLightProjectorBlockEntity placed) {
            ItemStack flashlight = new ItemStack(ModItems.MIRAGE_FLASHLIGHT.get());
            MirageFlashlightItem.setMode(flashlight, placed.mode());
            ItemStack cell = placed.extractEnergyCell();
            if (!cell.isEmpty()) {
                MirageFlashlightItem.replaceEnergyCell(flashlight, cell, level.registryAccess());
            }
            // Suppress onRemove's ordinary drop path: state is transferred directly to the
            // player's guaranteed-empty hand.
            placed.suppressFlashlightDrop();
            level.removeBlock(pos, false);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, flashlight);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof MirageLightProjectorBlockEntity placed
                && !placed.shouldSuppressFlashlightDrop()) {
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

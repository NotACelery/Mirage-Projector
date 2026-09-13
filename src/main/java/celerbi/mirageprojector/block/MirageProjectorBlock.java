package celerbi.mirageprojector.block;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import org.jetbrains.annotations.Nullable;

public final class MirageProjectorBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<MirageProjectorBlock> CODEC = simpleCodec(MirageProjectorBlock::new);

    // Canonical projector collision/outline shapes. Keep model and geometry synchronized
    // per chassis. Quarter-pixel
    // coordinates are intentional where they prevent translucent/solid depth conflicts.
    private static final VoxelShape COMPACT_SHAPE = Shapes.or(
            box(2, 0, 2, 14, 2, 14),
            box(5, 2, 5, 11, 3, 11),
            box(3, 2, 3, 5, 4, 13),
            box(11, 2, 3, 13, 4, 13),
            box(5, 2, 3, 11, 3, 5),
            box(5, 2, 11, 11, 3, 13),
            box(5.25, 3.25, 5.25, 10.75, 6.75, 10.75)
    );

    private static final VoxelShape DISPLAY_SHAPE = Shapes.or(
            box(1, 0, 1, 15, 3, 15),
            box(5, 3, 5, 11, 4, 11),
            box(2, 3, 2, 4, 6, 4),
            box(12, 3, 2, 14, 6, 4),
            box(2, 3, 12, 4, 6, 14),
            box(12, 3, 12, 14, 6, 14),
            box(3, 3, 2, 13, 4, 3),
            box(3, 3, 13, 13, 4, 14),
            box(2, 3, 3, 3, 4, 13),
            box(13, 3, 3, 14, 4, 13),
            box(5, 4, 5, 11, 8, 11)
    );

    private static final VoxelShape WIDE_SHAPE = Shapes.or(
            box(0, 0, 4, 16, 2, 12),
            box(1, 2, 5, 3, 5, 11),
            box(3, 2, 5, 5, 4, 11),
            box(5, 2, 5, 6, 3, 11),
            box(10, 2, 5, 11, 3, 11),
            box(11, 2, 5, 13, 4, 11),
            box(13, 2, 5, 15, 5, 11),
            box(0, 2, 5, 1, 6, 11),
            box(15, 2, 5, 16, 6, 11),
            box(5, 2, 4, 11, 3, 5),
            box(5, 2, 11, 11, 3, 12),
            box(5.25, 2.25, 6.25, 10.75, 2.75, 9.75),
            box(2.25, 3.25, 7.25, 4.75, 3.75, 8.75),
            box(11.25, 3.25, 7.25, 13.75, 3.75, 8.75),
            box(5.25, 3.25, 5.25, 10.75, 6.75, 10.75)
    );

    private static final VoxelShape TALL_SHAPE = Shapes.or(
            box(2, 0, 2, 14, 2, 14),
            box(4, 2, 4, 12, 3, 12),
            box(4, 3, 4, 12, 4, 5),
            box(4, 3, 11, 12, 4, 12),
            box(4, 3, 5, 5, 4, 11),
            box(11, 3, 5, 12, 4, 11),
            box(4, 4, 4, 5, 10, 5),
            box(11, 4, 4, 12, 10, 5),
            box(4, 4, 11, 5, 10, 12),
            box(11, 4, 11, 12, 10, 12),
            box(4, 10, 4, 12, 11, 5),
            box(4, 10, 11, 12, 11, 12),
            box(4, 10, 5, 5, 11, 11),
            box(11, 10, 5, 12, 11, 11),
            box(6, 2, 2, 10, 4, 4),
            box(6, 2, 12, 10, 4, 14),
            box(6, 4, 6, 10, 8, 10),
            box(5, 4, 4, 11, 10, 5),
            box(5, 4, 11, 11, 10, 12),
            box(4, 4, 5, 5, 10, 11),
            box(11, 4, 5, 12, 10, 11),
            box(5, 10, 5, 11, 11, 11)
    );

    private static final VoxelShape FIELD_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 3, 16),
            box(4, 3, 4, 12, 4, 12),
            box(0, 3, 0, 3, 6, 3),
            box(13, 3, 0, 16, 6, 3),
            box(0, 3, 13, 3, 6, 16),
            box(13, 3, 13, 16, 6, 16),
            box(4.25, 3.25, 7.25, 11.75, 3.75, 8.75),
            box(7.25, 3.25, 4.25, 8.75, 3.75, 11.75),
            box(1.25, 5.25, 1.25, 1.75, 5.75, 1.75),
            box(14.25, 5.25, 1.25, 14.75, 5.75, 1.75),
            box(1.25, 5.25, 14.25, 1.75, 5.75, 14.75),
            box(14.25, 5.25, 14.25, 14.75, 5.75, 14.75),
            box(5.25, 4.25, 5.25, 10.75, 7.75, 10.75)
    );

    // Canonical Mirage Prism shape.
    private static final VoxelShape PRISM_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 3, 16),
            box(0, 3, 0, 3, 8, 3),
            box(13, 3, 0, 16, 8, 3),
            box(0, 3, 13, 3, 8, 16),
            box(13, 3, 13, 16, 8, 16),
            box(5, 3, 5, 11, 4, 11),
            box(3, 5, 7, 6, 6, 9),
            box(10, 5, 7, 13, 6, 9),
            box(7, 5, 3, 9, 6, 6),
            box(7, 5, 10, 9, 6, 13),
            box(7, 4, 7, 9, 5, 9),
            box(5, 4, 5, 11, 10, 11),
            box(1, 8, 1, 2, 12, 2),
            box(14, 8, 1, 15, 12, 2),
            box(1, 8, 14, 2, 12, 15),
            box(14, 8, 14, 15, 12, 15)
    );
    public MirageProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MirageProjectorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        ProjectionChassisProfile profile = chassisProfile(state);
        VoxelShape shape = switch (profile) {
            case DISPLAY -> DISPLAY_SHAPE;
            case WIDE -> WIDE_SHAPE;
            case TALL -> TALL_SHAPE;
            case FIELD -> FIELD_SHAPE;
            case PRISM -> PRISM_SHAPE;
            default -> COMPACT_SHAPE;
        };
        return orientShape(profile, state, shape);
    }

    private static VoxelShape orientShape(ProjectionChassisProfile profile, BlockState state, VoxelShape shape) {
        if ((profile != ProjectionChassisProfile.WIDE && profile != ProjectionChassisProfile.TALL)
                || !state.hasProperty(FACING)
                || state.getValue(FACING).getAxis() != Direction.Axis.X) {
            return shape;
        }
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated[0] = Shapes.or(rotated[0], Shapes.box(
                        1.0D - maxZ, minY, minX,
                        1.0D - minZ, maxY, maxX
                )));
        return rotated[0];
    }


    public static ProjectionChassisProfile chassisProfile(BlockState state) {
        if (state != null) {
            if (state.is(ModBlocks.MIRAGE_DISPLAY.get())) {
                return ProjectionChassisProfile.DISPLAY;
            }
            if (state.is(ModBlocks.WIDE_MIRAGE_PROJECTOR.get())) {
                return ProjectionChassisProfile.WIDE;
            }
            if (state.is(ModBlocks.TALL_MIRAGE_PROJECTOR.get())) {
                return ProjectionChassisProfile.TALL;
            }
            if (state.is(ModBlocks.MIRAGE_FIELD_PROJECTOR.get())) {
                return ProjectionChassisProfile.FIELD;
            }
            if (state.is(ModBlocks.MIRAGE_PRISM.get())) {
                return ProjectionChassisProfile.PRISM;
            }
        }
        return ProjectionChassisProfile.COMPACT;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack result = new ItemStack(this);
        if (level instanceof Level actualLevel
                && level.getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
            projector.saveToItem(result, actualLevel.registryAccess());
        }
        return result;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide
                && !player.isCreative()
                && level.getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
            projector.preparePackedPlayerBreak(level.registryAccess());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        if (blockEntity instanceof MirageProjectorBlockEntity projector
                && projector.hasPendingPackedPlayerBreakDrop()) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            Block.popResource(level, pos, projector.takePendingPackedPlayerBreakDrop());
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {

            if (projector.hasPendingPackedPlayerBreakDrop()) {
                super.onRemove(state, level, pos, newState, movedByPiston);
                return;
            }

            ItemStack legacyStored = projector.extractLegacyProjectionReturnItem();
            if (!legacyStored.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, legacyStored);
            }
            ItemStack stagedScanCard = projector.entityScanCard().extractItem(0, 1, false);
            if (!stagedScanCard.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stagedScanCard);
            }
            dropHandler(level, pos, projector.humanoidStagingItems());
            dropHandler(level, pos, projector.horseStagingItems());
            ItemStack core = projector.coreItem().extractItem(0, 1, false);
            if (!core.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, core);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void dropHandler(Level level, BlockPos pos, net.neoforged.neoforge.items.ItemStackHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.extractItem(slot, handler.getSlotLimit(slot), false);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) {
            ((IPlayerExtension) serverPlayer).openMenu(projector, projector::writeMenuData);
        }
        return InteractionResult.SUCCESS;
    }
}

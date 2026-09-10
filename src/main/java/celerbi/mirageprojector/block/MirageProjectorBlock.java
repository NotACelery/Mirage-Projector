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

    private static final VoxelShape COMPACT_BASE_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 1, 16),
            box(2, 1, 2, 14, 2, 14)
    );
    private static final VoxelShape COMPACT_CORE_SHAPE = box(7, 2, 7, 9, 5, 9);

    private static final VoxelShape DISPLAY_BASE_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 2, 16),
            box(1, 2, 1, 15, 3, 15)
    );
    private static final VoxelShape DISPLAY_CORE_SHAPE = box(6, 3, 6, 10, 6, 10);

    private static final VoxelShape WIDE_BASE_SHAPE = Shapes.or(
            box(0, 0, 3, 16, 2, 13),
            box(0, 2, 5, 16, 3, 11),
            box(0, 2, 3, 2, 4, 13),
            box(14, 2, 3, 16, 4, 13)
    );
    private static final VoxelShape WIDE_CORE_SHAPE = box(6, 3, 6, 10, 6, 10);

    private static final VoxelShape TALL_BASE_SHAPE = Shapes.or(
            box(2, 0, 2, 14, 2, 14),
            box(3, 2, 3, 13, 3, 13),
            box(2, 2, 6, 4, 6, 10),
            box(12, 2, 6, 14, 6, 10)
    );
    private static final VoxelShape TALL_CORE_SHAPE = box(6, 3, 6, 10, 8, 10);

    private static final VoxelShape FIELD_BASE_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 2, 16),
            box(1, 2, 1, 15, 3, 15),
            box(0, 2, 0, 3, 5, 3),
            box(13, 2, 0, 16, 5, 3),
            box(0, 2, 13, 3, 5, 16),
            box(13, 2, 13, 16, 5, 16)
    );
    private static final VoxelShape FIELD_CORE_SHAPE = box(6, 3, 6, 10, 7, 10);

    private static final VoxelShape PRISM_BASE_SHAPE = Shapes.or(
            box(0, 0, 0, 16, 2, 16),
            box(1, 2, 1, 15, 3, 15),
            box(0, 2, 0, 3, 5, 3),
            box(13, 2, 0, 16, 5, 3),
            box(0, 2, 13, 3, 5, 16),
            box(13, 2, 13, 16, 5, 16)
    );
    private static final VoxelShape PRISM_CORE_SHAPE = box(6, 3, 6, 10, 7, 10);

    public MirageProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // SOUTH preserves the historical pre-facing projection orientation for
        // worlds upgraded from older dev builds. Newly placed blocks face the player.
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
        VoxelShape base = switch (profile) {
            case DISPLAY -> DISPLAY_BASE_SHAPE;
            case WIDE -> WIDE_BASE_SHAPE;
            case TALL -> TALL_BASE_SHAPE;
            case FIELD -> FIELD_BASE_SHAPE;
            case PRISM -> PRISM_BASE_SHAPE;
            default -> COMPACT_BASE_SHAPE;
        };

        if (!(level.getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector) || projector.coreStack().isEmpty()) {
            return orientShape(profile, state, base);
        }

        VoxelShape core = switch (profile) {
            case DISPLAY -> DISPLAY_CORE_SHAPE;
            case WIDE -> WIDE_CORE_SHAPE;
            case TALL -> TALL_CORE_SHAPE;
            case FIELD -> FIELD_CORE_SHAPE;
            case PRISM -> PRISM_CORE_SHAPE;
            default -> COMPACT_CORE_SHAPE;
        };
        VoxelShape combined = Shapes.or(base, core);
        return orientShape(profile, state, combined);
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
            // Ordinary Survival mining is packed into one stateful projector ItemStack.
            // In that path the Core/card/staging stacks must not also eject here.
            if (projector.hasPendingPackedPlayerBreakDrop()) {
                super.onRemove(state, level, pos, newState, movedByPiston);
                return;
            }

            // Non-player destruction keeps the historical safe fallback: virtual
            // snapshots are lost with the chassis, but real physical contents are
            // still ejected rather than silently deleted.
            // Render snapshots are virtual copies and never drop as obtainable items.
            // Only a real pre-dev.12 item retained for migration is returned here.
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

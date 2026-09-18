package celerbi.mirageprojector.block;

import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Low-profile Mirage Wall Projector presentation chassis. It sits on a complete flat block top and projects toward the
 * horizontal FACING direction until it finds a real planar wall. It is deliberately not a
 * wall-mounted hologram emitter.
 */
public final class MirageWallProjectorBlock extends MirageProjectorBlock {
    public static final MapCodec<MirageWallProjectorBlock> CODEC = simpleCodec(MirageWallProjectorBlock::new);

    public MirageWallProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        BlockState support = level.getBlockState(supportPos);
        // Intentionally stricter than isFaceSturdy: slabs/stairs/top-shell blocks are not valid
        // projector tables even if a particular face happens to report sturdy.
        return support.isCollisionShapeFullBlock(level, supportPos);
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
            if (level instanceof Level actualLevel && !actualLevel.isClientSide
                    && actualLevel.getBlockEntity(pos) instanceof MirageProjectorBlockEntity projector
                    && !projector.hasPendingPackedPlayerBreakDrop()) {
                projector.preparePackedPlayerBreak(actualLevel.registryAccess());
                ItemStack packed = projector.copyPendingPackedPlayerBreakDrop();
                if (!packed.isEmpty()) {
                    Block.popResource(actualLevel, pos, packed);
                }
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}

package celerbi.mirageprojector.block;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Single configurable replacement for the five dev.46 Improved Core blocks. */
public final class CoreBoosterBlock extends BaseEntityBlock {
    public static final MapCodec<CoreBoosterBlock> CODEC = simpleCodec(CoreBoosterBlock::new);
    public static final EnumProperty<CoreBoosterMaterial> MATERIAL = EnumProperty.create("material", CoreBoosterMaterial.class);
    private static final VoxelShape SHAPE = Shapes.block();

    public CoreBoosterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MATERIAL, CoreBoosterMaterial.EMPTY));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoreBoosterBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MATERIAL);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        CoreBoosterMaterial material = CoreBoosterBlockEntity.materialFromStack(stack);
        BlockState current = level.getBlockState(pos);
        if (current.is(this) && current.getValue(MATERIAL) != material) {
            level.setBlock(pos, current.setValue(MATERIAL, material), Block.UPDATE_ALL);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CoreBoosterBlockEntity booster) {
            return booster.packedStack();
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CoreBoosterBlockEntity booster) {
            if (player.isCreative()) {
                booster.prepareCreativeBreak();
            } else if (player.hasCorrectToolForDrops(state)) {
                booster.preparePackedPlayerBreak();
            }
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
        if (blockEntity instanceof CoreBoosterBlockEntity booster
                && booster.hasPendingPackedPlayerBreakDrop()) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            Block.popResource(level, pos, booster.takePendingPackedPlayerBreakDrop());
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CoreBoosterBlockEntity booster) {
            // Correct-tool player mining packs the material into the dropped Booster.
            if (!booster.hasPendingPackedPlayerBreakDrop() && !booster.suppressRemovalDrops()) {
                // Other destruction paths still return the real inserted mineral
                // rather than silently deleting it.
                ItemStack core = booster.extractMaterial();
                if (!core.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, core);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

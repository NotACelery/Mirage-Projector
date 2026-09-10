package celerbi.mirageprojector.block;

import celerbi.mirageprojector.registry.ModDamageTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ObsidianSpikeBlock extends Block {
    public static final MapCodec<ObsidianSpikeBlock> CODEC = simpleCodec(ObsidianSpikeBlock::new);

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
    private static final Vec3 MOVEMENT_MULTIPLIER = new Vec3(0.8F, 0.75F, 0.8F);
    private static final double HURT_MOVEMENT_THRESHOLD = 0.003D;
    private static final float DAMAGE_PER_HURT = 2.0F;

    public ObsidianSpikeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
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
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        entity.makeStuckInBlock(state, MOVEMENT_MULTIPLIER);

        if (level.isClientSide) {
            return;
        }

        double dx = Math.abs(entity.getX() - entity.xOld);
        double dy = Math.abs(entity.getY() - entity.yOld);
        double dz = Math.abs(entity.getZ() - entity.zOld);
        if (dx < HURT_MOVEMENT_THRESHOLD
                && dy < HURT_MOVEMENT_THRESHOLD
                && dz < HURT_MOVEMENT_THRESHOLD) {
            return;
        }

        var damageType = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageTypes.OBSIDIAN_SPIKE);
        entity.hurt(new net.minecraft.world.damagesource.DamageSource(damageType), DAMAGE_PER_HURT);
    }
}

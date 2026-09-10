package celerbi.mirageprojector.block;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import celerbi.mirageprojector.blockentity.ImprovedCoreBlockEntity;
import celerbi.mirageprojector.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Physical Improved Core shell. The shell remains a normal baked block model,
 * while the material in the middle is rendered as one real spinning item by a BER.
 */
public final class ImprovedCoreBlock extends BaseEntityBlock {
    public static final MapCodec<ImprovedCoreBlock> CODEC = simpleCodec(ImprovedCoreBlock::new);

    public ImprovedCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ImprovedCoreBlockEntity(pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        CoreBoosterMaterial material = legacyMaterial(state);
        if (!material.present()) {
            return;
        }
        BlockState replacement = ModBlocks.CORE_BOOSTER.get().defaultBlockState()
                .setValue(CoreBoosterBlock.MATERIAL, material);
        level.setBlock(pos, replacement, UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CoreBoosterBlockEntity booster) {
            booster.setMaterial(material);
        }
    }

    public static CoreBoosterMaterial legacyMaterial(BlockState state) {
        if (state.is(ModBlocks.IMPROVED_GLASS_CORE.get())) return CoreBoosterMaterial.GLASS;
        if (state.is(ModBlocks.IMPROVED_QUARTZ_CORE.get())) return CoreBoosterMaterial.QUARTZ;
        if (state.is(ModBlocks.IMPROVED_AMETHYST_CORE.get())) return CoreBoosterMaterial.AMETHYST;
        if (state.is(ModBlocks.IMPROVED_DIAMOND_CORE.get())) return CoreBoosterMaterial.DIAMOND;
        if (state.is(ModBlocks.IMPROVED_NETHERITE_CORE.get())) return CoreBoosterMaterial.NETHERITE;
        return CoreBoosterMaterial.EMPTY;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}

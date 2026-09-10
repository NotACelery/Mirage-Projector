package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stateless block entity used only to anchor the animated center-item renderer
 * of placed Improved Projection Cores.
 */
public final class ImprovedCoreBlockEntity extends BlockEntity {
    public ImprovedCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IMPROVED_CORE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }
}

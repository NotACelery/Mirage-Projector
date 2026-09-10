package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class LegacyImprovedCoreBlockEntity extends BlockEntity {
    public LegacyImprovedCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_IMPROVED_CORE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }
}

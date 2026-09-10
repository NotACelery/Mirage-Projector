package celerbi.mirageprojector;

import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ProjectorStateTransfer {
    private ProjectorStateTransfer() {
    }

    public static ItemStack packPlacedProjector(
            MirageProjectorBlockEntity projector,
            HolderLookup.Provider registries
    ) {
        ItemStack result = new ItemStack(projector.getBlockState().getBlock());
        projector.saveToItem(result, registries);
        return result;
    }

    public static ItemStack upgrade(
            ItemStack source,
            Block sourceBlock,
            Block targetBlock,
            HolderLookup.Provider registries
    ) {
        if (source == null || source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = source.transmuteCopy(targetBlock, 1);

        CompoundTag sourceState = sourceState(source, sourceBlock, registries);
        CompoundTag normalized = normalizeForTarget(sourceState, targetBlock.defaultBlockState(), registries);
        BlockItem.setBlockEntityData(result, ModBlockEntities.MIRAGE_PROJECTOR.get(), normalized);
        return result;
    }

    private static CompoundTag sourceState(
            ItemStack source,
            Block sourceBlock,
            HolderLookup.Provider registries
    ) {
        CustomData packed = source.get(DataComponents.BLOCK_ENTITY_DATA);
        if (packed != null && !packed.isEmpty()) {
            CompoundTag tag = packed.copyTag();
            tag.remove("id");
            return tag;
        }

        MirageProjectorBlockEntity defaults = new MirageProjectorBlockEntity(
                BlockPos.ZERO,
                sourceBlock.defaultBlockState()
        );
        return defaults.saveCustomOnly(registries);
    }

    private static CompoundTag normalizeForTarget(
            CompoundTag sourceState,
            BlockState targetState,
            HolderLookup.Provider registries
    ) {
        MirageProjectorBlockEntity target = new MirageProjectorBlockEntity(BlockPos.ZERO, targetState);
        target.loadCustomOnly(sourceState.copy(), registries);
        return target.saveCustomOnly(registries);
    }
}

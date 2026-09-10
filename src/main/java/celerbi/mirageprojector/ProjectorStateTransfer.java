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

/**
 * Canonical projector-state transport used by mining and chassis upgrade recipes.
 *
 * <p>The persistent BlockEntity payload is moved wholesale instead of copying an
 * ever-growing hand-written field list. A temporary target-chassis BlockEntity is
 * used to run the same load/migration/sanitization rules that a placed projector
 * uses, then the normalized state is written back to the result ItemStack through
 * vanilla {@link DataComponents#BLOCK_ENTITY_DATA}.</p>
 */
public final class ProjectorStateTransfer {
    private ProjectorStateTransfer() {
    }

    /**
     * Packs the full current projector state into its dropped BlockItem.
     * BlockEntity.saveToItem is the canonical vanilla bridge because it writes
     * custom BlockEntity data and any BlockEntity-provided item components.
     */
    public static ItemStack packPlacedProjector(
            MirageProjectorBlockEntity projector,
            HolderLookup.Provider registries
    ) {
        ItemStack result = new ItemStack(projector.getBlockState().getBlock());
        projector.saveToItem(result, registries);
        return result;
    }

    /**
     * Transfers a projector ItemStack into a new chassis while preserving all
     * existing item-component patches and normalizing the BlockEntity state for
     * the target chassis.
     *
     * <p>A legacy/brand-new clean Compact item has no BLOCK_ENTITY_DATA yet. In
     * that case a temporary source BlockEntity is created first, which correctly
     * materializes the Compact's implicit default Glass Core before the upgrade.</p>
     */
    public static ItemStack upgrade(
            ItemStack source,
            Block sourceBlock,
            Block targetBlock,
            HolderLookup.Provider registries
    ) {
        if (source == null || source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 1.21.1 transmuteCopy keeps the source component patch while changing
        // the actual item, which also preserves custom names and future item-side
        // projector components without making this class know about them.
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

        // Clean legacy/new items have no serialized BlockEntity state. Recreate
        // the source defaults so Compact -> Display does not silently lose the
        // implicit Glass Core that Compact receives on first placement.
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

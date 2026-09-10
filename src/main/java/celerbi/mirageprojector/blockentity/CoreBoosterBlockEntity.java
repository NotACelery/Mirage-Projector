package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.block.CoreBoosterBlock;
import celerbi.mirageprojector.crying.CryingObsidianLightField;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CoreBoosterBlockEntity extends BlockEntity {
    public static final String MATERIAL_TAG = "CoreMaterial";

    private CoreBoosterMaterial legacyLoadedMaterial = CoreBoosterMaterial.EMPTY;
    @Nullable
    private ItemStack pendingPackedPlayerBreakDrop;
    private boolean suppressRemovalDrops;

    public CoreBoosterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORE_BOOSTER.get(), pos, state);
    }

    public CoreBoosterMaterial material() {
        BlockState state = getBlockState();
        return state.hasProperty(CoreBoosterBlock.MATERIAL)
                ? state.getValue(CoreBoosterBlock.MATERIAL)
                : CoreBoosterMaterial.EMPTY;
    }

    public boolean empty() {
        return !material().present();
    }

    public boolean insert(ItemStack source) {
        if (!empty()) {
            return false;
        }
        CoreBoosterMaterial candidate = CoreBoosterMaterial.fromInsertStack(source);
        if (!candidate.present()) {
            return false;
        }
        setMaterial(candidate);
        return true;
    }

    public ItemStack extractMaterial() {
        CoreBoosterMaterial current = material();
        ItemStack result = current.centerStack();
        if (!result.isEmpty()) {
            setMaterial(CoreBoosterMaterial.EMPTY);
        }
        return result;
    }

    public void setMaterial(CoreBoosterMaterial newMaterial) {
        CoreBoosterMaterial safe = newMaterial == null ? CoreBoosterMaterial.EMPTY : newMaterial;
        if (level == null) {
            legacyLoadedMaterial = safe;
            setChanged();
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(CoreBoosterBlock.MATERIAL)) {
            return;
        }
        if (state.getValue(CoreBoosterBlock.MATERIAL) != safe) {
            level.setBlock(worldPosition, state.setValue(CoreBoosterBlock.MATERIAL, safe), Block.UPDATE_ALL);
            if (level instanceof ServerLevel serverLevel) {
                // Booster swaps change reflected power without a placement event. Force
                // nearby Mature fields to rebuild in the same server operation instead
                // of waiting for their 20-tick optics fallback.
                CryingObsidianLightField.refreshSourcesNearNow(serverLevel, List.of(worldPosition));
            }
        }
        legacyLoadedMaterial = CoreBoosterMaterial.EMPTY;
        setChanged();
    }

    public ItemStack packedStack() {
        return stackForMaterial(material());
    }

    public void preparePackedPlayerBreak() {
        pendingPackedPlayerBreakDrop = packedStack();
    }

    public void prepareCreativeBreak() {
        suppressRemovalDrops = true;
    }

    public boolean suppressRemovalDrops() {
        return suppressRemovalDrops;
    }

    public boolean hasPendingPackedPlayerBreakDrop() {
        return pendingPackedPlayerBreakDrop != null && !pendingPackedPlayerBreakDrop.isEmpty();
    }

    public ItemStack takePendingPackedPlayerBreakDrop() {
        ItemStack result = pendingPackedPlayerBreakDrop == null ? ItemStack.EMPTY : pendingPackedPlayerBreakDrop;
        pendingPackedPlayerBreakDrop = null;
        return result;
    }

    public static ItemStack stackForMaterial(CoreBoosterMaterial material) {
        ItemStack result = new ItemStack(ModItems.CORE_BOOSTER.get());
        CoreBoosterMaterial safe = material == null ? CoreBoosterMaterial.EMPTY : material;
        if (!safe.present()) {
            return result;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString(MATERIAL_TAG, safe.name());
        BlockItem.setBlockEntityData(result, ModBlockEntities.CORE_BOOSTER.get(), tag);
        return result;
    }

    public static CoreBoosterMaterial materialFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(ModItems.CORE_BOOSTER.get())) {
            return CoreBoosterMaterial.EMPTY;
        }
        CustomData packed = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (packed == null || packed.isEmpty()) {
            return CoreBoosterMaterial.EMPTY;
        }
        CompoundTag tag = packed.copyTag();
        return CoreBoosterMaterial.byName(tag.getString(MATERIAL_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CoreBoosterMaterial current = material();
        if (current.present()) {
            tag.putString(MATERIAL_TAG, current.name());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        legacyLoadedMaterial = tag.contains(MATERIAL_TAG)
                ? CoreBoosterMaterial.byName(tag.getString(MATERIAL_TAG))
                : CoreBoosterMaterial.EMPTY;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && !level.isClientSide && legacyLoadedMaterial.present()) {
            BlockState state = getBlockState();
            if (state.hasProperty(CoreBoosterBlock.MATERIAL)
                    && state.getValue(CoreBoosterBlock.MATERIAL) == CoreBoosterMaterial.EMPTY) {
                level.setBlock(worldPosition, state.setValue(CoreBoosterBlock.MATERIAL, legacyLoadedMaterial), Block.UPDATE_ALL);
            }
            legacyLoadedMaterial = CoreBoosterMaterial.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

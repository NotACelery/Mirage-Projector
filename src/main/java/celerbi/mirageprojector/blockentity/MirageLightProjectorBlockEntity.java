package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.menu.MirageLightProjectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent state for the physical Mirage light projector.
 *
 * <p>The rechargeable medium remains a real ItemStack so partial charge survives insertion,
 * extraction, chunk reload and multiplayer synchronization without copying charge into a
 * second block-owned counter.</p>
 */
public final class MirageLightProjectorBlockEntity extends BlockEntity implements MenuProvider {
    private static final String ENERGY_CELL_TAG = "EnergyCell";
    private static final String MODE_TAG = "LightMode";

    private ItemStack energyCell = ItemStack.EMPTY;
    private PortableLightMode mode = PortableLightMode.FOCUS;

    public MirageLightProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRAGE_LIGHT_PROJECTOR.get(), pos, state);
    }

    public ItemStack energyCell() {
        return energyCell;
    }

    public void setEnergyCell(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            energyCell = ItemStack.EMPTY;
        } else if (RechargeableEnergyItem.isRechargeable(stack)) {
            energyCell = RechargeableEnergyItem.normalizeForDevice(stack);
        } else {
            return;
        }
        setChangedAndSync();
    }

    public void markEnergyChanged() {
        setChangedAndSync();
    }

    public boolean hasEnergyCell() {
        return RechargeableEnergyItem.isRechargeable(energyCell);
    }

    public int chargePercent() {
        return RechargeableEnergyItem.chargePercent(energyCell);
    }

    public PortableLightMode mode() {
        return mode;
    }

    public boolean emitting() {
        RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(energyCell);
        return mode.emitsLight() && energy != null && !energy.depleted(energyCell);
    }

    public boolean insertEnergyCell(ItemStack source) {
        if (hasEnergyCell() || source == null || source.isEmpty() || !RechargeableEnergyItem.isRechargeable(source)) {
            return false;
        }
        energyCell = RechargeableEnergyItem.normalizeForDevice(source);
        setChangedAndSync();
        return true;
    }

    public ItemStack extractEnergyCell() {
        if (!hasEnergyCell()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = energyCell;
        energyCell = ItemStack.EMPTY;
        setChangedAndSync();
        return result;
    }

    public void setMode(PortableLightMode mode) {
        this.mode = mode == null ? PortableLightMode.OFF : mode;
        setChangedAndSync();
    }

    public PortableLightMode cycleMode() {
        mode = mode.next();
        setChangedAndSync();
        return mode;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MirageLightProjectorBlockEntity projector) {
        if (!(level instanceof ServerLevel serverLevel)
                || projector == null
                || serverLevel.getGameTime() % 20L != 0L
                || !projector.emitting()) {
            return;
        }
        RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(projector.energyCell);
        if (energy == null) {
            return;
        }
        int consumed = energy.consumeStoredCharge(projector.energyCell, projector.mode.chargePerSecond());
        if (consumed > 0) {
            projector.setChangedAndSync();
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        if (getBlockState().is(ModBlocks.MIRAGE_WALL_PROJECTOR)) {
            return Component.translatable("container.mirage_projector.wall_projector");
        }
        if (getBlockState().is(ModBlocks.MIRAGE_FLASHLIGHT_BEACON)) {
            return Component.translatable("container.mirage_projector.flashlight");
        }
        return Component.translatable("container.mirage_projector.light_projector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MirageLightProjectorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(MODE_TAG, mode.serializedName());
        if (hasEnergyCell()) {
            tag.put(ENERGY_CELL_TAG, energyCell.copyWithCount(1).save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mode = PortableLightMode.byName(tag.getString(MODE_TAG));
        energyCell = tag.contains(ENERGY_CELL_TAG)
                ? ItemStack.parseOptional(registries, tag.getCompound(ENERGY_CELL_TAG))
                : ItemStack.EMPTY;
        if (!energyCell.isEmpty() && !RechargeableEnergyItem.isRechargeable(energyCell)) {
            energyCell = ItemStack.EMPTY;
        }
        if (!energyCell.isEmpty() && energyCell.getCount() != 1) {
            energyCell = energyCell.copyWithCount(1);
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

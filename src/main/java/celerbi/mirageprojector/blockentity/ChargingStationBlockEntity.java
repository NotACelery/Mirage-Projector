package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.block.ChargingStationBlock;
import celerbi.mirageprojector.crying.CryingObsidianLightField;
import celerbi.mirageprojector.energy.BeaconRechargeableCharger;
import celerbi.mirageprojector.energy.GlowDustBeaconCharging;
import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.menu.ChargingStationMenu;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Nine-slot charging station inventory:
 * 0..3 input queue, 4 active single-cell charger, 5..8 completed outputs.
 */
public final class ChargingStationBlockEntity extends BlockEntity
        implements MenuProvider, BeaconRechargeableCharger {
    public static final int INPUT_START = 0;
    public static final int INPUT_COUNT = 4;
    public static final int CHARGING_SLOT = 4;
    public static final int OUTPUT_START = 5;
    public static final int OUTPUT_COUNT = 4;
    public static final int SLOT_COUNT = 9;
    private static final int OUTPUT_PUSH_INTERVAL_TICKS = 8;
    private static final String INVENTORY_TAG = "ChargingStationInventory";

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= INPUT_START && slot < INPUT_START + INPUT_COUNT) {
                return isChargeableInput(stack);
            }
            if (slot == CHARGING_SLOT) {
                return isChargeableInput(stack);
            }
            return slot >= OUTPUT_START && slot < OUTPUT_START + OUTPUT_COUNT && isRegularChargeMedium(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == CHARGING_SLOT ? 1 : super.getSlotLimit(slot);
        }
    };

    private final IItemHandler inputAutomation = new InputAutomationHandler();
    private final IItemHandler outputAutomation = new OutputAutomationHandler();
    private final IItemHandler unsidedAutomation = new UnsidedAutomationHandler();

    public ChargingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARGING_STATION.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public static boolean isRegularChargeMedium(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && (stack.is(ModItems.GLOW_DUST.get()) || stack.is(Items.GLOWSTONE_DUST) || stack.is(ModItems.LIGHT_BATTERY.get()));
    }

    public static boolean isChargeableInput(ItemStack stack) {
        return isRegularChargeMedium(stack)
                && RechargeableEnergyItem.isRechargeable(stack)
                && !RechargeableEnergyItem.isFull(stack);
    }

    @Override
    public ItemStack activeChargingStack() {
        return inventory.getStackInSlot(CHARGING_SLOT);
    }

    public Direction outputDirection() {
        BlockState state = getBlockState();
        return state.hasProperty(ChargingStationBlock.FACING)
                ? state.getValue(ChargingStationBlock.FACING)
                : Direction.NORTH;
    }

    /** Side-aware automation contract used by NeoForge ItemHandler capabilities. */
    public IItemHandler itemHandlerFor(@Nullable Direction side) {
        if (side == null) {
            return unsidedAutomation;
        }
        return side == outputDirection() ? outputAutomation : inputAutomation;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChargingStationBlockEntity station) {
        if (!(level instanceof ServerLevel serverLevel) || station == null) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (gameTime % OUTPUT_PUSH_INTERVAL_TICKS == 0L) {
            station.pushOneOutputItem(serverLevel);
        }

        station.moveCompletedCellToOutput();
        station.advanceInputQueue();

        if (gameTime % GlowDustBeaconCharging.CHARGE_INTERVAL_TICKS != 0L
                || !GlowDustBeaconCharging.canChargeAt(serverLevel, pos, station)) {
            return;
        }

        int added = RechargeableEnergyItem.addCharge(station.activeChargingStack());
        if (added <= 0) {
            return;
        }

        station.setChanged();
        boolean full = RechargeableEnergyItem.isFull(station.activeChargingStack());
        if (full || gameTime % 20L == 0L) {
            station.syncToClients();
        }
        if (full) {
            GlowDustBeaconCharging.scheduleCrystalRecheckAbove(serverLevel, pos);
            CryingObsidianLightField.refreshSourcesNearNow(serverLevel, List.of(pos));
            station.moveCompletedCellToOutput();
            station.advanceInputQueue();
        }
    }

    /** Move exactly one queued cell into the active charging slot. */
    private void advanceInputQueue() {
        if (!inventory.getStackInSlot(CHARGING_SLOT).isEmpty()) {
            return;
        }
        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            ItemStack queued = inventory.getStackInSlot(slot);
            if (queued.isEmpty() || !isChargeableInput(queued)) {
                continue;
            }
            ItemStack one = inventory.extractItem(slot, 1, false);
            if (one.isEmpty()) {
                continue;
            }
            ItemStack remainder = inventory.insertItem(CHARGING_SLOT, one, false);
            if (!remainder.isEmpty()) {
                inventory.insertItem(slot, remainder, false);
            }
            return;
        }
    }

    /**
     * Completed media moves to the leftmost output slot that can accept it.
     * If all four outputs are blocked, the full cell stays in the active slot.
     */
    private void moveCompletedCellToOutput() {
        ItemStack active = inventory.getStackInSlot(CHARGING_SLOT);
        if (active.isEmpty() || !RechargeableEnergyItem.isRechargeable(active)
                || !RechargeableEnergyItem.isFull(active)) {
            return;
        }
        ItemStack normalized = RechargeableEnergyItem.normalizeFullyChargedOutput(active);
        if (!ItemStack.isSameItemSameComponents(active, normalized)) {
            inventory.setStackInSlot(CHARGING_SLOT, normalized);
            active = normalized;
        }

        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_COUNT; slot++) {
            ItemStack simulated = inventory.insertItem(slot, active, true);
            if (!simulated.isEmpty()) {
                continue;
            }
            ItemStack moving = inventory.extractItem(CHARGING_SLOT, 1, false);
            ItemStack remainder = inventory.insertItem(slot, moving, false);
            if (!remainder.isEmpty()) {
                inventory.insertItem(CHARGING_SLOT, remainder, false);
            }
            return;
        }
    }

    /** Hopper-like automatic ejection: one item every eight ticks from the leftmost output. */
    private void pushOneOutputItem(ServerLevel level) {
        Direction output = outputDirection();
        BlockPos targetPos = worldPosition.relative(output);
        IItemHandler target = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                targetPos,
                output.getOpposite()
        );
        if (target == null) {
            return;
        }

        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_COUNT; slot++) {
            ItemStack stored = inventory.getStackInSlot(slot);
            if (stored.isEmpty()) {
                continue;
            }
            ItemStack one = stored.copyWithCount(1);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, one, false);
            if (remainder.isEmpty()) {
                inventory.extractItem(slot, 1, false);
                return;
            }
            // A filtered target may reject the leftmost completed medium while accepting
            // a later output. Preserve left-to-right priority, but do not let one rejected
            // item block every other completed cell behind it.
        }
    }

    private void setChangedAndSync() {
        setChanged();
        syncToClients();
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(INVENTORY_TAG, inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(INVENTORY_TAG)) {
            inventory.deserializeNBT(registries, tag.getCompound(INVENTORY_TAG));
        }
        ItemStack active = inventory.getStackInSlot(CHARGING_SLOT);
        if (!active.isEmpty() && active.getCount() != 1) {
            ItemStack overflow = active.copy();
            overflow.shrink(1);
            inventory.setStackInSlot(CHARGING_SLOT, active.copyWithCount(1));
            for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT && !overflow.isEmpty(); slot++) {
                overflow = inventory.insertItem(slot, overflow, false);
            }
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mirage_projector.charging_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChargingStationMenu(containerId, playerInventory, this);
    }

    private final class InputAutomationHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return INPUT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return validLocalSlot(slot) ? inventory.getStackInSlot(INPUT_START + slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return validLocalSlot(slot)
                    ? inventory.insertItem(INPUT_START + slot, stack, simulate)
                    : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return validLocalSlot(slot) ? inventory.getSlotLimit(INPUT_START + slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return validLocalSlot(slot) && isChargeableInput(stack);
        }

        private boolean validLocalSlot(int slot) {
            return slot >= 0 && slot < INPUT_COUNT;
        }
    }

    private final class OutputAutomationHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return OUTPUT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return validLocalSlot(slot) ? inventory.getStackInSlot(OUTPUT_START + slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return validLocalSlot(slot)
                    ? inventory.extractItem(OUTPUT_START + slot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return validLocalSlot(slot) ? inventory.getSlotLimit(OUTPUT_START + slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        private boolean validLocalSlot(int slot) {
            return slot >= 0 && slot < OUTPUT_COUNT;
        }
    }

    private final class UnsidedAutomationHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return INPUT_COUNT + OUTPUT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < INPUT_COUNT) {
                return inputAutomation.getStackInSlot(slot);
            }
            int outputSlot = slot - INPUT_COUNT;
            return outputSlot >= 0 && outputSlot < OUTPUT_COUNT
                    ? outputAutomation.getStackInSlot(outputSlot)
                    : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < INPUT_COUNT
                    ? inputAutomation.insertItem(slot, stack, simulate)
                    : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int outputSlot = slot - INPUT_COUNT;
            return outputSlot >= 0 && outputSlot < OUTPUT_COUNT
                    ? outputAutomation.extractItem(outputSlot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= 0 && slot < INPUT_COUNT) {
                return inputAutomation.getSlotLimit(slot);
            }
            int outputSlot = slot - INPUT_COUNT;
            return outputSlot >= 0 && outputSlot < OUTPUT_COUNT
                    ? outputAutomation.getSlotLimit(outputSlot)
                    : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < INPUT_COUNT && inputAutomation.isItemValid(slot, stack);
        }
    }
}

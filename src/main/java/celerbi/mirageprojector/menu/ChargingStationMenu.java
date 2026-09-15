package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.blockentity.ChargingStationBlockEntity;
import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public final class ChargingStationMenu extends AbstractContainerMenu {
    public static final int INPUT_X = 20;
    public static final int INPUT_Y = 55;
    public static final int CHARGING_X = 126;
    public static final int CHARGING_Y = 55;
    public static final int OUTPUT_X = 178;
    public static final int OUTPUT_Y = 55;
    public static final int PLAYER_INV_X = 54;
    public static final int PLAYER_INV_Y = 132;

    public static final int MACHINE_SLOT_COUNT = ChargingStationBlockEntity.SLOT_COUNT;
    public static final int FIRST_PLAYER_SLOT = MACHINE_SLOT_COUNT;

    private final BlockPos stationPos;
    @Nullable
    private final ChargingStationBlockEntity station;

    public ChargingStationMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.CHARGING_STATION.get(), containerId);
        stationPos = buffer.readBlockPos();
        station = playerInventory.player.level().getBlockEntity(stationPos) instanceof ChargingStationBlockEntity be
                ? be
                : null;
        addMachineSlots(station == null ? new ItemStackHandler(MACHINE_SLOT_COUNT) : station.inventory());
        addPlayerInventory(playerInventory);
    }

    public ChargingStationMenu(int containerId, Inventory playerInventory, ChargingStationBlockEntity station) {
        super(ModMenus.CHARGING_STATION.get(), containerId);
        this.stationPos = station.getBlockPos();
        this.station = station;
        addMachineSlots(station.inventory());
        addPlayerInventory(playerInventory);
    }

    private void addMachineSlots(ItemStackHandler handler) {
        for (int i = 0; i < ChargingStationBlockEntity.INPUT_COUNT; i++) {
            addSlot(new RechargeableSlot(handler, ChargingStationBlockEntity.INPUT_START + i, INPUT_X + i * 18, INPUT_Y, false));
        }
        addSlot(new RechargeableSlot(handler, ChargingStationBlockEntity.CHARGING_SLOT, CHARGING_X, CHARGING_Y, false) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < ChargingStationBlockEntity.OUTPUT_COUNT; i++) {
            addSlot(new RechargeableSlot(handler, ChargingStationBlockEntity.OUTPUT_START + i, OUTPUT_X + i * 18, OUTPUT_Y, true));
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        inventory,
                        col + row * 9 + 9,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18
                ));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, PLAYER_INV_X + col * 18, PLAYER_INV_Y + 58));
        }
    }

    public ItemStack activeChargingStack() {
        return getSlot(ChargingStationBlockEntity.CHARGING_SLOT).getItem();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getItem();
        ItemStack copy = current.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(current, FIRST_PLAYER_SLOT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ChargingStationBlockEntity.isChargeableInput(current)) {
            int inputEnd = ChargingStationBlockEntity.INPUT_START + ChargingStationBlockEntity.INPUT_COUNT;
            if (!moveItemStackTo(current, ChargingStationBlockEntity.INPUT_START, inputEnd, false)) {
                if (!moveItemStackTo(
                        current,
                        ChargingStationBlockEntity.CHARGING_SLOT,
                        ChargingStationBlockEntity.CHARGING_SLOT + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                stationPos.getX() + 0.5D,
                stationPos.getY() + 0.5D,
                stationPos.getZ() + 0.5D
        ) <= 64.0D;
    }

    private static class RechargeableSlot extends SlotItemHandler {
        private final boolean outputOnly;

        private RechargeableSlot(ItemStackHandler handler, int index, int x, int y, boolean outputOnly) {
            super(handler, index, x, y);
            this.outputOnly = outputOnly;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !outputOnly && ChargingStationBlockEntity.isChargeableInput(stack);
        }
    }
}

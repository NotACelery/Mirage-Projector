package celerbi.mirageprojector.equipment;

import celerbi.mirageprojector.item.ShoulderMountableDevice;
import celerbi.mirageprojector.item.ShoulderUpgrade;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/** Item-backed inventory owned by one Shoulder Strap ItemStack. */
public final class ShoulderStrapContainer extends SimpleContainer {
    public static final int DEVICE_SLOT = 0;
    public static final int BATTERY_START = 1;
    public static final int BATTERY_SLOTS = 12;
    public static final int UPGRADE_START = BATTERY_START + BATTERY_SLOTS;
    public static final int UPGRADE_SLOTS = 3;
    public static final int SLOT_COUNT = UPGRADE_START + UPGRADE_SLOTS;
    private static final int LEGACY_BATTERY_SLOTS = 9;
    private static final int LEGACY_UPGRADE_START = BATTERY_START + LEGACY_BATTERY_SLOTS;
    private static final int LEGACY_SLOT_COUNT = LEGACY_UPGRADE_START + UPGRADE_SLOTS;

    private final ItemStack strap;
    private boolean loading;

    public ShoulderStrapContainer(ItemStack strap) {
        super(SLOT_COUNT);
        this.strap = strap == null ? ItemStack.EMPTY : strap;
        this.loading = true;
        ItemContainerContents contents = this.strap.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (contents.getSlots() == LEGACY_SLOT_COUNT) {
            setItem(DEVICE_SLOT, contents.getStackInSlot(DEVICE_SLOT).copy());
            for (int i = 0; i < LEGACY_BATTERY_SLOTS; i++) {
                setItem(BATTERY_START + i, contents.getStackInSlot(BATTERY_START + i).copy());
            }
            for (int i = 0; i < UPGRADE_SLOTS; i++) {
                setItem(UPGRADE_START + i, contents.getStackInSlot(LEGACY_UPGRADE_START + i).copy());
            }
        } else {
            contents.copyInto(getItems());
        }
        this.loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (loading || strap.isEmpty()) {
            return;
        }
        strap.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == DEVICE_SLOT) {
            return stack != null && !stack.isEmpty() && stack.getItem() instanceof ShoulderMountableDevice;
        }
        if (isBatterySlot(slot)) {
            return ShoulderEquipment.isStorableItem(stack);
        }
        if (isUpgradeSlot(slot)) {
            return stack != null && !stack.isEmpty() && stack.getItem() instanceof ShoulderUpgrade;
        }
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    public int slotLimit(int slot, ItemStack stack) {
        if (slot == DEVICE_SLOT || isUpgradeSlot(slot)) {
            return 1;
        }
        return stack == null || stack.isEmpty() ? 64 : stack.getMaxStackSize();
    }

    public boolean hasExpansionPatch() {
        return hasUpgradeFamily(ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION);
    }

    public boolean hasUpgradeFamily(ResourceLocation family) {
        if (family == null) {
            return false;
        }
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            ItemStack stack = getItem(UPGRADE_START + i);
            if (stack.getItem() instanceof ShoulderUpgrade upgrade
                    && family.equals(upgrade.shoulderUpgradeFamily(stack))) {
                return true;
            }
        }
        return false;
    }

    public boolean canPlaceUpgrade(int index, ItemStack stack) {
        if (index < 0 || index >= UPGRADE_SLOTS
                || stack == null || stack.isEmpty()
                || !(stack.getItem() instanceof ShoulderUpgrade upgrade)) {
            return false;
        }
        ResourceLocation family = upgrade.shoulderUpgradeFamily(stack);
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            if (i == index) {
                continue;
            }
            ItemStack installed = getItem(UPGRADE_START + i);
            if (installed.getItem() instanceof ShoulderUpgrade other
                    && family.equals(other.shoulderUpgradeFamily(installed))) {
                return false;
            }
        }
        return true;
    }

    public boolean expansionDependentSlotsEmpty() {
        for (int i = ShoulderEquipment.BASE_BATTERY_SLOTS; i < BATTERY_SLOTS; i++) {
            if (!getItem(BATTERY_START + i).isEmpty()) {
                return false;
            }
        }
        return getItem(UPGRADE_START + UPGRADE_SLOTS - 1).isEmpty();
    }

    public List<ItemStack> snapshot() {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.set(i, getItem(i).copy());
        }
        return List.copyOf(items);
    }

    public static boolean isBatterySlot(int slot) {
        return slot >= BATTERY_START && slot < BATTERY_START + BATTERY_SLOTS;
    }

    public static boolean isUpgradeSlot(int slot) {
        return slot >= UPGRADE_START && slot < UPGRADE_START + UPGRADE_SLOTS;
    }
}

package celerbi.mirageprojector.equipment;

import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.item.ShoulderUpgrade;
import celerbi.mirageprojector.item.ShoulderMountableDevice;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Player attachment that owns the Shoulder Strap slot. */
public final class ShoulderEquipment extends ItemStackHandler {
    public static final int STRAP_SLOT = 0;

    public static final int DEVICE_SLOT = 1;
    public static final int BATTERY_START = 2;
    private static final int LEGACY_BATTERY_SLOTS = 9;
    public static final int BASE_BATTERY_SLOTS = 8;
    public static final int EXPANDED_BATTERY_SLOTS = 12;
    private static final int LEGACY_UPGRADE_START = BATTERY_START + LEGACY_BATTERY_SLOTS;
    public static final int UPGRADE_START = LEGACY_UPGRADE_START;
    public static final int BASE_UPGRADE_SLOTS = 2;
    public static final int EXPANDED_UPGRADE_SLOTS = 3;
    public static final int LEGACY_SLOT_COUNT = LEGACY_UPGRADE_START + EXPANDED_UPGRADE_SLOTS;
    private static final int BARE_DEVICE_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    private final Player owner;

    public ShoulderEquipment(Player owner) {
        super(SLOT_COUNT);
        this.owner = owner;
    }

    public Player owner() {
        return owner;
    }

    public boolean hasStrap() {
        return strap().is(ModItems.SHOULDER_STRAP.get());
    }

    public ItemStack strap() {
        return getStackInSlot(STRAP_SLOT);
    }

    public ShoulderStrapContainer strapInventory() {
        return new ShoulderStrapContainer(strap());
    }

    public ItemStack device() {
        if (hasStrap()) {
            return strapInventory().getItem(ShoulderStrapContainer.DEVICE_SLOT);
        }
        ItemStack direct = getStackInSlot(STRAP_SLOT);
        return direct.getItem() instanceof ShoulderMountableDevice ? direct : ItemStack.EMPTY;
    }

    public boolean hasDevice() {
        return !device().isEmpty();
    }

    public void setDevice(ItemStack stack) {
        if (!hasStrap()) {
            setStackInSlot(STRAP_SLOT, safeOne(stack));
            return;
        }
        ShoulderStrapContainer container = strapInventory();
        container.setItem(ShoulderStrapContainer.DEVICE_SLOT, safeOne(stack));
        container.setChanged();
    }

    public ItemStack extractDevice() {
        if (!hasStrap()) {
            ItemStack direct = getStackInSlot(STRAP_SLOT);
            if (!(direct.getItem() instanceof ShoulderMountableDevice)) {
                return ItemStack.EMPTY;
            }
            return extractItem(STRAP_SLOT, 1, false);
        }
        ShoulderStrapContainer container = strapInventory();
        ItemStack removed = container.removeItemNoUpdate(ShoulderStrapContainer.DEVICE_SLOT);
        container.setChanged();
        return removed;
    }

    public int activeBatterySlots() {
        return hasExpansionPatch() ? EXPANDED_BATTERY_SLOTS : BASE_BATTERY_SLOTS;
    }

    public int activeUpgradeSlots() {
        return hasExpansionPatch() ? EXPANDED_UPGRADE_SLOTS : BASE_UPGRADE_SLOTS;
    }

    public static int batterySlot(int pouchIndex) {
        return BATTERY_START + pouchIndex;
    }

    public static int upgradeSlot(int upgradeIndex) {
        return UPGRADE_START + upgradeIndex;
    }

    public ItemStack battery(int pouchIndex) {
        if (!hasStrap() || !validBatteryIndex(pouchIndex)) {
            return ItemStack.EMPTY;
        }
        return strapInventory().getItem(ShoulderStrapContainer.BATTERY_START + pouchIndex);
    }

    public void setBattery(int pouchIndex, ItemStack stack) {
        if (!hasStrap() || !validBatteryIndex(pouchIndex)) {
            return;
        }
        ShoulderStrapContainer container = strapInventory();
        container.setItem(ShoulderStrapContainer.BATTERY_START + pouchIndex, safeCopy(stack));
        container.setChanged();
    }

    public ItemStack extractBattery(int pouchIndex, int amount) {
        if (!hasStrap() || !validBatteryIndex(pouchIndex) || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ShoulderStrapContainer container = strapInventory();
        ItemStack removed = container.removeItem(ShoulderStrapContainer.BATTERY_START + pouchIndex, amount);
        container.setChanged();
        return removed;
    }

    public ItemStack insertBattery(int pouchIndex, ItemStack stack, boolean simulate) {
        if (!hasStrap() || pouchIndex < 0 || pouchIndex >= activeBatterySlots()
                || !isStorableItem(stack)) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        ShoulderStrapContainer container = strapInventory();
        int slot = ShoulderStrapContainer.BATTERY_START + pouchIndex;
        ItemStack installed = container.getItem(slot);
        int limit = stack.getMaxStackSize();
        if (!installed.isEmpty() && !ItemStack.isSameItemSameComponents(installed, stack)) {
            return stack;
        }
        int room = installed.isEmpty() ? limit : Math.max(0, limit - installed.getCount());
        int moved = Math.min(room, stack.getCount());
        if (moved <= 0) {
            return stack;
        }
        if (!simulate) {
            ItemStack replacement = installed.isEmpty() ? stack.copyWithCount(moved) : installed.copy();
            if (!installed.isEmpty()) {
                replacement.grow(moved);
            }
            container.setItem(slot, replacement);
            container.setChanged();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(moved);
        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    public ItemStack upgrade(int upgradeIndex) {
        if (!hasStrap() || !validUpgradeIndex(upgradeIndex)) {
            return ItemStack.EMPTY;
        }
        return strapInventory().getItem(ShoulderStrapContainer.UPGRADE_START + upgradeIndex);
    }

    public void setUpgrade(int upgradeIndex, ItemStack stack) {
        if (!hasStrap() || !validUpgradeIndex(upgradeIndex)) {
            return;
        }
        ShoulderStrapContainer container = strapInventory();
        container.setItem(ShoulderStrapContainer.UPGRADE_START + upgradeIndex, safeOne(stack));
        container.setChanged();
    }

    public ItemStack extractUpgrade(int upgradeIndex) {
        if (!hasStrap() || !validUpgradeIndex(upgradeIndex)) {
            return ItemStack.EMPTY;
        }
        ShoulderStrapContainer container = strapInventory();
        ItemStack installed = container.getItem(ShoulderStrapContainer.UPGRADE_START + upgradeIndex);
        if (isExpansion(installed) && !container.expansionDependentSlotsEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = container.removeItemNoUpdate(ShoulderStrapContainer.UPGRADE_START + upgradeIndex);
        container.setChanged();
        return removed;
    }

    public boolean canPlaceUpgrade(int upgradeIndex, ItemStack stack) {
        return hasStrap()
                && upgradeIndex >= 0
                && upgradeIndex < activeUpgradeSlots()
                && strapInventory().canPlaceUpgrade(upgradeIndex, stack);
    }

    public boolean hasAutoBatterySwapPatch() {
        return hasUpgradeFamily(ShoulderUpgradeFamilies.AUTO_BATTERY_SWAP);
    }

    public boolean hasExpansionPatch() {
        return hasUpgradeFamily(ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION);
    }

    public boolean hasUpgradeFamily(ResourceLocation family) {
        return hasStrap() && strapInventory().hasUpgradeFamily(family);
    }

    public boolean canRemoveStrap() {
        return true;
    }

    public boolean expansionDependentSlotsEmpty() {
        return !hasStrap() || strapInventory().expansionDependentSlotsEmpty();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == STRAP_SLOT && stack != null
                && (stack.is(ModItems.SHOULDER_STRAP.get()) || stack.getItem() instanceof ShoulderMountableDevice);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == STRAP_SLOT ? 1 : 0;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == STRAP_SLOT && !canRemoveStrap()) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    /** Migrates the legacy attachment inventory into the Strap ItemStack. */
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag source = nbt == null ? new CompoundTag() : nbt.copy();
        int serializedSize = source.contains("Size") ? source.getInt("Size") : SLOT_COUNT;
        if (serializedSize <= SLOT_COUNT) {
            source.putInt("Size", SLOT_COUNT);
            super.deserializeNBT(provider, source);
            return;
        }

        ItemStackHandler legacy = new ItemStackHandler(LEGACY_SLOT_COUNT);
        source.putInt("Size", LEGACY_SLOT_COUNT);
        legacy.deserializeNBT(provider, source);

        ItemStack strap = legacy.getStackInSlot(STRAP_SLOT).copyWithCount(1);
        boolean dependentContent = false;
        for (int slot = DEVICE_SLOT; slot < LEGACY_SLOT_COUNT; slot++) {
            if (!legacy.getStackInSlot(slot).isEmpty()) {
                dependentContent = true;
                break;
            }
        }
        if (!strap.is(ModItems.SHOULDER_STRAP.get()) && dependentContent) {
            strap = new ItemStack(ModItems.SHOULDER_STRAP.get());
        }
        if (strap.is(ModItems.SHOULDER_STRAP.get())) {
            ShoulderStrapContainer container = new ShoulderStrapContainer(strap);
            container.setItem(ShoulderStrapContainer.DEVICE_SLOT, legacy.getStackInSlot(DEVICE_SLOT).copy());
            for (int i = 0; i < LEGACY_BATTERY_SLOTS; i++) {
                container.setItem(
                        ShoulderStrapContainer.BATTERY_START + i,
                        legacy.getStackInSlot(BATTERY_START + i).copy()
                );
            }
            for (int i = 0; i < EXPANDED_UPGRADE_SLOTS; i++) {
                container.setItem(
                        ShoulderStrapContainer.UPGRADE_START + i,
                        legacy.getStackInSlot(UPGRADE_START + i).copy()
                );
            }
            container.setChanged();
        }

        ItemStackHandler migrated = new ItemStackHandler(SLOT_COUNT);
        if (!strap.isEmpty()) {
            migrated.setStackInSlot(STRAP_SLOT, strap);
        }
        super.deserializeNBT(provider, migrated.serializeNBT(provider));
    }

    private static boolean isExpansion(ItemStack stack) {
        return stack.getItem() instanceof ShoulderUpgrade upgrade
                && ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION.equals(upgrade.shoulderUpgradeFamily(stack));
    }

    private static ItemStack safeOne(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    }

    private static ItemStack safeCopy(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private static boolean validBatteryIndex(int index) {
        return index >= 0 && index < EXPANDED_BATTERY_SLOTS;
    }

    public static boolean isStorableItem(ItemStack stack) {
        return RechargeableEnergyItem.isRechargeable(stack)
                || stack.is(ModItems.SCAN_CODEX.get())
                || stack.is(ModItems.ENTITY_SCAN_CARD.get())
                || stack.is(ModItems.MIRAGE_FLASHLIGHT.get())
                || stack.is(ModItems.MIRAGE_HAND_PROJECTOR.get());
    }

    private static boolean validUpgradeIndex(int index) {
        return index >= 0 && index < EXPANDED_UPGRADE_SLOTS;
    }
}

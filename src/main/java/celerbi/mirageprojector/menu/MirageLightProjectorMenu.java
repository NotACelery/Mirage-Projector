package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Battery/configuration container for the placed Mirage Light Projector. */
public final class MirageLightProjectorMenu extends AbstractContainerMenu {
    public static final int BATTERY_X = 31;
    public static final int BATTERY_Y = 48;
    public static final int PLAYER_INV_X = 17;
    public static final int PLAYER_INV_Y = 100;

    private final BlockPos pos;
    @Nullable private final MirageLightProjectorBlockEntity projector;

    public MirageLightProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos());
    }

    private MirageLightProjectorMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.MIRAGE_LIGHT_PROJECTOR.get(), containerId);
        this.pos = pos;
        this.projector = inventory.player.level().getBlockEntity(pos) instanceof MirageLightProjectorBlockEntity be ? be : null;
        addSlots(inventory);
    }

    public MirageLightProjectorMenu(int containerId, Inventory inventory, MirageLightProjectorBlockEntity projector) {
        super(ModMenus.MIRAGE_LIGHT_PROJECTOR.get(), containerId);
        this.pos = projector.getBlockPos();
        this.projector = projector;
        addSlots(inventory);
    }

    private void addSlots(Inventory inventory) {
        Container battery = new ProjectorBatteryContainer(projector);
        addSlot(new Slot(battery, 0, BATTERY_X, BATTERY_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return RechargeableEnergyItem.isRechargeable(stack); }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, PLAYER_INV_X + col * 18, PLAYER_INV_Y + 58));
        }
    }

    public BlockPos projectorPos() { return pos; }
    @Nullable public MirageLightProjectorBlockEntity projector() { return projector; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        if (index == 0) {
            if (!moveItemStackTo(current, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (RechargeableEnergyItem.isRechargeable(current)) {
            if (!moveItemStackTo(current, 0, 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (current.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    private static final class ProjectorBatteryContainer implements Container {
        @Nullable private final MirageLightProjectorBlockEntity projector;
        private ItemStack clientStack = ItemStack.EMPTY;
        private ProjectorBatteryContainer(@Nullable MirageLightProjectorBlockEntity projector) { this.projector = projector; }
        @Override public int getContainerSize() { return 1; }
        @Override public boolean isEmpty() { return getItem(0).isEmpty(); }
        @Override public ItemStack getItem(int slot) { return slot != 0 ? ItemStack.EMPTY : projector != null ? projector.energyCell() : clientStack; }
        @Override public ItemStack removeItem(int slot, int amount) {
            if (slot != 0 || amount <= 0) return ItemStack.EMPTY;
            ItemStack current = getItem(0);
            if (current.isEmpty()) return ItemStack.EMPTY;
            ItemStack removed = current.copyWithCount(Math.min(amount, current.getCount()));
            setItem(0, ItemStack.EMPTY);
            return removed;
        }
        @Override public ItemStack removeItemNoUpdate(int slot) { return removeItem(slot, 64); }
        @Override public void setItem(int slot, ItemStack stack) {
            if (slot != 0) return;
            ItemStack safe = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
            if (projector != null) projector.setEnergyCell(safe);
            else clientStack = safe;
        }
        @Override public void setChanged() { if (projector != null) projector.markEnergyChanged(); }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 0 && RechargeableEnergyItem.isRechargeable(stack); }
        @Override public void clearContent() { setItem(0, ItemStack.EMPTY); }
    }
}

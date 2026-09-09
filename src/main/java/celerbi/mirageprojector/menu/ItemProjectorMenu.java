package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Dedicated Item Snapshot workspace. The source slot is virtual and never stores the real item. */
public final class ItemProjectorMenu extends AbstractContainerMenu {
    public static final int SNAPSHOT_X = 30;
    public static final int SNAPSHOT_Y = 68;
    public static final int PLAYER_INV_X = 99;
    public static final int PLAYER_INV_Y = 174;
    public static final int SNAPSHOT_SLOT_INDEX = 0;
    public static final int FIRST_PLAYER_SLOT_INDEX = 1;

    private final BlockPos projectorPos;
    @Nullable
    private final MirageProjectorBlockEntity projector;

    public ItemProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.ITEM_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;
        addSnapshotSlot(projector == null ? new ItemStackHandler(1) : projector.projectionSnapshot());
        addPlayerInventory(inventory);
    }

    public ItemProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.ITEM_PROJECTOR.get(), containerId);
        this.projectorPos = projector.getBlockPos();
        this.projector = projector;
        addSnapshotSlot(projector.projectionSnapshot());
        addPlayerInventory(inventory);
    }

    private void addSnapshotSlot(ItemStackHandler handler) {
        addSlot(new SlotItemHandler(handler, 0, SNAPSHOT_X, SNAPSHOT_Y) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPickup(Player player) { return false; }
            @Override public boolean isFake() { return true; }
        });
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, PLAYER_INV_X + col * 18, PLAYER_INV_Y + 58));
        }
    }

    public BlockPos projectorPos() { return projectorPos; }
    @Nullable public MirageProjectorBlockEntity projector() { return projector; }
    public ItemStack snapshotStack() { return getSlot(SNAPSHOT_SLOT_INDEX).getItem(); }
    @Nullable public UUID snapshotId() { return projector == null ? null : projector.projectionSnapshotId(); }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == SNAPSHOT_SLOT_INDEX) {
            if (projector == null || player.level().isClientSide) return;
            switch (clickType) {
                case PICKUP -> {
                    ItemStack carried = getCarried();
                    if (carried.isEmpty()) projector.clearProjectionSnapshot();
                    else projector.captureProjectionSnapshot(carried);
                }
                case SWAP -> {
                    ItemStack source = ItemStack.EMPTY;
                    if (button >= 0 && button < 9) source = player.getInventory().getItem(button);
                    else if (button == 40) source = player.getOffhandItem();
                    if (!source.isEmpty()) projector.captureProjectionSnapshot(source);
                }
                case QUICK_MOVE, THROW -> projector.clearProjectionSnapshot();
                case CLONE, QUICK_CRAFT, PICKUP_ALL -> { }
            }
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.index != SNAPSHOT_SLOT_INDEX && super.canDragTo(slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        if (index == SNAPSHOT_SLOT_INDEX) {
            if (projector != null && !player.level().isClientSide) projector.clearProjectionSnapshot();
            return ItemStack.EMPTY;
        }
        if (projector != null && !player.level().isClientSide) {
            projector.captureProjectionSnapshot(slot.getItem());
            broadcastChanges();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(projectorPos.getX() + 0.5D, projectorPos.getY() + 0.5D, projectorPos.getZ() + 0.5D) <= 64.0D;
    }
}

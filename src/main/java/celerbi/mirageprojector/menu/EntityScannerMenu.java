package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.item.EntityScannerItem;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

public final class EntityScannerMenu extends AbstractContainerMenu {
    private final InteractionHand hand;
    private final SimpleContainer storage = new SimpleContainer(1);
    private final int lockedHotbarIndex;
    private final int lockedMenuSlot;

    public EntityScannerMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    public EntityScannerMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ModMenus.ENTITY_SCANNER.get(), id);
        this.hand = hand;
        this.lockedHotbarIndex = inventory.selected;
        this.lockedMenuSlot = 1 + 27 + lockedHotbarIndex;
        if (!inventory.player.level().isClientSide && inventory.player instanceof ServerPlayer player) {
            storage.setItem(0, EntityScannerItem.containedCodex(player.getItemInHand(hand), player));
        }
        addSlot(new Slot(storage, 0, 80, 28) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(ModItems.SCAN_CODEX.get()); }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addPlayerSlot(inventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18);
        for (int col = 0; col < 9; col++) addPlayerSlot(inventory, col, 8 + col * 18, 142);
    }

    public static void open(ServerPlayer player, InteractionHand hand, ItemStack scanner) {
        if (hand != InteractionHand.MAIN_HAND || !scanner.is(ModItems.ENTITY_SCANNER.get())) {
            return;
        }
        ((IPlayerExtension) player).openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new EntityScannerMenu(id, inventory, hand), Component.translatable("gui.mirage_projector.entity_scanner.title")), buffer -> buffer.writeBoolean(hand == InteractionHand.OFF_HAND));
    }

    @Override public boolean stillValid(Player player) { return hand == InteractionHand.MAIN_HAND && player.getMainHandItem().is(ModItems.ENTITY_SCANNER.get()); }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack result = source.copy();
        if (index == 0) {
            if (!moveItemStackTo(source, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(ModItems.SCAN_CODEX.get()) && storage.getItem(0).isEmpty()) {
            if (!moveItemStackTo(source, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == lockedMenuSlot || (clickType == ClickType.SWAP && button == lockedHotbarIndex)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && player instanceof ServerPlayer server && stillValid(player)) EntityScannerItem.setContainedCodex(player.getItemInHand(hand), storage.getItem(0), server);
    }

    private void addPlayerSlot(Inventory inventory, int inventorySlot, int x, int y) {
        if (inventorySlot != lockedHotbarIndex) {
            addSlot(new Slot(inventory, inventorySlot, x, y));
            return;
        }
        addSlot(new Slot(inventory, inventorySlot, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        });
    }
}

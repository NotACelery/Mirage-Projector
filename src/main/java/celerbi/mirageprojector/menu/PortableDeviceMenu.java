package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

/** One-cell configuration container shared by the Lantern and Hand Projector. */
public final class PortableDeviceMenu extends AbstractContainerMenu {
    public static final int BATTERY_X = 31;
    public static final int BATTERY_Y = 48;
    public static final int PLAYER_INV_X = 17;
    public static final int COMPACT_PLAYER_INV_Y = 100;
    public static final int PROJECTOR_PLAYER_INV_Y = 190;
    public static final int MACHINE_SLOT_COUNT = 1;

    private final Inventory playerInventory;
    private final PortableDeviceSource source;
    private final DeviceBatteryContainer battery;
    private final int playerInvY;

    public PortableDeviceMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, PortableDeviceSource.byOrdinal(buffer.readVarInt()));
    }

    public PortableDeviceMenu(int containerId, Inventory inventory, PortableDeviceSource source) {
        super(ModMenus.PORTABLE_DEVICE.get(), containerId);
        this.playerInventory = inventory;
        this.source = source == null ? PortableDeviceSource.MAIN_HAND : source;
        ItemStack device = this.source.resolve(inventory.player);
        this.playerInvY = device.getItem() instanceof MirageHandProjectorItem
                ? PROJECTOR_PLAYER_INV_Y
                : COMPACT_PLAYER_INV_Y;
        this.battery = new DeviceBatteryContainer(inventory.player, this.source);
        addSlot(new Slot(battery, 0, BATTERY_X, BATTERY_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return RechargeableEnergyItem.isRechargeable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addPlayerInventory(inventory);
    }

    public PortableDeviceSource source() {
        return source;
    }

    public int playerInvY() {
        return playerInvY;
    }

    public boolean projectorLayout() {
        return playerInvY == PROJECTOR_PLAYER_INV_Y;
    }

    public ItemStack batteryStack() {
        return getSlot(0).getItem();
    }

    public static boolean supported(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && (stack.getItem() instanceof MirageLanternItem
                || stack.getItem() instanceof MirageHandProjectorItem);
    }

    public static void open(ServerPlayer player, PortableDeviceSource source) {
        if (player == null || source == null || !supported(source.resolve(player))) {
            return;
        }
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new PortableDeviceMenu(containerId, inventory, source),
                Component.translatable("container.mirage_projector.portable_device")
        );
        ((IPlayerExtension) player).openMenu(provider, buffer -> buffer.writeVarInt(source.ordinal()));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, PLAYER_INV_X + col * 18, playerInvY + 58));
        }
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
        ItemStack original = current.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(current, MACHINE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (RechargeableEnergyItem.isRechargeable(current)) {
            if (!moveItemStackTo(current, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return supported(source.resolve(player)) || player.level().isClientSide;
    }

    private static final class DeviceBatteryContainer extends SimpleContainer {
        private final Player player;
        private final PortableDeviceSource source;
        private boolean loading;

        private DeviceBatteryContainer(Player player, PortableDeviceSource source) {
            super(1);
            this.player = player;
            this.source = source;
            loading = true;
            if (player != null && !player.level().isClientSide) {
                ItemStack device = source.resolve(player);
                if (device.getItem() instanceof MirageLanternItem) {
                    super.setItem(0, MirageLanternItem.energyCell(device, player.registryAccess()));
                } else if (device.getItem() instanceof MirageHandProjectorItem) {
                    super.setItem(0, MirageHandProjectorItem.energyCell(device, player.registryAccess()));
                }
            }
            loading = false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot == 0 && RechargeableEnergyItem.isRechargeable(stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (loading || player == null || player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ItemStack device = source.resolve(player);
            if (!supported(device)) {
                return;
            }
            ItemStack cell = getItem(0);
            if (device.getItem() instanceof MirageLanternItem) {
                MirageLanternItem.replaceEnergyCell(device, cell, player.registryAccess());
            } else if (device.getItem() instanceof MirageHandProjectorItem) {
                MirageHandProjectorItem.replaceEnergyCell(device, cell, player.registryAccess());
                MirageHandProjectorItem.publishState(player, device);
            }
            source.commit(serverPlayer, device);
        }
    }
}

package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageFlashlightItem;
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
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

/** One-cell configuration container shared by the Flashlight and Hand Projector. */
public final class PortableDeviceMenu extends AbstractContainerMenu {
    public static final int BATTERY_X = 89;
    public static final int BATTERY_Y = 42;
    public static final int PROJECTOR_BATTERY_X = 28;
    public static final int PROJECTOR_BATTERY_Y = 148;
    public static final int PROJECTOR_CORE_X = 28;
    public static final int PROJECTOR_CORE_Y = 206;
    public static final int PROJECTOR_SOURCE_X = 28;
    public static final int PROJECTOR_SOURCE_Y = 90;
    public static final int PLAYER_INV_X = 17;
    public static final int PROJECTOR_PLAYER_INV_X = 99;
    public static final int COMPACT_PLAYER_INV_Y = 112;
    public static final int PROJECTOR_PLAYER_INV_Y = 286;
    public static final int BATTERY_SLOT_INDEX = 0;
    public static final int CORE_SLOT_INDEX = 1;
    public static final int SOURCE_SLOT_INDEX = 2;
    public static final int MACHINE_SLOT_COUNT = 3;

    private final Inventory playerInventory;
    private final PortableDeviceSource source;
    private final DeviceBatteryContainer battery;
    private final DeviceCoreContainer core;
    private final SimpleContainer sourcePreview = new SimpleContainer(1);
    private final int playerInvX;
    private final int playerInvY;
    private final int batteryX;
    private final int batteryY;
    private final int coreX;
    private final int coreY;
    private final int sourceX;
    private final int sourceY;
    private final boolean projectorLayout;

    public PortableDeviceMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                inventory,
                PortableDeviceSource.byOrdinal(buffer.readVarInt()),
                buffer.readBoolean()
        );
    }

    public PortableDeviceMenu(int containerId, Inventory inventory, PortableDeviceSource source) {
        this(containerId, inventory, source,
                source != null && source.resolve(inventory.player).getItem() instanceof MirageHandProjectorItem);
    }

    private PortableDeviceMenu(
            int containerId,
            Inventory inventory,
            PortableDeviceSource source,
            boolean projectorLayout
    ) {
        super(ModMenus.PORTABLE_DEVICE.get(), containerId);
        this.playerInventory = inventory;
        this.source = source == null ? PortableDeviceSource.MAIN_HAND : source;
        this.projectorLayout = projectorLayout;
        this.playerInvX = projectorLayout ? PROJECTOR_PLAYER_INV_X : PLAYER_INV_X;
        this.playerInvY = projectorLayout ? PROJECTOR_PLAYER_INV_Y : COMPACT_PLAYER_INV_Y;
        this.batteryX = projectorLayout ? PROJECTOR_BATTERY_X : BATTERY_X;
        this.batteryY = projectorLayout ? PROJECTOR_BATTERY_Y : BATTERY_Y;
        this.coreX = PROJECTOR_CORE_X;
        this.coreY = PROJECTOR_CORE_Y;
        this.sourceX = PROJECTOR_SOURCE_X;
        this.sourceY = PROJECTOR_SOURCE_Y;
        this.battery = new DeviceBatteryContainer(inventory.player, this.source);
        this.core = new DeviceCoreContainer(inventory.player, this.source);
        addSlot(new Slot(battery, 0, batteryX, batteryY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return RechargeableEnergyItem.isRechargeable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(core, 0, coreX, coreY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ItemStack current = PortableDeviceMenu.this.source.resolve(playerInventory.player);
                return current.getItem() instanceof MirageHandProjectorItem && ProjectionCoreProfile.isCoreItem(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return PortableDeviceMenu.this.source.resolve(playerInventory.player).getItem() instanceof MirageHandProjectorItem;
            }
        });
        addSlot(new Slot(sourcePreview, 0, sourceX, sourceY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean isFake() {
                return true;
            }

            @Override
            public boolean isActive() {
                ItemStack current = PortableDeviceMenu.this.source.resolve(playerInventory.player);
                return current.getItem() instanceof MirageHandProjectorItem
                        && MirageHandProjectorItem.sourceMode(current) != celerbi.mirageprojector.ProjectionSettings.SourceMode.IMAGE;
            }
        });
        refreshSourceSnapshot();
        addPlayerInventory(inventory);
    }

    public PortableDeviceSource source() {
        return source;
    }

    public int playerInvX() {
        return playerInvX;
    }

    public int playerInvY() {
        return playerInvY;
    }

    public int batteryX() {
        return batteryX;
    }

    public int batteryY() {
        return batteryY;
    }

    public int coreX() {
        return coreX;
    }

    public int coreY() {
        return coreY;
    }

    public int sourceX() {
        return sourceX;
    }

    public int sourceY() {
        return sourceY;
    }

    public void refreshSourceSnapshot() {
        ItemStack device = source.resolve(playerInventory.player);
        if (device.getItem() instanceof MirageHandProjectorItem) {
            sourcePreview.setItem(0, MirageHandProjectorItem.sourceSnapshot(device, playerInventory.player.level()));
        } else {
            sourcePreview.setItem(0, ItemStack.EMPTY);
        }
    }

    public ItemStack sourceSnapshotStack() {
        return sourcePreview.getItem(0);
    }

    public boolean projectorLayout() {
        return projectorLayout;
    }

    public ItemStack batteryStack() {
        return getSlot(BATTERY_SLOT_INDEX).getItem();
    }

    public ItemStack coreStack() {
        return getSlot(CORE_SLOT_INDEX).getItem();
    }

    public static boolean supported(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && (stack.getItem() instanceof MirageFlashlightItem
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
        ((IPlayerExtension) player).openMenu(provider, buffer -> {
            buffer.writeVarInt(source.ordinal());
            buffer.writeBoolean(source.resolve(player).getItem() instanceof MirageHandProjectorItem);
        });
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9,
                        playerInvX + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, playerInvX + col * 18, playerInvY + 58));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == SOURCE_SLOT_INDEX) {
            if (player.level().isClientSide) {
                return;
            }
            ItemStack device = source.resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)) {
                return;
            }
            boolean changed = false;
            if (clickType == ClickType.PICKUP) {
                ItemStack carried = getCarried();
                changed = carried.isEmpty()
                        ? MirageHandProjectorItem.clearSourceSnapshot(device, player.level())
                        : MirageHandProjectorItem.captureSourceSnapshot(device, carried, player.level());
            } else if (clickType == ClickType.SWAP) {
                ItemStack candidate = ItemStack.EMPTY;
                if (button >= 0 && button < 9) {
                    candidate = player.getInventory().getItem(button);
                } else if (button == 40) {
                    candidate = player.getOffhandItem();
                }
                if (!candidate.isEmpty()) {
                    changed = MirageHandProjectorItem.captureSourceSnapshot(device, candidate, player.level());
                }
            } else if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.THROW) {
                changed = MirageHandProjectorItem.clearSourceSnapshot(device, player.level());
            }
            if (changed && player instanceof ServerPlayer serverPlayer) {
                MirageHandProjectorItem.publishState(player, device);
                source.commit(serverPlayer, device);
                refreshSourceSnapshot();
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return (SOURCE_SLOT_INDEX >= slots.size() || slot != slots.get(SOURCE_SLOT_INDEX)) && super.canDragTo(slot);
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

        if (index == BATTERY_SLOT_INDEX || index == CORE_SLOT_INDEX) {
            if (!moveItemStackTo(current, MACHINE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index == SOURCE_SLOT_INDEX) {
            if (!player.level().isClientSide) {
                ItemStack device = source.resolve(player);
                if (device.getItem() instanceof MirageHandProjectorItem
                        && MirageHandProjectorItem.clearSourceSnapshot(device, player.level())
                        && player instanceof ServerPlayer serverPlayer) {
                    MirageHandProjectorItem.publishState(player, device);
                    source.commit(serverPlayer, device);
                    refreshSourceSnapshot();
                    broadcastChanges();
                }
            }
            return ItemStack.EMPTY;
        } else if (RechargeableEnergyItem.isRechargeable(current)) {
            if (!moveItemStackTo(current, BATTERY_SLOT_INDEX, BATTERY_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (ProjectionCoreProfile.isCoreItem(current) && projectorLayout()) {
            if (!moveItemStackTo(current, CORE_SLOT_INDEX, CORE_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!player.level().isClientSide) {
                ItemStack device = source.resolve(player);
                if (device.getItem() instanceof MirageHandProjectorItem
                        && MirageHandProjectorItem.captureSourceSnapshot(device, current, player.level())
                        && player instanceof ServerPlayer serverPlayer) {
                    MirageHandProjectorItem.publishState(player, device);
                    source.commit(serverPlayer, device);
                    refreshSourceSnapshot();
                    broadcastChanges();
                }
            }
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
                if (device.getItem() instanceof MirageFlashlightItem) {
                    super.setItem(0, MirageFlashlightItem.energyCell(device, player.registryAccess()));
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
            if (device.getItem() instanceof MirageFlashlightItem) {
                MirageFlashlightItem.replaceEnergyCell(device, cell, player.registryAccess());
            } else if (device.getItem() instanceof MirageHandProjectorItem) {
                MirageHandProjectorItem.replaceEnergyCell(device, cell, player.registryAccess());
                MirageHandProjectorItem.publishState(player, device);
            }
            source.commit(serverPlayer, device);
        }
    }

    private static final class DeviceCoreContainer extends SimpleContainer {
        private final Player player;
        private final PortableDeviceSource source;
        private boolean loading;

        private DeviceCoreContainer(Player player, PortableDeviceSource source) {
            super(1);
            this.player = player;
            this.source = source;
            loading = true;
            if (player != null && !player.level().isClientSide) {
                ItemStack device = source.resolve(player);
                if (device.getItem() instanceof MirageHandProjectorItem) {
                    super.setItem(0, MirageHandProjectorItem.coreStack(device, player.registryAccess()));
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
            return slot == 0 && ProjectionCoreProfile.isCoreItem(stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (loading || player == null || player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ItemStack device = source.resolve(player);
            if (!(device.getItem() instanceof MirageHandProjectorItem)) {
                return;
            }
            MirageHandProjectorItem.replaceCore(device, getItem(0), player.registryAccess());
            MirageHandProjectorItem.publishState(player, device);
            source.commit(serverPlayer, device);
        }
    }
}

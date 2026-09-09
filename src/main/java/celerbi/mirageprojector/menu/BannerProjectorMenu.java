package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/** Dedicated virtual Banner source workspace. No real banner is stored by the projector. */
public final class BannerProjectorMenu extends AbstractContainerMenu {
    public static final int FACE_COUNT = 4;
    public static final int[] FACE_X = {42, 104, 166, 228};
    public static final int FACE_Y = 74;
    public static final int PLANE_FACE_X = 74;
    public static final int PLAYER_INV_X = 129;
    public static final int PLAYER_INV_Y = 194;
    public static final int FIRST_PLAYER_SLOT_INDEX = FACE_COUNT;

    private final BlockPos projectorPos;
    @Nullable
    private final MirageProjectorBlockEntity projector;
    private final boolean prism;

    public BannerProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.BANNER_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;
        prism = projector != null && projector.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM;
        addBannerSlots(projector == null ? new ItemStackHandler(FACE_COUNT) : projector.bannerSnapshots());
        addPlayerInventory(inventory);
    }

    public BannerProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.BANNER_PROJECTOR.get(), containerId);
        projectorPos = projector.getBlockPos();
        this.projector = projector;
        prism = projector.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM;
        addBannerSlots(projector.bannerSnapshots());
        addPlayerInventory(inventory);
    }

    private void addBannerSlots(ItemStackHandler handler) {
        for (int face = 0; face < FACE_COUNT; face++) {
            final int faceIndex = face;
            int x = prism ? FACE_X[face] : (face == 0 ? PLANE_FACE_X : -1000);
            int y = face == 0 || prism ? FACE_Y : -1000;
            addSlot(new SlotItemHandler(handler, face, x, y) {
                @Override public int getMaxStackSize() { return 1; }
                @Override public boolean mayPickup(Player player) { return false; }
                @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof BannerItem; }
                @Override public boolean isFake() { return true; }
                @Override public boolean isActive() { return faceIndex == 0 || prism; }
            });
        }
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
    public boolean prism() { return prism; }
    public ItemStack bannerSnapshot(int face) {
        return face >= 0 && face < FACE_COUNT ? getSlot(face).getItem() : ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < FACE_COUNT) {
            if (projector == null || player.level().isClientSide || !projector.bannerFaceAvailable(slotId)) return;
            switch (clickType) {
                case PICKUP -> {
                    ItemStack carried = getCarried();
                    if (carried.isEmpty()) projector.clearBannerSnapshot(slotId);
                    else if (carried.getItem() instanceof BannerItem) projector.captureBannerSnapshot(slotId, carried);
                }
                case SWAP -> {
                    ItemStack source = ItemStack.EMPTY;
                    if (button >= 0 && button < 9) source = player.getInventory().getItem(button);
                    else if (button == 40) source = player.getOffhandItem();
                    if (source.getItem() instanceof BannerItem) projector.captureBannerSnapshot(slotId, source);
                }
                case QUICK_MOVE, THROW -> projector.clearBannerSnapshot(slotId);
                case CLONE, QUICK_CRAFT, PICKUP_ALL -> { }
            }
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.index >= FACE_COUNT && super.canDragTo(slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        if (index < FACE_COUNT) {
            if (projector != null && !player.level().isClientSide && projector.bannerFaceAvailable(index)) {
                projector.clearBannerSnapshot(index);
            }
            return ItemStack.EMPTY;
        }
        if (projector != null && !player.level().isClientSide && slot.getItem().getItem() instanceof BannerItem) {
            projector.captureBannerSnapshot(0, slot.getItem());
            broadcastChanges();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(projectorPos.getX() + 0.5D, projectorPos.getY() + 0.5D, projectorPos.getZ() + 0.5D) <= 64.0D;
    }
}

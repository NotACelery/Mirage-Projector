package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public final class MirageProjectorMenu extends AbstractContainerMenu {
    public static final int PROJECTED_ITEM_SLOT_X = 386;
    public static final int PROJECTED_ITEM_SLOT_Y = 244;
    public static final int CORE_SLOT_X = 386;
    public static final int CORE_SLOT_Y = 218;
    public static final int PLAYER_INV_X = 127;
    public static final int PLAYER_INV_Y = 350;

    public static final int PROJECTED_ITEM_SLOT_INDEX = 0;
    public static final int CORE_SLOT_INDEX = 1;
    public static final int FIRST_PLAYER_SLOT_INDEX = 2;

    private final BlockPos projectorPos;
    private final ProjectionSettings initialSettings;
    @Nullable
    private final MirageProjectorBlockEntity projector;

    public MirageProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.MIRAGE_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        initialSettings = readSettings(buffer);
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;
        ItemStackHandler projectionHandler = projector == null ? new ItemStackHandler(1) : projector.projectionItem();
        ItemStackHandler coreHandler = projector == null ? new ItemStackHandler(1) : projector.coreItem();
        addProjectionSlot(projectionHandler);
        addCoreSlot(coreHandler);
        addPlayerInventory(inventory);
    }

    public MirageProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.MIRAGE_PROJECTOR.get(), containerId);
        this.projectorPos = projector.getBlockPos();
        this.initialSettings = projector.settings();
        this.projector = projector;
        addProjectionSlot(projector.projectionItem());
        addCoreSlot(projector.coreItem());
        addPlayerInventory(inventory);
    }

    private void addProjectionSlot(ItemStackHandler handler) {
        addSlot(new SlotItemHandler(handler, 0, PROJECTED_ITEM_SLOT_X, PROJECTED_ITEM_SLOT_Y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private void addCoreSlot(ItemStackHandler handler) {
        addSlot(new SlotItemHandler(handler, 0, CORE_SLOT_X, CORE_SLOT_Y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return ProjectionCoreProfile.isCoreItem(stack);
            }
        });
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new net.minecraft.world.inventory.Slot(
                        inventory,
                        col + row * 9 + 9,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new net.minecraft.world.inventory.Slot(
                    inventory,
                    col,
                    PLAYER_INV_X + col * 18,
                    PLAYER_INV_Y + 58
            ));
        }
    }

    private static ProjectionSettings readSettings(RegistryFriendlyByteBuf buffer) {
        String imageId = buffer.readUtf(128);
        int imageWidth = buffer.readVarInt();
        int imageHeight = buffer.readVarInt();
        String backImageId = buffer.readUtf(128);
        int backImageWidth = buffer.readVarInt();
        int backImageHeight = buffer.readVarInt();

        return new ProjectionSettings(
                imageId,
                imageWidth,
                imageHeight,
                backImageId,
                backImageWidth,
                backImageHeight,
                ProjectionSettings.SourceMode.fromOrdinal(buffer.readVarInt()),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readBoolean(),
                ProjectionSettings.FloatMode.fromOrdinal(buffer.readVarInt()),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                ProjectionSettings.BackFaceMode.fromOrdinal(buffer.readVarInt()),
                buffer.readBoolean(),
                buffer.readBoolean()
        ).sanitized();
    }

    public BlockPos projectorPos() {
        return projectorPos;
    }

    public ProjectionSettings initialSettings() {
        return initialSettings;
    }

    @Nullable
    public MirageProjectorBlockEntity projector() {
        return projector;
    }

    public ItemStack projectedItemStack() {
        return getSlot(PROJECTED_ITEM_SLOT_INDEX).getItem();
    }

    public ItemStack coreStack() {
        return getSlot(CORE_SLOT_INDEX).getItem();
    }

    public ProjectionCoreProfile coreProfile() {
        return ProjectionCoreProfile.fromStack(coreStack());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        var slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getItem();
        ItemStack copy = current.copy();

        if (index < FIRST_PLAYER_SLOT_INDEX) {
            if (!moveItemStackTo(current, FIRST_PLAYER_SLOT_INDEX, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ProjectionCoreProfile.isCoreItem(current)) {
            if (!moveItemStackTo(current, CORE_SLOT_INDEX, CORE_SLOT_INDEX + 1, false)) {
                if (!moveItemStackTo(current, PROJECTED_ITEM_SLOT_INDEX, PROJECTED_ITEM_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!moveItemStackTo(current, PROJECTED_ITEM_SLOT_INDEX, PROJECTED_ITEM_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
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
                projectorPos.getX() + 0.5D,
                projectorPos.getY() + 0.5D,
                projectorPos.getZ() + 0.5D
        ) <= 64.0D;
    }
}

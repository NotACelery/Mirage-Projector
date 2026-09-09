package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.HumanoidPosePreset;
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

import java.util.UUID;

/**
 * Primary projector menu. dev.19 intentionally keeps only global presentation,
 * power/core and player inventory here. Source-specific editing lives in dedicated workspaces.
 */
public final class MirageProjectorMenu extends AbstractContainerMenu {
    public static final int CORE_SLOT_X = 24;
    public static final int CORE_SLOT_Y = 300;
    public static final int PLAYER_INV_X = 126;
    public static final int PLAYER_INV_Y = 384;

    public static final int CORE_SLOT_INDEX = 0;
    public static final int FIRST_PLAYER_SLOT_INDEX = 1;

    private final BlockPos projectorPos;
    private final ProjectionSettings initialSettings;
    @Nullable
    private final MirageProjectorBlockEntity projector;

    public MirageProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.MIRAGE_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        initialSettings = ProjectionSettings.read(buffer);
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;
        ItemStackHandler coreHandler = projector == null ? new ItemStackHandler(1) : projector.coreItem();
        addCoreSlot(coreHandler);
        addPlayerInventory(inventory);
    }

    public MirageProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.MIRAGE_PROJECTOR.get(), containerId);
        this.projectorPos = projector.getBlockPos();
        this.initialSettings = projector.settings();
        this.projector = projector;
        addCoreSlot(projector.coreItem());
        addPlayerInventory(inventory);
    }

    private void addCoreSlot(ItemStackHandler handler) {
        addSlot(new SlotItemHandler(handler, 0, CORE_SLOT_X, CORE_SLOT_Y) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return ProjectionCoreProfile.isCoreItem(stack); }
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
    public ProjectionSettings initialSettings() { return initialSettings; }
    @Nullable public MirageProjectorBlockEntity projector() { return projector; }
    public ItemStack projectedItemStack() { return projector == null ? ItemStack.EMPTY : projector.projectedStack(); }
    @Nullable public UUID projectionSnapshotId() { return projector == null ? null : projector.projectionSnapshotId(); }
    public boolean hasLegacyProjectionReturnItem() { return projector != null && projector.hasLegacyProjectionReturnItem(); }
    public ItemStack coreStack() { return getSlot(CORE_SLOT_INDEX).getItem(); }
    public ProjectionCoreProfile coreProfile() { return ProjectionCoreProfile.fromStack(coreStack()); }
    public ProjectionChassisProfile chassisProfile() { return projector == null ? ProjectionChassisProfile.COMPACT : projector.chassisProfile(); }
    public boolean hasProjectedSourceContent() { return projector != null && projector.hasProjectedSourceContent(); }
    public int projectedSourceCount() { return projector == null ? 0 : projector.projectedSourceCount(); }
    @Nullable
    public HumanoidPosePreset activeHumanoidPose() {
        if (projector == null || projector.settings().sourceMode() != ProjectionSettings.SourceMode.ENTITY) return null;
        if (projector.entityProjectionState().hasActiveEntity()
                && projector.entityProjectionState().activeKind() != EntityScanData.Kind.HUMANOID) return null;
        return projector.entityProjectionState().humanoidPose();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack current = slot.getItem();
        ItemStack copy = current.copy();
        if (index == CORE_SLOT_INDEX) {
            if (!moveItemStackTo(current, FIRST_PLAYER_SLOT_INDEX, slots.size(), true)) return ItemStack.EMPTY;
        } else if (ProjectionCoreProfile.isCoreItem(current)) {
            if (!moveItemStackTo(current, CORE_SLOT_INDEX, CORE_SLOT_INDEX + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (current.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(projectorPos.getX() + 0.5D, projectorPos.getY() + 0.5D, projectorPos.getZ() + 0.5D) <= 64.0D;
    }
}

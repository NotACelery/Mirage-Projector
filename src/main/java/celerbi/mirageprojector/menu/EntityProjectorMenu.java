package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EquipmentSnapshotRules;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Dedicated backend for Entity/Humanoid projection editing.
 *
 * <p>Only physical scan cards and physical staging sources are vanilla menu
 * slots. Incoming card snapshots and projected snapshots remain render-only
 * data and are drawn/edited by explicit GUI actions.</p>
 */
public final class EntityProjectorMenu extends AbstractContainerMenu {
    public static final int CARD_X = 28;
    public static final int CARD_Y = 59;
    public static final int STAGING_X = 28;
    public static final int HUMANOID_FIRST_Y = 126;
    public static final int ROW_STEP = 22;
    public static final int HORSE_FIRST_Y = 126;
    public static final int PLAYER_INV_X = 204;
    public static final int PLAYER_INV_Y = 350;

    public static final int CARD_SLOT_INDEX = 0;
    public static final int HUMANOID_FIRST_SLOT_INDEX = 1;
    public static final int HORSE_FIRST_SLOT_INDEX = 7;
    public static final int FIRST_PLAYER_SLOT_INDEX = 9;

    private final BlockPos projectorPos;
    @Nullable
    private final MirageProjectorBlockEntity projector;
    private final EntityProjectionState fallbackState = new EntityProjectionState();

    public EntityProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.ENTITY_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;

        ItemStackHandler card = projector == null ? new ItemStackHandler(1) : projector.entityScanCard();
        ItemStackHandler humanoid = projector == null
                ? new ItemStackHandler(EntityScanData.HUMANOID_SLOTS.length)
                : projector.humanoidStagingItems();
        ItemStackHandler horse = projector == null
                ? new ItemStackHandler(EntityScanData.HORSE_CHANNELS.length)
                : projector.horseStagingItems();

        addCardSlot(card);
        addHumanoidStaging(humanoid);
        addHorseStaging(horse);
        addPlayerInventory(inventory);
    }

    public EntityProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.ENTITY_PROJECTOR.get(), containerId);
        this.projectorPos = projector.getBlockPos();
        this.projector = projector;
        addCardSlot(projector.entityScanCard());
        addHumanoidStaging(projector.humanoidStagingItems());
        addHorseStaging(projector.horseStagingItems());
        addPlayerInventory(inventory);
    }

    private void addCardSlot(ItemStackHandler handler) {
        addSlot(new SlotItemHandler(handler, 0, CARD_X, CARD_Y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return EntityScanData.hasScan(stack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return projector == null
                        || projector.stagedEntityCardKind() != EntityScanData.Kind.HORSE
                        || !projector.hasPhysicalHorseStaging();
            }
        });
    }

    private void addHumanoidStaging(ItemStackHandler handler) {
        for (int row = 0; row < EntityScanData.HUMANOID_SLOTS.length; row++) {
            int slotIndex = row;
            addSlot(new SlotItemHandler(handler, row, STAGING_X, HUMANOID_FIRST_Y + row * ROW_STEP) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return effectiveKind() == EntityScanData.Kind.HUMANOID;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    EquipmentSlot target = EntityScanData.HUMANOID_SLOTS[slotIndex];
                    if (target == EquipmentSlot.MAINHAND || target == EquipmentSlot.OFFHAND) {
                        return !stack.isEmpty();
                    }
                    return EquipmentSnapshotRules.fitsHumanoid(stack, target);
                }
            });
        }
    }

    private void addHorseStaging(ItemStackHandler handler) {
        for (int row = 0; row < EntityScanData.HORSE_CHANNELS.length; row++) {
            int slotIndex = row;
            addSlot(new SlotItemHandler(handler, row, STAGING_X, HORSE_FIRST_Y + row * ROW_STEP) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return effectiveKind() == EntityScanData.Kind.HORSE;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return EquipmentSnapshotRules.fitsHorse(
                            stack,
                            EntityScanData.HORSE_CHANNELS[slotIndex]
                    );
                }
            });
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        inventory,
                        col + row * 9 + 9,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18
                ));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(
                    inventory,
                    col,
                    PLAYER_INV_X + col * 18,
                    PLAYER_INV_Y + 58
            ));
        }
    }

    public BlockPos projectorPos() {
        return projectorPos;
    }

    @Nullable
    public MirageProjectorBlockEntity projector() {
        return projector;
    }

    public EntityProjectionState state() {
        return projector == null ? fallbackState : projector.entityProjectionState();
    }

    public ItemStack cardStack() {
        return getSlot(CARD_SLOT_INDEX).getItem();
    }

    public boolean supportsGenericSittingPose() {
        return state().activeEntity()
                .map(EntityScanData.View::entityType)
                .map(EntityScanData::supportsSittingPose)
                .orElse(false);
    }

    public EntityScanData.Kind effectiveKind() {
        if (projector != null && !projector.stagedEntityCard().isEmpty()) {
            return projector.stagedEntityCardKind();
        }
        // Empty Entity workspace intentionally falls back to Humanoid so the six
        // mannequin channels remain usable without requiring a body scan card.
        return EntityScanData.Kind.HUMANOID;
    }

    public ItemStack physicalStaging(VirtualEquipmentSnapshots.Channel channel) {
        int slotIndex = switch (channel) {
            case HEAD -> HUMANOID_FIRST_SLOT_INDEX;
            case CHEST -> HUMANOID_FIRST_SLOT_INDEX + 1;
            case LEGS -> HUMANOID_FIRST_SLOT_INDEX + 2;
            case FEET -> HUMANOID_FIRST_SLOT_INDEX + 3;
            case MAIN_HAND -> HUMANOID_FIRST_SLOT_INDEX + 4;
            case OFF_HAND -> HUMANOID_FIRST_SLOT_INDEX + 5;
            case SADDLE -> HORSE_FIRST_SLOT_INDEX;
            case BODY -> HORSE_FIRST_SLOT_INDEX + 1;
        };
        return getSlot(slotIndex).getItem();
    }

    public VirtualEquipmentSnapshots.Snapshot incoming(VirtualEquipmentSnapshots.Channel channel) {
        ItemStack physical = physicalStaging(channel);
        if (!physical.isEmpty()) {
            return new VirtualEquipmentSnapshots.Snapshot(new java.util.UUID(0L, 0L), physical.copyWithCount(1));
        }
        return channel.humanoid()
                ? state().humanoidIncoming().get(channel)
                : state().horseIncoming().get(channel);
    }

    public VirtualEquipmentSnapshots.Snapshot projected(VirtualEquipmentSnapshots.Channel channel) {
        return channel.humanoid()
                ? state().humanoidProjected().get(channel)
                : state().horseProjected().get(channel);
    }

    /**
     * Whether the Incoming side has a distinct action left to perform. Physical
     * staging always counts so an identical real item can still be accepted and
     * returned immediately; duplicate virtual snapshots do not keep the left
     * rail visually occupied after they are already Projected.
     */
    public boolean hasActionableIncoming(VirtualEquipmentSnapshots.Channel channel) {
        ItemStack physical = physicalStaging(channel);
        if (!physical.isEmpty()) {
            return true;
        }

        VirtualEquipmentSnapshots.Snapshot incoming = incoming(channel);
        if (incoming.stack().isEmpty()) {
            return false;
        }
        VirtualEquipmentSnapshots.Snapshot projected = projected(channel);
        return projected.stack().isEmpty()
                || !ItemStack.matches(incoming.stack(), projected.stack());
    }

    public boolean hasConflict(VirtualEquipmentSnapshots.Channel channel) {
        if (!hasActionableIncoming(channel)) {
            return false;
        }
        VirtualEquipmentSnapshots.Snapshot incoming = incoming(channel);
        VirtualEquipmentSnapshots.Snapshot projected = projected(channel);
        return !projected.stack().isEmpty()
                && !ItemStack.matches(incoming.stack(), projected.stack());
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
        ItemStack copy = current.copy();
        if (index == CARD_SLOT_INDEX && !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        if (index < FIRST_PLAYER_SLOT_INDEX) {
            if (!moveItemStackTo(current, FIRST_PLAYER_SLOT_INDEX, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (EntityScanData.hasScan(current) && getSlot(CARD_SLOT_INDEX).getItem().isEmpty()) {
            if (!moveItemStackTo(current, CARD_SLOT_INDEX, CARD_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int destination = preferredStagingSlot(current);
            if (destination < 0 || !moveItemStackTo(current, destination, destination + 1, false)) {
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

    private int preferredStagingSlot(ItemStack stack) {
        if (effectiveKind() == EntityScanData.Kind.HORSE) {
            if (EquipmentSnapshotRules.fitsHorse(stack, VirtualEquipmentSnapshots.Channel.SADDLE)) {
                return HORSE_FIRST_SLOT_INDEX;
            }
            if (EquipmentSnapshotRules.fitsHorse(stack, VirtualEquipmentSnapshots.Channel.BODY)) {
                return HORSE_FIRST_SLOT_INDEX + 1;
            }
            return -1;
        }
        if (effectiveKind() != EntityScanData.Kind.HUMANOID) {
            return -1;
        }

        EquipmentSlot equipmentSlot = EquipmentSnapshotRules.equipmentSlot(stack);
        if (equipmentSlot == null) {
            return preferredHandSlot();
        }
        return switch (equipmentSlot) {
            case HEAD -> HUMANOID_FIRST_SLOT_INDEX;
            case CHEST -> HUMANOID_FIRST_SLOT_INDEX + 1;
            case LEGS -> HUMANOID_FIRST_SLOT_INDEX + 2;
            case FEET -> HUMANOID_FIRST_SLOT_INDEX + 3;
            case MAINHAND, OFFHAND, BODY -> preferredHandSlot();
        };
    }

    private int preferredHandSlot() {
        return getSlot(HUMANOID_FIRST_SLOT_INDEX + 4).hasItem()
                ? HUMANOID_FIRST_SLOT_INDEX + 5
                : HUMANOID_FIRST_SLOT_INDEX + 4;
    }


    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && projector != null) {
            // Leaving the workspace must never leave real staging items trapped
            // inside Mirage. Inventory overflow is dropped by the BlockEntity.
            projector.returnPhysicalStagingTo(player);
        }
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

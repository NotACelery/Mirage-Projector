package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.compat.EasyMobFarmCompat;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.registry.ModMenus;
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

/**
 * Transient extension slots for a Scan Codex mounted in a vanilla lectern.
 * The lectern still owns the Codex itself; these slots only exist while the UI is open.
 */
public final class ScanCodexMenu extends AbstractContainerMenu {
    public static final int DUPLICATE_SLOT = 0;
    public static final int IMPORT_SLOT = 1;
    public static final int EXTENSION_SLOT_COUNT = 2;

    // Relative to the screen origin. The screen renders side-page extensions around these anchors.
    public static final int DUPLICATE_SLOT_X = 500;
    public static final int DUPLICATE_SLOT_Y = 172;
    public static final int IMPORT_SLOT_X = 500;
    public static final int IMPORT_SLOT_Y = 93;

    // Center the 9-slot inventory against the physical Codex pages, not the optional side extensions.
    public static final int PLAYER_INV_X = 163;
    public static final int PLAYER_INV_Y = 337;

    private final Inventory playerInventory;
    private final boolean lecternMode;
    private final BlockPos lecternPos;
    private final UUID codexId;
    private final boolean easyMobFarmAvailable;
    private final SimpleContainer extensionSlots = new SimpleContainer(EXTENSION_SLOT_COUNT);

    // Client presentation flags. They deliberately do not need network sync; the server validates contents/actions.
    private boolean duplicatePanelOpen;
    private boolean importPanelOpen;

    public ScanCodexMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                inventory,
                buffer.readBoolean(),
                buffer.readBlockPos(),
                buffer.readUUID(),
                buffer.readBoolean()
        );
    }

    public ScanCodexMenu(
            int containerId,
            Inventory inventory,
            boolean lecternMode,
            BlockPos lecternPos,
            UUID codexId,
            boolean easyMobFarmAvailable
    ) {
        super(ModMenus.SCAN_CODEX.get(), containerId);
        this.playerInventory = inventory;
        this.lecternMode = lecternMode;
        this.lecternPos = lecternPos.immutable();
        this.codexId = codexId;
        this.easyMobFarmAvailable = easyMobFarmAvailable;

        addSlot(new Slot(extensionSlots, DUPLICATE_SLOT, DUPLICATE_SLOT_X, DUPLICATE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.ENTITY_SCAN_CARD.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return lecternMode && (!playerInventory.player.level().isClientSide || duplicatePanelOpen);
            }
        });

        addSlot(new Slot(extensionSlots, IMPORT_SLOT, IMPORT_SLOT_X, IMPORT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return (stack.is(ModItems.ENTITY_SCAN_CARD.get()) && EntityScanData.hasScan(stack))
                        || EasyMobFarmCompat.isCaptureCard(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return lecternMode && (!playerInventory.player.level().isClientSide || importPanelOpen);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18) {
                    @Override
                    public boolean isActive() {
                        return lecternMode;
                    }
                });
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col,
                    PLAYER_INV_X + col * 18,
                    PLAYER_INV_Y + 58) {
                @Override
                public boolean isActive() {
                    return lecternMode;
                }
            });
        }
    }

    public static void openHandheld(ServerPlayer player, ItemStack codex) {
        if (player == null || codex == null || !codex.is(ModItems.SCAN_CODEX.get())) {
            return;
        }
        UUID id = ScanCodexItem.ensureCodexId(codex);
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new ScanCodexMenu(
                        containerId,
                        inventory,
                        false,
                        BlockPos.ZERO,
                        id,
                        false
                ),
                Component.translatable("gui.mirage_projector.scan_codex.title")
        );
        ((IPlayerExtension) player).openMenu(provider, buffer -> {
            buffer.writeBoolean(false);
            buffer.writeBlockPos(BlockPos.ZERO);
            buffer.writeUUID(id);
            buffer.writeBoolean(false);
        });
    }

    public boolean lecternMode() {
        return lecternMode;
    }

    public BlockPos lecternPos() {
        return lecternPos;
    }

    public UUID codexId() {
        return codexId;
    }

    public boolean easyMobFarmAvailable() {
        return easyMobFarmAvailable;
    }

    public void setDuplicatePanelOpen(boolean open) {
        duplicatePanelOpen = open;
    }

    public void setImportPanelOpen(boolean open) {
        importPanelOpen = lecternMode && open;
    }

    public ItemStack duplicateCard() {
        return extensionSlots.getItem(DUPLICATE_SLOT);
    }

    public ItemStack importCard() {
        return extensionSlots.getItem(IMPORT_SLOT);
    }

    public void setDuplicateCard(ItemStack stack) {
        extensionSlots.setItem(DUPLICATE_SLOT, stack == null ? ItemStack.EMPTY : stack);
        broadcastChanges();
    }

    public void setImportCard(ItemStack stack) {
        extensionSlots.setItem(IMPORT_SLOT, stack == null ? ItemStack.EMPTY : stack);
        broadcastChanges();
    }

    public void returnDuplicateCard(ServerPlayer player) {
        returnExtensionSlot(player, DUPLICATE_SLOT);
    }

    public void returnImportCard(ServerPlayer player) {
        returnExtensionSlot(player, IMPORT_SLOT);
    }

    private void returnExtensionSlot(ServerPlayer player, int slotIndex) {
        if (player == null || slotIndex < 0 || slotIndex >= EXTENSION_SLOT_COUNT) {
            return;
        }
        ItemStack stack = extensionSlots.removeItemNoUpdate(slotIndex);
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        if (!lecternMode) {
            return player instanceof ServerPlayer serverPlayer
                    ? !ScanCodexItem.findOwnedCodex(serverPlayer, codexId).isEmpty()
                    : player != null;
        }
        if (player == null || player.distanceToSqr(
                lecternPos.getX() + 0.5D,
                lecternPos.getY() + 0.5D,
                lecternPos.getZ() + 0.5D
        ) > 64.0D) {
            return false;
        }
        if (!(player.level().getBlockState(lecternPos).getBlock() instanceof LecternBlock)
                || !(player.level().getBlockEntity(lecternPos) instanceof LecternBlockEntity lectern)) {
            return false;
        }
        ItemStack codex = lectern.getBook();
        return codex.is(ModItems.SCAN_CODEX.get()) && codexId.equals(ScanCodexItem.codexId(codex));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!lecternMode) {
            return ItemStack.EMPTY;
        }
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();

        if (index < EXTENSION_SLOT_COUNT) {
            if (!moveItemStackTo(source, EXTENSION_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Side-page slots only exist when their page is visibly open on the client.
            // Do not let shift-click silently route items into an extension that may currently be hidden.
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (lecternMode && !player.level().isClientSide) {
            clearContainer(player, extensionSlots);
        }
    }

    public static ScanCodexMenu current(ServerPlayer player, BlockPos pos, UUID codexId) {
        if (player != null
                && player.containerMenu instanceof ScanCodexMenu menu
                && menu.lecternMode
                && menu.lecternPos.equals(pos)
                && menu.codexId.equals(codexId)) {
            return menu;
        }
        return null;
    }
}

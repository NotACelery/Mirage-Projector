package celerbi.mirageprojector.equipment;

import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.item.ShoulderMountableDevice;
import celerbi.mirageprojector.item.ShoulderRechargeableDevice;
import celerbi.mirageprojector.item.ShoulderUpgrade;
import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import celerbi.mirageprojector.network.ShoulderEquipmentInventoryPayload;
import celerbi.mirageprojector.network.ShoulderEquipmentCursorPayload;
import celerbi.mirageprojector.network.ShoulderEquipmentStatePayload;
import celerbi.mirageprojector.registry.ModAttachments;
import celerbi.mirageprojector.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-authoritative manipulation, ticking, pouch automation and publication for shoulder equipment. */
public final class ShoulderEquipmentRuntime {
    private ShoulderEquipmentRuntime() {
    }

    public static ShoulderEquipment get(net.minecraft.world.entity.player.Player player) {
        return player.getData(ModAttachments.SHOULDER_EQUIPMENT);
    }

    public static void tick(ServerPlayer player) {
        ShoulderEquipment equipment = get(player);
        sanitize(player, equipment);

        ItemStack device = equipment.device();
        if (!device.isEmpty() && device.getItem() instanceof ShoulderMountableDevice mountable) {
            mountable.serverTickShoulder(device, player);
            // Device state is nested inside the strap. Rewriting the slot commits any component
            // mutations performed by the device tick to the strap's container component.
            equipment.setDevice(device);
        }

        if (player.level().getGameTime() % 20L == 0L) {
            tryAutoBatterySwap(player, equipment);
            broadcast(player, equipment);
            syncOwnerInventory(player, equipment);
        }
    }

    public static void handleInventoryClick(
            ServerPlayer player,
            ShoulderEquipmentActionPayload.Target target,
            ItemStack clientCarried
    ) {
        if (player.containerMenu == null || target == null) {
            return;
        }
        ShoulderEquipment equipment = get(player);
        // CreativeModeInventoryScreen owns a client-side picker menu, so its cursor stack is not
        // guaranteed to exist in the server container. Creative players are already allowed to
        // create arbitrary stacks; use the visible client cursor snapshot there. Survival keeps
        // using the authoritative server cursor and ignores the packet snapshot.
        ItemStack carried = player.isCreative()
                ? (clientCarried == null ? ItemStack.EMPTY : clientCarried.copy())
                : player.containerMenu.getCarried();
        if (player.isCreative()) {
            player.containerMenu.setCarried(carried.copy());
        }
        boolean changed;
        if (target == ShoulderEquipmentActionPayload.Target.STRAP) {
            changed = interactStrap(player, equipment, carried);
        } else if (target == ShoulderEquipmentActionPayload.Target.DEVICE) {
            changed = interactDevice(player, equipment, carried);
        } else if (target.battery()) {
            changed = interactBattery(player, equipment, target.batteryIndex(), carried);
        } else if (target.upgrade()) {
            changed = interactUpgrade(player, equipment, target.upgradeIndex(), carried);
        } else {
            changed = false;
        }

        if (changed) {
            player.containerMenu.broadcastChanges();
            broadcast(player, equipment);
            syncOwnerInventory(player, equipment);
        }
        // The Mirage Equipment panel sits outside the vanilla container bounds. Always correct
        // the client cursor explicitly so Creative inventory interaction behaves like a real slot
        // rather than dropping the carried item outside the GUI.
        PacketDistributor.sendToPlayer(player, new ShoulderEquipmentCursorPayload(
                player.containerMenu.getCarried().copy()
        ));
    }

    public static void broadcast(ServerPlayer owner, ShoulderEquipment equipment) {
        ShoulderEquipmentStatePayload payload = new ShoulderEquipmentStatePayload(
                owner.getUUID(),
                equipment.hasStrap(),
                equipment.device()
        );
        if (owner.getServer() == null) {
            return;
        }
        for (ServerPlayer target : owner.getServer().getPlayerList().getPlayers()) {
            if (target.level().dimension().equals(owner.level().dimension())) {
                PacketDistributor.sendToPlayer(target, payload);
            }
        }
    }

    public static void syncOwnerInventory(ServerPlayer owner, ShoulderEquipment equipment) {
        List<ItemStack> batteries = new ArrayList<>(ShoulderEquipment.EXPANDED_BATTERY_SLOTS);
        for (int i = 0; i < ShoulderEquipment.EXPANDED_BATTERY_SLOTS; i++) {
            batteries.add(equipment.battery(i).copy());
        }
        List<ItemStack> upgrades = new ArrayList<>(ShoulderEquipment.EXPANDED_UPGRADE_SLOTS);
        for (int i = 0; i < ShoulderEquipment.EXPANDED_UPGRADE_SLOTS; i++) {
            upgrades.add(equipment.upgrade(i).copy());
        }
        PacketDistributor.sendToPlayer(owner, new ShoulderEquipmentInventoryPayload(
                owner.getUUID(),
                batteries,
                upgrades
        ));
    }

    private static boolean interactStrap(ServerPlayer player, ShoulderEquipment equipment, ItemStack carried) {
        ItemStack installed = equipment.strap();
        if (carried.isEmpty()) {
            if (installed.isEmpty()) {
                return false;
            }
            if (installed.is(ModItems.SHOULDER_STRAP.get()) && !equipment.canRemoveStrap()) {
                player.displayClientMessage(Component.translatable(
                        "message.mirage_projector.shoulder.strap_device_blocked"
                ), true);
                return false;
            }
            ItemStack removed = equipment.extractItem(ShoulderEquipment.STRAP_SLOT, 1, false);
            player.containerMenu.setCarried(removed);
            return !removed.isEmpty();
        }

        boolean validBaseSlotItem = carried.is(ModItems.SHOULDER_STRAP.get())
                || carried.getItem() instanceof ShoulderMountableDevice;
        if (!validBaseSlotItem || !installed.isEmpty() || carried.getCount() != 1) {
            return false;
        }
        ItemStack inserted = carried.copyWithCount(1);
        ItemStack remainder = equipment.insertItem(ShoulderEquipment.STRAP_SLOT, inserted, false);
        if (!remainder.isEmpty()) {
            return false;
        }
        carried.shrink(1);
        player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
        return true;
    }

    private static boolean interactDevice(ServerPlayer player, ShoulderEquipment equipment, ItemStack carried) {
        if (!equipment.hasStrap()) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.requires_strap"
            ), true);
            return false;
        }

        ItemStack installed = equipment.device();
        if (carried.isEmpty()) {
            if (installed.isEmpty()) {
                return false;
            }
            ItemStack removed = equipment.extractDevice();
            player.containerMenu.setCarried(removed);
            return !removed.isEmpty();
        }

        if (!(carried.getItem() instanceof ShoulderMountableDevice mountable)
                || !mountable.canMountOnShoulder(carried, player)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.invalid_device"
            ), true);
            return false;
        }
        if (!player.getShoulderEntityRight().isEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.vanilla_occupied"
            ), true);
            return false;
        }
        if (carried.getCount() != 1) {
            return false;
        }

        ItemStack incoming = carried.copyWithCount(1);
        equipment.setDevice(incoming);
        if (installed.isEmpty()) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        } else {
            // Preserve vanilla swap semantics: the old device remains on the cursor.
            player.containerMenu.setCarried(installed.copy());
        }
        return true;
    }

    private static boolean interactBattery(
            ServerPlayer player,
            ShoulderEquipment equipment,
            int index,
            ItemStack carried
    ) {
        if (!equipment.hasStrap()) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.requires_strap"
            ), true);
            return false;
        }
        if (index < 0 || index >= equipment.activeBatterySlots()) {
            return false;
        }
        ItemStack installed = equipment.battery(index);

        if (carried.isEmpty()) {
            if (installed.isEmpty()) {
                return false;
            }
            ItemStack removed = equipment.extractBattery(index, installed.getCount());
            player.containerMenu.setCarried(removed);
            return !removed.isEmpty();
        }
        if (!RechargeableEnergyItem.isRechargeable(carried)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.invalid_battery"
            ), true);
            return false;
        }

        if (installed.isEmpty() || ItemStack.isSameItemSameComponents(installed, carried)) {
            ItemStack remainder = equipment.insertBattery(index, carried.copy(), false);
            int moved = carried.getCount() - remainder.getCount();
            if (moved <= 0) {
                return false;
            }
            carried.shrink(moved);
            player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            return true;
        }

        if (carried.getCount() > carried.getMaxStackSize()) {
            return false;
        }
        equipment.setBattery(index, carried.copy());
        player.containerMenu.setCarried(installed.copy());
        return true;
    }

    private static boolean interactUpgrade(
            ServerPlayer player,
            ShoulderEquipment equipment,
            int index,
            ItemStack carried
    ) {
        if (!equipment.hasStrap()) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.requires_strap"
            ), true);
            return false;
        }
        if (index < 0 || index >= equipment.activeUpgradeSlots()) {
            return false;
        }

        ItemStack installed = equipment.upgrade(index);
        if (carried.isEmpty()) {
            if (installed.isEmpty()) {
                return false;
            }
            ItemStack removed = equipment.extractUpgrade(index);
            if (removed.isEmpty()) {
                player.displayClientMessage(Component.translatable(
                        "message.mirage_projector.shoulder.expansion_in_use"
                ), true);
                return false;
            }
            player.containerMenu.setCarried(removed);
            return true;
        }

        if (!(carried.getItem() instanceof ShoulderUpgrade)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.invalid_upgrade"
            ), true);
            return false;
        }
        if (carried.getCount() != 1 || !equipment.canPlaceUpgrade(index, carried)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.duplicate_upgrade"
            ), true);
            return false;
        }

        if (!installed.isEmpty() && isExpansion(installed) && !equipment.expansionDependentSlotsEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.shoulder.expansion_in_use"
            ), true);
            return false;
        }

        equipment.setUpgrade(index, carried.copyWithCount(1));
        player.containerMenu.setCarried(installed.isEmpty() ? ItemStack.EMPTY : installed.copy());
        return true;
    }

    private static boolean tryAutoBatterySwap(ServerPlayer player, ShoulderEquipment equipment) {
        if (!equipment.hasStrap() || !equipment.hasAutoBatterySwapPatch()) {
            return false;
        }
        ItemStack deviceStack = equipment.device();
        if (!(deviceStack.getItem() instanceof ShoulderRechargeableDevice device)) {
            return false;
        }

        ItemStack oldCell = device.shoulderEnergyCell(deviceStack, player.level().registryAccess());
        RechargeableEnergyItem oldEnergy = RechargeableEnergyItem.fromStack(oldCell);
        if (oldEnergy == null || !oldEnergy.depleted(oldCell)) {
            return false;
        }

        int candidateIndex = bestChargedBatterySlot(equipment);
        if (candidateIndex < 0 || !canStoreOldCellAfterCandidateRemoval(equipment, candidateIndex, oldCell)) {
            return false;
        }

        ItemStack candidate = equipment.extractBattery(candidateIndex, 1);
        if (candidate.isEmpty()) {
            return false;
        }

        ItemStack extractedOld = device.shoulderExtractEnergyCell(deviceStack, player.level().registryAccess());
        if (extractedOld.isEmpty()) {
            equipment.insertBattery(candidateIndex, candidate, false);
            return false;
        }

        if (!device.shoulderInsertEnergyCell(deviceStack, candidate, player.level().registryAccess())) {
            device.shoulderInsertEnergyCell(deviceStack, extractedOld, player.level().registryAccess());
            equipment.insertBattery(candidateIndex, candidate, false);
            return false;
        }

        ItemStack remainder = insertIntoBatteryPouch(equipment, extractedOld);
        if (!remainder.isEmpty()) {
            ItemStack rollbackCandidate = device.shoulderExtractEnergyCell(deviceStack, player.level().registryAccess());
            device.shoulderInsertEnergyCell(deviceStack, extractedOld, player.level().registryAccess());
            insertIntoBatteryPouch(equipment, rollbackCandidate);
            return false;
        }

        equipment.setDevice(deviceStack);
        return true;
    }

    private static int bestChargedBatterySlot(ShoulderEquipment equipment) {
        int bestIndex = -1;
        int bestCharge = -1;
        int bestCapacity = -1;
        for (int i = 0; i < equipment.activeBatterySlots(); i++) {
            ItemStack stack = equipment.battery(i);
            RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(stack);
            if (energy == null) {
                continue;
            }
            int charge = energy.storedCharge(stack);
            if (charge <= 0) {
                continue;
            }
            int capacity = energy.maxCharge();
            if (charge > bestCharge || (charge == bestCharge && capacity > bestCapacity)) {
                bestIndex = i;
                bestCharge = charge;
                bestCapacity = capacity;
            }
        }
        return bestIndex;
    }

    private static boolean canStoreOldCellAfterCandidateRemoval(
            ShoulderEquipment equipment,
            int candidateIndex,
            ItemStack oldCell
    ) {
        for (int i = 0; i < equipment.activeBatterySlots(); i++) {
            ItemStack simulated = equipment.battery(i).copy();
            if (i == candidateIndex && !simulated.isEmpty()) {
                simulated.shrink(1);
            }
            if (simulated.isEmpty()) {
                return true;
            }
            int limit = simulated.getMaxStackSize();
            if (ItemStack.isSameItemSameComponents(simulated, oldCell) && simulated.getCount() < limit) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack insertIntoBatteryPouch(ShoulderEquipment equipment, ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int i = 0; i < equipment.activeBatterySlots() && !remainder.isEmpty(); i++) {
            remainder = equipment.insertBattery(i, remainder, false);
        }
        return remainder;
    }

    private static boolean isExpansion(ItemStack stack) {
        if (!(stack.getItem() instanceof ShoulderUpgrade upgrade)) {
            return false;
        }
        ResourceLocation family = upgrade.shoulderUpgradeFamily(stack);
        return ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION.equals(family);
    }

    /**
     * Legacy safety only: 1.0.18 normally cannot have expansion-only contents without the patch,
     * because both live inside the same strap ItemStack. If an older/corrupt stack does, return
     * those hidden items to the player rather than leaving inaccessible data behind.
     */
    private static void sanitize(ServerPlayer player, ShoulderEquipment equipment) {
        if (!equipment.hasStrap() || equipment.hasExpansionPatch()) {
            return;
        }
        for (int i = ShoulderEquipment.BASE_BATTERY_SLOTS; i < ShoulderEquipment.EXPANDED_BATTERY_SLOTS; i++) {
            returnBatteryToPlayer(player, equipment, i);
        }
        int extraUpgrade = ShoulderEquipment.EXPANDED_UPGRADE_SLOTS - 1;
        ItemStack hidden = equipment.upgrade(extraUpgrade);
        if (!hidden.isEmpty()) {
            equipment.setUpgrade(extraUpgrade, ItemStack.EMPTY);
            giveOrDrop(player, hidden);
        }
    }

    private static void returnBatteryToPlayer(ServerPlayer player, ShoulderEquipment equipment, int index) {
        ItemStack stack = equipment.battery(index);
        if (stack.isEmpty()) {
            return;
        }
        ItemStack moving = equipment.extractBattery(index, stack.getCount());
        giveOrDrop(player, moving);
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack moving = stack.copy();
        if (!player.getInventory().add(moving)) {
            player.drop(moving, false);
        }
    }
}

package celerbi.mirageprojector.energy;

import celerbi.mirageprojector.item.RechargeableEnergyItem;
import net.minecraft.world.item.ItemStack;

/**
 * Shared view of a Mirage block that charges one rechargeable stack from a Beacon beam.
 *
 * <p>The active stack is always a single physical cell/item. Queueing, outputs and other
 * inventory policy belong to the concrete machine.</p>
 */
public interface BeaconRechargeableCharger {
    ItemStack activeChargingStack();

    default boolean hasActiveChargingStack() {
        return RechargeableEnergyItem.isRechargeable(activeChargingStack());
    }

    default boolean activelyConsumesBeaconTransmission() {
        return hasActiveChargingStack() && !RechargeableEnergyItem.isFull(activeChargingStack());
    }
}

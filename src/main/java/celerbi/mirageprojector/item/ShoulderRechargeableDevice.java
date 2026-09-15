package celerbi.mirageprojector.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

/**
 * Shoulder-mountable device exposing one removable rechargeable cell to the Shoulder Strap runtime.
 *
 * <p>The strap owns battery-pouch automation, while the device remains authoritative for how its
 * embedded cell is serialized. This keeps auto-swap generic without teaching shoulder code about
 * Lantern/Hand Projector NBT keys.</p>
 */
public interface ShoulderRechargeableDevice extends ShoulderMountableDevice {
    ItemStack shoulderEnergyCell(ItemStack device, HolderLookup.Provider registries);

    boolean shoulderInsertEnergyCell(
            ItemStack device,
            ItemStack source,
            HolderLookup.Provider registries
    );

    ItemStack shoulderExtractEnergyCell(ItemStack device, HolderLookup.Provider registries);
}

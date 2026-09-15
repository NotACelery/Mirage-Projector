package celerbi.mirageprojector.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Generic upgrade contract for the Mirage Shoulder Strap upgrade sockets. */
public interface ShoulderUpgrade {
    ResourceLocation shoulderUpgradeFamily(ItemStack stack);
}

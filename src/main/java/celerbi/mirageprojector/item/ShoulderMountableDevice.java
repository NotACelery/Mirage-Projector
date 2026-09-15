package celerbi.mirageprojector.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Contract for Mirage devices that may live in the dedicated Shoulder Device slot.
 *
 * <p>The slot is intentionally separate from vanilla armor/offhand equipment. Implementations
 * receive a server tick while mounted because vanilla Player inventory ticking does not visit
 * items stored inside Mirage's player attachment.</p>
 */
public interface ShoulderMountableDevice {
    default boolean canMountOnShoulder(ItemStack stack, ServerPlayer player) {
        return stack != null && !stack.isEmpty();
    }

    default void serverTickShoulder(ItemStack stack, ServerPlayer player) {
    }
}

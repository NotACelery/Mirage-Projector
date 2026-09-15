package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.equipment.ShoulderEquipment;
import celerbi.mirageprojector.equipment.ShoulderEquipmentRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Location of a portable Mirage device whose configuration menu is open. */
public enum PortableDeviceSource {
    MAIN_HAND,
    OFF_HAND,
    SHOULDER;

    public ItemStack resolve(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        return switch (this) {
            case MAIN_HAND -> player.getMainHandItem();
            case OFF_HAND -> player.getOffhandItem();
            case SHOULDER -> player.level().isClientSide
                    ? ItemStack.EMPTY
                    : ShoulderEquipmentRuntime.get(player).device();
        };
    }

    public void commit(ServerPlayer player, ItemStack device) {
        if (player == null || device == null) {
            return;
        }
        switch (this) {
            case MAIN_HAND -> player.setItemInHand(InteractionHand.MAIN_HAND, device);
            case OFF_HAND -> player.setItemInHand(InteractionHand.OFF_HAND, device);
            case SHOULDER -> {
                ShoulderEquipment equipment = ShoulderEquipmentRuntime.get(player);
                equipment.setDevice(device);
                ShoulderEquipmentRuntime.broadcast(player, equipment);
            }
        }
        player.getInventory().setChanged();
    }

    public static PortableDeviceSource byOrdinal(int ordinal) {
        PortableDeviceSource[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MAIN_HAND;
    }
}

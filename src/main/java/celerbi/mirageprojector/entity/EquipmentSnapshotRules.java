package celerbi.mirageprojector.entity;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public final class EquipmentSnapshotRules {
    private EquipmentSnapshotRules() {
    }

    @Nullable
    public static EquipmentSlot equipmentSlot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot != null) {
            return slot;
        }
        Equipable equipable = Equipable.get(stack);
        return equipable == null ? EquipmentSlot.MAINHAND : equipable.getEquipmentSlot();
    }

    public static boolean fitsHumanoid(ItemStack stack, EquipmentSlot target) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (target == EquipmentSlot.MAINHAND || target == EquipmentSlot.OFFHAND) {
            return true;
        }
        return equipmentSlot(stack) == target;
    }

    public static boolean fitsHorse(ItemStack stack, VirtualEquipmentSnapshots.Channel channel) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (channel) {
            case SADDLE -> stack.is(Items.SADDLE);
            case BODY -> equipmentSlot(stack) == EquipmentSlot.BODY;
            default -> false;
        };
    }
}

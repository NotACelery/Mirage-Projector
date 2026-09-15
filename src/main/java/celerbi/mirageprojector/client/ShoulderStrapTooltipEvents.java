package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.equipment.ShoulderEquipment;
import celerbi.mirageprojector.equipment.ShoulderStrapContainer;
import celerbi.mirageprojector.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client-only shulker-style preview for packed Shoulder Strap contents. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT)
public final class ShoulderStrapTooltipEvents {
    private ShoulderStrapTooltipEvents() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack strap = event.getItemStack();
        if (strap == null || !strap.is(ModItems.SHOULDER_STRAP.get())) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.mirage_projector.shoulder_strap.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        ShoulderStrapContainer contents = new ShoulderStrapContainer(strap);
        List<ItemStack> visible = new ArrayList<>();
        ItemStack device = contents.getItem(ShoulderStrapContainer.DEVICE_SLOT);
        if (!device.isEmpty()) {
            visible.add(device);
        }
        int batterySlots = contents.hasExpansionPatch()
                ? ShoulderEquipment.EXPANDED_BATTERY_SLOTS
                : ShoulderEquipment.BASE_BATTERY_SLOTS;
        for (int i = 0; i < batterySlots; i++) {
            ItemStack cell = contents.getItem(ShoulderStrapContainer.BATTERY_START + i);
            if (!cell.isEmpty()) {
                visible.add(cell);
            }
        }
        int upgradeSlots = contents.hasExpansionPatch()
                ? ShoulderEquipment.EXPANDED_UPGRADE_SLOTS
                : ShoulderEquipment.BASE_UPGRADE_SLOTS;
        for (int i = 0; i < upgradeSlots; i++) {
            ItemStack upgrade = contents.getItem(ShoulderStrapContainer.UPGRADE_START + i);
            if (!upgrade.isEmpty()) {
                visible.add(upgrade);
            }
        }

        if (visible.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.mirage_projector.shoulder_strap.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        int shown = Math.min(6, visible.size());
        for (int i = 0; i < shown; i++) {
            ItemStack item = visible.get(i);
            Component line = item.getCount() > 1
                    ? Component.literal(item.getCount() + "x ").append(item.getHoverName())
                    : item.getHoverName();
            tooltip.add(line.copy().withStyle(ChatFormatting.GRAY));
        }
        if (visible.size() > shown) {
            tooltip.add(Component.translatable(
                    "tooltip.mirage_projector.shoulder_strap.more",
                    visible.size() - shown
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

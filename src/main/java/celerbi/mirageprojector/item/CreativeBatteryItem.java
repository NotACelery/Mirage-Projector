package celerbi.mirageprojector.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Creative/debug-only rechargeable medium with infinite effective charge.
 *
 * <p>It participates in the same RechargeableEnergyItem contract as Survival cells so device
 * code does not need a Creative-specific branch. Consumption reports success while intentionally
 * leaving the stack at full charge forever.</p>
 */
public final class CreativeBatteryItem extends Item implements RechargeableEnergyItem {
    public static final int MAX_CHARGE = 1_000_000;
    private static final String UNUSED_CHARGE_TAG = "MirageCreativeBatteryCharge";

    public CreativeBatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public int maxCharge() {
        return MAX_CHARGE;
    }

    @Override
    public String chargeDataKey() {
        return UNUSED_CHARGE_TAG;
    }

    @Override
    public int chargePerInterval() {
        return 0;
    }

    @Override
    public int storedCharge(ItemStack stack) {
        return MAX_CHARGE;
    }

    @Override
    public void setStoredCharge(ItemStack stack, int value) {
        // Intentionally immutable: this QA/admin cell never depletes.
    }

    @Override
    public int addStoredCharge(ItemStack stack, int amount) {
        return 0;
    }

    @Override
    public int consumeStoredCharge(ItemStack stack, int amount) {
        return Math.max(0, amount);
    }

    @Override
    public boolean depleted(ItemStack stack) {
        return false;
    }

    @Override
    public boolean full(ItemStack stack) {
        return true;
    }

    @Override
    public float storedChargeFraction(ItemStack stack) {
        return 1.0F;
    }

    @Override
    public int storedChargePercent(ItemStack stack) {
        return 100;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.creative_battery.infinite"
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.creative_battery.debug_only"
        ).withStyle(ChatFormatting.GRAY));
    }
}

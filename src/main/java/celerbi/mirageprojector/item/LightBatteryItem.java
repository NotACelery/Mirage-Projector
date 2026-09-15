package celerbi.mirageprojector.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Higher-capacity rechargeable medium for the 1.1 portable-device line.
 *
 * <p>Capacity is four times Glow Dust. At the current Core Booster cadence it
 * receives eight units per 10-tick pulse, taking roughly five times as long as
 * Glow Dust to recharge from empty to full.</p>
 */
public final class LightBatteryItem extends Item implements RechargeableEnergyItem {
    public static final int MAX_CHARGE = 4000;
    public static final int CHARGE_PER_INTERVAL = 8;
    private static final String CHARGE_TAG = "MirageLightBatteryCharge";

    public LightBatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public int maxCharge() {
        return MAX_CHARGE;
    }

    @Override
    public String chargeDataKey() {
        return CHARGE_TAG;
    }

    @Override
    public int chargePerInterval() {
        return CHARGE_PER_INTERVAL;
    }

    public static float chargeFraction(ItemStack stack) {
        RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(stack);
        return energy == null ? 0.0F : energy.storedChargeFraction(stack);
    }

    public static ItemStack depletedStack(Item item) {
        ItemStack stack = new ItemStack(item);
        RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(stack);
        if (energy != null) {
            energy.setStoredCharge(stack, 0);
        }
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        return depleted(stack)
                ? Component.translatable("item.mirage_projector.light_battery.depleted")
                : super.getName(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        int charge = storedCharge(stack);
        int percent = storedChargePercent(stack);
        if (charge <= 0) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.light_battery.discharged"
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.light_battery.charge",
                    percent
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !full(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * storedChargeFraction(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = storedChargeFraction(stack);
        int red = Math.round(Mth.lerp(fraction, 74.0F, 184.0F));
        int green = Math.round(Mth.lerp(fraction, 68.0F, 122.0F));
        int blue = Math.round(Mth.lerp(fraction, 86.0F, 255.0F));
        return (red << 16) | (green << 8) | blue;
    }
}

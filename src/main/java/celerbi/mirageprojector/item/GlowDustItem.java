package celerbi.mirageprojector.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/**
 * Rechargeable portable-energy medium used by the future 1.1 device family.
 *
 * <p>Charge is stored on the individual stack through CUSTOM_DATA. Fresh/default
 * Glow Dust is fully charged; discharged/partial stacks retain their charge state
 * when moved through inventories or the Core Booster charging cradle.</p>
 */
public final class GlowDustItem extends Item {
    public static final int MAX_CHARGE = 1000;
    private static final String CHARGE_TAG = "MirageGlowCharge";

    public GlowDustItem(Properties properties) {
        super(properties);
    }

    public static int charge(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag();
        if (!tag.contains(CHARGE_TAG)) {
            return MAX_CHARGE;
        }
        return Mth.clamp(tag.getInt(CHARGE_TAG), 0, MAX_CHARGE);
    }

    public static void setCharge(ItemStack stack, int value) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int clamped = Mth.clamp(value, 0, MAX_CHARGE);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (clamped >= MAX_CHARGE) {
                tag.remove(CHARGE_TAG);
            } else {
                tag.putInt(CHARGE_TAG, clamped);
            }
        });
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    public static int addCharge(ItemStack stack, int amount) {
        if (amount <= 0 || stack == null || stack.isEmpty()) {
            return 0;
        }
        int before = charge(stack);
        int after = Mth.clamp(before + amount, 0, MAX_CHARGE);
        setCharge(stack, after);
        return after - before;
    }

    public static int consumeCharge(ItemStack stack, int amount) {
        if (amount <= 0 || stack == null || stack.isEmpty()) {
            return 0;
        }
        int before = charge(stack);
        int consumed = Math.min(before, amount);
        setCharge(stack, before - consumed);
        return consumed;
    }

    public static boolean isDepleted(ItemStack stack) {
        return charge(stack) <= 0;
    }

    public static boolean isFull(ItemStack stack) {
        return charge(stack) >= MAX_CHARGE;
    }

    public static float chargeFraction(ItemStack stack) {
        return charge(stack) / (float) MAX_CHARGE;
    }

    public static int chargePercent(ItemStack stack) {
        return Math.round(chargeFraction(stack) * 100.0F);
    }

    public static ItemStack depletedStack(Item item) {
        ItemStack stack = new ItemStack(item);
        setCharge(stack, 0);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (isDepleted(stack)) {
            return Component.translatable("item.mirage_projector.glow_dust.depleted");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        int charge = charge(stack);
        int percent = chargePercent(stack);
        if (charge <= 0) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.glow_dust.discharged"
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else if (charge >= MAX_CHARGE) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.glow_dust.full"
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.glow_dust.charge",
                    percent
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.glow_dust.recharge"
        ).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !isFull(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * chargeFraction(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = chargeFraction(stack);
        int red = Math.round(Mth.lerp(fraction, 92.0F, 190.0F));
        int green = Math.round(Mth.lerp(fraction, 69.0F, 114.0F));
        int blue = Math.round(Mth.lerp(fraction, 105.0F, 255.0F));
        return (red << 16) | (green << 8) | blue;
    }
}

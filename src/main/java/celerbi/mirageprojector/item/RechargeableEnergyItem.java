package celerbi.mirageprojector.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * Shared item-owned charge contract for Mirage rechargeable energy media.
 *
 * <p>The charge belongs to the ItemStack, not to the charger. Different media can
 * expose different capacities and charging rates while Core Boosters and future
 * charging stations keep one generic interaction path.</p>
 */
public interface RechargeableEnergyItem {
    int maxCharge();

    String chargeDataKey();

    /** Charge restored by one 10-tick Core Booster charging pulse. */
    int chargePerInterval();

    default int storedCharge(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag();
        if (!tag.contains(chargeDataKey())) {
            return maxCharge();
        }
        return Mth.clamp(tag.getInt(chargeDataKey()), 0, maxCharge());
    }

    default void setStoredCharge(ItemStack stack, int value) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int clamped = Mth.clamp(value, 0, maxCharge());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (clamped >= maxCharge()) {
                tag.remove(chargeDataKey());
            } else {
                tag.putInt(chargeDataKey(), clamped);
            }
        });
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    default int addStoredCharge(ItemStack stack, int amount) {
        if (amount <= 0 || stack == null || stack.isEmpty()) {
            return 0;
        }
        int before = storedCharge(stack);
        int after = Mth.clamp(before + amount, 0, maxCharge());
        setStoredCharge(stack, after);
        return after - before;
    }

    default int consumeStoredCharge(ItemStack stack, int amount) {
        if (amount <= 0 || stack == null || stack.isEmpty()) {
            return 0;
        }
        int before = storedCharge(stack);
        int consumed = Math.min(before, amount);
        setStoredCharge(stack, before - consumed);
        return consumed;
    }

    default boolean depleted(ItemStack stack) {
        return storedCharge(stack) <= 0;
    }

    default boolean full(ItemStack stack) {
        return storedCharge(stack) >= maxCharge();
    }

    default float storedChargeFraction(ItemStack stack) {
        return storedCharge(stack) / (float) Math.max(1, maxCharge());
    }

    default int storedChargePercent(ItemStack stack) {
        return Math.round(storedChargeFraction(stack) * 100.0F);
    }

    RechargeableEnergyItem VANILLA_GLOW_DUST = new RechargeableEnergyItem() {
        @Override public int maxCharge() { return GlowDustItem.MAX_CHARGE; }
        @Override public String chargeDataKey() { return "MirageVanillaGlowDustFull"; }
        @Override public int chargePerInterval() { return 0; }
        @Override public int storedCharge(ItemStack stack) { return GlowDustItem.MAX_CHARGE; }
        @Override public void setStoredCharge(ItemStack stack, int value) { }
        @Override public int addStoredCharge(ItemStack stack, int amount) { return 0; }
        @Override public int consumeStoredCharge(ItemStack stack, int amount) { return Math.max(0, amount); }
    };

    @Nullable
    static RechargeableEnergyItem fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (stack.is(Items.GLOWSTONE_DUST)) {
            return VANILLA_GLOW_DUST;
        }
        return stack.getItem() instanceof RechargeableEnergyItem energy ? energy : null;
    }

    /** Full vanilla Glowstone Dust becomes the rechargeable custom medium only while installed in a device. */
    static ItemStack normalizeForDevice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        if (!stack.is(Items.GLOWSTONE_DUST)) return stack.copyWithCount(1);
        ItemStack normalized = new ItemStack(ModItems.GLOW_DUST.get());
        GlowDustItem.setCharge(normalized, GlowDustItem.MAX_CHARGE);
        return normalized;
    }

    /** A fully recharged custom Glow Dust returns to vanilla material semantics. */
    static ItemStack normalizeFullyChargedOutput(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        if (stack.is(ModItems.GLOW_DUST.get()) && GlowDustItem.isFull(stack)) {
            return new ItemStack(Items.GLOWSTONE_DUST, stack.getCount());
        }
        return stack;
    }

    static boolean isRechargeable(ItemStack stack) {
        return fromStack(stack) != null;
    }

    static boolean isFull(ItemStack stack) {
        RechargeableEnergyItem energy = fromStack(stack);
        return energy == null || energy.full(stack);
    }

    static int chargePercent(ItemStack stack) {
        RechargeableEnergyItem energy = fromStack(stack);
        return energy == null ? 0 : energy.storedChargePercent(stack);
    }

    static int addCharge(ItemStack stack) {
        RechargeableEnergyItem energy = fromStack(stack);
        return energy == null ? 0 : energy.addStoredCharge(stack, energy.chargePerInterval());
    }
}

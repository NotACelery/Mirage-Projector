package celerbi.mirageprojector;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Non-PU activation providers for fixed Mirage chassis.
 *
 * <p>Special resonance is deliberately separate from {@link ProjectionCoreProfile}: catalysts
 * never become PU and must opt into an explicit chassis capability.</p>
 */
public enum SpecialResonanceProfile {
    NONE,
    END_RESONANCE;

    public boolean present() {
        return this != NONE;
    }

    public boolean supportedBy(ProjectionChassisProfile chassis) {
        return this == END_RESONANCE && chassis != null && chassis.supportsEndResonance();
    }

    public static SpecialResonanceProfile fromStack(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.is(Items.DRAGON_EGG)) {
            return END_RESONANCE;
        }
        return NONE;
    }
}

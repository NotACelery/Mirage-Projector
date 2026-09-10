package celerbi.mirageprojector;

import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Material profile for the removable Projection Core socket.
 *
 * <p>dev.38 power-system contract:</p>
 * <ul>
 *     <li>A Core contributes only a base PU output plus an amplification multiplier.</li>
 *     <li>A Core no longer owns hard Scale/Lift/Float caps.</li>
 *     <li>The chassis multiplies the Core output and supplies nominal geometry/efficiency targets.</li>
 *     <li>Improved crafted cores keep their material's base PU and raise only
 *     {@link #amplificationMultiplier()} (dev.46 initial balance: x1.50).</li>
 * </ul>
 *
 * <p>Raw-material cores are STANDARD grade at x1.00. dev.54 replaces the five
 * user-facing Improved Core items with one stateful Core Booster at x1.50.</p>
 */
public enum ProjectionCoreProfile {
    NONE(0, 0.0F),
    GLASS(32, 1.0F),
    QUARTZ(48, 1.0F),
    AMETHYST(64, 1.0F),
    DIAMOND(96, 1.0F),
    NETHERITE(128, 1.0F),
    IMPROVED_GLASS(32, 1.50F),
    IMPROVED_QUARTZ(48, 1.50F),
    IMPROVED_AMETHYST(64, 1.50F),
    IMPROVED_DIAMOND(96, 1.50F),
    IMPROVED_NETHERITE(128, 1.50F);

    private final int basePower;
    private final float amplificationMultiplier;

    ProjectionCoreProfile(int basePower, float amplificationMultiplier) {
        this.basePower = basePower;
        this.amplificationMultiplier = amplificationMultiplier;
    }

    public Component displayComponent() {
        return this == NONE
                ? Component.translatable("gui.mirage_projector.core.none")
                : Component.translatable("gui.mirage_projector.core.named",
                Component.translatable("gui.mirage_projector.core." + name().toLowerCase()));
    }

    /** Raw PU produced by the material before chassis efficiency/amplification are applied. */
    public int basePower() {
        return basePower;
    }

    /**
     * Core-grade multiplier. Standard raw-material cores are x1.00. dev.46 Improved
     * Cores use x1.50 while retaining the same material basePower.
     */
    public float amplificationMultiplier() {
        return amplificationMultiplier;
    }

    public boolean present() {
        return this != NONE;
    }

    public static ProjectionCoreProfile fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return NONE;
        }

        if (stack.is(ModItems.CORE_BOOSTER.get())) {
            return CoreBoosterBlockEntity.materialFromStack(stack).improvedProfile();
        }


        if (stack.is(Blocks.GLASS.asItem())) {
            return GLASS;
        }
        if (stack.is(Items.QUARTZ) || stack.is(Blocks.QUARTZ_BLOCK.asItem())) {
            return QUARTZ;
        }
        if (stack.is(Items.AMETHYST_SHARD) || stack.is(Blocks.AMETHYST_BLOCK.asItem())) {
            return AMETHYST;
        }
        if (stack.is(Items.DIAMOND) || stack.is(Blocks.DIAMOND_BLOCK.asItem())) {
            return DIAMOND;
        }
        if (stack.is(Items.NETHERITE_INGOT) || stack.is(Blocks.NETHERITE_BLOCK.asItem())) {
            return NETHERITE;
        }
        return NONE;
    }

    /** Effective output before chassis efficiency, useful for ordered UI lists. */
    public float materialOutput() {
        return basePower * amplificationMultiplier;
    }

    public boolean improved() {
        return amplificationMultiplier > 1.0F;
    }

    public static boolean isCoreItem(ItemStack stack) {
        return fromStack(stack).present();
    }
}

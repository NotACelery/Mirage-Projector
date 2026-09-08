package celerbi.mirageprojector;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Material profile for the removable Projection Core socket.
 *
 * <p>These numbers are intentionally provisional during the 0.1.0 development line.
 * The point of dev.8 is to establish the data/validation architecture so later chassis
 * can reuse the same core without duplicating BlockEntity/Menu types.</p>
 */
public enum ProjectionCoreProfile {
    NONE("None", 0, 0, 0, 0, 0),
    GLASS("Glass", 8, 10, 16, 1, 1),
    QUARTZ("Quartz", 16, 16, 32, 2, 1),
    AMETHYST("Amethyst", 48, 48, 64, 8, 2),
    DIAMOND("Diamond", 96, 80, 96, 16, 4),
    NETHERITE("Netherite", 192, 160, 160, 32, 8);

    private final String displayName;
    private final int power;
    private final int maxScalePixels;
    private final int maxLiftPixels;
    private final int maxFloatPixels;
    private final int futureSourceCapacity;

    ProjectionCoreProfile(
            String displayName,
            int power,
            int maxScalePixels,
            int maxLiftPixels,
            int maxFloatPixels,
            int futureSourceCapacity
    ) {
        this.displayName = displayName;
        this.power = power;
        this.maxScalePixels = maxScalePixels;
        this.maxLiftPixels = maxLiftPixels;
        this.maxFloatPixels = maxFloatPixels;
        this.futureSourceCapacity = futureSourceCapacity;
    }

    public String displayName() {
        return displayName;
    }

    public Component displayComponent() {
        return Component.literal(displayName + " Core");
    }

    public int power() {
        return power;
    }

    public int maxScalePixels() {
        return maxScalePixels;
    }

    public int maxLiftPixels() {
        return maxLiftPixels;
    }

    public int maxFloatPixels() {
        return maxFloatPixels;
    }

    /** Reserved for the multi-source/projector-chassis pass. */
    public int futureSourceCapacity() {
        return futureSourceCapacity;
    }

    public boolean present() {
        return this != NONE;
    }

    public static ProjectionCoreProfile fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return NONE;
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

    public static boolean isCoreItem(ItemStack stack) {
        return fromStack(stack).present();
    }

    /**
     * Full block visual used by the BER for the tiny 2x3x2-pixel core column.
     * The inserted raw item (quartz shard/diamond/etc.) is intentionally represented
     * as its material block so the pedestal remains readable from several blocks away.
     */
    public ItemStack visualStack() {
        return switch (this) {
            case GLASS -> new ItemStack(Blocks.GLASS);
            case QUARTZ -> new ItemStack(Blocks.QUARTZ_BLOCK);
            case AMETHYST -> new ItemStack(Blocks.AMETHYST_BLOCK);
            case DIAMOND -> new ItemStack(Blocks.DIAMOND_BLOCK);
            case NETHERITE -> new ItemStack(Blocks.NETHERITE_BLOCK);
            case NONE -> ItemStack.EMPTY;
        };
    }
}

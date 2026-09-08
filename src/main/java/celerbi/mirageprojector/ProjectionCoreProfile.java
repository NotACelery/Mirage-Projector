package celerbi.mirageprojector;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Material profile for the removable Projection Core socket.
 *
 * <p>These numbers are intentionally provisional during the 0.1.0 development line.
 * The data/validation architecture is shared by every chassis. dev.19 raises the provisional
 * power curve without allowing a Core to bypass the physical geometry limits of its chassis.</p>
 */
public enum ProjectionCoreProfile {
    NONE("None", 0, 0, 0, 0, 0),
    GLASS("Glass", 16, 16, 32, 2, 1),
    QUARTZ("Quartz", 32, 32, 64, 4, 1),
    AMETHYST("Amethyst", 96, 80, 96, 12, 4),
    DIAMOND("Diamond", 192, 128, 128, 24, 8),
    NETHERITE("Netherite", 384, 160, 160, 32, 16);

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
        return this == NONE
                ? Component.translatable("gui.mirage_projector.core.none")
                : Component.translatable("gui.mirage_projector.core.named", Component.translatable("gui.mirage_projector.core." + name().toLowerCase()));
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

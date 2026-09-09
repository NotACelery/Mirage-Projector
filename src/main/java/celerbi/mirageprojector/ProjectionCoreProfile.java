package celerbi.mirageprojector;

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
 *     <li>Future improved crafted cores should keep their material's base PU and raise only
 *     {@link #amplificationMultiplier()} (planned first target: roughly x1.50).</li>
 * </ul>
 *
 * <p>The current raw-material cores are all STANDARD grade, so their amplification is x1.00.
 * The field is deliberately present now so improved cores can be added without another power-model rewrite.</p>
 */
public enum ProjectionCoreProfile {
    NONE(0, 0.0F),
    GLASS(32, 1.0F),
    QUARTZ(48, 1.0F),
    AMETHYST(64, 1.0F),
    DIAMOND(96, 1.0F),
    NETHERITE(128, 1.0F);

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
     * Core-grade multiplier. Standard raw-material cores are x1.00. Future improved
     * cores should increase this value while retaining the same material basePower.
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
     * Temporary dev.40-era BER visual. The raw socket item is represented by its material
     * block only until the dev.41+ Core Chamber redesign is implemented. Final design renders
     * the actual installed item inside a small glass chamber; do not build new mechanics around
     * this block substitution.
     */
    public ItemStack legacyBlockVisualStack() {
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

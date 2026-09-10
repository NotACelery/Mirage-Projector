package celerbi.mirageprojector;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public enum CoreBoosterMaterial implements StringRepresentable {
    EMPTY,
    GLASS,
    QUARTZ,
    AMETHYST,
    DIAMOND,
    NETHERITE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean present() {
        return this != EMPTY;
    }

    public Component displayComponent() {
        return Component.translatable("gui.mirage_projector.core_booster.material." + name().toLowerCase());
    }

    public Component beaconEffectComponent() {
        return Component.translatable("gui.mirage_projector.core_booster.effect." + name().toLowerCase());
    }

    public ItemStack centerStack() {
        return switch (this) {
            case GLASS -> new ItemStack(Blocks.GLASS);
            case QUARTZ -> new ItemStack(Items.QUARTZ);
            case AMETHYST -> new ItemStack(Items.AMETHYST_SHARD);
            case DIAMOND -> new ItemStack(Items.DIAMOND);
            case NETHERITE -> new ItemStack(Items.NETHERITE_INGOT);
            default -> ItemStack.EMPTY;
        };
    }

    public ProjectionCoreProfile improvedProfile() {
        return switch (this) {
            case GLASS -> ProjectionCoreProfile.IMPROVED_GLASS;
            case QUARTZ -> ProjectionCoreProfile.IMPROVED_QUARTZ;
            case AMETHYST -> ProjectionCoreProfile.IMPROVED_AMETHYST;
            case DIAMOND -> ProjectionCoreProfile.IMPROVED_DIAMOND;
            case NETHERITE -> ProjectionCoreProfile.IMPROVED_NETHERITE;
            default -> ProjectionCoreProfile.NONE;
        };
    }

    public static CoreBoosterMaterial fromInsertStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EMPTY;
        }
        if (stack.is(Blocks.GLASS.asItem())) {
            return GLASS;
        }
        if (stack.is(Items.QUARTZ)) {
            return QUARTZ;
        }
        if (stack.is(Items.AMETHYST_SHARD)) {
            return AMETHYST;
        }
        if (stack.is(Items.DIAMOND)) {
            return DIAMOND;
        }
        if (stack.is(Items.NETHERITE_INGOT)) {
            return NETHERITE;
        }
        return EMPTY;
    }

    public static CoreBoosterMaterial byName(String value) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }
        try {
            return valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return EMPTY;
        }
    }
}

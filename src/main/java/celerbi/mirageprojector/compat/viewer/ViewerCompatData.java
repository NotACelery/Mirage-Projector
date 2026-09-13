package celerbi.mirageprojector.compat.viewer;

import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class ViewerCompatData {
    private ViewerCompatData() {
    }

    public static List<ItemStack> cryingObsidianInfoStacks() {
        return List.of(
                new ItemStack(Blocks.CRYING_OBSIDIAN),
                new ItemStack(ModBlocks.SMALL_CRYING_OBSIDIAN_BUD.get()),
                new ItemStack(ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD.get()),
                new ItemStack(ModBlocks.LARGE_CRYING_OBSIDIAN_BUD.get()),
                new ItemStack(ModBlocks.CRYING_OBSIDIAN_CLUSTER.get()),
                new ItemStack(ModItems.CRYING_OBSIDIAN_SHARD.get())
        );
    }

    public static List<ItemStack> projectorInfoStacks() {
        return List.of(
                new ItemStack(ModItems.MIRAGE_PROJECTOR.get()),
                new ItemStack(ModItems.MIRAGE_DISPLAY.get()),
                new ItemStack(ModItems.WIDE_MIRAGE_PROJECTOR.get()),
                new ItemStack(ModItems.TALL_MIRAGE_PROJECTOR.get()),
                new ItemStack(ModItems.MIRAGE_PRISM.get()),
                new ItemStack(ModItems.MIRAGE_FIELD_PROJECTOR.get())
        );
    }

    public static ItemStack silkTouchPickaxeIcon() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    public static ItemStack regularPickaxeIcon() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    public static Component tooltip(String key, Object... args) {
        return Component.translatable(key, args);
    }
}

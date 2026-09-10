package celerbi.mirageprojector.recipe;

import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Five one-way chassis upgrades introduced by dev.45. */
public enum ProjectorUpgradePath {
    DISPLAY("display", new String[]{"QSA", "SMS", "ASQ"}),
    WIDE("wide", new String[]{"SSS", "GDG", "SSS"}),
    TALL("tall", new String[]{"SGS", "SDS", "SGS"}),
    PRISM("prism", new String[]{"SGS", "GDG", "SGS"}),
    FIELD("field", new String[]{"CSC", "SDS", "CSC"});

    private final String id;
    private final String[] pattern;

    ProjectorUpgradePath(String id, String[] pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    public String id() {
        return id;
    }

    public String[] pattern() {
        return pattern.clone();
    }

    public Block sourceBlock() {
        return this == DISPLAY ? ModBlocks.MIRAGE_PROJECTOR.get() : ModBlocks.MIRAGE_DISPLAY.get();
    }

    public Block targetBlock() {
        return switch (this) {
            case DISPLAY -> ModBlocks.MIRAGE_DISPLAY.get();
            case WIDE -> ModBlocks.WIDE_MIRAGE_PROJECTOR.get();
            case TALL -> ModBlocks.TALL_MIRAGE_PROJECTOR.get();
            case PRISM -> ModBlocks.MIRAGE_PRISM.get();
            case FIELD -> ModBlocks.MIRAGE_FIELD_PROJECTOR.get();
        };
    }

    public ItemLike ingredient(char key) {
        return switch (key) {
            case 'Q' -> Items.QUARTZ;
            case 'A' -> Items.AMETHYST_SHARD;
            case 'S' -> ModItems.CRYING_OBSIDIAN_SHARD.get();
            case 'M' -> ModItems.MIRAGE_PROJECTOR.get();
            case 'D' -> ModItems.MIRAGE_DISPLAY.get();
            case 'G' -> Blocks.GLASS;
            case 'C' -> Blocks.CRYING_OBSIDIAN;
            default -> null;
        };
    }

    public boolean matchesKey(char key, Item item) {
        ItemLike expected = ingredient(key);
        return expected != null && item == expected.asItem();
    }

    public static ProjectorUpgradePath byId(String id) {
        for (ProjectorUpgradePath path : values()) {
            if (path.id.equals(id)) {
                return path;
            }
        }
        throw new IllegalArgumentException("Unknown Mirage projector upgrade path: " + id);
    }
}

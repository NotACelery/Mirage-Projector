package celerbi.mirageprojector.compat.emi;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.compat.viewer.ViewerCompatData;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModItems;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public final class CryingObsidianGrowthEmiRecipe extends BasicEmiRecipe {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "emi/crying_obsidian_growth");

    public CryingObsidianGrowthEmiRecipe() {
        super(VanillaEmiRecipeCategories.WORLD_INTERACTION, ID, 166, 78);

        inputs.add(EmiStack.of(Blocks.CRYING_OBSIDIAN));
        inputs.add(EmiStack.of(Fluids.LAVA, 1000));
        inputs.add(EmiStack.of(ModBlocks.SMALL_CRYING_OBSIDIAN_BUD.get()));
        inputs.add(EmiStack.of(ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD.get()));
        inputs.add(EmiStack.of(ModBlocks.LARGE_CRYING_OBSIDIAN_BUD.get()));
        inputs.add(EmiStack.of(ModBlocks.CRYING_OBSIDIAN_CLUSTER.get()));
        inputs.add(EmiStack.of(ModItems.CRYING_OBSIDIAN_SHARD.get()));
        inputs.add(EmiStack.of(ViewerCompatData.silkTouchPickaxeIcon()));
        inputs.add(EmiStack.of(ViewerCompatData.regularPickaxeIcon()));

        outputs.add(EmiStack.of(Blocks.CRYING_OBSIDIAN));
        outputs.add(EmiStack.of(ModBlocks.SMALL_CRYING_OBSIDIAN_BUD.get()));
        outputs.add(EmiStack.of(ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD.get()));
        outputs.add(EmiStack.of(ModBlocks.LARGE_CRYING_OBSIDIAN_BUD.get()));
        outputs.add(EmiStack.of(ModBlocks.CRYING_OBSIDIAN_CLUSTER.get()));
        outputs.add(EmiStack.of(ModItems.CRYING_OBSIDIAN_SHARD.get(), 4));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Main world setup centered on the left: lava above Crying Obsidian, then growth below.
        widgets.addTank(EmiStack.of(Fluids.LAVA, 1000), 16, 4, 16, 16, 1000)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.step.lava"));
        widgets.addSlot(EmiStack.of(Blocks.CRYING_OBSIDIAN), 16, 24)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.step.base"));
        widgets.addSlot(EmiStack.of(ModBlocks.SMALL_CRYING_OBSIDIAN_BUD.get()), 16, 50)
                .recipeContext(this)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.step.start"));

        addSmallArrow(widgets, 40, 55);
        widgets.addSlot(EmiStack.of(ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD.get()), 58, 50)
                .recipeContext(this)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.stage.medium"));
        addSmallArrow(widgets, 82, 55);
        widgets.addSlot(EmiStack.of(ModBlocks.LARGE_CRYING_OBSIDIAN_BUD.get()), 100, 50)
                .recipeContext(this)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.stage.large"));
        addSmallArrow(widgets, 124, 55);
        widgets.addSlot(EmiStack.of(ModBlocks.CRYING_OBSIDIAN_CLUSTER.get()), 142, 50)
                .recipeContext(this)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.stage.cluster"));

        // Harvest examples on the top row.
        widgets.addSlot(EmiStack.of(ViewerCompatData.silkTouchPickaxeIcon()), 102, 4)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.silk_touch_pickaxe"));
        addSmallArrow(widgets, 126, 8);
        widgets.addSlot(EmiStack.of(ModBlocks.CRYING_OBSIDIAN_CLUSTER.get()), 148, 4)
                .recipeContext(this)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.silk_touch_result"));

        widgets.addSlot(EmiStack.of(ViewerCompatData.regularPickaxeIcon()), 102, 24)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.regular_pickaxe"));
        addSmallArrow(widgets, 126, 28);
        widgets.addSlot(EmiStack.of(ModItems.CRYING_OBSIDIAN_SHARD.get(), 4), 148, 24)
                .recipeContext(this)
                .appendTooltip(ViewerCompatData.tooltip("emi.mirage_projector.crying_obsidian.shard_drop"));
    }

    private static void addSmallArrow(WidgetHolder widgets, int x, int y) {
        widgets.addTexture(
                EmiTexture.EMPTY_ARROW.texture,
                x, y,
                12, 9,
                EmiTexture.EMPTY_ARROW.u, EmiTexture.EMPTY_ARROW.v,
                EmiTexture.EMPTY_ARROW.regionWidth, EmiTexture.EMPTY_ARROW.regionHeight,
                EmiTexture.EMPTY_ARROW.textureWidth, EmiTexture.EMPTY_ARROW.textureHeight
        );
    }
}

package celerbi.mirageprojector.compat.emi;

import celerbi.mirageprojector.registry.ModItems;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public final class CryingObsidianCraftingEmiRecipe extends BasicEmiRecipe {
    private final EmiIngredient[] grid = new EmiIngredient[9];

    public CryingObsidianCraftingEmiRecipe(ItemLike centerIngredient, ResourceLocation id) {
        super(VanillaEmiRecipeCategories.CRAFTING, id, 116, 54);

        EmiStack shard = EmiStack.of(ModItems.CRYING_OBSIDIAN_SHARD.get());
        EmiStack center = EmiStack.of(centerIngredient);

        for (int i = 0; i < grid.length; i++) {
            grid[i] = i == 4 ? center : shard;
            inputs.add(grid[i]);
        }
        outputs.add(EmiStack.of(Blocks.CRYING_OBSIDIAN));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                widgets.addSlot(grid[y * 3 + x], x * 18, y * 18);
            }
        }

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
        widgets.addSlot(outputs.getFirst(), 90, 14)
                .large(true)
                .recipeContext(this);
    }
}

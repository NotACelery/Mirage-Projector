package celerbi.mirageprojector.compat.emi;

import celerbi.mirageprojector.recipe.ProjectorUpgradePath;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public final class ProjectorUpgradeEmiRecipe extends BasicEmiRecipe {
    private final EmiIngredient[] grid = new EmiIngredient[9];

    public ProjectorUpgradeEmiRecipe(ProjectorUpgradePath path, ResourceLocation id) {
        super(VanillaEmiRecipeCategories.CRAFTING, id, 116, 54);

        String[] pattern = path.pattern();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int index = y * 3 + x;
                char key = pattern[y].charAt(x);
                if (key == ' ') {
                    grid[index] = EmiStack.EMPTY;
                    continue;
                }
                ItemLike item = path.ingredient(key);
                grid[index] = item == null ? EmiStack.EMPTY : EmiStack.of(item);
                if (item != null) {
                    inputs.add(grid[index]);
                }
            }
        }

        outputs.add(EmiStack.of(path.targetBlock()));
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

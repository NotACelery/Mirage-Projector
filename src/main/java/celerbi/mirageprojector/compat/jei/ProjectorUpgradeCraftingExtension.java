package celerbi.mirageprojector.compat.jei;

import celerbi.mirageprojector.recipe.ProjectorUpgradePath;
import celerbi.mirageprojector.recipe.ProjectorUpgradeRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class ProjectorUpgradeCraftingExtension implements ICraftingCategoryExtension<ProjectorUpgradeRecipe> {
    @Override
    public void setRecipe(RecipeHolder<ProjectorUpgradeRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        ProjectorUpgradePath path = recipeHolder.value().path();
        String[] pattern = path.pattern();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                char key = pattern[y].charAt(x);
                if (key == ' ') {
                    continue;
                }
                ItemLike item = path.ingredient(key);
                if (item == null) {
                    continue;
                }
                builder.addInputSlot(x * 18 + 1, y * 18 + 1)
                        .addItemStack(new ItemStack(item))
                        .setStandardSlotBackground();
            }
        }

        builder.addOutputSlot(95, 19)
                .addItemStack(new ItemStack(path.targetBlock()))
                .setOutputSlotBackground();
    }

    @Override
    public int getWidth(RecipeHolder<ProjectorUpgradeRecipe> recipeHolder) {
        return 3;
    }

    @Override
    public int getHeight(RecipeHolder<ProjectorUpgradeRecipe> recipeHolder) {
        return 3;
    }
}

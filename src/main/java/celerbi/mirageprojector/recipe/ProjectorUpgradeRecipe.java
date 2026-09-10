package celerbi.mirageprojector.recipe;

import celerbi.mirageprojector.ProjectorStateTransfer;
import celerbi.mirageprojector.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public final class ProjectorUpgradeRecipe implements CraftingRecipe {
    private static final String GROUP = "mirage_projector_chassis_upgrades";

    private final ProjectorUpgradePath path;

    public ProjectorUpgradeRecipe(ProjectorUpgradePath path) {
        this.path = path;
    }

    public ProjectorUpgradePath path() {
        return path;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }

        String[] pattern = path.pattern();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                char key = pattern[y].charAt(x);
                ItemStack stack = input.getItem(x, y);
                if (key == ' ') {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                } else if (stack.isEmpty() || !path.matchesKey(key, stack.getItem())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (input.width() != 3 || input.height() != 3) {
            return ItemStack.EMPTY;
        }
        ItemStack source = input.getItem(1, 1);
        if (source.isEmpty() || source.getItem() != path.sourceBlock().asItem()) {
            return ItemStack.EMPTY;
        }
        return ProjectorStateTransfer.upgrade(source, path.sourceBlock(), path.targetBlock(), registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(path.targetBlock());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (String row : path.pattern()) {
            for (int i = 0; i < row.length(); i++) {
                char key = row.charAt(i);
                if (key == ' ') {
                    continue;
                }
                ItemLike item = path.ingredient(key);
                if (item != null) {
                    ingredients.add(Ingredient.of(item));
                }
            }
        }
        return ingredients;
    }

    @Override
    public String getGroup() {
        return GROUP;
    }

    public CraftingBookCategory category() {
        return CraftingBookCategory.BUILDING;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PROJECTOR_UPGRADE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}

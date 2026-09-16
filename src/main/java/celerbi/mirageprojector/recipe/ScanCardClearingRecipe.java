package celerbi.mirageprojector.recipe;

import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** One filled Mirage Entity Scan Card by itself crafts back into one blank card. */
public final class ScanCardClearingRecipe implements CraftingRecipe {
    public static final ScanCardClearingRecipe INSTANCE = new ScanCardClearingRecipe();

    private ScanCardClearingRecipe() {
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return hasExactlyOneFilledCard(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return hasExactlyOneFilledCard(input)
                ? new ItemStack(ModItems.ENTITY_SCAN_CARD.get())
                : ItemStack.EMPTY;
    }

    private static boolean hasExactlyOneFilledCard(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return false;
            }
            found = stack;
        }
        return !found.isEmpty()
                && found.is(ModItems.ENTITY_SCAN_CARD.get())
                && EntityScanData.hasScan(found);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.ENTITY_SCAN_CARD.get());
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SCAN_CARD_CLEARING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}

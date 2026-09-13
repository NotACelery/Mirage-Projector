package celerbi.mirageprojector.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Keeps EMI/JEMI's original Block Drops rendering/category, but gives the four
 * Crying Obsidian crystal stages deterministic IDs so they stay ordered by age.
 */
public final class OrderedCrystalDropEmiRecipe implements EmiRecipe {
    private final EmiRecipe delegate;
    private final ResourceLocation id;

    public OrderedCrystalDropEmiRecipe(EmiRecipe delegate, ResourceLocation id) {
        this.delegate = delegate;
        this.id = id;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return delegate.getCategory();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return delegate.getInputs();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return delegate.getCatalysts();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return delegate.getOutputs();
    }

    @Override
    public int getDisplayWidth() {
        return delegate.getDisplayWidth();
    }

    @Override
    public int getDisplayHeight() {
        return delegate.getDisplayHeight();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        delegate.addWidgets(widgets);
    }

    @Override
    public boolean supportsRecipeTree() {
        return delegate.supportsRecipeTree();
    }

    @Override
    public boolean hideCraftable() {
        return delegate.hideCraftable();
    }
}

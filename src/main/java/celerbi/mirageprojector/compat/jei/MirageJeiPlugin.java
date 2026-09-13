package celerbi.mirageprojector.compat.jei;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.compat.viewer.ViewerCompatData;
import celerbi.mirageprojector.recipe.ProjectorUpgradeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class MirageJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(ProjectorUpgradeRecipe.class, new ProjectorUpgradeCraftingExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                ViewerCompatData.cryingObsidianInfoStacks(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.1"),
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.2"),
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.3"),
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.4"),
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.5"),
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.6"),
                Component.translatable("jei.mirage_projector.info.crying_obsidian_growth.7")
        );

        registration.addIngredientInfo(
                ViewerCompatData.projectorInfoStacks(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.mirage_projector.info.projector_upgrades.1"),
                Component.translatable("jei.mirage_projector.info.projector_upgrades.2"),
                Component.translatable("jei.mirage_projector.info.projector_upgrades.3")
        );
    }
}

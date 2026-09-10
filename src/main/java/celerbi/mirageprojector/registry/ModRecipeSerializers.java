package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.recipe.ProjectorUpgradeRecipe;
import celerbi.mirageprojector.recipe.ProjectorUpgradeRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MirageProjector.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProjectorUpgradeRecipe>> PROJECTOR_UPGRADE =
            RECIPE_SERIALIZERS.register("projector_upgrade", ProjectorUpgradeRecipeSerializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

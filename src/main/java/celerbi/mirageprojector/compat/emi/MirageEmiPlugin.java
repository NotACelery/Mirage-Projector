package celerbi.mirageprojector.compat.emi;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.recipe.ProjectorUpgradePath;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModItems;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EmiEntrypoint
public final class MirageEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registerProjectorCrafting(registry);
        registerCryingObsidianCrafting(registry);
        orderCrystalBlockDrops(registry);
        registry.addRecipe(new CryingObsidianGrowthEmiRecipe());
    }

    private static void registerProjectorCrafting(EmiRegistry registry) {
        for (ProjectorUpgradePath path : ProjectorUpgradePath.values()) {
            // Suppress any automatic/JEMI representation of the custom serializer, then add our
            // explicit vanilla-crafting presentation under a separate synthetic ID.
            registry.removeRecipes(projectorDataRecipeId(path));
            registry.addRecipe(new ProjectorUpgradeEmiRecipe(path, projectorEmiRecipeId(path)));
        }
    }

    private static void registerCryingObsidianCrafting(EmiRegistry registry) {
        ResourceLocation fireChargeData = ResourceLocation.fromNamespaceAndPath(
                MirageProjector.MOD_ID, "crying_obsidian_from_shards_fire_charge");
        ResourceLocation magmaCreamData = ResourceLocation.fromNamespaceAndPath(
                MirageProjector.MOD_ID, "crying_obsidian_from_shards_magma_cream");

        // Force these two recipes into EMI's Crafting tab even when another viewer bridge is
        // controlling vanilla crafting discovery.
        registry.removeRecipes(fireChargeData);
        registry.removeRecipes(magmaCreamData);
        registry.addRecipe(new CryingObsidianCraftingEmiRecipe(
                Items.FIRE_CHARGE,
                ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "emi/crafting/crying_obsidian_fire_charge")
        ));
        registry.addRecipe(new CryingObsidianCraftingEmiRecipe(
                Items.MAGMA_CREAM,
                ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "emi/crafting/crying_obsidian_magma_cream")
        ));
    }

    private static void orderCrystalBlockDrops(EmiRegistry registry) {
        List<EmiRecipe> captured = new ArrayList<>();
        boolean[] replaying = {false};

        // EMI/JEMI supplies the Block Drops category. Capture only our four crystal-drop rows,
        // then replay them after plugin registration with deterministic age-prefixed IDs.
        registry.removeRecipes(recipe -> {
            if (replaying[0] || recipe instanceof CryingObsidianGrowthEmiRecipe) {
                return false;
            }
            int stage = crystalDropStage(recipe);
            if (stage < 0) {
                return false;
            }
            captured.add(recipe);
            return true;
        });

        registry.addDeferredRecipes(add -> {
            replaying[0] = true;
            captured.stream()
                    .sorted(Comparator.comparingInt(MirageEmiPlugin::crystalDropStage))
                    .forEach(recipe -> {
                        int stage = crystalDropStage(recipe);
                        add.accept(new OrderedCrystalDropEmiRecipe(
                                recipe,
                                ResourceLocation.fromNamespaceAndPath(
                                        MirageProjector.MOD_ID,
                                        "emi/ordered_block_drops/0" + (stage + 1) + "_" + crystalStageName(stage)
                                )
                        ));
                    });
        });
    }

    private static int crystalDropStage(EmiRecipe recipe) {
        if (!containsOutput(recipe, EmiStack.of(ModItems.CRYING_OBSIDIAN_SHARD.get()))) {
            return -1;
        }

        List<EmiStack> stages = List.of(
                EmiStack.of(ModBlocks.SMALL_CRYING_OBSIDIAN_BUD.get()),
                EmiStack.of(ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD.get()),
                EmiStack.of(ModBlocks.LARGE_CRYING_OBSIDIAN_BUD.get()),
                EmiStack.of(ModBlocks.CRYING_OBSIDIAN_CLUSTER.get())
        );
        for (int i = 0; i < stages.size(); i++) {
            EmiStack target = stages.get(i);
            if (recipe.getInputs().stream().anyMatch(ingredient -> containsStack(ingredient, target))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean containsOutput(EmiRecipe recipe, EmiStack target) {
        return recipe.getOutputs().stream().anyMatch(stack -> stack.isEqual(target));
    }

    private static boolean containsStack(EmiIngredient ingredient, EmiStack target) {
        return ingredient.getEmiStacks().stream().anyMatch(stack -> stack.isEqual(target));
    }

    private static String crystalStageName(int stage) {
        return switch (stage) {
            case 0 -> "small";
            case 1 -> "medium";
            case 2 -> "large";
            case 3 -> "cluster";
            default -> "unknown";
        };
    }

    private static ResourceLocation projectorDataRecipeId(ProjectorUpgradePath path) {
        String pathId = switch (path) {
            case DISPLAY -> "mirage_display_upgrade";
            case WIDE -> "wide_mirage_projector_upgrade";
            case TALL -> "tall_mirage_projector_upgrade";
            case PRISM -> "mirage_prism_upgrade";
            case FIELD -> "mirage_field_projector_upgrade";
        };
        return ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, pathId);
    }

    private static ResourceLocation projectorEmiRecipeId(ProjectorUpgradePath path) {
        return ResourceLocation.fromNamespaceAndPath(
                MirageProjector.MOD_ID,
                "emi/projector_upgrade/" + path.id()
        );
    }
}

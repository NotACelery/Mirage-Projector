package celerbi.mirageprojector.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ProjectorUpgradeRecipeSerializer implements RecipeSerializer<ProjectorUpgradeRecipe> {
    private static final Codec<ProjectorUpgradePath> PATH_CODEC = Codec.STRING.xmap(
            ProjectorUpgradePath::byId,
            ProjectorUpgradePath::id
    );

    public static final MapCodec<ProjectorUpgradeRecipe> CODEC = PATH_CODEC
            .fieldOf("upgrade")
            .xmap(ProjectorUpgradeRecipe::new, ProjectorUpgradeRecipe::path);

    public static final StreamCodec<RegistryFriendlyByteBuf, ProjectorUpgradeRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ProjectorUpgradeRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return new ProjectorUpgradeRecipe(ProjectorUpgradePath.byId(buffer.readUtf(32)));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ProjectorUpgradeRecipe recipe) {
                    buffer.writeUtf(recipe.path().id(), 32);
                }
            };

    @Override
    public MapCodec<ProjectorUpgradeRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ProjectorUpgradeRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

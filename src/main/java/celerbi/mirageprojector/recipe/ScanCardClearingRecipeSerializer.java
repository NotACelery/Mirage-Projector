package celerbi.mirageprojector.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ScanCardClearingRecipeSerializer implements RecipeSerializer<ScanCardClearingRecipe> {
    private static final MapCodec<ScanCardClearingRecipe> CODEC = MapCodec.unit(() -> ScanCardClearingRecipe.INSTANCE);
    private static final StreamCodec<RegistryFriendlyByteBuf, ScanCardClearingRecipe> STREAM_CODEC =
            StreamCodec.unit(ScanCardClearingRecipe.INSTANCE);

    @Override
    public MapCodec<ScanCardClearingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ScanCardClearingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

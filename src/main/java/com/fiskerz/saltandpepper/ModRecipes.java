package com.fiskerz.saltandpepper;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    private ModRecipes() {}

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SaltandPepper.MODID);

    /**
     * A special crafting recipe must <em>not</em> register its own
     * {@link net.minecraft.world.item.crafting.RecipeType}. {@code CraftingRecipe#getType()} returns
     * {@code RecipeType.CRAFTING}, which is what the crafting menu queries, and the JSON {@code "type"}
     * field dispatches against the <em>serializer</em> registry. Registering a custom RecipeType and
     * returning it from {@code getType()} would make the crafting table never find this recipe.
     * Vanilla's {@code ArmorDyeRecipe} works the same way.
     *
     * <p>So {@code "type": "saltandpepper:seasoning"} in JSON resolves to this serializer.
     */
    public static final RegistryObject<SimpleCraftingRecipeSerializer<SeasoningRecipe>> SEASONING_SERIALIZER =
            RECIPE_SERIALIZERS.register("seasoning", () -> new SimpleCraftingRecipeSerializer<>(SeasoningRecipe::new));
}

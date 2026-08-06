package com.fiskerz.saltandpepper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class ModRecipes {
    private ModRecipes() {}

    /**
     * A special crafting recipe must <em>not</em> register its own
     * {@link net.minecraft.world.item.crafting.RecipeType}. {@code CraftingRecipe#getType()} returns
     * {@code RecipeType.CRAFTING}, which is what the crafting menu queries, and {@code Recipe.CODEC}
     * dispatches the JSON {@code "type"} field against the <em>serializer</em> registry (see
     * {@code Recipe.CODEC} / {@code RecipeManager#apply}). Registering a custom RecipeType and returning
     * it from {@code getType()} would make the crafting table never find this recipe. Vanilla's
     * {@code ArmorDyeRecipe} works the same way.
     *
     * <p>So {@code "type": "saltandpepper:seasoning"} in JSON resolves to this serializer.
     */
    public static final SimpleCraftingRecipeSerializer<SeasoningRecipe> SEASONING_SERIALIZER =
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, SaltandPepper.id("seasoning"),
                    new SimpleCraftingRecipeSerializer<>(SeasoningRecipe::new));

    /** Forces class initialisation, which is what actually performs the registration above. */
    static void init() {}
}

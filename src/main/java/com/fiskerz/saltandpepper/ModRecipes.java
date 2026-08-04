package com.fiskerz.saltandpepper;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    private ModRecipes() {}

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SaltandPepper.MODID);

    /**
     * Deviation from the spec's "register recipe type + serializer": a special crafting recipe must
     * <em>not</em> register its own {@link net.minecraft.world.item.crafting.RecipeType}.
     * {@code CraftingRecipe#getType()} returns {@code RecipeType.CRAFTING}, which is what the crafting
     * menu queries, and {@code Recipe.CODEC} dispatches the JSON {@code "type"} field against the
     * <em>serializer</em> registry (see {@code Recipe.CODEC} / {@code RecipeManager#apply}). Registering
     * a custom RecipeType and returning it from {@code getType()} would make the crafting table never
     * find this recipe. Vanilla's {@code ArmorDyeRecipe} works the same way.
     *
     * <p>So {@code "type": "saltandpepper:seasoning"} in JSON resolves to this serializer.
     */
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<SeasoningRecipe>> SEASONING_SERIALIZER =
            RECIPE_SERIALIZERS.register("seasoning", () -> new SimpleCraftingRecipeSerializer<>(SeasoningRecipe::new));
}

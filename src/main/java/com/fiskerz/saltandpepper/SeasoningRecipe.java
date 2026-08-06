package com.fiskerz.saltandpepper;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Shapeless "food + seasoning" crafting recipe.
 *
 * <p>This class only decides how a crafting grid maps onto a (food, seasoning) pair. The rules for
 * what may be seasoned and what seasoning does live in {@link SeasoningHelper}, shared with
 * {@link ShakerItem} so the two application paths cannot drift apart.
 */
public class SeasoningRecipe extends CustomRecipe {
    public SeasoningRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return resolve(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Match match = resolve(input);
        return match == null ? ItemStack.EMPTY : SeasoningHelper.season(match.food, match.seasoningId, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SEASONING_SERIALIZER;
    }

    // getType() is deliberately not overridden: CraftingRecipe supplies RecipeType.CRAFTING, which is
    // what the crafting menu queries. See ModRecipes for why there is no custom RecipeType.

    // -- Matching -------------------------------------------------------------------------------

    /** A resolved grid: exactly one seasonable food plus exactly one not-yet-applied seasoning. */
    private record Match(ItemStack food, ResourceLocation seasoningId) {}

    @Nullable
    private static Match resolve(CraftingInput input) {
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (first.isEmpty()) {
                first = stack;
            } else if (second.isEmpty()) {
                second = stack;
            } else {
                return null; // More than two items in the grid.
            }
        }

        if (second.isEmpty()) {
            return null; // Fewer than two items.
        }

        // Either arrangement is valid; a seasoning that is itself a food must not match against itself.
        Match match = tryMatch(first, second);
        return match != null ? match : tryMatch(second, first);
    }

    @Nullable
    private static Match tryMatch(ItemStack food, ItemStack seasoning) {
        if (!SeasoningHelper.isSeasoning(seasoning)) {
            return null;
        }

        // canSeason covers the food checks and the "not already applied" check in one place.
        ResourceLocation seasoningId = SeasoningHelper.seasoningId(seasoning);
        return SeasoningHelper.canSeason(food, seasoningId) ? new Match(food, seasoningId) : null;
    }
}

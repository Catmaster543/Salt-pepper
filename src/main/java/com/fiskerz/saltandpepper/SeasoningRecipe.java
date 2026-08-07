package com.fiskerz.saltandpepper;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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
 *
 * <p>26.1 reshaped special recipes: {@code CustomRecipe} no longer takes a
 * {@code CraftingBookCategory} (it hardcodes {@code MISC}), {@code SimpleCraftingRecipeSerializer} is
 * gone in favour of a {@code RecipeSerializer} record holding a codec pair, {@code assemble} no longer
 * receives a {@code HolderLookup.Provider}, and {@code canCraftInDimensions} was removed - the
 * inherited {@code placementInfo()} of {@code NOT_PLACEABLE} covers what it used to. Structure copied
 * from vanilla {@code BannerDuplicateRecipe}.
 */
public class SeasoningRecipe extends CustomRecipe {
    /**
     * The recipe carries no data of its own - the JSON is just
     * {@code {"type": "saltandpepper:seasoning"}} - so one shared instance serves every lookup. Using
     * a single instance also keeps {@link StreamCodec#unit} happy, which rejects encoding any value
     * that is not equal to the one it was built with.
     */
    public static final SeasoningRecipe INSTANCE = new SeasoningRecipe();

    public static final MapCodec<SeasoningRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SeasoningRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return resolve(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Match match = resolve(input);
        return match == null ? ItemStack.EMPTY : SeasoningHelper.season(match.food, match.seasoningId, 1);
    }

    @Override
    public RecipeSerializer<SeasoningRecipe> getSerializer() {
        return ModRecipes.SEASONING_SERIALIZER.get();
    }

    // getType() is deliberately not overridden: CraftingRecipe supplies RecipeType.CRAFTING, which is
    // what the crafting menu queries. See ModRecipes for why there is no custom RecipeType.

    // -- Matching -------------------------------------------------------------------------------

    /** A resolved grid: exactly one seasonable food plus exactly one not-yet-applied seasoning. */
    private record Match(ItemStack food, Identifier seasoningId) {}

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
        Identifier seasoningId = SeasoningHelper.seasoningId(seasoning);
        return SeasoningHelper.canSeason(food, seasoningId) ? new Match(food, seasoningId) : null;
    }
}

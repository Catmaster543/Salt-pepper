package com.fiskerz.saltandpepper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Shapeless "food + seasoning" crafting recipe.
 *
 * <p>Rather than shipping a seasoned variant of every food item, this transmutes the food stack in
 * place using data components, so it works with every food from every mod automatically.
 *
 * <p>Note on {@link FoodProperties} in 1.21.1: the record stores <em>absolute</em> saturation, not
 * the saturation modifier that {@code FoodProperties.Builder} accepts. The relationship is
 * {@code saturation = nutrition * modifier * 2} ({@link FoodConstants#saturationByModifier}), so to
 * apply a modifier bonus we have to recover the food's existing modifier, add to it, then convert
 * back. The record also has a {@code usingConvertsTo} field on this version (it is not 1.21.2+ only),
 * which is copied through unchanged along with canAlwaysEat, eatSeconds and effects.
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
        return match == null ? ItemStack.EMPTY : season(match.food, match.seasoningId);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SEASONING_SERIALIZER.get();
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
        if (!isSeasonableFood(food) || !seasoning.is(ModTags.Items.SEASONINGS)) {
            return null;
        }

        ResourceLocation seasoningId = seasoningId(seasoning);
        // A food cannot receive the same seasoning twice.
        if (getSeasonings(food).contains(seasoningId)) {
            return null;
        }
        return new Match(food, seasoningId);
    }

    public static boolean isSeasonableFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD)
                && stack.is(ModTags.Items.SEASONABLE)
                && !stack.is(ModTags.Items.SEASONING_BLACKLIST);
    }

    private static ResourceLocation seasoningId(ItemStack seasoning) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(seasoning.getItem());
    }

    /** The seasonings already applied to a stack, or an empty list. */
    public static List<ResourceLocation> getSeasonings(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SEASONINGS.get(), List.of());
    }

    // -- Assembly -------------------------------------------------------------------------------

    private static ItemStack season(ItemStack food, ResourceLocation seasoningId) {
        ItemStack result = food.copyWithCount(1);

        FoodProperties old = food.get(DataComponents.FOOD);
        if (old == null) {
            return ItemStack.EMPTY;
        }

        int bonusNutrition = bonusNutrition(seasoningId);
        float bonusModifier = bonusSaturationModifier(seasoningId);

        int newNutrition = Math.min(old.nutrition() + bonusNutrition, Config.MAX_NUTRITION.get());
        // Recover the existing modifier from the stored absolute saturation, add the bonus, convert back.
        float oldModifier = old.nutrition() <= 0 ? 0.0F : old.saturation() / (2.0F * old.nutrition());
        float newModifier = Math.min(oldModifier + bonusModifier, 2.0F);
        float newSaturation = FoodConstants.saturationByModifier(newNutrition, newModifier);

        result.set(DataComponents.FOOD, new FoodProperties(
                newNutrition,
                newSaturation,
                old.canAlwaysEat(),
                old.eatSeconds(),
                old.usingConvertsTo(),
                old.effects()));

        List<ResourceLocation> seasonings = new ArrayList<>(getSeasonings(food));
        seasonings.add(seasoningId);
        result.set(ModDataComponents.SEASONINGS.get(), List.copyOf(seasonings));

        return result;
    }

    /**
     * Ground pepper uses the pepper bonuses; everything else in {@code #saltandpepper:seasonings} -
     * our salt, Salt: Renewed's salt, and any seasoning a pack adds to the tag - uses the salt bonuses.
     *
     * <p>These read the common config, which NeoForge does not sync to clients. The server is
     * authoritative for the actual craft; a client with a different config file would only ever see a
     * misleading result preview in the output slot.
     */
    private static int bonusNutrition(ResourceLocation seasoningId) {
        return isPepper(seasoningId) ? Config.PEPPER_BONUS_NUTRITION.get() : Config.SALT_BONUS_NUTRITION.get();
    }

    private static float bonusSaturationModifier(ResourceLocation seasoningId) {
        double bonus = isPepper(seasoningId)
                ? Config.PEPPER_BONUS_SATURATION_MODIFIER.get()
                : Config.SALT_BONUS_SATURATION_MODIFIER.get();
        return (float) bonus;
    }

    private static boolean isPepper(ResourceLocation seasoningId) {
        return seasoningId.equals(ModItems.GROUND_PEPPER.getId());
    }
}

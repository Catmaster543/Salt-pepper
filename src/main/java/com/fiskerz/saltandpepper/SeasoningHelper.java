package com.fiskerz.saltandpepper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * The single source of truth for what may be seasoned and what seasoning does to a food.
 *
 * <p>Both application paths - the {@link SeasoningRecipe} crafting recipe and {@link ShakerItem} -
 * route through here, so a food that can be seasoned by hand is always seasonable by shaker and both
 * produce byte-identical results.
 *
 * <p>Rather than shipping a seasoned variant of every food item, seasoning transmutes the food stack
 * in place using data components, so it works with every food from every mod automatically.
 *
 * <p>Note on {@link FoodProperties}: the record stores <em>absolute</em> saturation, not the
 * saturation modifier that {@code FoodProperties.Builder} accepts. The relationship is
 * {@code saturation = nutrition * modifier * 2} ({@link FoodConstants#saturationByModifier}), so to
 * apply a modifier bonus we have to recover the food's existing modifier, add to it, then convert
 * back.
 *
 * <p>On 26.1 {@code FoodProperties} is {@code (nutrition, saturation, canAlwaysEat)} and nothing else.
 * Eat duration, animation, sound and consume effects moved to {@code minecraft:consumable}, and the
 * old {@code usingConvertsTo} moved to {@code minecraft:use_remainder}. On 1.21.1 this method had to
 * hand-copy those four fields just to rebuild the record; now they live in components we never touch,
 * so they are preserved by construction rather than by remembering to copy them.
 */
public final class SeasoningHelper {
    private SeasoningHelper() {}

    // -- Eligibility ----------------------------------------------------------------------------

    /** Whether a stack is a food this mod is allowed to season at all, ignoring what is already on it. */
    public static boolean isSeasonableFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD)
                && stack.is(ModTags.Items.SEASONABLE)
                && !stack.is(ModTags.Items.SEASONING_BLACKLIST);
    }

    /** Whether a stack may be used as a seasoning, i.e. is in {@code #saltandpepper:seasonings}. */
    public static boolean isSeasoning(ItemStack stack) {
        return stack.is(ModTags.Items.SEASONINGS);
    }

    /**
     * The full eligibility test: {@code food} is a seasonable food that has not already had
     * {@code seasoningId} applied to it.
     */
    public static boolean canSeason(ItemStack food, Identifier seasoningId) {
        return isSeasonableFood(food) && !getSeasonings(food).contains(seasoningId);
    }

    /** The registry id used to identify a seasoning on a seasoned food. */
    public static Identifier seasoningId(ItemStack seasoning) {
        return BuiltInRegistries.ITEM.getKey(seasoning.getItem());
    }

    /** The seasonings already applied to a stack, or an empty list. */
    public static List<Identifier> getSeasonings(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SEASONINGS, List.of());
    }

    // -- Application ----------------------------------------------------------------------------

    /**
     * The seasoned form of {@code food}, as a new stack of {@code count}. The caller is responsible
     * for having checked {@link #canSeason}; a food without a {@code minecraft:food} component
     * yields {@link ItemStack#EMPTY}.
     */
    public static ItemStack season(ItemStack food, Identifier seasoningId, int count) {
        ItemStack result = food.copyWithCount(count);

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

        // Only minecraft:food is rewritten. minecraft:consumable and minecraft:use_remainder carry
        // everything else about eating this food and are left exactly as they were.
        result.set(DataComponents.FOOD, new FoodProperties(
                newNutrition,
                newSaturation,
                old.canAlwaysEat()));

        List<Identifier> seasonings = new ArrayList<>(getSeasonings(food));
        seasonings.add(seasoningId);
        result.set(ModDataComponents.SEASONINGS, List.copyOf(seasonings));

        return result;
    }

    // -- Balance --------------------------------------------------------------------------------

    /**
     * Ground pepper uses the pepper bonuses; everything else in {@code #saltandpepper:seasonings} -
     * our salt, Salt: Renewed's salt, and any seasoning a pack adds to the tag - uses the salt bonuses.
     *
     * <p>These read the config file, which is not synced to clients. The server is
     * authoritative for the actual craft; a client with a different config file would only ever see a
     * misleading result preview in the output slot, or a shaker interaction the server immediately
     * corrects.
     */
    private static int bonusNutrition(Identifier seasoningId) {
        return isPepper(seasoningId) ? Config.PEPPER_BONUS_NUTRITION.get() : Config.SALT_BONUS_NUTRITION.get();
    }

    private static float bonusSaturationModifier(Identifier seasoningId) {
        double bonus = isPepper(seasoningId)
                ? Config.PEPPER_BONUS_SATURATION_MODIFIER.get()
                : Config.SALT_BONUS_SATURATION_MODIFIER.get();
        return (float) bonus;
    }

    private static boolean isPepper(Identifier seasoningId) {
        return seasoningId.equals(ModItems.GROUND_PEPPER_ID);
    }
}

package com.fiskerz.saltandpepper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/**
 * The single source of truth for what may be seasoned and what seasoning records on a stack.
 *
 * <p>Both application paths - the {@link SeasoningRecipe} crafting recipe and {@link ShakerItem} -
 * route through here, so a food that can be seasoned by hand is always seasonable by shaker and both
 * produce identical stacks.
 *
 * <h2>How this differs from the 1.21.1 build</h2>
 *
 * <p>Data components do not exist on 1.20.1 and {@code FoodProperties} is static per-{@code Item},
 * not per-{@code ItemStack}, so the 1.21.1 approach - rewriting the {@code minecraft:food} component
 * on the individual stack - is impossible here. Instead:
 *
 * <ul>
 *   <li>{@link #season} records the seasoning id in stack NBT and <em>does not touch food properties
 *       at all</em>. A seasoned stack is the same item with one extra NBT tag.</li>
 *   <li>{@link SeasoningEffectHandler} applies the nutrition and saturation bonus at the moment the
 *       food is eaten, straight onto the player's {@code FoodData}.</li>
 * </ul>
 *
 * <p>The observable result - how much hunger and saturation a seasoned food restores - is the same.
 * What differs is that nothing <em>displays</em> the boosted values, because the item's own
 * FoodProperties are unchanged; see the deviation note in PARITY.md.
 */
public final class SeasoningHelper {
    private SeasoningHelper() {}

    // -- Eligibility ----------------------------------------------------------------------------

    /**
     * Whether a stack is a food this mod is allowed to season at all, ignoring what is already on it.
     *
     * <p>1.21.1 additionally required membership of {@code #saltandpepper:seasonable}, which shipped
     * containing {@code #c:foods}. That common tag is a 1.20.2+ convention with no 1.20.1 equivalent,
     * so the gate is "is edible and not blacklisted" - the same set of items, since {@code #c:foods}
     * is exactly the edible items.
     */
    public static boolean isSeasonableFood(ItemStack stack) {
        return stack.isEdible() && !stack.is(ModTags.Items.SEASONING_BLACKLIST);
    }

    /** Whether a stack may be used as a seasoning, i.e. is in {@code #saltandpepper:seasonings}. */
    public static boolean isSeasoning(ItemStack stack) {
        return stack.is(ModTags.Items.SEASONINGS);
    }

    /**
     * The full eligibility test: {@code food} is a seasonable food that has not already had
     * {@code seasoningId} applied to it.
     */
    public static boolean canSeason(ItemStack food, ResourceLocation seasoningId) {
        return isSeasonableFood(food) && !getSeasonings(food).contains(seasoningId);
    }

    /** The registry id used to identify a seasoning on a seasoned food. */
    public static ResourceLocation seasoningId(ItemStack seasoning) {
        return BuiltInRegistries.ITEM.getKey(seasoning.getItem());
    }

    /** The seasonings already applied to a stack, in application order. */
    public static List<ResourceLocation> getSeasonings(ItemStack stack) {
        return SeasoningNbt.getSeasonings(stack);
    }

    // -- Application ----------------------------------------------------------------------------

    /**
     * The seasoned form of {@code food}, as a new stack of {@code count}. The caller is responsible
     * for having checked {@link #canSeason}.
     *
     * <p>Unlike the 1.21.1 build this only appends to the seasoning list; the food's nutrition and
     * saturation are applied on consumption by {@link SeasoningEffectHandler}.
     */
    public static ItemStack season(ItemStack food, ResourceLocation seasoningId, int count) {
        ItemStack result = food.copy();
        result.setCount(count);

        List<ResourceLocation> seasonings = new ArrayList<>(getSeasonings(food));
        seasonings.add(seasoningId);
        SeasoningNbt.setSeasonings(result, seasonings);

        return result;
    }

    // -- Balance --------------------------------------------------------------------------------

    /**
     * Ground pepper uses the pepper bonuses; everything else in {@code #saltandpepper:seasonings} -
     * our salt, Salt: Renewed's salt, and any seasoning a pack adds to the tag - uses the salt bonuses.
     *
     * <p>These read the common config, which Forge does not sync to clients. The server is
     * authoritative for both the craft and the eaten result.
     */
    public static int bonusNutrition(ResourceLocation seasoningId) {
        return isPepper(seasoningId) ? Config.PEPPER_BONUS_NUTRITION.get() : Config.SALT_BONUS_NUTRITION.get();
    }

    public static float bonusSaturationModifier(ResourceLocation seasoningId) {
        double bonus = isPepper(seasoningId)
                ? Config.PEPPER_BONUS_SATURATION_MODIFIER.get()
                : Config.SALT_BONUS_SATURATION_MODIFIER.get();
        return (float) bonus;
    }

    private static boolean isPepper(ResourceLocation seasoningId) {
        return seasoningId.equals(ModItems.GROUND_PEPPER_ID);
    }
}

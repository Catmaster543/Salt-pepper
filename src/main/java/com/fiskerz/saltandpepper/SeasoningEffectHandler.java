package com.fiskerz.saltandpepper;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Applies the seasoning bonus at the moment a seasoned food is eaten.
 *
 * <h2>Why this exists</h2>
 *
 * <p>On 1.21.1 seasoning rewrites the {@code minecraft:food} data component of the individual stack,
 * so vanilla's own eating code restores the boosted amounts. Neither half of that is available on
 * 1.20.1: there are no data components, and {@code FoodProperties} is attached to the {@code Item},
 * not the {@code ItemStack}. Changing it would change every stack of that food in the game, seasoned
 * or not.
 *
 * <p>So the bonus is applied after the fact instead. Vanilla has already run
 * {@code FoodData.eat(item, stack)} for the base food by the time this is called; this adds the
 * difference between the seasoned and unseasoned values.
 *
 * <p>This class holds no loader API of its own. On Forge it was driven by
 * {@code LivingEntityUseItemEvent.Finish}; here {@code PlayerEatMixin} calls it, because Fabric API
 * 0.92.11 exposes no finish-using-item event. The arithmetic below is byte-for-byte the same on both
 * branches, which is what keeps the two numerically identical.
 *
 * <h2>Matching the numbers exactly</h2>
 *
 * <p>1.21.1 computes, with {@code n}/{@code m} the base nutrition and saturation modifier:
 * <pre>
 *   n' = min(n + bonusNutrition, maxNutrition)
 *   m' = min(m + bonusSaturationModifier, 2.0)
 *   s' = n' * m' * 2                       (FoodConstants.saturationByModifier)
 * </pre>
 * and then {@code FoodData.add(n', s')}, which is
 * {@code foodLevel = clamp(foodLevel + n', 0, 20); saturation = min(saturation + s', foodLevel)}.
 *
 * <p>Here vanilla has already applied {@code (n, n*m*2)}, so this adds the deltas
 * {@code (n' - n, n'*m'*2 - n*m*2)} through the same two clamps, in the same order - nutrition first,
 * because the saturation ceiling is the food level. Applying the clamp twice against the same ceiling
 * is equivalent to applying it once against the total, since the deltas are non-negative:
 * {@code min(min(a, c) + d, c) == min(a + d, c)} for {@code d >= 0}.
 *
 * <p>One subtlety mirrored deliberately: 1.21.1 recovers the base modifier from the stored absolute
 * saturation as {@code n <= 0 ? 0 : s / (2n)}. 1.20.1 stores the modifier directly, but the same guard
 * is applied so a zero-nutrition food behaves identically on both.
 */
public final class SeasoningEffectHandler {
    private SeasoningEffectHandler() {}

    /** Vanilla's hard cap on the food level, matching {@code FoodData.eat}. */
    private static final int MAX_FOOD_LEVEL = 20;
    /** Vanilla's cap on a saturation modifier, matching the 1.21.1 clamp. */
    private static final float MAX_SATURATION_MODIFIER = 2.0F;

    /**
     * Called from {@link com.fiskerz.saltandpepper.mixin.PlayerEatMixin}, immediately after vanilla's
     * {@code FoodData.eat(item, stack)} inside {@code Player.eat} - so consumption has completed and
     * the unseasoned food has already been applied, leaving this to add the difference.
     *
     * @param eaten the stack that was consumed, still at its pre-shrink count. That matters: this
     *              method needs {@code eaten.getItem()}, and on 1.20.1 that getter answers
     *              {@code Items.AIR} once the stack is empty. See the note in the mixin about why the
     *              injection cannot simply sit at the end of the method.
     */
    public static void applyOnEaten(Player player, Level level, ItemStack eaten) {
        if (level.isClientSide) {
            return;
        }

        List<ResourceLocation> seasonings = SeasoningHelper.getSeasonings(eaten);
        if (seasonings.isEmpty()) {
            return;
        }

        // Deliberately the item's own food properties rather than any per-stack override: this must
        // read whatever vanilla's FoodData.eat(Item, ItemStack) read a few instructions ago, or the
        // delta would not line up. On 1.20.1 that is Item.getFoodProperties(). Fabric API 0.92.11 has
        // no per-stack food API for this to disagree with.
        FoodProperties food = eaten.getItem().getFoodProperties();
        if (food == null) {
            return;
        }

        int baseNutrition = food.getNutrition();
        // Mirrors the 1.21.1 guard: a zero-nutrition food contributes no saturation either way.
        float baseModifier = baseNutrition <= 0 ? 0.0F : food.getSaturationModifier();

        int bonusNutrition = 0;
        float bonusModifier = 0.0F;
        for (ResourceLocation seasoning : seasonings) {
            bonusNutrition += SeasoningHelper.bonusNutrition(seasoning);
            bonusModifier += SeasoningHelper.bonusSaturationModifier(seasoning);
        }

        int newNutrition = Math.min(baseNutrition + bonusNutrition, Config.MAX_NUTRITION.get());
        float newModifier = Math.min(baseModifier + bonusModifier, MAX_SATURATION_MODIFIER);

        int deltaNutrition = newNutrition - baseNutrition;
        float deltaSaturation = saturationByModifier(newNutrition, newModifier)
                - saturationByModifier(baseNutrition, baseModifier);

        if (deltaNutrition == 0 && deltaSaturation == 0.0F) {
            return;
        }

        FoodData data = player.getFoodData();
        // Nutrition first: the saturation clamp uses the food level as its ceiling.
        data.setFoodLevel(Mth.clamp(data.getFoodLevel() + deltaNutrition, 0, MAX_FOOD_LEVEL));
        data.setSaturation(Math.min(data.getSaturationLevel() + deltaSaturation, data.getFoodLevel()));
    }

    /**
     * {@code nutrition * modifier * 2}, which is what {@code FoodConstants.saturationByModifier} does
     * on 1.21.1 and what {@code FoodData.eat} does inline on 1.20.1.
     */
    private static float saturationByModifier(int nutrition, float modifier) {
        return nutrition * modifier * 2.0F;
    }
}

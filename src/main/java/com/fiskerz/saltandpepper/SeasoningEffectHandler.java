package com.fiskerz.saltandpepper;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Applies the seasoning bonus at the moment a seasoned food is eaten.
 *
 * <h2>Why this exists</h2>
 *
 * <p>On 1.21.1 seasoning rewrites the {@code minecraft:food} data component of the individual stack,
 * so vanilla's own eating code restores the boosted amounts. Neither half of that is available on
 * 1.20.1: there are no data components, and {@code FoodProperties} is attached to the
 * {@code Item}, not the {@code ItemStack}. Changing it would change every stack of that food in the
 * game, seasoned or not.
 *
 * <p>So the bonus is applied after the fact instead. Vanilla has already run
 * {@code FoodData.eat(nutrition, saturationModifier)} for the base food by the time this fires; this
 * handler adds the difference between the seasoned and unseasoned values.
 *
 * <h2>Matching the 1.21.1 numbers exactly</h2>
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
 * {@code (n' - n, n'*m'*2 - n*m*2)} through the same two clamps, in the same order - nutrition
 * first, because the saturation ceiling is the food level. Applying the clamp twice against the same
 * ceiling is equivalent to applying it once against the total, since the deltas are non-negative:
 * {@code min(min(a, c) + d, c) == min(a + d, c)} for {@code d >= 0}.
 *
 * <p>One subtlety mirrored deliberately: 1.21.1 recovers the base modifier from the stored absolute
 * saturation as {@code n <= 0 ? 0 : s / (2n)}. 1.20.1 stores the modifier directly, but the same
 * guard is applied so a zero-nutrition food behaves identically on both.
 */
@Mod.EventBusSubscriber(modid = SaltandPepper.MODID)
public final class SeasoningEffectHandler {
    private SeasoningEffectHandler() {}

    /** Vanilla's hard cap on the food level, matching {@code FoodData.eat}. */
    private static final int MAX_FOOD_LEVEL = 20;
    /** Vanilla's cap on a saturation modifier, matching the 1.21.1 clamp. */
    private static final float MAX_SATURATION_MODIFIER = 2.0F;

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        // getItem() is the stack as it was when consumption started - the seasoned one.
        ItemStack eaten = event.getItem();
        List<ResourceLocation> seasonings = SeasoningHelper.getSeasonings(eaten);
        if (seasonings.isEmpty()) {
            return;
        }

        FoodProperties food = eaten.getFoodProperties(player);
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

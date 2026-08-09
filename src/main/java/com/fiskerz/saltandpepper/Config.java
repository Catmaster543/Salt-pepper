package com.fiskerz.saltandpepper;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config, written to {@code saltandpepper-common.toml}.
 *
 * <p>Balance note on the seasoning bonuses: in 1.21.1 {@link net.minecraft.world.food.FoodProperties}
 * stores <em>absolute</em> saturation, not the modifier that authors write in a builder. The two are
 * related by {@link net.minecraft.world.food.FoodConstants#saturationByModifier}, which is
 * {@code saturation = nutrition * saturationModifier * 2}. So the modifier scales with how filling
 * the food already is: +0.2 on a 1-nutrition berry is worth almost nothing, while +0.2 on an
 * 8-nutrition steak is worth 3.2 saturation. One nutrition point is half a hunger shank, so salt and
 * pepper together give +2 nutrition (one full shank) on a fully seasoned food.
 */
public final class Config {
    private Config() {}

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // -- Seasoning ------------------------------------------------------------------------------

    public static final ForgeConfigSpec.IntValue MAX_NUTRITION;
    public static final ForgeConfigSpec.IntValue PEPPER_BONUS_NUTRITION;
    public static final ForgeConfigSpec.DoubleValue PEPPER_BONUS_SATURATION_MODIFIER;
    public static final ForgeConfigSpec.IntValue SALT_BONUS_NUTRITION;
    public static final ForgeConfigSpec.DoubleValue SALT_BONUS_SATURATION_MODIFIER;

    // -- Shakers --------------------------------------------------------------------------------

    public static final ForgeConfigSpec.IntValue SHAKER_CAPACITY;
    public static final ForgeConfigSpec.IntValue USES_PER_REFILL_ITEM;

    // -- Pepper ---------------------------------------------------------------------------------

    public static final ForgeConfigSpec.BooleanValue RESTRICT_GROWTH_TO_JUNGLE;
    public static final ForgeConfigSpec.IntValue BLANCH_BATCH_SIZE;

    // -- Salt -----------------------------------------------------------------------------------

    public static final ForgeConfigSpec.BooleanValue ENABLE_SALT;

    static final ForgeConfigSpec SPEC;

    static {
        BUILDER.comment("Seasoning balance.",
                        "Saturation restored = nutrition * saturationModifier * 2, so the modifier bonus",
                        "scales with how filling the food already is. One nutrition point = half a shank.")
                .push("seasoning");

        MAX_NUTRITION = BUILDER
                .comment("Upper clamp on the nutrition of a seasoned food. The saturation modifier is always clamped to 2.0.")
                .defineInRange("maxNutrition", 20, 0, 100);

        BUILDER.pop();

        BUILDER.comment("Ground pepper seasoning bonus.").push("pepper");

        PEPPER_BONUS_NUTRITION = BUILDER
                .comment("Nutrition added by ground pepper.")
                .defineInRange("bonusNutrition", 1, 0, 20);

        PEPPER_BONUS_SATURATION_MODIFIER = BUILDER
                .comment("Saturation modifier added by ground pepper.")
                .defineInRange("bonusSaturationModifier", 0.2D, 0.0D, 2.0D);

        BUILDER.pop();

        BUILDER.comment("Salt seasoning bonus.").push("salt");

        SALT_BONUS_NUTRITION = BUILDER
                .comment("Nutrition added by salt.")
                .defineInRange("bonusNutrition", 1, 0, 20);

        SALT_BONUS_SATURATION_MODIFIER = BUILDER
                .comment("Saturation modifier added by salt.")
                .defineInRange("bonusSaturationModifier", 0.2D, 0.0D, 2.0D);

        ENABLE_SALT = BUILDER
                .comment("Whether this mod's own salt ore, items and recipes are enabled.",
                         "Forced off at runtime if Salt: Renewed (mod id 'salt') is installed, so the two do not duplicate each other.")
                .define("enableSalt", true);

        BUILDER.pop();

        BUILDER.comment("Salt and pepper shakers.",
                        "One use seasons one food item, so a full shaker seasons a full stack of 64.",
                        "The default 8 seasoning items per 64 uses is an 8x efficiency gain over seasoning",
                        "by hand at a crafting table - the shaker's value is convenience, not power.")
                .push("shaker");

        SHAKER_CAPACITY = BUILDER
                .comment("How many foods a full shaker can season.")
                .defineInRange("shakerCapacity", 64, 1, 1024);

        USES_PER_REFILL_ITEM = BUILDER
                .comment("Uses added per seasoning item consumed when filling or refilling a shaker.")
                .defineInRange("usesPerRefillItem", 8, 1, 1024);

        BUILDER.pop();

        BUILDER.comment("Pepper vine and processing.").push("pepper_vine");

        RESTRICT_GROWTH_TO_JUNGLE = BUILDER
                .comment("When true, pepper vines only advance an age stage inside biomes tagged #minecraft:is_jungle or #c:is_jungle.",
                         "Bone meal is unaffected. Vines still survive and can be harvested anywhere.")
                .define("restrictGrowthToJungle", false);

        BLANCH_BATCH_SIZE = BUILDER
                .comment("Maximum green peppercorns converted per cauldron interaction.")
                .defineInRange("blanchBatchSize", 8, 1, 64);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /** Convenience accessor that also honours the Salt: Renewed runtime override. */
    public static boolean saltEnabled() {
        return ENABLE_SALT.get() && !SaltCompat.isSaltRenewedLoaded();
    }
}

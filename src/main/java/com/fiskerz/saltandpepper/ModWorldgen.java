package com.fiskerz.saltandpepper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ModWorldgen {
    private ModWorldgen() {}

    public static final WildPepperVineFeature WILD_PEPPER_VINE =
            Registry.register(BuiltInRegistries.FEATURE, SaltandPepper.id("wild_pepper_vine"),
                    new WildPepperVineFeature(NoneFeatureConfiguration.CODEC));

    public static final PlacementModifierType<SaltEnabledFilter> SALT_ENABLED_FILTER =
            Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, SaltandPepper.id("salt_enabled"),
                    () -> SaltEnabledFilter.CODEC);

    /** Forces class initialisation, which is what actually performs the registrations above. */
    static void init() {}
}

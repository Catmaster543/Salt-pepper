package com.fiskerz.saltandpepper;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {
    private ModWorldgen() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, SaltandPepper.MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, SaltandPepper.MODID);

    public static final DeferredHolder<Feature<?>, WildPepperVineFeature> WILD_PEPPER_VINE =
            FEATURES.register("wild_pepper_vine", () -> new WildPepperVineFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<SaltEnabledFilter>> SALT_ENABLED_FILTER =
            PLACEMENT_MODIFIER_TYPES.register("salt_enabled", () -> () -> SaltEnabledFilter.CODEC);
}

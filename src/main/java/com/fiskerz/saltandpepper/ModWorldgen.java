package com.fiskerz.saltandpepper;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModWorldgen {
    private ModWorldgen() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, SaltandPepper.MODID);

    /**
     * Placement modifier types are a vanilla registry with no {@code ForgeRegistries} wrapper on
     * 1.20.1, so this DeferredRegister is created from the vanilla registry key instead.
     */
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, SaltandPepper.MODID);

    public static final RegistryObject<WildPepperVineFeature> WILD_PEPPER_VINE =
            FEATURES.register("wild_pepper_vine", () -> new WildPepperVineFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<PlacementModifierType<SaltEnabledFilter>> SALT_ENABLED_FILTER =
            PLACEMENT_MODIFIER_TYPES.register("salt_enabled", () -> () -> SaltEnabledFilter.CODEC);
}

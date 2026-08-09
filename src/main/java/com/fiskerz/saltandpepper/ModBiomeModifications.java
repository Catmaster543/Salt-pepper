package com.fiskerz.saltandpepper;

import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Biome injection.
 *
 * <p>On NeoForge this was three {@code neoforge:add_features} JSON files under
 * {@code data/saltandpepper/neoforge/biome_modifier/}. Fabric has no datapack equivalent, so the same
 * three injections are declared in code against the Fabric Biome API. The biome selectors and
 * generation steps mirror those files exactly, and the placed features they point at are unchanged -
 * all of the density tuning lives in the placed feature JSON, not here.
 */
public final class ModBiomeModifications {
    private ModBiomeModifications() {}

    public static void register() {
        // add_rock_salt_ore.json: "#minecraft:is_overworld", step underground_ores.
        // Selected by tag rather than BiomeSelectors.foundInOverworld(), which selects by dimension and
        // would not match the reference's tag-based targeting.
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                placed("ore_rock_salt"));

        // add_rock_salt_ore_coastal.json: neoforge:or of #c:is_beach, #c:is_ocean, #minecraft:is_beach,
        // #minecraft:is_ocean and minecraft:dripstone_caves, step underground_ores.
        Predicate<BiomeSelectionContext> coastal = BiomeSelectors.tag(ConventionalBiomeTags.BEACH)
                .or(BiomeSelectors.tag(ConventionalBiomeTags.OCEAN))
                .or(BiomeSelectors.tag(BiomeTags.IS_BEACH))
                .or(BiomeSelectors.tag(BiomeTags.IS_OCEAN))
                .or(BiomeSelectors.includeByKey(Biomes.DRIPSTONE_CAVES));

        BiomeModifications.addFeature(coastal,
                GenerationStep.Decoration.UNDERGROUND_ORES,
                placed("ore_rock_salt_coastal"));

        // add_wild_pepper_vines.json: neoforge:or of #minecraft:is_jungle and #c:is_jungle,
        // step vegetal_decoration. Both tags so modded jungle analogues are covered.
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.tag(ConventionalBiomeTags.JUNGLE)),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                placed("wild_pepper_vine"));
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, SaltandPepper.id(path));
    }
}

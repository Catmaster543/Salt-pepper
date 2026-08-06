package com.fiskerz.saltandpepper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class SaltandPepper implements ModInitializer {
    public static final String MODID = "saltandpepper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        // Config first: creative tab building and worldgen both read it.
        Config.load();

        // Registration is done in static initialisers, so these calls exist to force class loading in a
        // deterministic order. Blocks before items, because the block items reference their block.
        ModBlocks.init();
        ModItems.init();
        ModDataComponents.init();
        ModRecipes.init();
        ModWorldgen.init();
        ModCreativeTabs.init();

        ModBiomeModifications.register();
        CauldronBlanchingHandler.register();

        SaltCompat.logIfDisabled();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

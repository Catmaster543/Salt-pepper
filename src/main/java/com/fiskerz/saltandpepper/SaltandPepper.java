package com.fiskerz.saltandpepper;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(SaltandPepper.MODID)
public class SaltandPepper {
    public static final String MODID = "saltandpepper";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SaltandPepper(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModWorldgen.FEATURES.register(modEventBus);
        ModWorldgen.PLACEMENT_MODIFIER_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(SaltCompat::logIfDisabled);
    }
}

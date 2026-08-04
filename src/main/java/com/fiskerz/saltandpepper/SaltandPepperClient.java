package com.fiskerz.saltandpepper;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SaltandPepper.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SaltandPepper.MODID, value = Dist.CLIENT)
public class SaltandPepperClient {
    public SaltandPepperClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // The pepper vine model has transparent pixels around the pod, same as cocoa.
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PEPPER_VINE.get(), RenderType.cutout()));
    }
}

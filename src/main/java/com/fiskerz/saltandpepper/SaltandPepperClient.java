package com.fiskerz.saltandpepper;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only setup.
 *
 * <p>1.21.1 registered a config screen through NeoForge's {@code IConfigScreenFactory} +
 * {@code ConfigurationScreen}. Forge 1.20.1's equivalent extension point is
 * {@code ConfigScreenHandler.ConfigScreenFactory}; it has no built-in generic screen, so this simply
 * does not register one - see the deviation note in PARITY.md.
 */
@Mod.EventBusSubscriber(modid = SaltandPepper.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SaltandPepperClient {
    private SaltandPepperClient() {}

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // The pepper vine model has transparent pixels around the pod, same as cocoa.
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.PEPPER_VINE.get(), RenderType.cutout()));
    }
}

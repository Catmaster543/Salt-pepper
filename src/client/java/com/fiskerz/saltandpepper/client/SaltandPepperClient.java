package com.fiskerz.saltandpepper.client;

import com.fiskerz.saltandpepper.ModBlocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class SaltandPepperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // The pepper vine model has transparent pixels around the pod, same as cocoa.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PEPPER_VINE, RenderType.cutout());

        SeasoningTooltipHandler.register();
    }
}

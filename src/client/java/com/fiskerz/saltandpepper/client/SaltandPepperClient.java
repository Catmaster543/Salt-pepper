package com.fiskerz.saltandpepper.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * On Fabric 1.21.1 this also put the pepper vine into the cutout render layer via
 * {@code BlockRenderLayerMap}, because the pod model has transparent pixels around it. 26.1 removed
 * that whole module: terrain layers are now {@code ChunkSectionLayer}s chosen automatically from the
 * sprite's own contents, so a block with transparent texels lands in cutout without being told.
 * The NeoForge 26.1 build dropped its equivalent {@code ItemBlockRenderTypes} call for the same reason.
 */
public class SaltandPepperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SeasoningTooltipHandler.register();
    }
}

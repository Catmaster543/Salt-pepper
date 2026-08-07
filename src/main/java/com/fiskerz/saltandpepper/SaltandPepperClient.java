package com.fiskerz.saltandpepper;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * On 1.21.1 this also registered the pepper vine into the cutout render layer, because the pod model
 * has transparent pixels around it. 26.1 removed {@code ItemBlockRenderTypes}: terrain layers are now
 * {@code ChunkSectionLayer}s chosen automatically from the sprite's own contents, so a block with
 * transparent texels lands in cutout without being told. Vanilla {@code CocoaBlock} - which this block
 * copies - correspondingly has no render layer registration of its own any more.
 */
@Mod(value = SaltandPepper.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SaltandPepper.MODID, value = Dist.CLIENT)
public class SaltandPepperClient {
    public SaltandPepperClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

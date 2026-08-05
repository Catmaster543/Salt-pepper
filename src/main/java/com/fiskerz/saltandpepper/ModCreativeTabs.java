package com.fiskerz.saltandpepper;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SaltandPepper.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.saltandpepper.main"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ModItems.GROUND_PEPPER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PEPPER_SEEDS.get());
                        output.accept(ModItems.GREEN_PEPPERCORNS.get());
                        output.accept(ModItems.BLANCHED_PEPPERCORNS.get());
                        output.accept(ModItems.BLACK_PEPPERCORNS.get());
                        output.accept(ModItems.GROUND_PEPPER.get());

                        // Salt content hides itself when Salt: Renewed is providing salt instead.
                        if (Config.saltEnabled()) {
                            output.accept(ModItems.ROCK_SALT_ORE.get());
                            output.accept(ModItems.DEEPSLATE_ROCK_SALT_ORE.get());
                            output.accept(ModItems.RAW_ROCK_SALT.get());
                            output.accept(ModItems.SALT.get());
                            output.accept(ModItems.SALT_BLOCK.get());
                        }

                        output.accept(ModItems.EMPTY_SHAKER.get());
                        output.accept(ModItems.SALT_SHAKER.get());
                        output.accept(ModItems.PEPPER_SHAKER.get());
                    })
                    .build());
}

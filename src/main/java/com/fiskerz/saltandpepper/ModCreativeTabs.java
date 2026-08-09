package com.fiskerz.saltandpepper;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final CreativeModeTab MAIN = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            SaltandPepper.id("main"),
            // The NeoForge build called withTabsBefore(CreativeModeTabs.SPAWN_EGGS) to sit ahead of the
            // spawn eggs tab. That method is a NeoForge addition to CreativeModeTab.Builder, not vanilla,
            // and Fabric has no equivalent - custom groups are appended after the vanilla ones. The tab's
            // contents and order are unchanged; only its position in the tab strip differs.
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.saltandpepper.main"))
                    .icon(() -> new ItemStack(ModItems.GROUND_PEPPER))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PEPPER_SEEDS);
                        output.accept(ModItems.GREEN_PEPPERCORNS);
                        output.accept(ModItems.BLANCHED_PEPPERCORNS);
                        output.accept(ModItems.BLACK_PEPPERCORNS);
                        output.accept(ModItems.GROUND_PEPPER);

                        // Salt content hides itself when Salt: Renewed is providing salt instead.
                        if (Config.saltEnabled()) {
                            output.accept(ModItems.ROCK_SALT_ORE);
                            output.accept(ModItems.DEEPSLATE_ROCK_SALT_ORE);
                            output.accept(ModItems.RAW_ROCK_SALT);
                            output.accept(ModItems.SALT);
                            output.accept(ModItems.SALT_BLOCK);
                        }

                        output.accept(ModItems.EMPTY_SHAKER);
                        output.accept(ModItems.SALT_SHAKER);
                        output.accept(ModItems.PEPPER_SHAKER);
                    })
                    .build());

    /** Forces class initialisation, which is what actually performs the registration above. */
    static void init() {}
}

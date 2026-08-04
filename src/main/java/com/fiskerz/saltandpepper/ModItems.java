package com.fiskerz.saltandpepper;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SaltandPepper.MODID);

    // -- Pepper chain ---------------------------------------------------------------------------

    /**
     * Places the pepper vine. An {@link ItemNameBlockItem} so the item keeps its own translation key
     * instead of inheriting the block's, exactly like vanilla cocoa beans.
     */
    public static final DeferredItem<ItemNameBlockItem> PEPPER_SEEDS = ITEMS.registerItem("pepper_seeds",
            props -> new ItemNameBlockItem(ModBlocks.PEPPER_VINE.get(), props));

    public static final DeferredItem<Item> GREEN_PEPPERCORNS = ITEMS.registerSimpleItem("green_peppercorns");
    public static final DeferredItem<Item> BLANCHED_PEPPERCORNS = ITEMS.registerSimpleItem("blanched_peppercorns");
    public static final DeferredItem<Item> BLACK_PEPPERCORNS = ITEMS.registerSimpleItem("black_peppercorns");
    public static final DeferredItem<Item> GROUND_PEPPER = ITEMS.registerSimpleItem("ground_pepper");

    // -- Salt chain -----------------------------------------------------------------------------

    public static final DeferredItem<Item> RAW_ROCK_SALT = ITEMS.registerSimpleItem("raw_rock_salt");
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");

    // -- Block items ----------------------------------------------------------------------------

    public static final DeferredItem<BlockItem> ROCK_SALT_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.ROCK_SALT_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_ROCK_SALT_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.DEEPSLATE_ROCK_SALT_ORE);
    public static final DeferredItem<BlockItem> SALT_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.SALT_BLOCK);
}

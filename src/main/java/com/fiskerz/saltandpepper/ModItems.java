package com.fiskerz.saltandpepper;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SaltandPepper.MODID);

    // -- Pepper chain ---------------------------------------------------------------------------

    /**
     * Places the pepper vine, keeping its own translation key instead of inheriting the block's,
     * exactly like vanilla cocoa beans.
     *
     * <p>26.1 removed {@code ItemNameBlockItem}. Vanilla now spells this as a plain {@link BlockItem}
     * whose properties opt into the item description prefix - see {@code Items#COCOA_BEANS}, which is
     * registered with {@code createBlockItemWithCustomItemName(Blocks.COCOA)}, i.e.
     * {@code new BlockItem(block, props.useItemDescriptionPrefix())}.
     */
    public static final DeferredItem<BlockItem> PEPPER_SEEDS = ITEMS.registerItem("pepper_seeds",
            props -> new BlockItem(ModBlocks.PEPPER_VINE.get(), props.useItemDescriptionPrefix()));

    public static final DeferredItem<Item> GREEN_PEPPERCORNS = ITEMS.registerSimpleItem("green_peppercorns");
    public static final DeferredItem<Item> BLANCHED_PEPPERCORNS = ITEMS.registerSimpleItem("blanched_peppercorns");
    public static final DeferredItem<Item> BLACK_PEPPERCORNS = ITEMS.registerSimpleItem("black_peppercorns");
    public static final DeferredItem<Item> GROUND_PEPPER = ITEMS.registerSimpleItem("ground_pepper");

    // -- Salt chain -----------------------------------------------------------------------------

    public static final DeferredItem<Item> RAW_ROCK_SALT = ITEMS.registerSimpleItem("raw_rock_salt");
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");

    // -- Shakers --------------------------------------------------------------------------------

    /** Near-white, to read as salt through the glass. */
    private static final int SALT_BAR_COLOR = 0xF2F0EB;
    /** Dark grey-brown, to read as ground pepper. */
    private static final int PEPPER_BAR_COLOR = 0x4A3B2F;

    public static final DeferredItem<EmptyShakerItem> EMPTY_SHAKER = ITEMS.registerItem("empty_shaker",
            EmptyShakerItem::new);

    /**
     * Filled shakers are {@code stacksTo(1)} because they carry a per-stack fill level, and are
     * deliberately kept out of every {@code #minecraft:enchantable/*} tag. Together with storing fill
     * in {@code saltandpepper:shaker_uses} instead of vanilla durability, that keeps Mending,
     * Unbreaking and anvil repair off the table entirely.
     */
    public static final DeferredItem<ShakerItem> SALT_SHAKER = ITEMS.registerItem("salt_shaker",
            props -> new ShakerItem(props.stacksTo(1), SALT, "seasoning.saltandpepper.salt", SALT_BAR_COLOR));

    public static final DeferredItem<ShakerItem> PEPPER_SHAKER = ITEMS.registerItem("pepper_shaker",
            props -> new ShakerItem(props.stacksTo(1), GROUND_PEPPER, "seasoning.saltandpepper.pepper", PEPPER_BAR_COLOR));

    // -- Block items ----------------------------------------------------------------------------

    public static final DeferredItem<BlockItem> ROCK_SALT_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.ROCK_SALT_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_ROCK_SALT_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.DEEPSLATE_ROCK_SALT_ORE);
    public static final DeferredItem<BlockItem> SALT_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.SALT_BLOCK);
}

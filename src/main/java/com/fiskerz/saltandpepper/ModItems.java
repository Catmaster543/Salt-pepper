package com.fiskerz.saltandpepper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;

public final class ModItems {
    private ModItems() {}

    /**
     * Ids of the two seasoning items, as constants rather than registry lookups. These are the values
     * stored in the {@code saltandpepper:seasonings} component, so they are part of the save format.
     */
    public static final ResourceLocation GROUND_PEPPER_ID = SaltandPepper.id("ground_pepper");
    public static final ResourceLocation SALT_ID = SaltandPepper.id("salt");

    // -- Pepper chain ---------------------------------------------------------------------------

    /**
     * Places the pepper vine. An {@link ItemNameBlockItem} so the item keeps its own translation key
     * instead of inheriting the block's, exactly like vanilla cocoa beans.
     */
    public static final ItemNameBlockItem PEPPER_SEEDS = register("pepper_seeds",
            new ItemNameBlockItem(ModBlocks.PEPPER_VINE, new Item.Properties()));

    public static final Item GREEN_PEPPERCORNS = register("green_peppercorns", new Item(new Item.Properties()));
    public static final Item BLANCHED_PEPPERCORNS = register("blanched_peppercorns", new Item(new Item.Properties()));
    public static final Item BLACK_PEPPERCORNS = register("black_peppercorns", new Item(new Item.Properties()));
    public static final Item GROUND_PEPPER = register("ground_pepper", new Item(new Item.Properties()));

    // -- Salt chain -----------------------------------------------------------------------------

    public static final Item RAW_ROCK_SALT = register("raw_rock_salt", new Item(new Item.Properties()));
    public static final Item SALT = register("salt", new Item(new Item.Properties()));

    // -- Shakers --------------------------------------------------------------------------------

    /** Near-white, to read as salt through the glass. */
    private static final int SALT_BAR_COLOR = 0xF2F0EB;
    /** Dark grey-brown, to read as ground pepper. */
    private static final int PEPPER_BAR_COLOR = 0x4A3B2F;

    public static final EmptyShakerItem EMPTY_SHAKER = register("empty_shaker",
            new EmptyShakerItem(new Item.Properties()));

    /**
     * Filled shakers are {@code stacksTo(1)} because they carry a per-stack fill level, and are
     * deliberately kept out of every {@code #minecraft:enchantable/*} tag. Together with storing fill
     * in {@code saltandpepper:shaker_uses} instead of vanilla durability, that keeps Mending,
     * Unbreaking and anvil repair off the table entirely.
     */
    public static final ShakerItem SALT_SHAKER = register("salt_shaker",
            new ShakerItem(new Item.Properties().stacksTo(1), () -> SALT, "seasoning.saltandpepper.salt", SALT_BAR_COLOR));

    public static final ShakerItem PEPPER_SHAKER = register("pepper_shaker",
            new ShakerItem(new Item.Properties().stacksTo(1), () -> GROUND_PEPPER, "seasoning.saltandpepper.pepper", PEPPER_BAR_COLOR));

    // -- Block items ----------------------------------------------------------------------------

    public static final BlockItem ROCK_SALT_ORE = register("rock_salt_ore",
            new BlockItem(ModBlocks.ROCK_SALT_ORE, new Item.Properties()));
    public static final BlockItem DEEPSLATE_ROCK_SALT_ORE = register("deepslate_rock_salt_ore",
            new BlockItem(ModBlocks.DEEPSLATE_ROCK_SALT_ORE, new Item.Properties()));
    public static final BlockItem SALT_BLOCK = register("salt_block",
            new BlockItem(ModBlocks.SALT_BLOCK, new Item.Properties()));

    /**
     * NeoForge replaces {@code Item.BY_BLOCK} with its own registry-backed map, so block items map back
     * to their block automatically. Vanilla's map is a plain {@link java.util.HashMap} that
     * {@code Items} populates by hand, so a block item registered outside that class has to do the same
     * or pick-block and {@code Block#asItem} return air.
     */
    private static <T extends Item> T register(String name, T item) {
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(BuiltInRegistries.ITEM, SaltandPepper.id(name), item);
    }

    /** Forces class initialisation, which is what actually performs the registrations above. */
    static void init() {}
}

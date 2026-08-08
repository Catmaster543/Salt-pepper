package com.fiskerz.saltandpepper;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {}

    /**
     * Ids of the two seasoning items, as constants rather than registry lookups. These are the values
     * stored in the {@code saltandpepper:seasonings} component, so they are part of the save format.
     */
    public static final Identifier GROUND_PEPPER_ID = SaltandPepper.id("ground_pepper");
    public static final Identifier SALT_ID = SaltandPepper.id("salt");

    // -- Pepper chain ---------------------------------------------------------------------------

    /**
     * Places the pepper vine, keeping its own translation key instead of inheriting the block's,
     * exactly like vanilla cocoa beans - which 26.1 registers as
     * {@code new BlockItem(block, props.useItemDescriptionPrefix())}.
     */
    public static final BlockItem PEPPER_SEEDS = register("pepper_seeds",
            props -> new BlockItem(ModBlocks.PEPPER_VINE, props.useItemDescriptionPrefix()));

    public static final Item GREEN_PEPPERCORNS = register("green_peppercorns", Item::new);
    public static final Item BLANCHED_PEPPERCORNS = register("blanched_peppercorns", Item::new);
    public static final Item BLACK_PEPPERCORNS = register("black_peppercorns", Item::new);
    public static final Item GROUND_PEPPER = register("ground_pepper", Item::new);

    // -- Salt chain -----------------------------------------------------------------------------

    public static final Item RAW_ROCK_SALT = register("raw_rock_salt", Item::new);
    public static final Item SALT = register("salt", Item::new);

    // -- Shakers --------------------------------------------------------------------------------

    /** Near-white, to read as salt through the glass. */
    private static final int SALT_BAR_COLOR = 0xF2F0EB;
    /** Dark grey-brown, to read as ground pepper. */
    private static final int PEPPER_BAR_COLOR = 0x4A3B2F;

    public static final EmptyShakerItem EMPTY_SHAKER = register("empty_shaker", EmptyShakerItem::new);

    /**
     * Filled shakers are {@code stacksTo(1)} because they carry a per-stack fill level, and are
     * deliberately kept out of every {@code #minecraft:enchantable/*} tag. Together with storing fill
     * in {@code saltandpepper:shaker_uses} instead of vanilla durability, that keeps Mending,
     * Unbreaking and anvil repair off the table entirely.
     */
    public static final ShakerItem SALT_SHAKER = register("salt_shaker",
            props -> new ShakerItem(props.stacksTo(1), () -> SALT, "seasoning.saltandpepper.salt", SALT_BAR_COLOR));

    public static final ShakerItem PEPPER_SHAKER = register("pepper_shaker",
            props -> new ShakerItem(props.stacksTo(1), () -> GROUND_PEPPER, "seasoning.saltandpepper.pepper", PEPPER_BAR_COLOR));

    // -- Block items ----------------------------------------------------------------------------

    public static final BlockItem ROCK_SALT_ORE = register("rock_salt_ore",
            props -> new BlockItem(ModBlocks.ROCK_SALT_ORE, props));
    public static final BlockItem DEEPSLATE_ROCK_SALT_ORE = register("deepslate_rock_salt_ore",
            props -> new BlockItem(ModBlocks.DEEPSLATE_ROCK_SALT_ORE, props));
    public static final BlockItem SALT_BLOCK = register("salt_block",
            props -> new BlockItem(ModBlocks.SALT_BLOCK, props));

    /**
     * Mirrors vanilla {@code Items#registerItem}: 26.1 requires the {@link ResourceKey} on the
     * properties before construction, and {@code Item.BY_BLOCK} is a plain map that only
     * {@code Items} populates - a block item registered outside that class has to do it itself or
     * pick-block and {@code Block#asItem} return air.
     */
    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SaltandPepper.id(name));
        T item = factory.apply(new Item.Properties().setId(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    /** Forces class initialisation, which is what actually performs the registrations above. */
    static void init() {}
}

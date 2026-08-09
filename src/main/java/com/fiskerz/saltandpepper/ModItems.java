package com.fiskerz.saltandpepper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SaltandPepper.MODID);

    /**
     * Ids of the two seasoning items. These are the values stored in the
     * {@code saltandpepper:seasonings} NBT list, so they are part of the save format.
     */
    public static final ResourceLocation GROUND_PEPPER_ID = new ResourceLocation(SaltandPepper.MODID, "ground_pepper");
    public static final ResourceLocation SALT_ID = new ResourceLocation(SaltandPepper.MODID, "salt");

    // -- Pepper chain ---------------------------------------------------------------------------

    /**
     * Places the pepper vine. An {@link ItemNameBlockItem} so the item keeps its own translation key
     * instead of inheriting the block's, exactly like vanilla cocoa beans.
     */
    public static final RegistryObject<ItemNameBlockItem> PEPPER_SEEDS = ITEMS.register("pepper_seeds",
            () -> new ItemNameBlockItem(ModBlocks.PEPPER_VINE.get(), new Item.Properties()));

    public static final RegistryObject<Item> GREEN_PEPPERCORNS = ITEMS.register("green_peppercorns",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLANCHED_PEPPERCORNS = ITEMS.register("blanched_peppercorns",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLACK_PEPPERCORNS = ITEMS.register("black_peppercorns",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GROUND_PEPPER = ITEMS.register("ground_pepper",
            () -> new Item(new Item.Properties()));

    // -- Salt chain -----------------------------------------------------------------------------

    public static final RegistryObject<Item> RAW_ROCK_SALT = ITEMS.register("raw_rock_salt",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SALT = ITEMS.register("salt",
            () -> new Item(new Item.Properties()));

    // -- Shakers --------------------------------------------------------------------------------

    /** Near-white, to read as salt through the glass. */
    private static final int SALT_BAR_COLOR = 0xF2F0EB;
    /** Dark grey-brown, to read as ground pepper. */
    private static final int PEPPER_BAR_COLOR = 0x4A3B2F;

    public static final RegistryObject<EmptyShakerItem> EMPTY_SHAKER = ITEMS.register("empty_shaker",
            () -> new EmptyShakerItem(new Item.Properties()));

    /**
     * Filled shakers are {@code stacksTo(1)} because they carry a per-stack fill level, and are
     * deliberately not enchantable. Together with storing fill in the
     * {@code saltandpepper:shaker_uses} NBT tag instead of vanilla durability, that keeps Mending,
     * Unbreaking and anvil repair off the table entirely.
     */
    public static final RegistryObject<ShakerItem> SALT_SHAKER = ITEMS.register("salt_shaker",
            () -> new ShakerItem(new Item.Properties().stacksTo(1), SALT, "seasoning.saltandpepper.salt", SALT_BAR_COLOR));

    public static final RegistryObject<ShakerItem> PEPPER_SHAKER = ITEMS.register("pepper_shaker",
            () -> new ShakerItem(new Item.Properties().stacksTo(1), GROUND_PEPPER, "seasoning.saltandpepper.pepper", PEPPER_BAR_COLOR));

    // -- Block items ----------------------------------------------------------------------------

    public static final RegistryObject<BlockItem> ROCK_SALT_ORE = ITEMS.register("rock_salt_ore",
            () -> new BlockItem(ModBlocks.ROCK_SALT_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> DEEPSLATE_ROCK_SALT_ORE = ITEMS.register("deepslate_rock_salt_ore",
            () -> new BlockItem(ModBlocks.DEEPSLATE_ROCK_SALT_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> SALT_BLOCK = ITEMS.register("salt_block",
            () -> new BlockItem(ModBlocks.SALT_BLOCK.get(), new Item.Properties()));
}

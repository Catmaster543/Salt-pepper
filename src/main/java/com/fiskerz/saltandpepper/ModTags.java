package com.fiskerz.saltandpepper;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Tag keys shipped by this mod. Contents live in {@code data/saltandpepper/tags/}. */
public final class ModTags {
    private ModTags() {}

    public static final class Items {
        private Items() {}

        /** Items that can be applied to a food as a seasoning. */
        public static final TagKey<Item> SEASONINGS = tag("seasonings");
        /** Foods that are allowed to be seasoned at all. Defaults to {@code #c:foods}. */
        public static final TagKey<Item> SEASONABLE = tag("seasonable");
        /** Foods that must never be seasoned, even if they are in {@link #SEASONABLE}. */
        public static final TagKey<Item> SEASONING_BLACKLIST = tag("seasoning_blacklist");

        // ItemTags#create is a NeoForge helper; vanilla's takes a plain String and assumes the
        // minecraft namespace, so this builds the TagKey directly. The tag ids are unchanged.
        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, SaltandPepper.id(name));
        }
    }

    public static final class Blocks {
        private Blocks() {}

        /** Blocks that can boil a cauldron sitting directly on top of them. */
        public static final TagKey<Block> HEAT_SOURCES = tag("heat_sources");
        /** Blocks a pepper vine can attach to. Ships as {@code #minecraft:jungle_logs}. */
        public static final TagKey<Block> PEPPER_VINE_SUPPORTS = tag("pepper_vine_supports");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, SaltandPepper.id(name));
        }
    }
}

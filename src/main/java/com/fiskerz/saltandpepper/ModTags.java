package com.fiskerz.saltandpepper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
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
        /**
         * Foods that datapack authors may restrict seasoning to.
         *
         * <p>On 1.21.1 this shipped containing {@code #c:foods} and was checked as a hard gate.
         * {@code c:foods} is a 1.20.2+ convention with no 1.20.1 equivalent, so the tag still exists
         * for datapack authors but ships empty and is <em>not</em> used as a gate - see
         * {@link SeasoningHelper#isSeasonableFood}. Eligibility is decided by "has food properties and
         * is not blacklisted", which is the same set of items the 1.21.1 build accepted.
         */
        public static final TagKey<Item> SEASONABLE = tag("seasonable");
        /** Foods that must never be seasoned. */
        public static final TagKey<Item> SEASONING_BLACKLIST = tag("seasoning_blacklist");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(SaltandPepper.MODID, name));
        }
    }

    public static final class Blocks {
        private Blocks() {}

        /** Blocks that can boil a cauldron sitting directly on top of them. */
        public static final TagKey<Block> HEAT_SOURCES = tag("heat_sources");
        /** Blocks a pepper vine can attach to. Ships as {@code #minecraft:jungle_logs}. */
        public static final TagKey<Block> PEPPER_VINE_SUPPORTS = tag("pepper_vine_supports");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(SaltandPepper.MODID, name));
        }
    }
}

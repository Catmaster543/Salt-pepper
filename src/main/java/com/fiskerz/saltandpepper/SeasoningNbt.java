package com.fiskerz.saltandpepper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Stack NBT storage, standing in for the data components the 1.21.1 build uses.
 *
 * <p>Data components arrived in 1.20.5, so on 1.20.1 both pieces of per-stack state live in the
 * stack's own {@code CompoundTag} instead. The tag names are deliberately the same strings as the
 * component ids they replace - {@code saltandpepper:seasonings} and {@code saltandpepper:shaker_uses}
 * - so the save format reads the same way and a datapack or command written against one build refers
 * to the same names on the other.
 *
 * <p>The seasoning list keeps insertion order, exactly like the component's {@code List<ResourceLocation>}:
 * the tooltip prints them in the order they were applied.
 */
public final class SeasoningNbt {
    private SeasoningNbt() {}

    /** Same string as the {@code saltandpepper:seasonings} data component id on 1.21.1. */
    public static final String SEASONINGS_KEY = "saltandpepper:seasonings";
    /** Same string as the {@code saltandpepper:shaker_uses} data component id on 1.21.1. */
    public static final String SHAKER_USES_KEY = "saltandpepper:shaker_uses";

    // -- Seasonings -----------------------------------------------------------------------------

    /** The seasonings applied to a stack, in application order, or an empty list. */
    public static List<ResourceLocation> getSeasonings(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SEASONINGS_KEY, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = tag.getList(SEASONINGS_KEY, Tag.TAG_STRING);
        List<ResourceLocation> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                result.add(id);
            }
        }
        return List.copyOf(result);
    }

    /** Overwrites the seasoning list on a stack, preserving the given order. */
    public static void setSeasonings(ItemStack stack, List<ResourceLocation> seasonings) {
        ListTag list = new ListTag();
        for (ResourceLocation id : seasonings) {
            list.add(StringTag.valueOf(id.toString()));
        }
        stack.getOrCreateTag().put(SEASONINGS_KEY, list);
    }

    // -- Shaker uses ----------------------------------------------------------------------------

    /** The stored use count, or null when the tag is absent (which reads as "full"). */
    public static Integer getShakerUses(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SHAKER_USES_KEY, Tag.TAG_INT)) {
            return null;
        }
        return tag.getInt(SHAKER_USES_KEY);
    }

    public static void setShakerUses(ItemStack stack, int uses) {
        stack.getOrCreateTag().putInt(SHAKER_USES_KEY, uses);
    }
}

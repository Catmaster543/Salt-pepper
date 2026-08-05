package com.fiskerz.saltandpepper;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * An ordinary stackable container with one piece of behaviour: right-clicking it against a pile of
 * salt or ground pepper fills it into the matching {@link ShakerItem}, the same path the filled
 * shakers use to top themselves up.
 *
 * <p>Only ever converts a <em>single</em> empty shaker at a time. Filled shakers are
 * {@code stacksTo(1)}, so if the acting stack held several there would be nowhere to put the result
 * without inserting into the inventory or dropping an item from inside an inventory-click handler -
 * the same thing that rules out partial seasoning in {@link ShakerItem}.
 */
public class EmptyShakerItem extends Item {
    public EmptyShakerItem(Properties properties) {
        super(properties);
    }

    /** The empty shaker is on the cursor and the player has right-clicked a pile of seasoning. */
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || stack.getCount() != 1 || !slot.allowModification(player)) {
            return false;
        }

        ItemStack seasoning = slot.getItem();
        ShakerItem shaker = ShakerItem.forSeasoning(seasoning);
        if (shaker == null) {
            return false;
        }

        int consumed = ShakerItem.refillCost(0, seasoning.getCount());
        if (consumed <= 0) {
            return false;
        }

        slot.setByPlayer(ShakerItem.shrunk(seasoning, consumed));
        player.containerMenu.setCarried(ShakerItem.filled(shaker, consumed * ShakerItem.usesPerRefillItem()));
        ShakerItem.playRefill(player);
        return true;
    }

    /** The empty shaker is in a slot and the player has right-clicked it holding seasoning. */
    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || stack.getCount() != 1 || !slot.allowModification(player)) {
            return false;
        }

        ShakerItem shaker = ShakerItem.forSeasoning(other);
        if (shaker == null) {
            return false;
        }

        int consumed = ShakerItem.refillCost(0, other.getCount());
        if (consumed <= 0) {
            return false;
        }

        access.set(ShakerItem.shrunk(other, consumed));
        slot.setByPlayer(ShakerItem.filled(shaker, consumed * ShakerItem.usesPerRefillItem()));
        ShakerItem.playRefill(player);
        return true;
    }
}

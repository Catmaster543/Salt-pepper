package com.fiskerz.saltandpepper.client;

import java.util.List;

import com.fiskerz.saltandpepper.ModItems;
import com.fiskerz.saltandpepper.SeasoningHelper;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Appends a single gray italic {@code Seasoned: Salt, Pepper} line to seasoned foods.
 * The item itself is deliberately never renamed.
 *
 * <p>NeoForge used {@code ItemTooltipEvent}; the Fabric equivalent is {@link ItemTooltipCallback},
 * which lives in the client-only half of fabric-item-api-v1 - hence this class living in the client
 * source set rather than alongside the rest of the mod.
 */
public final class SeasoningTooltipHandler {
    private SeasoningTooltipHandler() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> appendSeasonings(stack, lines));
    }

    private static void appendSeasonings(ItemStack stack, List<Component> lines) {
        List<Identifier> seasonings = SeasoningHelper.getSeasonings(stack);
        if (seasonings.isEmpty()) {
            return;
        }

        MutableComponent names = Component.empty();
        for (int i = 0; i < seasonings.size(); i++) {
            if (i > 0) {
                names.append(Component.literal(", "));
            }
            names.append(displayName(seasonings.get(i)));
        }

        lines.add(Component.translatable("tooltip.saltandpepper.seasoned", names)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Short display name for a seasoning. Our own seasonings get the dedicated
     * {@code seasoning.saltandpepper.*} keys so the tooltip reads "Pepper" rather than "Ground Pepper";
     * anything else added to {@code #saltandpepper:seasonings} may supply
     * {@code seasoning.<namespace>.<path>}, and otherwise falls back to the item's own name.
     */
    private static Component displayName(Identifier id) {
        if (id.equals(ModItems.GROUND_PEPPER_ID)) {
            return Component.translatable("seasoning.saltandpepper.pepper");
        }
        if (id.equals(ModItems.SALT_ID)) {
            return Component.translatable("seasoning.saltandpepper.salt");
        }

        String key = "seasoning." + id.getNamespace() + "." + id.getPath();
        if (Language.getInstance().has(key)) {
            return Component.translatable(key);
        }

        // 26.1: Registry#get returns an Optional holder, so the direct lookup is getValue; and
        // Item#getDescription is gone - it was exactly Component.translatable(getDescriptionId()).
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return Component.translatable(item.getDescriptionId());
    }
}

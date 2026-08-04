package com.fiskerz.saltandpepper;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Appends a single gray italic {@code Seasoned: Salt, Pepper} line to seasoned foods.
 * The item itself is deliberately never renamed.
 */
@EventBusSubscriber(modid = SaltandPepper.MODID, value = Dist.CLIENT)
public final class SeasoningTooltipHandler {
    private SeasoningTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        List<ResourceLocation> seasonings = SeasoningRecipe.getSeasonings(event.getItemStack());
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

        event.getToolTip().add(Component.translatable("tooltip.saltandpepper.seasoned", names)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Short display name for a seasoning. Our own seasonings get the dedicated
     * {@code seasoning.saltandpepper.*} keys so the tooltip reads "Pepper" rather than "Ground Pepper";
     * anything else added to {@code #saltandpepper:seasonings} may supply
     * {@code seasoning.<namespace>.<path>}, and otherwise falls back to the item's own name.
     */
    private static Component displayName(ResourceLocation id) {
        if (id.equals(ModItems.GROUND_PEPPER.getId())) {
            return Component.translatable("seasoning.saltandpepper.pepper");
        }
        if (id.equals(ModItems.SALT.getId())) {
            return Component.translatable("seasoning.saltandpepper.salt");
        }

        String key = "seasoning." + id.getNamespace() + "." + id.getPath();
        if (Language.getInstance().has(key)) {
            return Component.translatable(key);
        }

        Item item = BuiltInRegistries.ITEM.get(id);
        return item.getDescription();
    }
}

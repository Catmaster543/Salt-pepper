package com.fiskerz.saltandpepper;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * A portable seasoning container. Pick it up onto the cursor, right-click a stack of food anywhere in
 * an inventory, and the whole stack is seasoned at once.
 *
 * <p>Structurally modelled on vanilla {@code BundleItem}: the work happens in
 * {@link #overrideStackedOnOther} and {@link #overrideOtherStackedOnMe}, which are plain
 * {@code Item} hooks rather than loader APIs. Inventory clicks run twice - predicted on the client and
 * authoritatively on the server - so every mutation here has to be a pure function of the stacks
 * involved. Nothing in this class reads the level, spawns entities, or branches on
 * {@code level.isClientSide}; the only side-specific thing is the action bar message, which is
 * server-only because {@code ServerPlayer#displayClientMessage} sends a packet.
 *
 * <p>Fill level lives in {@code saltandpepper:shaker_uses} rather than vanilla durability - see
 * {@link ModDataComponents#SHAKER_USES}. An absent component reads as "full", which is what lets the
 * crafting recipes hand out a full shaker without hardcoding the configured capacity.
 */
public class ShakerItem extends Item {
    private final Supplier<? extends Item> seasoning;
    private final String seasoningNameKey;
    private final int barColor;

    public ShakerItem(Properties properties, Supplier<? extends Item> seasoning, String seasoningNameKey, int barColor) {
        super(properties);
        this.seasoning = seasoning;
        this.seasoningNameKey = seasoningNameKey;
        this.barColor = barColor;
    }

    // -- Fill level -----------------------------------------------------------------------------

    public static int capacity() {
        return Config.SHAKER_CAPACITY.get();
    }

    public static int usesPerRefillItem() {
        return Config.USES_PER_REFILL_ITEM.get();
    }

    /**
     * Uses remaining. A missing component means a shaker that has never been used, which reads as full
     * at whatever {@code shakerCapacity} currently is; a stored value above the capacity (because the
     * config was lowered) is clamped down rather than left over-full.
     */
    public static int getUses(ItemStack stack) {
        Integer stored = stack.get(ModDataComponents.SHAKER_USES.get());
        return stored == null ? capacity() : Mth.clamp(stored, 0, capacity());
    }

    /** A new shaker stack holding {@code uses}, clamped to capacity. */
    public static ItemStack filled(ShakerItem shaker, int uses) {
        ItemStack stack = new ItemStack(shaker);
        stack.set(ModDataComponents.SHAKER_USES.get(), Mth.clamp(uses, 0, capacity()));
        return stack;
    }

    /** The shaker that a pile of seasoning fills, or null if nothing this mod ships a shaker for. */
    @Nullable
    public static ShakerItem forSeasoning(ItemStack seasoning) {
        if (seasoning.isEmpty() || !SeasoningHelper.isSeasoning(seasoning)) {
            return null;
        }
        if (seasoning.is(ModItems.SALT.get())) {
            return ModItems.SALT_SHAKER.get();
        }
        if (seasoning.is(ModItems.GROUND_PEPPER.get())) {
            return ModItems.PEPPER_SHAKER.get();
        }
        return null;
    }

    /**
     * How many seasoning items to consume to top a shaker up from {@code currentUses}, capped by how
     * many are actually available. Zero means "already full", or nothing to take.
     */
    public static int refillCost(int currentUses, int available) {
        int missing = capacity() - currentUses;
        if (missing <= 0 || available <= 0) {
            return 0;
        }
        int per = usesPerRefillItem();
        return Math.min((missing + per - 1) / per, available);
    }

    /** {@code stack} minus {@code amount}, as a new stack, without touching the original. */
    public static ItemStack shrunk(ItemStack stack, int amount) {
        ItemStack remaining = stack.copy();
        remaining.shrink(amount);
        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    // -- Interaction ----------------------------------------------------------------------------

    /** The shaker is on the cursor and the player has right-clicked {@code slot}. */
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (!isUsable(stack, action) || !slot.allowModification(player)) {
            return false;
        }

        ItemStack target = slot.getItem();

        // (A) The slot holds this shaker's seasoning - refill.
        if (isOwnSeasoning(target)) {
            int uses = getUses(stack);
            int consumed = refillCost(uses, target.getCount());
            if (consumed <= 0) {
                return false; // Already full, or nothing to take.
            }
            slot.setByPlayer(shrunk(target, consumed));
            stack.set(ModDataComponents.SHAKER_USES.get(),
                    Math.min(uses + consumed * usesPerRefillItem(), capacity()));
            playRefill(player);
            return true;
        }

        // (B) The slot holds a seasonable food - season the whole stack, or refuse it outright.
        ResourceLocation seasoningId = seasoningId();
        if (SeasoningHelper.canSeason(target, seasoningId)) {
            int count = target.getCount();
            int uses = getUses(stack);
            if (uses < count) {
                // Deliberately all-or-nothing: partially seasoning would mean splitting the stack and
                // finding a home for the remainder while the cursor is occupied, i.e. inventory
                // insertion or item drops inside a handler that runs on both sides. Consume the click
                // so it does not fall through to a vanilla stack swap.
                reportInsufficient(player, count, uses);
                return true;
            }
            slot.setByPlayer(SeasoningHelper.season(target, seasoningId, count));
            setCarriedUses(player, stack, uses - count);
            playSeason(player);
            return true;
        }

        return false; // (C) Anything else - vanilla behaviour.
    }

    /** The shaker is sitting in {@code slot} and the player has right-clicked it holding {@code other}. */
    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (!isUsable(stack, action) || !slot.allowModification(player)) {
            return false;
        }

        // (A) The cursor holds this shaker's seasoning - refill.
        if (isOwnSeasoning(other)) {
            int uses = getUses(stack);
            int consumed = refillCost(uses, other.getCount());
            if (consumed <= 0) {
                return false; // Already full, or nothing to take.
            }
            access.set(shrunk(other, consumed));
            stack.set(ModDataComponents.SHAKER_USES.get(),
                    Math.min(uses + consumed * usesPerRefillItem(), capacity()));
            slot.setChanged();
            playRefill(player);
            return true;
        }

        // (B) The cursor holds a seasonable food.
        ResourceLocation seasoningId = seasoningId();
        if (SeasoningHelper.canSeason(other, seasoningId)) {
            int count = other.getCount();
            int uses = getUses(stack);
            if (uses < count) {
                reportInsufficient(player, count, uses);
                return true;
            }
            access.set(SeasoningHelper.season(other, seasoningId, count));
            setSlotUses(slot, stack, uses - count);
            playSeason(player);
            return true;
        }

        return false; // (C)
    }

    /**
     * A shaker only ever acts on a right-click, and only as a single item - the filled shakers are
     * {@code stacksTo(1)}, so this is an invariant guard rather than a real branch.
     *
     * <p>Also refuses to act if a pack has taken this shaker's seasoning out of
     * {@code #saltandpepper:seasonings}, which is what disables the crafting-table path. The two must
     * stand or fall together.
     */
    private boolean isUsable(ItemStack stack, ClickAction action) {
        return action == ClickAction.SECONDARY
                && stack.getCount() == 1
                && SeasoningHelper.isSeasoning(new ItemStack(this.seasoning.get()));
    }

    private boolean isOwnSeasoning(ItemStack stack) {
        return stack.is(this.seasoning.get());
    }

    private ResourceLocation seasoningId() {
        return BuiltInRegistries.ITEM.getKey(this.seasoning.get());
    }

    /** Spends uses on the cursor-held shaker, leaving an empty shaker behind at zero. */
    private static void setCarriedUses(Player player, ItemStack shaker, int uses) {
        if (uses <= 0) {
            player.containerMenu.setCarried(new ItemStack(ModItems.EMPTY_SHAKER.get()));
        } else {
            shaker.set(ModDataComponents.SHAKER_USES.get(), uses);
        }
    }

    /** Spends uses on a shaker sitting in a slot, leaving an empty shaker behind at zero. */
    private static void setSlotUses(Slot slot, ItemStack shaker, int uses) {
        if (uses <= 0) {
            slot.setByPlayer(new ItemStack(ModItems.EMPTY_SHAKER.get()));
        } else {
            shaker.set(ModDataComponents.SHAKER_USES.get(), uses);
            slot.setChanged();
        }
    }

    // -- Feedback -------------------------------------------------------------------------------

    static void playSeason(Player player) {
        player.playSound(SoundEvents.BONE_MEAL_USE, 0.8F, 0.9F + player.level().getRandom().nextFloat() * 0.3F);
    }

    static void playRefill(Player player) {
        player.playSound(SoundEvents.BONE_MEAL_USE, 0.5F, 0.6F + player.level().getRandom().nextFloat() * 0.2F);
    }

    static void playFailure(Player player) {
        player.playSound(SoundEvents.DISPENSER_FAIL, 0.4F, 1.0F);
    }

    /** Action bar feedback. Server side only - the client already played the failure click. */
    private void reportInsufficient(Player player, int needed, int available) {
        playFailure(player);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.saltandpepper.shaker_insufficient",
                    Component.translatable(this.seasoningNameKey),
                    needed,
                    available).withStyle(ChatFormatting.RED), true);
        }
    }

    // -- Display --------------------------------------------------------------------------------

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getUses(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Mth.ceil(MAX_BAR_WIDTH * (float) getUses(stack) / capacity()), 0, MAX_BAR_WIDTH);
    }

    /** Coloured to match the contents rather than using the vanilla green-to-red damage gradient. */
    @Override
    public int getBarColor(ItemStack stack) {
        return this.barColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.saltandpepper.shaker_uses", getUses(stack), capacity())
                .withStyle(ChatFormatting.GRAY));
    }
}

package com.fiskerz.saltandpepper.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fiskerz.saltandpepper.SeasoningEffectHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Applies the seasoning bonus the moment a seasoned food finishes being eaten.
 *
 * <h2>Why a mixin at all</h2>
 *
 * <p>The Forge 1.20.1 branch hooks {@code LivingEntityUseItemEvent.Finish}. Fabric API 0.92.11 has no
 * equivalent: {@code UseItemCallback} fires when use <em>begins</em> and never sees completion, and
 * {@code ServerLivingEntityEvents} carries only damage, death and mob-conversion events. Every other
 * module was checked too. So one mixin it is.
 *
 * <h2>Why this target</h2>
 *
 * <p>{@code Player.eat(Level, ItemStack)} is the narrowest method that sees both the consumed stack
 * and the player's food data, and it exists in 1.20.1 with exactly this signature (verified against
 * the Loom-mapped 1.20.1 jar, not assumed). It is reached only from {@code Item#finishUsingItem}, i.e.
 * only when consumption actually completes, which is what makes the three awkward cases fall out for
 * free:
 *
 * <ul>
 *   <li><b>Interrupted eating</b> - the method is never called, so there is nothing to undo. This is
 *       exactly why polling the use-item state per tick was rejected.</li>
 *   <li><b>Offhand</b> - the stack arrives as a parameter rather than being read back off the main
 *       hand, so which hand it came from is irrelevant.</li>
 *   <li><b>Creative</b> - vanilla still routes through here; only the stack shrink is skipped.</li>
 * </ul>
 *
 * <h2>Why the injection point is this precise</h2>
 *
 * <p>It has to sit in the window <em>after</em> {@code getFoodData().eat(item, stack)} - the handler
 * adds the difference between the seasoned and unseasoned values, so vanilla must already have
 * applied the base food, and the two clamps have to happen in that order - but <em>before</em>
 * {@code super.eat(...)} shrinks the stack.
 *
 * <p>{@code RETURN} looks like it would work and does not. By then {@code shrink(1)} has run, and
 * when the eaten stack held a single item - the normal case, since seasoning a food by crafting
 * yields exactly one - the count reaches zero and {@code ItemStack.getItem()} answers
 * {@code Items.AIR}, because 1.20.1 guards that getter with an {@code isEmpty()} check. The food
 * properties then come back null and the bonus is silently skipped. ({@code getTag()} carries no such
 * guard, so the seasoning NBT reads back fine - which is what makes the failure look so puzzling.)
 *
 * <p>Anchoring to the {@code FoodData.eat} call instead lands between the two, where the stack is
 * still intact. The Forge branch avoids the whole issue because its event hands back a copy of the
 * stack taken before consumption.
 */
@Mixin(Player.class)
public abstract class PlayerEatMixin {
    @Inject(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER))
    private void saltandpepper$applySeasoningBonus(Level level, ItemStack food, CallbackInfoReturnable<ItemStack> cir) {
        SeasoningEffectHandler.applyOnEaten((Player) (Object) this, level, food);
    }
}

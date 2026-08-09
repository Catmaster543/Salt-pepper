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
 * <p>Injecting at {@code RETURN} matters. The first thing {@code Player.eat} does is
 * {@code getFoodData().eat(item, stack)}, so by the time this fires vanilla has already applied the
 * base food and the handler can add the difference. The stack has also been shrunk by then, possibly
 * to zero, but {@code ItemStack.getTag()} on 1.20.1 returns the tag regardless of count, so the
 * seasoning NBT is still readable.
 */
@Mixin(Player.class)
public abstract class PlayerEatMixin {
    @Inject(method = "eat", at = @At("RETURN"))
    private void saltandpepper$applySeasoningBonus(Level level, ItemStack food, CallbackInfoReturnable<ItemStack> cir) {
        SeasoningEffectHandler.applyOnEaten((Player) (Object) this, level, food);
    }
}

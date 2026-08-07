package com.fiskerz.saltandpepper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Step 1 of the pepper chain: blanching green peppercorns in a water cauldron sitting on a heat source.
 *
 * <p>Implemented as an interaction event handler rather than by overriding the cauldron block, so
 * vanilla cauldrons keep all their normal behaviour and no block entity is needed.
 */
@EventBusSubscriber(modid = SaltandPepper.MODID)
public final class CauldronBlanchingHandler {
    private CauldronBlanchingHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // The event fires for both hands; only the main hand should blanch, otherwise a player holding
        // peppercorns in both hands would convert two batches from one click.
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (player.isSecondaryUseActive()) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (!held.is(ModItems.GREEN_PEPPERCORNS.get())) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState cauldron = level.getBlockState(pos);

        if (!cauldron.is(Blocks.WATER_CAULDRON) || cauldron.getValue(LayeredCauldronBlock.LEVEL) < 1) {
            return;
        }
        if (!isHeated(level, pos.below())) {
            return;
        }

        // Consume the interaction on both sides so the arm swings client-side and nothing else fires.
        // 26.1 removed InteractionResult.sidedSuccess; SUCCESS is the sealed-interface constant that
        // swings the arm client-side, matching what vanilla block interactions now return.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (level.isClientSide()) {
            return;
        }

        int batch = Math.min(held.getCount(), Config.BLANCH_BATCH_SIZE.get());
        if (!player.getAbilities().instabuild) {
            held.shrink(batch);
        }

        ItemStack blanched = new ItemStack(ModItems.BLANCHED_PEPPERCORNS.get(), batch);
        if (!player.getInventory().add(blanched)) {
            player.drop(blanched, false);
        }

        // Uses one level of water; reverts to an empty cauldron at level 0.
        LayeredCauldronBlock.lowerFillLevel(cauldron, level, pos);

        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.6F + level.getRandom().nextFloat() * 0.4F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    8, 0.2, 0.1, 0.2, 0.01);
        }
    }

    /**
     * A block heats the cauldron above it if it is in {@code #saltandpepper:heat_sources}. Campfires
     * additionally have to be lit, which a tag cannot express.
     */
    private static boolean isHeated(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModTags.Blocks.HEAT_SOURCES)) {
            return false;
        }
        if (state.getBlock() instanceof CampfireBlock) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return true;
    }
}

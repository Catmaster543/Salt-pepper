package com.fiskerz.saltandpepper;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * Step 1 of the pepper chain: blanching green peppercorns in a water cauldron sitting on a heat source.
 *
 * <p>Implemented as an interaction callback rather than by overriding the cauldron block, so vanilla
 * cauldrons keep all their normal behaviour and no block entity is needed.
 *
 * <p>NeoForge used {@code PlayerInteractEvent.RightClickBlock}; the Fabric equivalent is
 * {@link UseBlockCallback}, which fires at the same point (before the block's own interaction) and
 * likewise once per hand. Returning anything other than {@code PASS} cancels further processing, which
 * is what the NeoForge version achieved by cancelling the event and setting a success result.
 */
public final class CauldronBlanchingHandler {
    private CauldronBlanchingHandler() {}

    public static void register() {
        UseBlockCallback.EVENT.register(CauldronBlanchingHandler::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        // The callback fires for both hands; only the main hand should blanch, otherwise a player holding
        // peppercorns in both hands would convert two batches from one click.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.GREEN_PEPPERCORNS)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState cauldron = level.getBlockState(pos);

        if (!cauldron.is(Blocks.WATER_CAULDRON) || cauldron.getValue(LayeredCauldronBlock.LEVEL) < 1) {
            return InteractionResult.PASS;
        }
        if (!isHeated(level, pos.below())) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            // Consume the interaction on both sides so the arm swings client-side and nothing else fires.
            // 26.1 removed InteractionResult.sidedSuccess; SUCCESS is the constant carrying
            // SwingSource.CLIENT, which is what sidedSuccess(true) used to return.
            return InteractionResult.SUCCESS;
        }

        int batch = Math.min(held.getCount(), Config.BLANCH_BATCH_SIZE.get());
        if (!player.getAbilities().instabuild) {
            held.shrink(batch);
        }

        ItemStack blanched = new ItemStack(ModItems.BLANCHED_PEPPERCORNS, batch);
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

        // sidedSuccess(false) was CONSUME: the client has already swung, so the server must not
        // trigger a second swing.
        return InteractionResult.CONSUME;
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

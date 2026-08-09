package com.fiskerz.saltandpepper;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Cocoa-pod-style pepper vine. Geometry, growth pacing and shapes are taken verbatim from vanilla
 * {@link net.minecraft.world.level.block.CocoaBlock}; the differences are:
 *
 * <ul>
 *   <li>attaches to anything in {@code #saltandpepper:pepper_vine_supports} instead of hard-coded jungle logs</li>
 *   <li>right-clicking a mature vine harvests peppercorns and resets it to age 0</li>
 *   <li>growth can optionally be restricted to jungle biomes via config</li>
 * </ul>
 *
 * <p>1.20.1 differences from the 1.21.1 build: block overrides are {@code public} rather than
 * {@code protected}, there is no {@code MapCodec}/{@code codec()} (that arrives in 1.20.5),
 * {@code isValidBonemealTarget} and {@code isPathfindable} take an extra parameter, and the
 * right-click hook is {@code use(...)} rather than {@code useWithoutItem(...)}.
 */
public class PepperVineBlock extends HorizontalDirectionalBlock implements BonemealableBlock {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;

    // Verbatim from CocoaBlock so the pods sit on the log face exactly like vanilla cocoa.
    protected static final VoxelShape[] EAST_AABB = new VoxelShape[] {
            Block.box(11.0, 7.0, 6.0, 15.0, 12.0, 10.0), Block.box(9.0, 5.0, 5.0, 15.0, 12.0, 11.0), Block.box(7.0, 3.0, 4.0, 15.0, 12.0, 12.0)
    };
    protected static final VoxelShape[] WEST_AABB = new VoxelShape[] {
            Block.box(1.0, 7.0, 6.0, 5.0, 12.0, 10.0), Block.box(1.0, 5.0, 5.0, 7.0, 12.0, 11.0), Block.box(1.0, 3.0, 4.0, 9.0, 12.0, 12.0)
    };
    protected static final VoxelShape[] NORTH_AABB = new VoxelShape[] {
            Block.box(6.0, 7.0, 1.0, 10.0, 12.0, 5.0), Block.box(5.0, 5.0, 1.0, 11.0, 12.0, 7.0), Block.box(4.0, 3.0, 1.0, 12.0, 12.0, 9.0)
    };
    protected static final VoxelShape[] SOUTH_AABB = new VoxelShape[] {
            Block.box(6.0, 7.0, 11.0, 10.0, 12.0, 15.0), Block.box(5.0, 5.0, 9.0, 11.0, 12.0, 15.0), Block.box(4.0, 3.0, 7.0, 12.0, 12.0, 15.0)
    };

    public PepperVineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
    }

    // -- Attachment -----------------------------------------------------------------------------

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(state.getValue(FACING))).is(ModTags.Blocks.PEPPER_VINE_SUPPORTS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                state = state.setValue(FACING, direction);
                if (state.canSurvive(level, pos)) {
                    return state;
                }
            }
        }

        // No supporting log on any horizontal face - reject the placement entirely.
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing == state.getValue(FACING) && !state.canSurvive(level, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_AABB[age];
            case WEST -> WEST_AABB[age];
            case EAST -> EAST_AABB[age];
            default -> NORTH_AABB[age];
        };
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathComputationType) {
        return false;
    }

    // -- Growth ---------------------------------------------------------------------------------

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) {
            return;
        }
        if (Config.RESTRICT_GROWTH_TO_JUNGLE.get() && !isJungle(level, pos)) {
            return;
        }
        // Same 1-in-5 pacing as cocoa. The Forge branch wrapped this in ForgeHooks.onCropsGrowPre/Post
        // so other mods could veto or observe the growth; Fabric has no equivalent hook, so the growth
        // is unconditional here, exactly as vanilla CocoaBlock does it.
        if (random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Vanilla's jungle tag plus the loader's convention tag, so modded jungle analogues count too.
     * On Fabric API 0.92.11 the convention tags are the <em>v1</em> set, where the constant is
     * {@code JUNGLE} ({@code #c:jungle}) rather than 1.21.1's v2 {@code IS_JUNGLE} ({@code #c:is_jungle}).
     */
    private static boolean isJungle(LevelReader level, BlockPos pos) {
        var biome = level.getBiome(pos);
        return biome.is(BiomeTags.IS_JUNGLE) || biome.is(ConventionalBiomeTags.JUNGLE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), Block.UPDATE_CLIENTS);
    }

    // -- Harvest --------------------------------------------------------------------------------

    /**
     * Right-click harvest. On 1.20.1 this is {@code use(...)}, which fires with an empty hand and with
     * any item that has no block interaction of its own, matching sweet berry bushes. Drops 2-3 green
     * peppercorns, no seeds, and resets to age 0.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(AGE) < MAX_AGE) {
            return super.use(state, level, pos, player, hand, hitResult);
        }

        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(ModItems.GREEN_PEPPERCORNS, 2 + level.random.nextInt(2)));
            BlockState reset = state.setValue(AGE, 0);
            level.setBlock(pos, reset, Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, reset));
        }
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                1.0F, 0.8F + level.random.nextFloat() * 0.4F);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

package com.fiskerz.saltandpepper;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Scatters mature pepper vines onto jungle tree trunks.
 *
 * <p>Vanilla attaches cocoa pods with a tree decorator baked into the jungle tree features themselves,
 * which cannot be extended without overwriting those features. This instead runs as an ordinary
 * {@code vegetal_decoration} feature that looks for already-generated trunks near the origin.
 *
 * <p>The placed feature puts the origin at the top of the canopy ({@code heightmap MOTION_BLOCKING}),
 * so each attempt walks a whole vertical column downward to find the trunk rather than sampling a
 * random y - a canopy-relative random y would almost always miss the trunk entirely.
 *
 * <p>Overall density comes from the placed feature's {@code count}; this class caps how many pods one
 * placement may drop, so a single tree rarely carries more than one or two.
 */
public class WildPepperVineFeature extends Feature<NoneFeatureConfiguration> {
    private static final int HORIZONTAL_RADIUS = 7;
    /** How far above the origin to start scanning, to catch trunks poking above the sampled column. */
    private static final int SCAN_ABOVE = 4;
    /** How far below the origin to scan. Comfortably covers a jungle trunk from canopy to root. */
    private static final int SCAN_BELOW = 32;
    private static final int COLUMN_ATTEMPTS = 10;
    private static final int MAX_VINES_PER_PLACEMENT = 2;

    public WildPepperVineFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int placed = 0;

        for (int attempt = 0; attempt < COLUMN_ATTEMPTS && placed < MAX_VINES_PER_PLACEMENT; attempt++) {
            int x = origin.getX() + random.nextInt(HORIZONTAL_RADIUS * 2 + 1) - HORIZONTAL_RADIUS;
            int z = origin.getZ() + random.nextInt(HORIZONTAL_RADIUS * 2 + 1) - HORIZONTAL_RADIUS;

            List<Candidate> candidates = scanColumn(level, x, z, origin.getY());
            if (candidates.isEmpty()) {
                continue;
            }

            Candidate chosen = candidates.get(random.nextInt(candidates.size()));
            BlockState vine = ModBlocks.PEPPER_VINE.defaultBlockState()
                    // FACING points from the vine back at its supporting log, as in CocoaBlock.
                    .setValue(PepperVineBlock.FACING, chosen.faceToLog)
                    .setValue(PepperVineBlock.AGE, PepperVineBlock.MAX_AGE);

            level.setBlock(chosen.vinePos, vine, Block.UPDATE_CLIENTS);
            placed++;
        }

        return placed > 0;
    }

    /** A spot where a vine could hang: the air block it occupies, and the way it faces its log. */
    private record Candidate(BlockPos vinePos, Direction faceToLog) {}

    private static List<Candidate> scanColumn(WorldGenLevel level, int x, int z, int originY) {
        List<Candidate> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = originY + SCAN_ABOVE; y >= originY - SCAN_BELOW; y--) {
            cursor.set(x, y, z);
            if (level.isOutsideBuildHeight(cursor)) {
                continue;
            }
            if (!level.getBlockState(cursor).is(ModTags.Blocks.PEPPER_VINE_SUPPORTS)) {
                continue;
            }

            Direction faceToLog = pickExposedFace(level, cursor);
            if (faceToLog != null) {
                candidates.add(new Candidate(cursor.relative(faceToLog.getOpposite()).immutable(), faceToLog));
            }
        }

        return candidates;
    }

    /**
     * Returns the direction pointing from an adjacent air block back at {@code logPos}, or null when
     * the log has no horizontally exposed side.
     */
    @Nullable
    private static Direction pickExposedFace(WorldGenLevel level, BlockPos logPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.isEmptyBlock(logPos.relative(direction))) {
                return direction.getOpposite();
            }
        }
        return null;
    }
}

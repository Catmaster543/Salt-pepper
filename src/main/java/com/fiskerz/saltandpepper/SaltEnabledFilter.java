package com.fiskerz.saltandpepper;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Placement modifier that drops every position when this mod's salt is disabled - either by config or
 * because Salt: Renewed is installed.
 *
 * <p>This exists because a biome modification cannot cleanly be retracted at runtime: the modified
 * modifiers are a datapack registry baked into the biome source when the level loads, and there is no
 * supported way to retract one afterwards based on a config value. Putting the guard in the placement
 * chain keeps the ore feature itself vanilla ({@code minecraft:ore}) while still letting the decision
 * be made per-world at generation time.
 */
public class SaltEnabledFilter extends PlacementFilter {
    public static final SaltEnabledFilter INSTANCE = new SaltEnabledFilter();
    public static final MapCodec<SaltEnabledFilter> CODEC = MapCodec.unit(INSTANCE);

    private SaltEnabledFilter() {}

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return Config.saltEnabled();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldgen.SALT_ENABLED_FILTER;
    }
}

package com.fiskerz.saltandpepper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    private ModBlocks() {}

    /** Properties copied from vanilla {@code Blocks.COCOA}. */
    public static final PepperVineBlock PEPPER_VINE = register("pepper_vine",
            new PepperVineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.2F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    /**
     * Rock salt ore. Hardness/resistance match coal ore, but deliberately without
     * {@code requiresCorrectToolForDrops} - salt is soft, so any pickaxe works.
     */
    // On 1.20.1 the xp range is the SECOND argument to DropExperienceBlock; 1.21.1 takes it first.
    // Same range either way, so the drops are unchanged.
    public static final Block ROCK_SALT_ORE = register("rock_salt_ore",
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F, 3.0F), UniformInt.of(0, 2)));

    public static final Block DEEPSLATE_ROCK_SALT_ORE = register("deepslate_rock_salt_ore",
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE), UniformInt.of(0, 2)));

    public static final Block SALT_BLOCK = register("salt_block",
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(1.0F, 1.0F)
                    .sound(SoundType.SAND)));

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, SaltandPepper.id(name), block);
    }

    /** Forces class initialisation, which is what actually performs the registrations above. */
    static void init() {}
}

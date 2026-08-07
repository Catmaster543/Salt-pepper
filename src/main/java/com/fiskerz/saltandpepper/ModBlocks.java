package com.fiskerz.saltandpepper;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SaltandPepper.MODID);

    // 26.1: blocks must carry a ResourceKey in their Properties. The registerBlock/registerSimpleBlock
    // overloads that take a properties operator set that id for us; the older register(name, Supplier)
    // form does not, and throws at construction.

    /** Properties copied from vanilla {@code Blocks.COCOA}. */
    public static final DeferredBlock<PepperVineBlock> PEPPER_VINE = BLOCKS.registerBlock("pepper_vine",
            PepperVineBlock::new,
            props -> props
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.2F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY));

    /**
     * Rock salt ore. Hardness/resistance match coal ore, but deliberately without
     * {@code requiresCorrectToolForDrops} - salt is soft, so any pickaxe works.
     */
    public static final DeferredBlock<Block> ROCK_SALT_ORE = BLOCKS.registerBlock("rock_salt_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            props -> props
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F, 3.0F));

    public static final DeferredBlock<Block> DEEPSLATE_ROCK_SALT_ORE = BLOCKS.registerBlock("deepslate_rock_salt_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            props -> props
                    .mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE));

    public static final DeferredBlock<Block> SALT_BLOCK = BLOCKS.registerSimpleBlock("salt_block",
            props -> props
                    .mapColor(MapColor.SNOW)
                    .strength(1.0F, 1.0F)
                    .sound(SoundType.SAND));
}

package com.fiskerz.saltandpepper;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SaltandPepper.MODID);

    /** Properties copied from vanilla {@code Blocks.COCOA}. */
    public static final RegistryObject<PepperVineBlock> PEPPER_VINE = BLOCKS.register("pepper_vine",
            () -> new PepperVineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.2F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    /**
     * Rock salt ore. Hardness/resistance match coal ore, but deliberately without
     * {@code requiresCorrectToolForDrops} - salt is soft, so any pickaxe works.
     *
     * <p>1.20.1 takes the xp range as the <em>second</em> constructor argument; 1.21.1 takes it first.
     */
    public static final RegistryObject<Block> ROCK_SALT_ORE = BLOCKS.register("rock_salt_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F, 3.0F), UniformInt.of(0, 2)));

    public static final RegistryObject<Block> DEEPSLATE_ROCK_SALT_ORE = BLOCKS.register("deepslate_rock_salt_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE), UniformInt.of(0, 2)));

    public static final RegistryObject<Block> SALT_BLOCK = BLOCKS.register("salt_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(1.0F, 1.0F)
                    .sound(SoundType.SAND)));
}

package com.fiskerz.saltandpepper;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
    public static final Block ROCK_SALT_ORE = register("rock_salt_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            props -> props
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F, 3.0F));

    public static final Block DEEPSLATE_ROCK_SALT_ORE = register("deepslate_rock_salt_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            props -> props
                    .mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE));

    public static final Block SALT_BLOCK = register("salt_block",
            Block::new,
            props -> props
                    .mapColor(MapColor.SNOW)
                    .strength(1.0F, 1.0F)
                    .sound(SoundType.SAND));

    /**
     * 26.1 requires every block's {@link BlockBehaviour.Properties} to carry its {@link ResourceKey}
     * before construction. NeoForge's {@code DeferredRegister.Blocks#registerBlock} did that for us;
     * a plain {@link Registry#register} does not, so this mirrors what vanilla {@code Blocks#register}
     * does: apply the id to the properties, build the block, then register it.
     */
    private static <T extends Block> T register(String name,
            Function<BlockBehaviour.Properties, T> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, SaltandPepper.id(name));
        T block = factory.apply(properties.apply(BlockBehaviour.Properties.of()).setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    /** Forces class initialisation, which is what actually performs the registrations above. */
    static void init() {}
}

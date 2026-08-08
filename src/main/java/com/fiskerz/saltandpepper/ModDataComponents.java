package com.fiskerz.saltandpepper;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    private ModDataComponents() {}

    /**
     * The seasonings applied to a food stack, as the item ids of the seasonings themselves
     * (e.g. {@code saltandpepper:salt}). Kept as a plain id list rather than a bespoke record so
     * third-party seasonings added to {@code #saltandpepper:seasonings} work without code changes.
     */
    public static final DataComponentType<List<Identifier>> SEASONINGS = register("seasonings",
            DataComponentType.<List<Identifier>>builder()
                    .persistent(Identifier.CODEC.listOf())
                    .networkSynchronized(Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    /**
     * How many uses a shaker has left. Deliberately not vanilla durability: damage would make the
     * shaker repairable on an anvil, enchantable with Mending and Unbreaking, and would destroy the
     * item at zero instead of leaving an {@code empty_shaker} behind.
     *
     * <p>An <em>absent</em> component means "full to the current {@code shakerCapacity}", which is how
     * a freshly crafted shaker can start full without the crafting recipe having to hardcode the
     * configured capacity. See {@link ShakerItem#getUses}.
     */
    public static final DataComponentType<Integer> SHAKER_USES = register("shaker_uses",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, SaltandPepper.id(name), type);
    }

    /** Forces class initialisation, which is what actually performs the registrations above. */
    static void init() {}
}

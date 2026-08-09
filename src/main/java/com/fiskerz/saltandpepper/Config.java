package com.fiskerz.saltandpepper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Config, written to {@code config/saltandpepper.json}.
 *
 * <p>Fabric ships no config system, so this is hand-rolled against Gson (which Minecraft already
 * bundles) rather than taking a Cloth Config dependency for ten values. The section and key names,
 * the defaults and the valid ranges are all identical to the NeoForge build's
 * {@code saltandpepper-common.toml}; only the file name and format differ. The
 * {@code SECTION.key.get()} accessor shape is preserved too, so call sites read the same on both
 * loaders.
 *
 * <p>Balance note on the seasoning bonuses: in 1.21.1 {@link net.minecraft.world.food.FoodProperties}
 * stores <em>absolute</em> saturation, not the modifier that authors write in a builder. The two are
 * related by {@link net.minecraft.world.food.FoodConstants#saturationByModifier}, which is
 * {@code saturation = nutrition * saturationModifier * 2}. So the modifier scales with how filling
 * the food already is: +0.2 on a 1-nutrition berry is worth almost nothing, while +0.2 on an
 * 8-nutrition steak is worth 3.2 saturation. One nutrition point is half a hunger shank, so salt and
 * pepper together give +2 nutrition (one full shank) on a fully seasoned food.
 */
public final class Config {
    private Config() {}

    private static final String FILE_NAME = "saltandpepper.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Declared before every value below, because static initialisers run in declaration order. */
    private static final List<Value<?>> VALUES = new ArrayList<>();

    // -- Seasoning ------------------------------------------------------------------------------

    /** Upper clamp on the nutrition of a seasoned food. The saturation modifier is always clamped to 2.0. */
    public static final IntValue MAX_NUTRITION = new IntValue("seasoning", "maxNutrition", 20, 0, 100);

    // -- Pepper ---------------------------------------------------------------------------------

    public static final IntValue PEPPER_BONUS_NUTRITION = new IntValue("pepper", "bonusNutrition", 1, 0, 20);
    public static final DoubleValue PEPPER_BONUS_SATURATION_MODIFIER =
            new DoubleValue("pepper", "bonusSaturationModifier", 0.2D, 0.0D, 2.0D);

    // -- Salt -----------------------------------------------------------------------------------

    public static final IntValue SALT_BONUS_NUTRITION = new IntValue("salt", "bonusNutrition", 1, 0, 20);
    public static final DoubleValue SALT_BONUS_SATURATION_MODIFIER =
            new DoubleValue("salt", "bonusSaturationModifier", 0.2D, 0.0D, 2.0D);

    /**
     * Whether this mod's own salt ore, items and recipes are enabled. Forced off at runtime if
     * Salt: Renewed (mod id 'salt') is installed, so the two do not duplicate each other.
     */
    public static final BooleanValue ENABLE_SALT = new BooleanValue("salt", "enableSalt", true);

    // -- Shakers --------------------------------------------------------------------------------

    public static final IntValue SHAKER_CAPACITY = new IntValue("shaker", "shakerCapacity", 64, 1, 1024);
    public static final IntValue USES_PER_REFILL_ITEM = new IntValue("shaker", "usesPerRefillItem", 8, 1, 1024);

    // -- Pepper vine ----------------------------------------------------------------------------

    /**
     * When true, pepper vines only advance an age stage inside biomes tagged {@code #minecraft:is_jungle}
     * or {@code #c:is_jungle}. Bone meal is unaffected.
     */
    public static final BooleanValue RESTRICT_GROWTH_TO_JUNGLE =
            new BooleanValue("pepper_vine", "restrictGrowthToJungle", false);

    /** Maximum green peppercorns converted per cauldron interaction. */
    public static final IntValue BLANCH_BATCH_SIZE = new IntValue("pepper_vine", "blanchBatchSize", 8, 1, 64);

    // -- Loading --------------------------------------------------------------------------------

    /**
     * Reads the config file, falling back to defaults for anything missing or out of range, then writes
     * the file back so it always contains the full set of keys.
     */
    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

        JsonObject root = new JsonObject();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonElement parsed = GSON.fromJson(reader, JsonElement.class);
                if (parsed != null && parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                } else {
                    SaltandPepper.LOGGER.warn("{} is not a JSON object; falling back to defaults.", FILE_NAME);
                }
            } catch (IOException | RuntimeException e) {
                SaltandPepper.LOGGER.warn("Could not read {}; falling back to defaults.", FILE_NAME, e);
            }
        }

        for (Value<?> value : VALUES) {
            value.read(root);
        }

        save(path);
    }

    private static void save(Path path) {
        JsonObject root = new JsonObject();
        for (Value<?> value : VALUES) {
            value.write(root);
        }

        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            SaltandPepper.LOGGER.warn("Could not write {}.", FILE_NAME, e);
        }
    }

    /** Convenience accessor that also honours the Salt: Renewed runtime override. */
    public static boolean saltEnabled() {
        return ENABLE_SALT.get() && !SaltCompat.isSaltRenewedLoaded();
    }

    // -- Value types ----------------------------------------------------------------------------

    private abstract static class Value<T> {
        final String section;
        final String key;

        Value(String section, String key) {
            this.section = section;
            this.key = key;
            VALUES.add(this);
        }

        /** The value's own object, or null when the file has no such section. */
        final JsonObject sectionOf(JsonObject root) {
            JsonElement element = root.get(this.section);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        }

        final JsonObject orCreateSection(JsonObject root) {
            JsonElement element = root.get(this.section);
            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            }
            JsonObject created = new JsonObject();
            root.add(this.section, created);
            return created;
        }

        abstract void read(JsonObject root);

        abstract void write(JsonObject root);
    }

    public static final class IntValue extends Value<Integer> {
        private final int defaultValue;
        private final int min;
        private final int max;
        private int value;

        IntValue(String section, String key, int defaultValue, int min, int max) {
            super(section, key);
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
            this.value = defaultValue;
        }

        public int get() {
            return this.value;
        }

        @Override
        void read(JsonObject root) {
            this.value = this.defaultValue;
            JsonObject section = sectionOf(root);
            if (section == null || !section.has(this.key)) {
                return;
            }
            try {
                int raw = section.get(this.key).getAsInt();
                this.value = Math.max(this.min, Math.min(this.max, raw));
            } catch (RuntimeException e) {
                SaltandPepper.LOGGER.warn("{}.{} in {} is not an integer; using {}.",
                        this.section, this.key, FILE_NAME, this.defaultValue);
            }
        }

        @Override
        void write(JsonObject root) {
            orCreateSection(root).addProperty(this.key, this.value);
        }
    }

    public static final class DoubleValue extends Value<Double> {
        private final double defaultValue;
        private final double min;
        private final double max;
        private double value;

        DoubleValue(String section, String key, double defaultValue, double min, double max) {
            super(section, key);
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
            this.value = defaultValue;
        }

        public double get() {
            return this.value;
        }

        @Override
        void read(JsonObject root) {
            this.value = this.defaultValue;
            JsonObject section = sectionOf(root);
            if (section == null || !section.has(this.key)) {
                return;
            }
            try {
                double raw = section.get(this.key).getAsDouble();
                this.value = Math.max(this.min, Math.min(this.max, raw));
            } catch (RuntimeException e) {
                SaltandPepper.LOGGER.warn("{}.{} in {} is not a number; using {}.",
                        this.section, this.key, FILE_NAME, this.defaultValue);
            }
        }

        @Override
        void write(JsonObject root) {
            orCreateSection(root).addProperty(this.key, this.value);
        }
    }

    public static final class BooleanValue extends Value<Boolean> {
        private final boolean defaultValue;
        private boolean value;

        BooleanValue(String section, String key, boolean defaultValue) {
            super(section, key);
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public boolean get() {
            return this.value;
        }

        @Override
        void read(JsonObject root) {
            this.value = this.defaultValue;
            JsonObject section = sectionOf(root);
            if (section == null || !section.has(this.key)) {
                return;
            }
            try {
                this.value = section.get(this.key).getAsBoolean();
            } catch (RuntimeException e) {
                SaltandPepper.LOGGER.warn("{}.{} in {} is not a boolean; using {}.",
                        this.section, this.key, FILE_NAME, this.defaultValue);
            }
        }

        @Override
        void write(JsonObject root) {
            orCreateSection(root).addProperty(this.key, this.value);
        }
    }
}

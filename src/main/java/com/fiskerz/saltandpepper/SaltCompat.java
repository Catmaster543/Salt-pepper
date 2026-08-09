package com.fiskerz.saltandpepper;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Runtime guard against <a href="https://modrinth.com/mod/salt">Salt: Renewed</a> (mod id {@code salt}).
 *
 * <p>When that mod is present our salt content stands down completely: worldgen is skipped, the items
 * are hidden from the creative tab, and the ore/refining recipes drop out via a
 * {@code fabric:not(fabric:all_mods_loaded)} resource condition. Their salt is added to
 * {@code #saltandpepper:seasonings} by an optional tag entry so it drives our seasoning system instead.
 *
 * <p>Registry entries cannot be conditional, so the items and blocks are always registered - only
 * visibility, worldgen and recipes are gated.
 */
public final class SaltCompat {
    private SaltCompat() {}

    public static final String SALT_RENEWED_MODID = "salt";

    /** Cached because this is polled from worldgen and creative tab building. */
    private static Boolean loaded;
    private static boolean loggedNotice;

    public static boolean isSaltRenewedLoaded() {
        Boolean cached = loaded;
        if (cached == null) {
            cached = FabricLoader.getInstance().isModLoaded(SALT_RENEWED_MODID);
            loaded = cached;
        }
        return cached;
    }

    /**
     * Logs the single INFO line explaining the automatic shutdown. Called once from mod initialisation.
     *
     * <p>The config value itself is deliberately left untouched rather than force-written, because
     * {@link Config#saltEnabled()} already folds this check in for every consumer.
     */
    public static void logIfDisabled() {
        if (isSaltRenewedLoaded() && !loggedNotice) {
            loggedNotice = true;
            SaltandPepper.LOGGER.info(
                    "Salt: Renewed (mod id '{}') is installed, so Salt & Pepper's own salt ore, items and recipes have been "
                            + "disabled to avoid duplicating it. Their salt has been added to #saltandpepper:seasonings and will "
                            + "season food through this mod's seasoning system.",
                    SALT_RENEWED_MODID);
        }
    }
}

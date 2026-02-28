package com.zeldatargeting.mod.compat;

import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraftforge.fml.common.Loader;

/**
 * Compatibility handler for Neat (health bar mod).
 * When both mods are present, applies HUD offset to avoid overlap.
 */
public class CompatNeat {

    private static final String MOD_ID = "neat";

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    /**
     * Returns the extra Y offset (in pixels) to apply to our HUD when Neat is loaded
     * and compat is enabled. Returns 0 if compat is disabled or Neat is not present.
     */
    public static int getHudOffsetY() {
        if (!isLoaded() || !TargetingConfig.neatCompatEnabled) {
            return 0;
        }
        return TargetingConfig.neatCompatOffsetY;
    }
}

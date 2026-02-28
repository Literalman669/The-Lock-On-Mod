package com.zeldatargeting.mod.compat;

import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;

/**
 * Compatibility handler for riding/mounts.
 * When enabled, lock-on is disabled while the player is riding.
 */
public class CompatRiding {

    /**
     * Returns true if lock-on should be blocked (e.g. player is riding and config disables it).
     */
    public static boolean shouldBlockLockOn() {
        if (!TargetingConfig.disableLockOnWhenRiding) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        return mc.player != null && mc.player.isRiding();
    }

    /**
     * Returns true if we should auto-release lock-on (e.g. player started riding).
     */
    public static boolean shouldReleaseLockOn() {
        return shouldBlockLockOn();
    }
}

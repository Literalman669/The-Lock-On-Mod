package com.zeldatargeting.mod.compat;

import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraftforge.fml.common.Loader;

/**
 * Compatibility handler for Better Third Person (BTP).
 * Provides camera behavior adjustments when BTP is present.
 */
public class CompatBTP {

    private static final String MOD_ID = "betterthirdperson";
    private static final float BTP_MIN_SMOOTHING = 0.05f;

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    /**
     * Returns true if we should apply camera rotation. When BTP is in visual_only
     * mode, we skip camera movement to prevent conflicts.
     */
    public static boolean shouldApplyCameraRotation() {
        if (!isLoaded()) return true;
        return !"visual_only".equals(TargetingConfig.btpCompatibilityMode);
    }

    /**
     * Returns the effective smoothing factor when BTP is in gentle mode.
     * Otherwise returns the base smoothing unchanged.
     */
    public static float getEffectiveSmoothing(float baseSmoothing) {
        if (!isLoaded() || !"gentle".equals(TargetingConfig.btpCompatibilityMode)) {
            return baseSmoothing;
        }
        return Math.max(baseSmoothing * TargetingConfig.btpCameraIntensity, BTP_MIN_SMOOTHING);
    }
}

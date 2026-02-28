package com.zeldatargeting.mod.compat;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;

/**
 * Compatibility handler for Shoulder Surfing Reloaded (SSR).
 * Provides crosshair alignment compensation when SSR is active.
 */
public class CompatSSR {

    private static final String MOD_ID = "shouldersurfing";

    private static Boolean loaded;
    private static Method getInstanceMethod;
    private static Method doShoulderSurfingMethod;
    private static Method getOffsetXMethod;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded(MOD_ID);
            if (loaded) {
                initReflection();
            }
        }
        return loaded;
    }

    private static void initReflection() {
        try {
            Class<?> cls = Class.forName("com.teamderpy.shouldersurfing.client.ShoulderInstance");
            getInstanceMethod = cls.getMethod("getInstance");
            doShoulderSurfingMethod = cls.getMethod("doShoulderSurfing");
            getOffsetXMethod = cls.getMethod("getOffsetX");
        } catch (Exception e) {
            ZeldaTargetingMod.getLogger().warn("Failed to cache Shoulder Surfing Reloaded reflection handles; compensation disabled", e);
            loaded = false;
        }
    }

    /**
     * Returns true only when SSR's shoulder-surfing perspective is actually active.
     */
    public static boolean isActive() {
        if (!isLoaded() || getInstanceMethod == null) return false;
        try {
            Object instance = getInstanceMethod.invoke(null);
            return (boolean) doShoulderSurfingMethod.invoke(instance);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns SSR's current runtime x-offset, or the config fallback if reflection fails.
     */
    public static double getOffsetX() {
        if (!isLoaded() || getInstanceMethod == null) return TargetingConfig.ssrXOffset;
        try {
            Object instance = getInstanceMethod.invoke(null);
            return (double) getOffsetXMethod.invoke(instance);
        } catch (Exception e) {
            return TargetingConfig.ssrXOffset;
        }
    }
}

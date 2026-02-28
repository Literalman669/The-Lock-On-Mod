package com.zeldatargeting.mod.compat;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry for mod compatibility handlers.
 * Initialized once in preInit; logs detected mods at startup.
 */
public class ModCompat {

    private static boolean initialized = false;

    public static void init(FMLPreInitializationEvent event) {
        if (initialized) return;
        initialized = true;

        List<String> detected = new ArrayList<>();

        if (CompatBTP.isLoaded()) {
            detected.add("Better Third Person");
        }
        if (CompatSSR.isLoaded()) {
            detected.add("Shoulder Surfing Reloaded");
        }
        if (CompatNeat.isLoaded()) {
            detected.add("Neat");
        }

        if (!detected.isEmpty()) {
            ZeldaTargetingMod.getLogger().info("Mod compatibility: " + String.join(", ", detected));
        }
    }
}

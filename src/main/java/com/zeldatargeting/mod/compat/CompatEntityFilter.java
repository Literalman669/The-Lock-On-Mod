package com.zeldatargeting.mod.compat;

import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity blacklist for modded mobs. Excludes entities matching registry names
 * or class name substrings from targeting.
 */
public class CompatEntityFilter {

    private static Set<String> blacklistSet;
    private static String lastBlacklistConfig = "";

    /**
     * Returns true if the entity should be excluded from targeting.
     */
    public static boolean isBlacklisted(Entity entity) {
        if (entity == null) return false;
        String config = TargetingConfig.entityBlacklist;
        if (config == null || config.trim().isEmpty()) return false;

        // Rebuild set if config changed
        if (!config.equals(lastBlacklistConfig)) {
            lastBlacklistConfig = config;
            blacklistSet = Arrays.stream(config.split("[,;\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }

        if (blacklistSet == null || blacklistSet.isEmpty()) return false;

        // Check registry name (1.12.2 EntityList API)
        ResourceLocation key = EntityList.getKey(entity);
        if (key != null) {
            String regName = key.toString().toLowerCase();
            if (blacklistSet.contains(regName)) return true;
            // Prefix match: "iceandfire:" matches all iceandfire entities
            for (String entry : blacklistSet) {
                if (entry.endsWith(":") && regName.startsWith(entry)) return true;
            }
        }

        // Check class name (for modded entities not in EntityList, or substring match)
        String className = entity.getClass().getName().toLowerCase();
        for (String entry : blacklistSet) {
            // Skip registry-style entries for class check
            if (entry.contains(":")) continue;
            if (className.contains(entry)) return true;
        }

        return false;
    }
}

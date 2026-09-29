package com.zeldatargeting.mod.client.targeting.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Entities the player never wants to lock onto, parsed from a comma, semicolon,
 * or space separated list. Entries match case-insensitively:
 * <ul>
 *     <li>{@code minecraft:iron_golem} matches that registry name exactly.</li>
 *     <li>{@code iceandfire:} matches every entity from that mod.</li>
 *     <li>{@code iron_golem} or {@code golem} (no colon) matches a registry path
 *     exactly, or any entity whose class name contains it.</li>
 * </ul>
 */
public final class EntityBlacklist {
    private static final EntityBlacklist EMPTY = new EntityBlacklist(Collections.<String>emptyList());

    private final List<String> entries;

    private EntityBlacklist(List<String> entries) {
        this.entries = entries;
    }

    public static EntityBlacklist parse(String config) {
        if (config == null || config.trim().isEmpty()) {
            return EMPTY;
        }
        List<String> entries = new ArrayList<>();
        for (String part : config.split("[,;\\s]+")) {
            String entry = part.trim().toLowerCase(Locale.ROOT);
            if (!entry.isEmpty() && !entries.contains(entry)) {
                entries.add(entry);
            }
        }
        return entries.isEmpty() ? EMPTY : new EntityBlacklist(Collections.unmodifiableList(entries));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * @param registryName the entity's registry name such as {@code minecraft:zombie}, or null
     * @param className    the entity's fully qualified class name, or null
     */
    public boolean matches(String registryName, String className) {
        if (entries.isEmpty()) {
            return false;
        }
        String registry = registryName == null ? "" : registryName.toLowerCase(Locale.ROOT);
        String path = registry.substring(registry.indexOf(':') + 1);
        String type = className == null ? "" : className.toLowerCase(Locale.ROOT);
        for (String entry : entries) {
            if (entry.endsWith(":")) {
                if (!registry.isEmpty() && registry.startsWith(entry)) {
                    return true;
                }
            } else if (entry.indexOf(':') >= 0) {
                if (entry.equals(registry)) {
                    return true;
                }
            } else if ((!path.isEmpty() && entry.equals(path)) || type.contains(entry)) {
                return true;
            }
        }
        return false;
    }
}

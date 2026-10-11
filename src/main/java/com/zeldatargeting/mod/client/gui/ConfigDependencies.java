package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingSettings;

/** Decides whether a setting applies right now, and what to tell the player when it does not. */
public final class ConfigDependencies {
    private ConfigDependencies() {
    }

    /** The first dependency of {@code entry} that {@code settings} does not meet, or null when it applies. */
    public static ConfigEntry.Dependency blockingDependency(ConfigEntry entry, TargetingSettings settings) {
        for (ConfigEntry.Dependency dependency : entry.getDependencies()) {
            boolean value;
            try {
                value = TargetingSettings.class.getField(dependency.getFieldName()).getBoolean(settings);
            } catch (ReflectiveOperationException | IllegalArgumentException unknown) {
                continue;
            }
            if (value != dependency.isRequiredValue()) {
                return dependency;
            }
        }
        return null;
    }

    public static String blockedMessage(ConfigEntry.Dependency dependency) {
        String label = ConfigLayout.labelOf(dependency.getFieldName());
        return (dependency.isRequiredValue() ? "Turn on " : "Turn off ")
            + (label == null ? dependency.getFieldName() : label)
            + " to change this.";
    }
}

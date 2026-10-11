package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ConfigDependenciesTest {
    @Test
    public void ringOptionsBlockedWhenMarkersOff() {
        TargetingSettings settings = new TargetingSettings();
        settings.showReticle = false;

        ConfigEntry.Dependency blocking = ConfigDependencies.blockingDependency(entry("ringThickness"), settings);

        assertNotNull(blocking);
        assertEquals("Turn on Show Target Markers to change this.", ConfigDependencies.blockedMessage(blocking));
    }

    @Test
    public void rangeBlockedWhileMatchingReach() {
        TargetingSettings settings = new TargetingSettings();
        settings.syncTargetingRangeWithReach = true;

        ConfigEntry.Dependency blocking = ConfigDependencies.blockingDependency(entry("targetingRange"), settings);

        assertNotNull(blocking);
        assertEquals("Turn off Match Range to Reach to change this.", ConfigDependencies.blockedMessage(blocking));
    }

    @Test
    public void nothingBlockedAtDefaults() {
        TargetingSettings settings = new TargetingSettings();
        for (ConfigPage page : ConfigPage.values()) {
            for (ConfigLayout.Item item : ConfigLayout.page(page)) {
                if (!item.isSection()) {
                    assertNull(item.getEntry().getLabel(),
                        ConfigDependencies.blockingDependency(item.getEntry(), settings));
                }
            }
        }
    }

    @Test
    public void firstUnmetDependencyWins() {
        TargetingSettings settings = new TargetingSettings();
        settings.enableSounds = false;
        settings.enableTargetLockSound = false;

        ConfigEntry.Dependency blocking = ConfigDependencies.blockingDependency(entry("targetLockVolume"), settings);

        assertEquals("enableSounds", blocking.getFieldName());
        assertEquals("Turn on Sounds to change this.", ConfigDependencies.blockedMessage(blocking));
    }

    private static ConfigEntry entry(String fieldName) {
        for (ConfigPage page : ConfigPage.values()) {
            for (ConfigLayout.Item item : ConfigLayout.page(page)) {
                if (!item.isSection() && fieldName.equals(item.getEntry().getFieldName())) {
                    return item.getEntry();
                }
            }
        }
        throw new AssertionError(fieldName + " is not on any page");
    }
}

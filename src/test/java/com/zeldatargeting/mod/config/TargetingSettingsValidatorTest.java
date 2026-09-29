package com.zeldatargeting.mod.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class TargetingSettingsValidatorTest {

    @Test
    public void sanitizeClampsUnsafeValuesAndNormalizesSelections() {
        TargetingSettings input = new TargetingSettings();
        input.schemaVersion = 1;
        input.targetingRange = -10.0D;
        input.maxTrackingDistance = Double.NaN;
        input.maxAngle = 999.0D;
        input.cameraSmoothness = Float.POSITIVE_INFINITY;
        input.hudOpacity = 0.01F;
        input.ringThickness = 99.0F;
        input.targetPriority = "not-a-priority";
        input.presentationPalette = "unknown";
        input.lockOnPreset = "missing";

        TargetingSettings sanitized = TargetingSettingsValidator.sanitize(input);

        assertNotSame(input, sanitized);
        assertEquals(4, sanitized.schemaVersion);
        assertEquals(1.0D, sanitized.targetingRange, 0.0D);
        assertEquals(20.0D, sanitized.maxTrackingDistance, 0.0D);
        assertEquals(180.0D, sanitized.maxAngle, 0.0D);
        assertEquals(0.4F, sanitized.cameraSmoothness, 0.0F);
        assertEquals(0.35F, sanitized.hudOpacity, 0.0F);
        assertEquals(2.5F, sanitized.ringThickness, 0.0F);
        assertEquals("nearest", sanitized.targetPriority);
        assertEquals("default", sanitized.presentationPalette);
        assertEquals("balanced", sanitized.lockOnPreset);
    }

    @Test
    public void sanitizeRetainsSupportedLegacyChoices() {
        TargetingSettings input = new TargetingSettings();
        input.targetPriority = "threat";
        input.hudAnchor = "center";
        input.soundTheme = "zelda";
        input.damageNumbersMotion = "arcade";

        TargetingSettings sanitized = TargetingSettingsValidator.sanitize(input);

        assertEquals("threat", sanitized.targetPriority);
        assertEquals("center", sanitized.hudAnchor);
        assertEquals("zelda", sanitized.soundTheme);
        assertEquals("arcade", sanitized.damageNumbersMotion);
    }

    @Test
    public void sanitizeCleansTheEntityBlacklist() {
        TargetingSettings missing = new TargetingSettings();
        missing.entityBlacklist = null;
        assertEquals("", TargetingSettingsValidator.sanitize(missing).entityBlacklist);

        TargetingSettings messy = new TargetingSettings();
        messy.entityBlacklist = "  minecraft:zombie,\nminecraft:creeper\t ";
        assertEquals("minecraft:zombie, minecraft:creeper",
            TargetingSettingsValidator.sanitize(messy).entityBlacklist);

        TargetingSettings huge = new TargetingSettings();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            builder.append('a');
        }
        huge.entityBlacklist = builder.toString();
        assertEquals(TargetingSettingsValidator.MAX_BLACKLIST_LENGTH,
            TargetingSettingsValidator.sanitize(huge).entityBlacklist.length());
    }
}

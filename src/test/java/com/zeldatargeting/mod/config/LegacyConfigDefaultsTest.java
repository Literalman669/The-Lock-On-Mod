package com.zeldatargeting.mod.config;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacyConfigDefaultsTest {
    private static final double DOUBLE_EPSILON = 0.0001D;
    private static final float FLOAT_EPSILON = 0.0001F;

    @After
    public void restoreDefaults() {
        TargetingConfig.resetToDefaults();
    }

    @Test
    public void resetRestoresTargetingAndCameraDefaults() {
        TargetingConfig.targetingRange = 50.0D;
        TargetingConfig.maxTrackingDistance = 90.0D;
        TargetingConfig.maxAngle = 180.0D;
        TargetingConfig.requireLineOfSight = false;
        TargetingConfig.cameraSmoothness = 0.9F;
        TargetingConfig.autoThirdPerson = true;

        TargetingConfig.resetToDefaults();

        assertEquals(16.0D, TargetingConfig.targetingRange, DOUBLE_EPSILON);
        assertEquals(20.0D, TargetingConfig.maxTrackingDistance, DOUBLE_EPSILON);
        assertEquals(60.0D, TargetingConfig.maxAngle, DOUBLE_EPSILON);
        assertTrue(TargetingConfig.requireLineOfSight);
        assertEquals("nearest", TargetingConfig.targetPriority);
        assertEquals(0.4F, TargetingConfig.cameraSmoothness, FLOAT_EPSILON);
        assertEquals(60.0F, TargetingConfig.maxPitchAdjustment, FLOAT_EPSILON);
        assertEquals(90.0F, TargetingConfig.maxYawAdjustment, FLOAT_EPSILON);
        assertTrue(TargetingConfig.enableCameraLockOn);
        assertFalse(TargetingConfig.autoThirdPerson);
        assertEquals("balanced", TargetingConfig.lockOnPreset);
    }

    @Test
    public void resetRestoresHudAndEntityFilterDefaults() {
        TargetingConfig.showReticle = false;
        TargetingConfig.bossStylePanel = false;
        TargetingConfig.targetPlayers = true;

        TargetingConfig.resetToDefaults();

        assertTrue(TargetingConfig.showReticle);
        assertTrue(TargetingConfig.showHealthBar);
        assertTrue(TargetingConfig.showDistance);
        assertTrue(TargetingConfig.showTargetName);
        assertEquals(1.0F, TargetingConfig.reticleScale, FLOAT_EPSILON);
        assertEquals(0xFF0000, TargetingConfig.reticleColor);
        assertFalse(TargetingConfig.compactHudMode);
        assertFalse(TargetingConfig.softAimIndicator);
        assertFalse(TargetingConfig.targetHistoryEnabled);
        assertTrue(TargetingConfig.bossStylePanel);
        assertEquals("top-right", TargetingConfig.hudAnchor);
        assertTrue(TargetingConfig.targetHostileMobs);
        assertTrue(TargetingConfig.targetNeutralMobs);
        assertTrue(TargetingConfig.targetPassiveMobs);
        assertFalse(TargetingConfig.targetPlayers);
    }

    @Test
    public void resetRestoresFeedbackAndPerformanceDefaults() {
        TargetingConfig.enableSounds = false;
        TargetingConfig.enableDamageNumbers = false;
        TargetingConfig.updateFrequency = 20;
        TargetingConfig.validationInterval = 1;

        TargetingConfig.resetToDefaults();

        assertTrue(TargetingConfig.enableSounds);
        assertEquals(1.0F, TargetingConfig.soundVolume, FLOAT_EPSILON);
        assertEquals("default", TargetingConfig.soundTheme);
        assertTrue(TargetingConfig.enableDamageNumbers);
        assertEquals(1.0F, TargetingConfig.damageNumbersScale, FLOAT_EPSILON);
        assertEquals(60, TargetingConfig.damageNumbersDuration);
        assertEquals("default", TargetingConfig.damageNumbersMotion);
        assertTrue(TargetingConfig.showDamagePrediction);
        assertTrue(TargetingConfig.showHitsToKill);
        assertTrue(TargetingConfig.showVulnerabilities);
        assertEquals(1, TargetingConfig.updateFrequency);
        assertEquals(10, TargetingConfig.validationInterval);
    }

    @Test
    public void resetRestoresBalancedPresentationDefaults() {
        TargetingConfig.presentationPalette = "protanopia";
        TargetingConfig.reducedMotion = true;
        TargetingConfig.hudScale = 1.5F;
        TargetingConfig.hudOpacity = 0.35F;
        TargetingConfig.ringEnabled = false;
        TargetingConfig.ringThickness = 2.5F;
        TargetingConfig.ringGlowStrength = 0.0F;
        TargetingConfig.ringHealthArcEnabled = false;

        TargetingConfig.resetToDefaults();

        assertEquals("default", TargetingConfig.presentationPalette);
        assertFalse(TargetingConfig.reducedMotion);
        assertEquals(1.0F, TargetingConfig.hudScale, FLOAT_EPSILON);
        assertEquals(1.0F, TargetingConfig.hudOpacity, FLOAT_EPSILON);
        assertTrue(TargetingConfig.ringEnabled);
        assertEquals(1.0F, TargetingConfig.ringThickness, FLOAT_EPSILON);
        assertEquals(1.0F, TargetingConfig.ringGlowStrength, FLOAT_EPSILON);
        assertTrue(TargetingConfig.ringHealthArcEnabled);
    }
}

package com.zeldatargeting.mod.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TargetingSettingsBridgeTest {

    @Test
    public void bridgeRoundTripsRepresentativeValuesFromEverySubsystem() {
        TargetingSettings original = TargetingSettingsBridge.captureRuntime();
        try {
            TargetingSettings changed = original.copy();
            changed.targetingRange = 32.0D;
            changed.showHealthBar = false;
            changed.cameraSmoothness = 0.25F;
            changed.targetPassiveMobs = false;
            changed.targetLockVolume = 0.2F;
            changed.enableDamageNumbers = false;
            changed.presentationPalette = "protanopia";
            changed.hudOpacity = 0.7F;
            changed.ringThickness = 1.75F;

            TargetingSettingsBridge.applyToRuntime(changed);
            TargetingSettings captured = TargetingSettingsBridge.captureRuntime();

            assertEquals(32.0D, captured.targetingRange, 0.0D);
            assertFalse(captured.showHealthBar);
            assertEquals(0.25F, captured.cameraSmoothness, 0.0F);
            assertFalse(captured.targetPassiveMobs);
            assertEquals(0.2F, captured.targetLockVolume, 0.0F);
            assertFalse(captured.enableDamageNumbers);
            assertEquals("protanopia", captured.presentationPalette);
            assertEquals(0.7F, captured.hudOpacity, 0.0F);
            assertEquals(1.75F, captured.ringThickness, 0.0F);
        } finally {
            TargetingSettingsBridge.applyToRuntime(original);
        }
    }
}

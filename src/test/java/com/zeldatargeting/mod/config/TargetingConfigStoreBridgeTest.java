package com.zeldatargeting.mod.config;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetingConfigStoreBridgeTest {

    @Test
    public void confirmingAPresetPersistsItAndClearsTheOnboardingRequirement() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-preset-confirm-test").toFile();
        File configFile = new File(directory, "zeldatargeting.cfg");
        TargetingSettings original = TargetingConfig.captureActiveSettings();

        try {
            TargetingConfig.initialize(configFile);
            assertTrue(TargetingConfig.requiresPresetSelection());

            TargetingConfig.confirmPreset(TargetingPreset.CINEMATIC);

            assertFalse(TargetingConfig.requiresPresetSelection());
            assertTrue(configFile.isFile());
            assertEquals("cinematic", TargetingConfig.lockOnPreset);
            assertTrue(TargetingConfig.autoThirdPerson);
        } finally {
            TargetingConfig.restoreSettings(original);
        }
    }

    @Test
    public void runtimeBridgeKeepsAFirstRunTemporaryUntilSettingsAreSaved() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-runtime-store-test").toFile();
        File configFile = new File(directory, "zeldatargeting.cfg");
        TargetingSettings original = TargetingConfig.captureActiveSettings();

        try {
            TargetingConfig.initialize(configFile);

            assertTrue(TargetingConfig.isPresetSelectionRequired());
            assertFalse(configFile.exists());
            assertEquals("balanced", TargetingConfig.lockOnPreset);

            TargetingConfig.saveSettings(TargetingPreset.SNAPPY.createSettings());

            assertFalse(TargetingConfig.isPresetSelectionRequired());
            assertTrue(configFile.isFile());

            TargetingConfig.initialize(configFile);

            assertFalse(TargetingConfig.isPresetSelectionRequired());
            assertEquals("snappy", TargetingConfig.lockOnPreset);
            assertFalse(TargetingConfig.autoThirdPerson);
        } finally {
            TargetingConfig.restoreSettings(original);
        }
    }
}

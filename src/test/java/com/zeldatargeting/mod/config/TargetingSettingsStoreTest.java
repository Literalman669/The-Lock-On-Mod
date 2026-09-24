package com.zeldatargeting.mod.config;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetingSettingsStoreTest {

    @Test
    public void schemaFourSettingsRoundTripWithoutRequestingAChooser() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-store-test").toFile();
        File configFile = new File(directory, "zeldatargeting.cfg");
        TargetingSettingsStore store = new TargetingSettingsStore(configFile);
        TargetingSettings settings = TargetingPreset.CINEMATIC.createSettings();
        settings.targetingRange = 28.0D;
        settings.showHealthBar = false;
        settings.soundTheme = "zelda";
        settings.presentationPalette = "tritanopia";
        settings.ringThickness = 1.75F;

        store.save(settings);
        TargetingSettingsStore.LoadResult loaded = store.load();
        String fileContents = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);

        assertFalse(loaded.requiresPresetSelection());
        assertFalse(loaded.hasBackupFailure());
        assertEquals(4, loaded.getSettings().schemaVersion);
        assertEquals(28.0D, loaded.getSettings().targetingRange, 0.0D);
        assertFalse(loaded.getSettings().showHealthBar);
        assertEquals("zelda", loaded.getSettings().soundTheme);
        assertEquals("tritanopia", loaded.getSettings().presentationPalette);
        assertEquals(1.75F, loaded.getSettings().ringThickness, 0.0F);
        assertTrue(fileContents.contains("schemaVersion=4"));
    }

    @Test
    public void legacyConfigIsBackedUpAndRequestsAPresetWithoutBeingOverwritten() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-store-legacy-test").toFile();
        File configFile = new File(directory, "zeldatargeting.cfg");
        Files.write(configFile.toPath(), (
                "# Legacy Forge configuration\n"
                        + "targeting {\n"
                        + "    D:targetingRange=40.0\n"
                        + "}\n").getBytes(StandardCharsets.UTF_8));
        String originalContents = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);

        TargetingSettingsStore.LoadResult loaded = new TargetingSettingsStore(configFile).load();

        assertTrue(loaded.requiresPresetSelection());
        assertFalse(loaded.hasBackupFailure());
        assertEquals("balanced", loaded.getSettings().lockOnPreset);
        assertEquals(originalContents, new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8));
        assertTrue(new File(directory, "zeldatargeting-1.3-backup.cfg").isFile());
    }

    @Test
    public void savingRemovesRetiredCameraCompatibilityKeys() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-retired-compat").toFile();
        File configFile = new File(directory, "zeldatargeting.cfg");
        Files.write(configFile.toPath(), (
                "meta.schemaVersion=4\n"
                        + "compatibility.btpCompatibilityMode=gentle\n"
                        + "compatibility.btpCameraIntensity=0.3\n"
                        + "compatibility.ssrXOffset=-0.875\n")
                .getBytes(StandardCharsets.UTF_8));

        TargetingSettingsStore store = new TargetingSettingsStore(configFile);
        store.save(store.load().getSettings());
        String contents = new String(
            Files.readAllBytes(configFile.toPath()),
            StandardCharsets.UTF_8
        );

        assertFalse(contents.contains("btpCompatibilityMode"));
        assertFalse(contents.contains("btpCameraIntensity"));
        assertFalse(contents.contains("ssrXOffset"));
        assertTrue(contents.contains("ssrCompensationEnabled"));
    }
}

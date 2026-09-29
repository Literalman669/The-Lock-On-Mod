package com.zeldatargeting.mod.config;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetingSettingsSessionTest {

    @Test
    public void cancelRestoresTheExactEntrySnapshot() {
        TargetingSettings entry = TargetingPreset.BALANCED.createSettings();
        TargetingSettingsSession session = new TargetingSettingsSession(entry);

        session.workingCopy().targetingRange = 48.0D;
        TargetingSettings restored = session.cancel();

        assertEquals(16.0D, restored.targetingRange, 0.0D);
        assertEquals(16.0D, entry.targetingRange, 0.0D);
    }

    @Test
    public void resetChangesOnlyTheWorkingCopyAndSaveValidatesIt() {
        TargetingSettings entry = TargetingPreset.CINEMATIC.createSettings();
        TargetingSettingsSession session = new TargetingSettingsSession(entry);

        session.reset(TargetingPreset.SNAPPY);
        session.workingCopy().hudOpacity = 10.0F;
        TargetingSettings saved = session.save();

        assertEquals("snappy", saved.lockOnPreset);
        assertFalse(saved.autoThirdPerson);
        assertEquals(1.0F, saved.hudOpacity, 0.0F);
        assertEquals("cinematic", session.cancel().lockOnPreset);
    }

    @Test
    public void resetFieldsRestoresOnlyTheNamedSettings() {
        TargetingSettingsSession session = new TargetingSettingsSession(TargetingPreset.CINEMATIC.createSettings());
        session.workingCopy().soundVolume = 0.2F;
        session.workingCopy().enableSounds = false;
        session.workingCopy().hudOpacity = 0.5F;

        session.resetFields(TargetingPreset.CINEMATIC,
            Arrays.asList("soundVolume", "enableSounds", "notARealSetting"));

        assertEquals(1.0F, session.workingCopy().soundVolume, 0.0F);
        assertTrue(session.workingCopy().enableSounds);
        assertEquals(0.5F, session.workingCopy().hudOpacity, 0.0F);
        assertEquals("cinematic", session.workingCopy().lockOnPreset);
    }
}

package com.zeldatargeting.mod.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
}

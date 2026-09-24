package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingPreset;
import com.zeldatargeting.mod.config.TargetingSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class ConfigEditSessionIntegrationTest {

    @Test
    public void previewDoesNotChangeTheEntrySnapshotAndCancelRestoresIt() {
        TargetingSettings entry = TargetingPreset.BALANCED.createSettings();
        ConfigEditSession session = new ConfigEditSession(entry);

        session.workingCopy().targetingRange = 42.0D;
        TargetingSettings preview = session.preview();

        assertEquals(42.0D, preview.targetingRange, 0.0D);
        assertEquals(16.0D, entry.targetingRange, 0.0D);
        assertEquals(16.0D, session.cancel().targetingRange, 0.0D);
    }

    @Test
    public void resetIsReversibleAndSaveReturnsAnIndependentValidatedCopy() {
        ConfigEditSession session = new ConfigEditSession(TargetingPreset.CINEMATIC.createSettings());

        session.reset(TargetingPreset.SNAPPY);
        TargetingSettings saved = session.save();

        assertEquals("snappy", saved.lockOnPreset);
        assertNotSame(saved, session.workingCopy());
        assertEquals("cinematic", session.cancel().lockOnPreset);
    }
}

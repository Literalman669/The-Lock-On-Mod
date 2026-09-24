package com.zeldatargeting.mod.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class TargetingPresetTest {

    @Test
    public void presetsExposeStableIdsAndIndependentCopies() {
        TargetingSettings cinematic = TargetingPreset.CINEMATIC.createSettings();
        TargetingSettings balanced = TargetingPreset.BALANCED.createSettings();
        TargetingSettings snappy = TargetingPreset.SNAPPY.createSettings();

        assertEquals("cinematic", cinematic.lockOnPreset);
        assertEquals("balanced", balanced.lockOnPreset);
        assertEquals("snappy", snappy.lockOnPreset);
        assertNotSame(cinematic, TargetingPreset.CINEMATIC.createSettings());
        assertTrue(cinematic.cameraSmoothness > balanced.cameraSmoothness);
        assertTrue(cinematic.autoThirdPerson);
        assertFalse(balanced.autoThirdPerson);
        assertFalse(snappy.autoThirdPerson);
        assertTrue(snappy.cameraSmoothness < balanced.cameraSmoothness);
    }
}

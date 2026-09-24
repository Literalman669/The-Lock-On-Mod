package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ConfigPageTest {

    @Test
    public void pagesExposeTheCuratedStageFiveInformationArchitecture() {
        ConfigPage[] pages = ConfigPage.values();

        assertArrayEquals(new ConfigPage[] {
            ConfigPage.PRESETS,
            ConfigPage.TARGETING,
            ConfigPage.CAMERA,
            ConfigPage.HUD,
            ConfigPage.AUDIO,
            ConfigPage.COMPATIBILITY,
            ConfigPage.ACCESSIBILITY
        }, pages);
    }
}

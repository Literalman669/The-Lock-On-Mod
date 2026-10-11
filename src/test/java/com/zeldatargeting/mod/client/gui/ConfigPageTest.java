package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ConfigPageTest {
    @Test
    public void pagesFollowThePlayerFacingLayout() {
        ConfigPage[] pages = ConfigPage.values();

        assertArrayEquals(new ConfigPage[] {
            ConfigPage.PRESETS,
            ConfigPage.TARGETING,
            ConfigPage.CAMERA,
            ConfigPage.HUD,
            ConfigPage.COMBAT,
            ConfigPage.SOUND,
            ConfigPage.ACCESSIBILITY,
            ConfigPage.ADVANCED
        }, pages);
    }

    @Test
    public void titlesMatchTheSpec() {
        ConfigPage[] pages = ConfigPage.values();
        String[] titles = new String[pages.length];
        String[] shortTitles = new String[pages.length];
        String[] descriptions = new String[pages.length];
        for (int i = 0; i < pages.length; i++) {
            titles[i] = pages[i].getTitle();
            shortTitles[i] = pages[i].getShortTitle();
            descriptions[i] = pages[i].getDescription();
        }

        assertArrayEquals(new String[] {
            "Presets", "Targeting", "Camera", "HUD & Ring", "Combat Info", "Sound", "Accessibility", "Advanced"
        }, titles);
        assertArrayEquals(new String[] {
            "Presets", "Targeting", "Camera", "HUD", "Combat", "Sound", "Access", "Advanced"
        }, shortTitles);
        assertArrayEquals(new String[] {
            "Choose how lock-on feels",
            "Range, selection, and what can be targeted",
            "How the camera follows your target",
            "The target panel and the markers on your target",
            "Damage readouts and damage numbers",
            "Lock-on sounds",
            "Colors and motion",
            "Defaults are fine for most players"
        }, descriptions);
    }
}

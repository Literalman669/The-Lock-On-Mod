package com.zeldatargeting.mod.client.gui;

/**
 * The player-facing pages of the configuration screen, in tab order.
 */
public enum ConfigPage {
    PRESETS("Presets", "Presets", "Choose how lock-on feels"),
    TARGETING("Targeting", "Targeting", "Range, selection, and what can be targeted"),
    CAMERA("Camera", "Camera", "How the camera follows your target"),
    HUD("HUD & Ring", "HUD", "The target panel and the markers on your target"),
    COMBAT("Combat Info", "Combat", "Damage readouts and damage numbers"),
    SOUND("Sound", "Sound", "Lock-on sounds"),
    ACCESSIBILITY("Accessibility", "Access", "Colors and motion"),
    ADVANCED("Advanced", "Advanced", "Defaults are fine for most players");

    private final String title;
    private final String shortTitle;
    private final String description;

    ConfigPage(String title, String shortTitle, String description) {
        this.title = title;
        this.shortTitle = shortTitle;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    /** A shorter tab label for windows too narrow for the full titles. */
    public String getShortTitle() {
        return shortTitle;
    }

    public String getDescription() {
        return description;
    }
}

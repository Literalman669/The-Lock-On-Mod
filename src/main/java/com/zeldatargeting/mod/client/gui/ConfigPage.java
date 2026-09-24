package com.zeldatargeting.mod.client.gui;

/**
 * The focused, player-facing sections of the 1.4 configuration screen.
 */
public enum ConfigPage {
    PRESETS("Presets", "Choose the overall feel"),
    TARGETING("Targeting", "Range, selection, and filters"),
    CAMERA("Camera", "Follow, focus, and third-person behavior"),
    HUD("HUD", "Panel, target ring, and placement"),
    AUDIO("Audio", "Target feedback and sound tuning"),
    COMPATIBILITY("Compatibility", "Vanilla-safe compatibility controls"),
    ACCESSIBILITY("Accessibility", "Palette, opacity, and reduced motion");

    private final String title;
    private final String description;

    ConfigPage(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

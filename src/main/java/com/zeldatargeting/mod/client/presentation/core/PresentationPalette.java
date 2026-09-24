package com.zeldatargeting.mod.client.presentation.core;

public enum PresentationPalette {
    DEFAULT(0xFF4CD964, 0xFFFFA340, 0xFFFF4B4B),
    DEUTERANOPIA(0xFF56B4E9, 0xFFE69F00, 0xFFCC79A7),
    PROTANOPIA(0xFF56B4E9, 0xFFF0E442, 0xFFCC79A7),
    TRITANOPIA(0xFF009E73, 0xFFD55E00, 0xFFCC79A7);

    private final int healthColor;
    private final int warningColor;
    private final int lethalColor;

    PresentationPalette(int healthColor, int warningColor, int lethalColor) {
        this.healthColor = healthColor;
        this.warningColor = warningColor;
        this.lethalColor = lethalColor;
    }

    public int getHealthColor() {
        return healthColor;
    }

    public int getWarningColor() {
        return warningColor;
    }

    public int getLethalColor() {
        return lethalColor;
    }

    public static PresentationPalette fromName(String value) {
        for (PresentationPalette palette : values()) {
            if (palette.name().equalsIgnoreCase(value)) {
                return palette;
            }
        }
        return DEFAULT;
    }
}

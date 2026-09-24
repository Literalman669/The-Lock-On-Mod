package com.zeldatargeting.mod.client.presentation.core;

public final class RingColorPolicy {
    private static final int OCCLUDED_COLOR = 0xFF9EA6AD;

    private RingColorPolicy() {
    }

    public static int colorFor(PresentationStatus status) {
        return colorFor(status, PresentationPalette.DEFAULT);
    }

    public static int colorFor(PresentationStatus status, PresentationPalette palette) {
        PresentationStatus safeStatus = status == null ? PresentationStatus.NORMAL : status;
        PresentationPalette safePalette = palette == null ? PresentationPalette.DEFAULT : palette;
        if (safeStatus == PresentationStatus.LETHAL) {
            return safePalette.getLethalColor();
        }
        if (safeStatus == PresentationStatus.WARNING || safeStatus == PresentationStatus.LOW_HEALTH) {
            return safePalette.getWarningColor();
        }
        if (safeStatus == PresentationStatus.OCCLUDED) {
            return OCCLUDED_COLOR;
        }
        return safePalette.getHealthColor();
    }
}

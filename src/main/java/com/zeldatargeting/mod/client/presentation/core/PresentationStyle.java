package com.zeldatargeting.mod.client.presentation.core;

import com.zeldatargeting.mod.config.TargetingConfig;

/**
 * Shared, render-only presentation policy.  Keeping these decisions here makes
 * the target ring and HUD panels agree without changing targeting behaviour.
 */
public final class PresentationStyle {
    private static final float MIN_HUD_OPACITY = 0.35F;
    private static final float MAX_HUD_OPACITY = 1.0F;
    private static final float MIN_RING_GLOW = 0.0F;
    private static final float MAX_RING_GLOW = 1.5F;

    private final PresentationPalette palette;
    private final float hudOpacity;
    private final float ringGlowStrength;
    private final boolean reducedMotion;

    private PresentationStyle(PresentationPalette palette, float hudOpacity,
                              float ringGlowStrength, boolean reducedMotion) {
        this.palette = palette;
        this.hudOpacity = hudOpacity;
        this.ringGlowStrength = ringGlowStrength;
        this.reducedMotion = reducedMotion;
    }

    public static PresentationStyle fromConfig() {
        return of(TargetingConfig.getPresentationPalette(), TargetingConfig.hudOpacity,
                TargetingConfig.ringGlowStrength, TargetingConfig.reducedMotion);
    }

    public static PresentationStyle of(PresentationPalette palette, float hudOpacity,
                                       float ringGlowStrength, boolean reducedMotion) {
        PresentationPalette safePalette = palette == null ? PresentationPalette.DEFAULT : palette;
        return new PresentationStyle(safePalette,
                clamp(hudOpacity, MIN_HUD_OPACITY, MAX_HUD_OPACITY),
                clamp(ringGlowStrength, MIN_RING_GLOW, MAX_RING_GLOW),
                reducedMotion);
    }

    public PresentationPalette getPalette() {
        return palette;
    }

    public float getHudOpacity() {
        return hudOpacity;
    }

    public float getRingGlowStrength() {
        return ringGlowStrength;
    }

    public boolean isReducedMotion() {
        return reducedMotion;
    }

    public int getStatusColor(PresentationStatus status) {
        return RingColorPolicy.colorFor(status, palette);
    }

    public int applyHudOpacity(int color) {
        int alpha = (color >>> 24) & 0xFF;
        int adjustedAlpha = Math.round(alpha * hudOpacity);
        return (color & 0x00FFFFFF) | (adjustedAlpha << 24);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

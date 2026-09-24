package com.zeldatargeting.mod.client.presentation.core;

public final class NeonRingAppearance {
    private static final double MIN_CORE_HALF_THICKNESS = 0.045D;
    private static final double MAX_CORE_HALF_THICKNESS = 0.16D;
    private static final double MIN_AURA_HALF_THICKNESS = 0.055D;
    private static final double MAX_AURA_HALF_THICKNESS = 0.22D;

    private final double auraHalfThickness;
    private final double coreHalfThickness;
    private final float auraAlphaMultiplier;
    private final float coreAlphaMultiplier;

    private NeonRingAppearance(
            double auraHalfThickness,
            double coreHalfThickness,
            float auraAlphaMultiplier,
            float coreAlphaMultiplier) {
        this.auraHalfThickness = sanitizeThickness(auraHalfThickness,
            MIN_AURA_HALF_THICKNESS, MAX_AURA_HALF_THICKNESS);
        this.coreHalfThickness = sanitizeThickness(coreHalfThickness,
            MIN_CORE_HALF_THICKNESS, MAX_CORE_HALF_THICKNESS);
        this.auraAlphaMultiplier = clampAlpha(auraAlphaMultiplier);
        this.coreAlphaMultiplier = clampAlpha(coreAlphaMultiplier);
    }

    public static NeonRingAppearance from(RingGeometry geometry) {
        return from(geometry, 1.0F);
    }

    /**
     * Builds the visual treatment for the established torus geometry.  Glow is
     * intentionally limited to opacity so the player-selected tube shape and
     * the health arc never expand, shrink, or otherwise change with glow.
     */
    public static NeonRingAppearance from(RingGeometry geometry, float glowStrength) {
        double radius = geometry == null ? 0.5D : geometry.getHalfWidth();
        double core = clamp(radius * 0.09D, MIN_CORE_HALF_THICKNESS, MAX_CORE_HALF_THICKNESS);
        float glow = clampGlowStrength(glowStrength);
        return new NeonRingAppearance(
            clamp(core * 1.35D, MIN_AURA_HALF_THICKNESS, MAX_AURA_HALF_THICKNESS),
            core,
            0.22F * glow,
            0.86F + 0.12F * glow
        );
    }

    public double getAuraHalfThickness() {
        return auraHalfThickness;
    }

    public double getCoreHalfThickness() {
        return coreHalfThickness;
    }

    public float getAuraAlphaMultiplier() {
        return auraAlphaMultiplier;
    }

    public float getCoreAlphaMultiplier() {
        return coreAlphaMultiplier;
    }

    private static double clamp(double value, double minimum, double maximum) {
        double safeValue = Double.isFinite(value) ? value : minimum;
        return Math.max(minimum, Math.min(maximum, safeValue));
    }

    private static double sanitizeThickness(double value, double fallback, double maximum) {
        double safeValue = Double.isFinite(value) ? value : fallback;
        return Math.max(0.001D, Math.min(maximum, safeValue));
    }

    private static float clampAlpha(float value) {
        float safeValue = Float.isFinite(value) ? value : 0.0F;
        return Math.max(0.0F, Math.min(1.0F, safeValue));
    }

    private static float clampGlowStrength(float value) {
        float safeValue = Float.isFinite(value) ? value : 1.0F;
        return Math.max(0.0F, Math.min(1.5F, safeValue));
    }
}

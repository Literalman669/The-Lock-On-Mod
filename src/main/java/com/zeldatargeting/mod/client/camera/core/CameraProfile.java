package com.zeldatargeting.mod.client.camera.core;

import java.util.Locale;

public final class CameraProfile {
    private final String name;
    private final double rotationHalfLifeMillis;
    private final double sizeDistanceGain;
    private final double maxFovScale;
    private final float maxYawAdjustment;
    private final float maxPitchAdjustment;
    private final long releaseDurationMillis;
    private final double baseDistance;
    private final double transparencyStartDistance;
    private final double transparencyInnerDistance;
    private final float minimumPlayerAlpha;
    private final double fallbackEnterDistance;
    private final double fallbackExitDistance;
    private final long fallbackEnterDelayMillis;
    private final long fallbackExitDelayMillis;
    private final double focusYOffset;
    private final double firstPersonHalfLifeScale;

    public CameraProfile(
            String name,
            double rotationHalfLifeMillis,
            double sizeDistanceGain,
            double maxFovScale,
            float maxYawAdjustment,
            float maxPitchAdjustment,
            long releaseDurationMillis,
            double baseDistance,
            double transparencyStartDistance,
            double transparencyInnerDistance,
            float minimumPlayerAlpha,
            double fallbackEnterDistance,
            double fallbackExitDistance,
            long fallbackEnterDelayMillis,
            long fallbackExitDelayMillis,
            double focusYOffset,
            double firstPersonHalfLifeScale) {
        requireFinitePositive(rotationHalfLifeMillis, "rotationHalfLifeMillis");
        requireFiniteNonNegative(sizeDistanceGain, "sizeDistanceGain");
        requireFiniteNonNegative(maxFovScale, "maxFovScale");
        requireFiniteNonNegative(maxYawAdjustment, "maxYawAdjustment");
        requireFiniteNonNegative(maxPitchAdjustment, "maxPitchAdjustment");
        requireNonNegative(releaseDurationMillis, "releaseDurationMillis");
        requireFiniteNonNegative(baseDistance, "baseDistance");
        requireFiniteNonNegative(transparencyStartDistance, "transparencyStartDistance");
        requireFiniteNonNegative(transparencyInnerDistance, "transparencyInnerDistance");
        requireFiniteNonNegative(minimumPlayerAlpha, "minimumPlayerAlpha");
        requireFiniteNonNegative(fallbackEnterDistance, "fallbackEnterDistance");
        requireFiniteNonNegative(fallbackExitDistance, "fallbackExitDistance");
        requireNonNegative(fallbackEnterDelayMillis, "fallbackEnterDelayMillis");
        requireNonNegative(fallbackExitDelayMillis, "fallbackExitDelayMillis");
        requireFinite(focusYOffset, "focusYOffset");
        requireFinitePositive(firstPersonHalfLifeScale, "firstPersonHalfLifeScale");
        if (transparencyInnerDistance > transparencyStartDistance) {
            throw new IllegalArgumentException("transparency distances are inverted");
        }
        if (minimumPlayerAlpha > 1.0F) {
            throw new IllegalArgumentException("minimumPlayerAlpha must be at most one");
        }
        if (fallbackEnterDistance > fallbackExitDistance) {
            throw new IllegalArgumentException("fallback distances are inverted");
        }

        String normalizedName = name == null
            ? "balanced"
            : name.trim().toLowerCase(Locale.ROOT);
        this.name = normalizedName.isEmpty() ? "balanced" : normalizedName;
        this.rotationHalfLifeMillis = rotationHalfLifeMillis;
        this.sizeDistanceGain = sizeDistanceGain;
        this.maxFovScale = maxFovScale;
        this.maxYawAdjustment = maxYawAdjustment;
        this.maxPitchAdjustment = maxPitchAdjustment;
        this.releaseDurationMillis = releaseDurationMillis;
        this.baseDistance = baseDistance;
        this.transparencyStartDistance = transparencyStartDistance;
        this.transparencyInnerDistance = transparencyInnerDistance;
        this.minimumPlayerAlpha = minimumPlayerAlpha;
        this.fallbackEnterDistance = fallbackEnterDistance;
        this.fallbackExitDistance = fallbackExitDistance;
        this.fallbackEnterDelayMillis = fallbackEnterDelayMillis;
        this.fallbackExitDelayMillis = fallbackExitDelayMillis;
        this.focusYOffset = focusYOffset;
        this.firstPersonHalfLifeScale = firstPersonHalfLifeScale;
    }

    public CameraProfile withLegacyOverrides(
            double halfLifeMillis,
            float maxYaw,
            float maxPitch,
            double focusY) {
        return new CameraProfile(
            name,
            halfLifeMillis,
            sizeDistanceGain,
            maxFovScale,
            maxYaw,
            maxPitch,
            releaseDurationMillis,
            baseDistance,
            transparencyStartDistance,
            transparencyInnerDistance,
            minimumPlayerAlpha,
            fallbackEnterDistance,
            fallbackExitDistance,
            fallbackEnterDelayMillis,
            fallbackExitDelayMillis,
            focusY,
            firstPersonHalfLifeScale
        );
    }

    public String getName() {
        return name;
    }

    public double getRotationHalfLifeMillis() {
        return rotationHalfLifeMillis;
    }

    public double getSizeDistanceGain() {
        return sizeDistanceGain;
    }

    public double getMaxFovScale() {
        return maxFovScale;
    }

    public float getMaxYawAdjustment() {
        return maxYawAdjustment;
    }

    public float getMaxPitchAdjustment() {
        return maxPitchAdjustment;
    }

    public long getReleaseDurationMillis() {
        return releaseDurationMillis;
    }

    public double getBaseDistance() {
        return baseDistance;
    }

    public double getTransparencyStartDistance() {
        return transparencyStartDistance;
    }

    public double getTransparencyInnerDistance() {
        return transparencyInnerDistance;
    }

    public float getMinimumPlayerAlpha() {
        return minimumPlayerAlpha;
    }

    public double getFallbackEnterDistance() {
        return fallbackEnterDistance;
    }

    public double getFallbackExitDistance() {
        return fallbackExitDistance;
    }

    public long getFallbackEnterDelayMillis() {
        return fallbackEnterDelayMillis;
    }

    public long getFallbackExitDelayMillis() {
        return fallbackExitDelayMillis;
    }

    public double getFocusYOffset() {
        return focusYOffset;
    }

    public double getFirstPersonHalfLifeScale() {
        return firstPersonHalfLifeScale;
    }

    private static void requireFinitePositive(double value, String field) {
        requireFinite(value, field);
        if (value <= 0.0D) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }

    private static void requireFiniteNonNegative(double value, String field) {
        requireFinite(value, field);
        if (value < 0.0D) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }

    private static void requireFinite(double value, String field) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(field + " must be finite");
        }
    }

    private static void requireNonNegative(long value, String field) {
        if (value < 0L) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }
}

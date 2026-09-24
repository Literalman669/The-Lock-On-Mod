package com.zeldatargeting.mod.client.camera.core;

public final class CameraFraming {
    private static final long MAX_ELAPSED_MILLIS = 250L;

    private CameraFraming() {
    }

    public static double desiredDistance(
            CameraProfile profile,
            double targetWidth,
            double targetHeight) {
        double size = Math.max(sanitizeNonNegative(targetWidth), sanitizeNonNegative(targetHeight));
        double sizeFactor = clamp((size - 1.8D) / 6.2D, 0.0D, 1.0D);
        return profile.getBaseDistance() + sizeFactor * profile.getSizeDistanceGain();
    }

    public static double fovMultiplier(
            CameraProfile profile,
            double targetWidth,
            double targetHeight,
            double targetDistance,
            double maxTrackingDistance) {
        double size = Math.max(sanitizeNonNegative(targetWidth), sanitizeNonNegative(targetHeight));
        double sizeFactor = clamp((size - 1.8D) / 6.2D, 0.0D, 1.0D);
        double distanceFactor = maxTrackingDistance > 0.0D && isFinite(maxTrackingDistance)
            ? clamp(sanitizeNonNegative(targetDistance) / maxTrackingDistance, 0.0D, 1.0D)
            : 0.0D;
        return 1.0D + profile.getMaxFovScale() * sizeFactor * distanceFactor;
    }

    public static float playerAlpha(CameraProfile profile, double safeDistance) {
        double distance = sanitizeNonNegative(safeDistance);
        double inner = profile.getTransparencyInnerDistance();
        double start = profile.getTransparencyStartDistance();
        if (distance <= inner) {
            return profile.getMinimumPlayerAlpha();
        }
        if (distance >= start) {
            return 1.0F;
        }
        double factor = (distance - inner) / (start - inner);
        return (float) (profile.getMinimumPlayerAlpha()
            + (1.0D - profile.getMinimumPlayerAlpha()) * factor);
    }

    public static double interpolationFactor(long elapsedMillis, double halfLifeMillis) {
        if (!isFinite(halfLifeMillis) || halfLifeMillis <= 0.0D) {
            throw new IllegalArgumentException("halfLifeMillis must be finite and positive");
        }
        long boundedElapsed = Math.max(0L, Math.min(MAX_ELAPSED_MILLIS, elapsedMillis));
        return 1.0D - Math.pow(0.5D, boundedElapsed / halfLifeMillis);
    }

    private static double sanitizeNonNegative(double value) {
        return isFinite(value) && value > 0.0D ? value : 0.0D;
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

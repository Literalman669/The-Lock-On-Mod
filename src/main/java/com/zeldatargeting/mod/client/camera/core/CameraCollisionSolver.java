package com.zeldatargeting.mod.client.camera.core;

public final class CameraCollisionSolver {
    private static final double SAFETY_MARGIN = 0.1D;
    private static final double VANILLA_DISTANCE = 4.0D;

    private CameraCollisionSolver() {
    }

    public static CameraCollisionResult solve(
            double desiredDistance,
            CameraCollisionSamples samples) {
        if (samples == null) {
            return new CameraCollisionResult(0.0D, 0.0D, 0.0D);
        }
        double desired = sanitizeNonNegative(desiredDistance);
        double shortest = samples.minimumValidDistance();
        double safe = clamp(shortest - SAFETY_MARGIN, 0.0D, desired);
        double vanilla = clamp(shortest, 0.0D, VANILLA_DISTANCE);
        return new CameraCollisionResult(safe, vanilla, safe - vanilla);
    }

    private static double sanitizeNonNegative(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

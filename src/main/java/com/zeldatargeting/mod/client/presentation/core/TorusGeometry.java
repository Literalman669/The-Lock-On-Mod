package com.zeldatargeting.mod.client.presentation.core;

public final class TorusGeometry {
    private static final double MIN_MAJOR_RADIUS = 0.01D;
    private static final double MAX_MAJOR_RADIUS = 4.0D;
    private static final double MIN_TUBE_RADIUS = 0.001D;
    private static final double MAX_TUBE_RATIO = 0.45D;

    private final double majorRadius;
    private final double tubeRadius;

    private TorusGeometry(double majorRadius, double tubeRadius) {
        this.majorRadius = majorRadius;
        this.tubeRadius = tubeRadius;
    }

    public static TorusGeometry create(double majorRadius, double tubeRadius) {
        double safeMajorRadius = clamp(majorRadius, MIN_MAJOR_RADIUS, MAX_MAJOR_RADIUS, 0.5D);
        double maximumTubeRadius = Math.max(MIN_TUBE_RADIUS, safeMajorRadius * MAX_TUBE_RATIO);
        double safeTubeRadius = clamp(tubeRadius, MIN_TUBE_RADIUS, maximumTubeRadius, 0.05D);
        return new TorusGeometry(safeMajorRadius, safeTubeRadius);
    }

    public double getMajorRadius() {
        return majorRadius;
    }

    public double getTubeRadius() {
        return tubeRadius;
    }

    public double getRadialDistance(double crossSectionAngleRadians) {
        return majorRadius + tubeRadius * Math.cos(finiteOr(crossSectionAngleRadians, 0.0D));
    }

    public double getVerticalOffset(double crossSectionAngleRadians) {
        return tubeRadius * Math.sin(finiteOr(crossSectionAngleRadians, 0.0D));
    }

    private static double clamp(double value, double minimum, double maximum, double fallback) {
        double safeValue = Double.isFinite(value) ? value : fallback;
        return Math.max(minimum, Math.min(maximum, safeValue));
    }

    private static double finiteOr(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }
}

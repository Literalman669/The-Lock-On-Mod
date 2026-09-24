package com.zeldatargeting.mod.client.presentation.core;

public final class RingGeometry {
    private final double halfWidth;
    private final double halfHeight;
    private final float healthArcDegrees;
    private final float alpha;
    private final float motionScale;

    private RingGeometry(double halfWidth, double halfHeight, float healthArcDegrees,
            float alpha, float motionScale) {
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.healthArcDegrees = healthArcDegrees;
        this.alpha = alpha;
        this.motionScale = motionScale;
    }

    public static RingGeometry create(
            double width, double height, double distance, float healthRatio, boolean reducedMotion) {
        double safeWidth = finiteOr(width, 0.5D);
        safeWidth = Math.max(0.5D, Math.min(3.0D, safeWidth));
        double safeHeight = finiteOr(height, 1.0D) / 2.0D;
        safeHeight = Math.max(0.5D, Math.min(2.0D, safeHeight));
        double safeDistance = Math.max(0.0D, finiteOr(distance, 0.0D));
        float safeRatio = Float.isFinite(healthRatio) ? healthRatio : 1.0F;
        safeRatio = Math.max(0.0F, Math.min(1.0F, safeRatio));
        float alpha = (float) Math.max(0.35D, Math.min(1.0D, 1.0D - safeDistance / 160.0D));
        float motionScale = reducedMotion ? 1.0F : 1.04F;
        return new RingGeometry(safeWidth, safeHeight, safeRatio * 360.0F, alpha, motionScale);
    }

    public double getHalfWidth() {
        return halfWidth;
    }

    public double getHalfHeight() {
        return halfHeight;
    }

    public float getHealthArcDegrees() {
        return healthArcDegrees;
    }

    public float getAlpha() {
        return alpha;
    }

    public float getMotionScale() {
        return motionScale;
    }

    private static double finiteOr(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }
}

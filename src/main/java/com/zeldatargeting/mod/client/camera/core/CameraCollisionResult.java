package com.zeldatargeting.mod.client.camera.core;

public final class CameraCollisionResult {
    private final double safeDistance;
    private final double vanillaDistance;
    private final double translationDelta;

    public CameraCollisionResult(
            double safeDistance,
            double vanillaDistance,
            double translationDelta) {
        this.safeDistance = safeDistance;
        this.vanillaDistance = vanillaDistance;
        this.translationDelta = translationDelta;
    }

    public double getSafeDistance() {
        return safeDistance;
    }

    public double getVanillaDistance() {
        return vanillaDistance;
    }

    public double getTranslationDelta() {
        return translationDelta;
    }
}

package com.zeldatargeting.mod.client.camera.core;

public final class CameraFrame {
    private final float yaw;
    private final float pitch;
    private final double cameraDistance;
    private final double translationDelta;
    private final double fovMultiplier;
    private final float playerAlpha;
    private final boolean applyRotation;
    private final int requestedPerspective;
    private final boolean restoreComplete;

    public CameraFrame(
            float yaw,
            float pitch,
            double cameraDistance,
            double translationDelta,
            double fovMultiplier,
            float playerAlpha,
            boolean applyRotation,
            int requestedPerspective,
            boolean restoreComplete) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.cameraDistance = cameraDistance;
        this.translationDelta = translationDelta;
        this.fovMultiplier = fovMultiplier;
        this.playerAlpha = playerAlpha;
        this.applyRotation = applyRotation;
        this.requestedPerspective = requestedPerspective;
        this.restoreComplete = restoreComplete;
    }

    public static CameraFrame baseline(int requestedPerspective) {
        return new CameraFrame(
            0.0F,
            0.0F,
            4.0D,
            0.0D,
            1.0D,
            1.0F,
            false,
            requestedPerspective,
            true
        );
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double getCameraDistance() {
        return cameraDistance;
    }

    public double getTranslationDelta() {
        return translationDelta;
    }

    public double getFovMultiplier() {
        return fovMultiplier;
    }

    public float getPlayerAlpha() {
        return playerAlpha;
    }

    public boolean shouldApplyRotation() {
        return applyRotation;
    }

    public int getRequestedPerspective() {
        return requestedPerspective;
    }

    public boolean isRestoreComplete() {
        return restoreComplete;
    }
}

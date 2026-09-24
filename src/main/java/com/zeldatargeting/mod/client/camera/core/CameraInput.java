package com.zeldatargeting.mod.client.camera.core;

public final class CameraInput {
    private final float playerYaw;
    private final float playerPitch;
    private final double targetDeltaX;
    private final double targetDeltaY;
    private final double targetDeltaZ;
    private final double targetWidth;
    private final double targetHeight;
    private final double targetDistance;
    private final CameraCollisionResult collision;
    private final long elapsedMillis;
    private final boolean freeLook;
    private final int requestedPerspective;

    public CameraInput(
            float playerYaw,
            float playerPitch,
            double targetDeltaX,
            double targetDeltaY,
            double targetDeltaZ,
            double targetWidth,
            double targetHeight,
            double targetDistance,
            CameraCollisionResult collision,
            long elapsedMillis,
            boolean freeLook,
            int requestedPerspective) {
        this.playerYaw = playerYaw;
        this.playerPitch = playerPitch;
        this.targetDeltaX = targetDeltaX;
        this.targetDeltaY = targetDeltaY;
        this.targetDeltaZ = targetDeltaZ;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.targetDistance = targetDistance;
        this.collision = collision;
        this.elapsedMillis = elapsedMillis;
        this.freeLook = freeLook;
        this.requestedPerspective = requestedPerspective;
    }

    public float getPlayerYaw() {
        return playerYaw;
    }

    public float getPlayerPitch() {
        return playerPitch;
    }

    public double getTargetDeltaX() {
        return targetDeltaX;
    }

    public double getTargetDeltaY() {
        return targetDeltaY;
    }

    public double getTargetDeltaZ() {
        return targetDeltaZ;
    }

    public double getTargetWidth() {
        return targetWidth;
    }

    public double getTargetHeight() {
        return targetHeight;
    }

    public double getTargetDistance() {
        return targetDistance;
    }

    public CameraCollisionResult getCollision() {
        return collision;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public boolean isFreeLook() {
        return freeLook;
    }

    public int getRequestedPerspective() {
        return requestedPerspective;
    }
}

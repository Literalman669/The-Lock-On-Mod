package com.zeldatargeting.mod.client.camera.compat;

/**
 * Immutable, dependency-free snapshot of Shoulder Surfing Reloaded's active
 * camera offset. Invalid snapshots remain observable but never participate in
 * aim compensation.
 */
public final class ShoulderCameraState {
    private static final double MINIMUM_OFFSET_LENGTH_SQUARED = 1.0E-12D;

    private final boolean active;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double cameraDistance;

    private ShoulderCameraState(
            boolean active,
            double offsetX,
            double offsetY,
            double offsetZ,
            double cameraDistance) {
        this.active = active;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.cameraDistance = cameraDistance;
    }

    public static ShoulderCameraState inactive() {
        return new ShoulderCameraState(false, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static ShoulderCameraState active(
            double offsetX,
            double offsetY,
            double offsetZ,
            double cameraDistance) {
        return new ShoulderCameraState(
            true,
            offsetX,
            offsetY,
            offsetZ,
            cameraDistance
        );
    }

    public boolean isActive() {
        return active;
    }

    public boolean canCompensate() {
        return active
            && isFinite(offsetX)
            && isFinite(offsetY)
            && isFinite(offsetZ)
            && isFinite(cameraDistance)
            && cameraDistance > 0.0D
            && offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ
                > MINIMUM_OFFSET_LENGTH_SQUARED;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public double getCameraDistance() {
        return cameraDistance;
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}

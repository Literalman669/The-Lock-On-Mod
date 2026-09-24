package com.zeldatargeting.mod.client.camera.compat;

/** Selects the single active camera host without rewriting movement input. */
public final class CameraRotationPolicy {

    private CameraRotationPolicy() {
    }

    public enum RotationOwner {
        PLAYER
    }

    public enum CameraHost {
        SHOULDER_SURFING,
        VANILLA
    }

    public enum OffsetOwner {
        CAMERA_HOST,
        ZELDA
    }

    public static final class Decision {
        private final CameraHost cameraHost;
        private final RotationOwner rotationOwner;
        private final OffsetOwner offsetOwner;

        private Decision(
                CameraHost cameraHost,
                RotationOwner rotationOwner,
                OffsetOwner offsetOwner) {
            this.cameraHost = cameraHost;
            this.rotationOwner = rotationOwner;
            this.offsetOwner = offsetOwner;
        }

        public CameraHost getCameraHost() {
            return cameraHost;
        }

        public RotationOwner getRotationOwner() {
            return rotationOwner;
        }

        public OffsetOwner getOffsetOwner() {
            return offsetOwner;
        }

        public boolean shouldRemapMovement() {
            return false;
        }
    }

    public static Decision resolve(boolean shoulderSurfingActive) {
        if (shoulderSurfingActive) {
            return new Decision(
                CameraHost.SHOULDER_SURFING,
                RotationOwner.PLAYER,
                OffsetOwner.CAMERA_HOST
            );
        }
        return new Decision(
            CameraHost.VANILLA,
            RotationOwner.PLAYER,
            OffsetOwner.ZELDA
        );
    }
}

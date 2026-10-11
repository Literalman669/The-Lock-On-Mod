package com.zeldatargeting.mod.client.camera.compat;

/**
 * Decides when a lock asks Shoulder Surfing Reloaded's Adaptive crosshair for its
 * dynamic, eye-based ray.
 *
 * <p>SSR's static ray starts at the shoulder camera, so it passes no closer to
 * the player's eyes than the camera's sideways offset. With a wide offset (2.5
 * blocks is common) every melee-range target is out of its reach, and
 * {@link ShoulderAimSolver} can only cap the miss. The eye ray reaches every
 * target exactly, so it is used for the whole lock, in Epic Fight's Battle mode
 * and outside it. Explicit Static or Dynamic modes are the player's choice and
 * are left alone.</p>
 */
public final class ShoulderCrosshairPolicy {
    private ShoulderCrosshairPolicy() {
    }

    public static boolean useDynamicCrosshair(
            boolean shoulderAimEnabled,
            boolean locked,
            boolean viewingAsPlayer,
            boolean shoulderCameraActive,
            boolean adaptiveCrosshairMode) {
        return shoulderAimEnabled
            && locked
            && viewingAsPlayer
            && shoulderCameraActive
            && adaptiveCrosshairMode;
    }
}

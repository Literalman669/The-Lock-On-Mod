package com.zeldatargeting.mod.client.camera.compat;

/**
 * Epic Fight places melee colliders from the player's eye-facing yaw, while
 * SSR's static crosshair traces from a shifted shoulder origin.
 */
public final class EpicFightAimPolicy {
    private EpicFightAimPolicy() {
    }

    public static ShoulderAimSolver.AimVector aim(
            double targetX,
            double targetY,
            double targetZ,
            ShoulderCameraState shoulder,
            boolean epicFightBattle) {
        ShoulderCameraState aimState = epicFightBattle
            && shoulder != null
            && shoulder.isActive()
                ? ShoulderCameraState.inactive()
                : shoulder;
        return ShoulderAimSolver.compensate(
            targetX, targetY, targetZ, aimState
        );
    }
}

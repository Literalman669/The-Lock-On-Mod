package com.zeldatargeting.mod.client.presentation.core;

/**
 * Where the soft aim marker sits relative to the crosshair: nudged toward the
 * target as seen from the player's view, fading in as the view drifts off it.
 */
public final class SoftAimOffset {
    private static final double HIDDEN_BELOW_DOT = 0.2D;
    private static final double MAX_NUDGE_PIXELS = 12.0D;

    private final int x;
    private final int y;
    private final int alpha;

    private SoftAimOffset(int x, int y, int alpha) {
        this.x = x;
        this.y = y;
        this.alpha = alpha;
    }

    /**
     * @param yawDegrees   player yaw, Minecraft convention (0 faces +Z, 90 faces -X)
     * @param pitchDegrees player pitch, positive looks down
     * @return the marker offset in GUI pixels (+x right, +y down), or null when the
     *         target is far enough off-screen that the marker is hidden
     */
    public static SoftAimOffset compute(
            float yawDegrees, float pitchDegrees,
            double toTargetX, double toTargetY, double toTargetZ) {
        double length = Math.sqrt(toTargetX * toTargetX + toTargetY * toTargetY + toTargetZ * toTargetZ);
        if (!(length > 0.001D) || Double.isInfinite(length)) {
            return null;
        }
        double dx = toTargetX / length;
        double dy = toTargetY / length;
        double dz = toTargetZ / length;

        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        double lookX = -Math.sin(yaw) * Math.cos(pitch);
        double lookY = -Math.sin(pitch);
        double lookZ = Math.cos(yaw) * Math.cos(pitch);
        // Screen right comes from yaw alone, so it stays defined when looking straight up or down.
        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);
        // Screen up = right x look.
        double upX = -rightZ * lookY;
        double upY = rightZ * lookX - rightX * lookZ;
        double upZ = rightX * lookY;

        double dot = lookX * dx + lookY * dy + lookZ * dz;
        if (dot < HIDDEN_BELOW_DOT) {
            return null;
        }
        double screenRight = rightX * dx + rightZ * dz;
        double screenUp = upX * dx + upY * dy + upZ * dz;
        double sideways = Math.sqrt(screenRight * screenRight + screenUp * screenUp);
        double nudge = Math.min(MAX_NUDGE_PIXELS, (1.0D - dot) * 80.0D);
        double scale = sideways > 1.0E-9D ? nudge / sideways : 0.0D;
        int alpha = (int) (Math.min(1.0D, (1.0D - dot) * 4.0D) * 200.0D);
        return new SoftAimOffset(
            (int) Math.round(screenRight * scale),
            (int) Math.round(-screenUp * scale),
            alpha
        );
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAlpha() {
        return alpha;
    }
}

package com.zeldatargeting.mod.client.camera.compat;

/**
 * Owns one reversible runtime-only centering operation for an active shoulder
 * camera. The access boundary keeps optional-mod reflection out of the state
 * machine and makes exact restoration independently testable.
 */
public final class ShoulderOffsetSession {
    private boolean active;
    private Offset original;

    public boolean begin(boolean shoulderCameraActive, OffsetAccess access) {
        if (active) {
            return true;
        }
        if (!shoulderCameraActive || access == null) {
            return false;
        }
        Offset observed = access.read();
        if (observed == null || !observed.isFinite()) {
            return false;
        }
        original = observed;
        access.center();
        active = true;
        return true;
    }

    public void end(OffsetAccess access) {
        if (!active) {
            return;
        }
        try {
            if (access != null && original != null) {
                access.restore(original);
            }
        } finally {
            active = false;
            original = null;
        }
    }

    public boolean isActive() {
        return active;
    }

    interface OffsetAccess {
        Offset read();

        void center();

        void restore(Offset offset);
    }

    static final class Offset {
        private final double x;
        private final double y;

        Offset(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double getX() {
            return x;
        }

        double getY() {
            return y;
        }

        private boolean isFinite() {
            return isFinite(x) && isFinite(y);
        }

        private static boolean isFinite(double value) {
            return !Double.isNaN(value) && !Double.isInfinite(value);
        }
    }
}

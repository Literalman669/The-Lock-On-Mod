package com.zeldatargeting.mod.client.camera.compat;

/**
 * Converges a target direction against Shoulder Surfing Reloaded's laterally
 * shifted ray origin. The implementation mirrors SSR 2.9.x vector conventions
 * without linking against Minecraft or the optional mod.
 */
public final class ShoulderAimSolver {
    private static final int CONVERGENCE_ITERATIONS = 4;
    private static final double MINIMUM_LENGTH_SQUARED = 1.0E-12D;

    private ShoulderAimSolver() {
    }

    public static AimVector compensate(
            double targetX,
            double targetY,
            double targetZ,
            ShoulderCameraState state) {
        if (!isFiniteVector(targetX, targetY, targetZ)) {
            return new AimVector(0.0D, 0.0D, 0.0D, false);
        }
        double targetLengthSquared = lengthSquared(targetX, targetY, targetZ);
        if (targetLengthSquared <= MINIMUM_LENGTH_SQUARED
                || state == null
                || !state.canCompensate()) {
            return new AimVector(targetX, targetY, targetZ, false);
        }

        double aimX = targetX;
        double aimY = targetY;
        double aimZ = targetZ;
        for (int iteration = 0; iteration < CONVERGENCE_ITERATIONS; iteration++) {
            Rotation rotation = lookAt(aimX, aimY, aimZ);
            Vector view = normalize(aimX, aimY, aimZ);
            Vector camera = rotateAndScaleLocalOffset(state, rotation);
            double parallelDistance = camera.dot(view);
            Vector lateralOrigin = camera.subtract(view.scale(parallelDistance));
            aimX = targetX - lateralOrigin.x;
            aimY = targetY - lateralOrigin.y;
            aimZ = targetZ - lateralOrigin.z;
        }

        if (!isFiniteVector(aimX, aimY, aimZ)
                || lengthSquared(aimX, aimY, aimZ) <= MINIMUM_LENGTH_SQUARED) {
            return new AimVector(targetX, targetY, targetZ, false);
        }
        return new AimVector(aimX, aimY, aimZ, true);
    }

    private static Rotation lookAt(double x, double y, double z) {
        double horizontal = Math.sqrt(x * x + z * z);
        double yaw = Math.toDegrees(Math.atan2(-x, z));
        double pitch = -Math.toDegrees(Math.atan2(y, horizontal));
        return new Rotation(yaw, pitch);
    }

    private static Vector rotateAndScaleLocalOffset(
            ShoulderCameraState state,
            Rotation rotation) {
        Vector local = normalize(
            state.getOffsetX(),
            state.getOffsetY(),
            -state.getOffsetZ()
        ).scale(state.getCameraDistance());

        double pitch = Math.toRadians(-rotation.pitch);
        double pitchCos = Math.cos(pitch);
        double pitchSin = Math.sin(pitch);
        Vector pitched = new Vector(
            local.x,
            local.y * pitchCos + local.z * pitchSin,
            local.z * pitchCos - local.y * pitchSin
        );

        double yaw = Math.toRadians(-rotation.yaw);
        double yawCos = Math.cos(yaw);
        double yawSin = Math.sin(yaw);
        return new Vector(
            pitched.x * yawCos + pitched.z * yawSin,
            pitched.y,
            pitched.z * yawCos - pitched.x * yawSin
        );
    }

    private static Vector normalize(double x, double y, double z) {
        double length = Math.sqrt(lengthSquared(x, y, z));
        if (!isFinite(length) || length <= 0.0D) {
            return new Vector(0.0D, 0.0D, 0.0D);
        }
        return new Vector(x / length, y / length, z / length);
    }

    private static double lengthSquared(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    private static boolean isFiniteVector(double x, double y, double z) {
        return isFinite(x) && isFinite(y) && isFinite(z);
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public static final class AimVector {
        private final double x;
        private final double y;
        private final double z;
        private final boolean compensated;

        private AimVector(double x, double y, double z, boolean compensated) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.compensated = compensated;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public boolean isCompensated() {
            return compensated;
        }
    }

    private static final class Rotation {
        private final double yaw;
        private final double pitch;

        private Rotation(double yaw, double pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private static final class Vector {
        private final double x;
        private final double y;
        private final double z;

        private Vector(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private Vector scale(double factor) {
            return new Vector(x * factor, y * factor, z * factor);
        }

        private Vector subtract(Vector other) {
            return new Vector(x - other.x, y - other.y, z - other.z);
        }

        private double dot(Vector other) {
            return x * other.x + y * other.y + z * other.z;
        }
    }
}

package com.zeldatargeting.mod.client.math;

public final class CameraMath {
    private CameraMath() {
    }

    public static Rotation lookAt(double deltaX, double deltaY, double deltaZ) {
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (horizontalDistance == 0.0D && deltaY == 0.0D) {
            return new Rotation(0.0F, 0.0F);
        }
        float yaw = (float) Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        return new Rotation(yaw, clamp(pitch, -90.0F, 90.0F));
    }

    public static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public static float interpolateAngle(float current, float target, float factor) {
        return current + wrapDegrees(target - current) * factor;
    }

    public static float interpolate(float current, float target, float factor) {
        return current + (target - current) * factor;
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static final class Rotation {
        private final float yaw;
        private final float pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }
    }
}

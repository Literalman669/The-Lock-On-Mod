package com.zeldatargeting.mod.client.math;

public final class TargetingMath {
    private TargetingMath() {
    }

    public static double horizontalBearing(double deltaX, double deltaZ) {
        double bearing = Math.toDegrees(Math.atan2(deltaX, -deltaZ));
        return bearing < 0.0D ? bearing + 360.0D : bearing;
    }

    public static double angleDegrees(
            double lookX,
            double lookY,
            double lookZ,
            double targetX,
            double targetY,
            double targetZ) {
        double lookLength = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
        double targetLength = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
        if (lookLength == 0.0D || targetLength == 0.0D) {
            return 180.0D;
        }
        double dot = (lookX * targetX + lookY * targetY + lookZ * targetZ)
                / (lookLength * targetLength);
        double clampedDot = Math.max(-1.0D, Math.min(1.0D, dot));
        return Math.toDegrees(Math.acos(clampedDot));
    }

    public static int adjacentIndex(int currentIndex, int count, boolean forward) {
        if (count <= 0) {
            return -1;
        }
        if (currentIndex < 0 || currentIndex >= count) {
            return forward ? 0 : count - 1;
        }
        return Math.floorMod(currentIndex + (forward ? 1 : -1), count);
    }
}

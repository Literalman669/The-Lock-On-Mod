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

    /**
     * Melee reach used when the targeting range follows the player's reach:
     * Minecraft 1.12.2 allows entity hits within 3 blocks in survival and 6 in
     * creative. {@code bonusReach} is extra reach granted by other mods.
     */
    public static double meleeReach(boolean creative, double bonusReach) {
        double bonus = Double.isFinite(bonusReach) ? Math.max(0.0D, bonusReach) : 0.0D;
        return (creative ? 6.0D : 3.0D) + bonus;
    }

    // Synced reach is measured to the hitbox, tracking to the center, so leave room
    // for wide mobs whose center sits well past the reach distance.
    static final double SYNCED_REACH_TRACKING_MARGIN = 3.0D;

    /**
     * Tracking distance that never releases a target the player could lock at that
     * same moment, even if Max Tracking Distance is set below the targeting range.
     */
    public static double effectiveTrackingDistance(
            double configuredTracking,
            double targetingRange,
            boolean rangeSyncedToReach) {
        double acquisition = rangeSyncedToReach
            ? targetingRange + SYNCED_REACH_TRACKING_MARGIN
            : targetingRange;
        return Math.max(configuredTracking, acquisition);
    }

    /** Distance from a point to the nearest point of a box; zero when inside it. */
    public static double distanceToBox(
            double x, double y, double z,
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ) {
        double dx = Math.max(0.0D, Math.max(minX - x, x - maxX));
        double dy = Math.max(0.0D, Math.max(minY - y, y - maxY));
        double dz = Math.max(0.0D, Math.max(minZ - z, z - maxZ));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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

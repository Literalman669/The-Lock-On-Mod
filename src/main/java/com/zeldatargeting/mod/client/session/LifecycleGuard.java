package com.zeldatargeting.mod.client.session;

public final class LifecycleGuard {
    private LifecycleGuard() {
    }

    public static LockReleaseReason evaluate(
            boolean worldAvailable,
            boolean playerAvailable,
            boolean playerAlive,
            boolean targetAvailable,
            boolean sameDimension) {
        if (!worldAvailable) {
            return LockReleaseReason.WORLD_UNAVAILABLE;
        }
        if (!playerAvailable) {
            return LockReleaseReason.PLAYER_UNAVAILABLE;
        }
        if (!playerAlive) {
            return LockReleaseReason.PLAYER_DEAD;
        }
        if (!targetAvailable) {
            return LockReleaseReason.TARGET_UNAVAILABLE;
        }
        if (!sameDimension) {
            return LockReleaseReason.DIMENSION_CHANGED;
        }
        return LockReleaseReason.NONE;
    }
}

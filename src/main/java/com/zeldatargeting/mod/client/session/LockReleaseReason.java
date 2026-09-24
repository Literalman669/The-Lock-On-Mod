package com.zeldatargeting.mod.client.session;

public enum LockReleaseReason {
    NONE,
    MANUAL,
    WORLD_UNAVAILABLE,
    PLAYER_UNAVAILABLE,
    PLAYER_DEAD,
    TARGET_UNAVAILABLE,
    TARGET_DEAD,
    TARGET_REMOVED,
    OUT_OF_RANGE,
    OCCLUDED,
    DIMENSION_CHANGED,
    DISCONNECTED,
    QUICK_SWITCH_FAILED
}

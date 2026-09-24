package com.zeldatargeting.mod.client.session;

public enum LockPhase {
    IDLE,
    ACQUIRING,
    LOCKED,
    OCCLUDED_GRACE,
    SWITCHING,
    RELEASING
}

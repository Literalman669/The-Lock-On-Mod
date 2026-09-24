package com.zeldatargeting.mod.client.targeting.core;

public enum TargetPriority {
    NEAREST,
    HEALTH,
    THREAT,
    ANGLE;

    public static TargetPriority fromConfig(String value) {
        if (value != null) {
            if ("health".equalsIgnoreCase(value)) {
                return HEALTH;
            }
            if ("threat".equalsIgnoreCase(value)) {
                return THREAT;
            }
            if ("angle".equalsIgnoreCase(value)) {
                return ANGLE;
            }
        }
        return NEAREST;
    }
}

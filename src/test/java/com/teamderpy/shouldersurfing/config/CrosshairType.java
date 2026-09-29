package com.teamderpy.shouldersurfing.config;

/** Test stand-in for the Shoulder Surfing Reloaded 2.9.x enum read by reflection. */
public enum CrosshairType {
    ADAPTIVE,
    DYNAMIC,
    STATIC;

    public static boolean holdingAdaptiveItem;

    public boolean isDynamic() {
        return this == ADAPTIVE ? holdingAdaptiveItem : this == DYNAMIC;
    }
}

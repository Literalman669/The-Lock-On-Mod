package com.teamderpy.shouldersurfing.client;

/** Test stand-in for the Shoulder Surfing Reloaded 2.9.x class read by reflection. */
public class ShoulderInstance {
    private static final ShoulderInstance INSTANCE = new ShoulderInstance();

    public static boolean shoulderSurfing = true;
    public static double offsetX;
    public static double offsetY;
    public static double offsetZ;
    public static double offsetXOld;
    public static double offsetYOld;
    public static double offsetZOld;

    public static ShoulderInstance getInstance() {
        return INSTANCE;
    }

    public boolean doShoulderSurfing() {
        return shoulderSurfing;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public double getOffsetXOld() {
        return offsetXOld;
    }

    public double getOffsetYOld() {
        return offsetYOld;
    }

    public double getOffsetZOld() {
        return offsetZOld;
    }
}

package com.teamderpy.shouldersurfing.client;

/** Test stand-in for the Shoulder Surfing Reloaded 2.9.x class read by reflection. */
public class ShoulderRenderer {
    private static final ShoulderRenderer INSTANCE = new ShoulderRenderer();

    public static double cameraDistance = 3.0D;

    public static ShoulderRenderer getInstance() {
        return INSTANCE;
    }

    public double getCameraDistance() {
        return cameraDistance;
    }
}

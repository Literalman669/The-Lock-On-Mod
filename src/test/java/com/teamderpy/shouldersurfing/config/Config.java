package com.teamderpy.shouldersurfing.config;

/** Test stand-in for the Shoulder Surfing Reloaded 2.9.x config read by reflection. */
public class Config {
    public static final ClientConfig CLIENT = new ClientConfig();

    public static class ClientConfig {
        public CrosshairType crosshairType = CrosshairType.STATIC;

        public CrosshairType getCrosshairType() {
            return crosshairType;
        }
    }
}

package com.zeldatargeting.mod.client.camera.core;

import java.util.Locale;

public final class CameraProfiles {
    public static final CameraProfile CINEMATIC = profile(
        "cinematic", 180.0D, 3.0D, 0.12D, 60.0F, 40.0F
    );
    public static final CameraProfile BALANCED = profile(
        "balanced", 100.0D, 2.0D, 0.06D, 90.0F, 60.0F
    );
    public static final CameraProfile SNAPPY = profile(
        "snappy", 45.0D, 1.0D, 0.0D, 120.0F, 75.0F
    );

    private CameraProfiles() {
    }

    public static CameraProfile fromName(String name) {
        if (name == null) {
            return BALANCED;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        if ("cinematic".equals(normalized)) {
            return CINEMATIC;
        }
        if ("snappy".equals(normalized)) {
            return SNAPPY;
        }
        return BALANCED;
    }

    private static CameraProfile profile(
            String name,
            double halfLife,
            double sizeGain,
            double maxFov,
            float maxYaw,
            float maxPitch) {
        return new CameraProfile(
            name, halfLife, sizeGain, maxFov, maxYaw, maxPitch, 200L,
            4.0D, 1.25D, 0.45D, 0.25F,
            0.65D, 1.25D, 100L, 200L,
            0.0D, 1.0D
        );
    }
}

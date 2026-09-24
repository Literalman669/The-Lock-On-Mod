package com.zeldatargeting.mod.client.targeting.core;

import java.util.Locale;

public final class TargetingOptions {
    private final TargetPriority priority;
    private final String cameraProfileName;
    private final double maxTrackingDistance;
    private final long occlusionGraceMillis;
    private final long releaseFadeMillis;
    private final long cycleCooldownMillis;
    private final boolean quickSwitchEnabled;

    public TargetingOptions(
            TargetPriority priority,
            String cameraProfileName,
            double maxTrackingDistance,
            long occlusionGraceMillis,
            long releaseFadeMillis,
            long cycleCooldownMillis,
            boolean quickSwitchEnabled) {
        if (Double.isNaN(maxTrackingDistance) || maxTrackingDistance < 0.0D) {
            throw new IllegalArgumentException("maxTrackingDistance must be non-negative");
        }
        if (occlusionGraceMillis < 0L) {
            throw new IllegalArgumentException("occlusionGraceMillis must be non-negative");
        }
        if (releaseFadeMillis < 0L) {
            throw new IllegalArgumentException("releaseFadeMillis must be non-negative");
        }
        if (cycleCooldownMillis < 0L) {
            throw new IllegalArgumentException("cycleCooldownMillis must be non-negative");
        }
        this.priority = priority == null ? TargetPriority.NEAREST : priority;
        this.cameraProfileName = normalizeCameraProfileName(cameraProfileName);
        this.maxTrackingDistance = maxTrackingDistance;
        this.occlusionGraceMillis = occlusionGraceMillis;
        this.releaseFadeMillis = releaseFadeMillis;
        this.cycleCooldownMillis = cycleCooldownMillis;
        this.quickSwitchEnabled = quickSwitchEnabled;
    }

    public TargetPriority getPriority() {
        return priority;
    }

    public String getCameraProfileName() {
        return cameraProfileName;
    }

    public double getMaxTrackingDistance() {
        return maxTrackingDistance;
    }

    public long getOcclusionGraceMillis() {
        return occlusionGraceMillis;
    }

    public long getReleaseFadeMillis() {
        return releaseFadeMillis;
    }

    public long getCycleCooldownMillis() {
        return cycleCooldownMillis;
    }

    public boolean isQuickSwitchEnabled() {
        return quickSwitchEnabled;
    }

    private static String normalizeCameraProfileName(String value) {
        if (value == null) {
            return "balanced";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("cinematic".equals(normalized) || "snappy".equals(normalized)) {
            return normalized;
        }
        return "balanced";
    }
}

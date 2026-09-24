package com.zeldatargeting.mod.config;

import java.util.Locale;

/** Validates untrusted configuration data before it can reach the client runtime. */
public final class TargetingSettingsValidator {

    private TargetingSettingsValidator() {
    }

    public static TargetingSettings sanitize(TargetingSettings source) {
        TargetingSettings defaults = TargetingPreset.BALANCED.createSettings();
        TargetingSettings settings = source == null ? defaults : source.copy();

        settings.schemaVersion = 4;

        settings.targetingRange = clampFinite(settings.targetingRange, defaults.targetingRange, 1.0D, 64.0D);
        settings.maxTrackingDistance = clampFinite(settings.maxTrackingDistance, defaults.maxTrackingDistance, 1.0D, 128.0D);
        settings.maxAngle = clampFinite(settings.maxAngle, defaults.maxAngle, 15.0D, 180.0D);
        settings.targetPriority = select(settings.targetPriority, defaults.targetPriority,
                "nearest", "angle", "health", "threat");

        settings.reticleScale = clampFinite(settings.reticleScale, defaults.reticleScale, 0.5F, 3.0F);
        settings.reticleColor = clampColor(settings.reticleColor, defaults.reticleColor);
        settings.hudAnchor = select(settings.hudAnchor, defaults.hudAnchor,
                "top-left", "top-right", "bottom-left", "bottom-right", "center");
        settings.hudOffsetX = clamp(settings.hudOffsetX, -500, 500);
        settings.hudOffsetY = clamp(settings.hudOffsetY, -500, 500);

        settings.cameraSmoothness = clampFinite(settings.cameraSmoothness, defaults.cameraSmoothness, 0.01F, 1.0F);
        settings.maxPitchAdjustment = clampFinite(settings.maxPitchAdjustment, defaults.maxPitchAdjustment, 0.0F, 90.0F);
        settings.maxYawAdjustment = clampFinite(settings.maxYawAdjustment, defaults.maxYawAdjustment, 0.0F, 180.0F);
        settings.cameraFocusYOffset = clampFinite(settings.cameraFocusYOffset, defaults.cameraFocusYOffset, -1.0F, 1.0F);
        settings.lockOnPreset = TargetingPreset.fromId(settings.lockOnPreset).getId();

        settings.soundVolume = clampFinite(settings.soundVolume, defaults.soundVolume, 0.0F, 1.0F);
        settings.targetLockVolume = clampFinite(settings.targetLockVolume, defaults.targetLockVolume, 0.0F, 1.0F);
        settings.targetSwitchVolume = clampFinite(settings.targetSwitchVolume, defaults.targetSwitchVolume, 0.0F, 1.0F);
        settings.lethalTargetVolume = clampFinite(settings.lethalTargetVolume, defaults.lethalTargetVolume, 0.0F, 1.0F);
        settings.targetLostVolume = clampFinite(settings.targetLostVolume, defaults.targetLostVolume, 0.0F, 1.0F);
        settings.targetLockPitch = clampFinite(settings.targetLockPitch, defaults.targetLockPitch, 0.5F, 2.0F);
        settings.targetSwitchPitch = clampFinite(settings.targetSwitchPitch, defaults.targetSwitchPitch, 0.5F, 2.0F);
        settings.lethalTargetPitch = clampFinite(settings.lethalTargetPitch, defaults.lethalTargetPitch, 0.5F, 2.0F);
        settings.targetLostPitch = clampFinite(settings.targetLostPitch, defaults.targetLostPitch, 0.5F, 2.0F);
        settings.soundTheme = select(settings.soundTheme, defaults.soundTheme,
                "default", "zelda", "modern", "subtle");

        settings.damageNumbersScale = clampFinite(settings.damageNumbersScale, defaults.damageNumbersScale, 0.5F, 3.0F);
        settings.damageNumbersDuration = clamp(settings.damageNumbersDuration, 20, 200);
        settings.damageNumbersColor = clampColor(settings.damageNumbersColor, defaults.damageNumbersColor);
        settings.criticalDamageColor = clampColor(settings.criticalDamageColor, defaults.criticalDamageColor);
        settings.lethalDamageColor = clampColor(settings.lethalDamageColor, defaults.lethalDamageColor);
        settings.damageNumbersOffset = clampFinite(settings.damageNumbersOffset, defaults.damageNumbersOffset, 0.0F, 2.0F);
        settings.damageNumbersMotion = select(settings.damageNumbersMotion, defaults.damageNumbersMotion,
                "default", "subtle", "arcade");
        settings.damagePredictionScale = clampFinite(settings.damagePredictionScale, defaults.damagePredictionScale, 0.5F, 2.0F);

        settings.updateFrequency = clamp(settings.updateFrequency, 1, 20);
        settings.validationInterval = clamp(settings.validationInterval, 1, 60);

        settings.presentationPalette = select(settings.presentationPalette, defaults.presentationPalette,
                "default", "deuteranopia", "protanopia", "tritanopia");
        settings.hudScale = clampFinite(settings.hudScale, defaults.hudScale, 0.75F, 1.5F);
        settings.hudOpacity = clampFinite(settings.hudOpacity, defaults.hudOpacity, 0.35F, 1.0F);
        settings.ringThickness = clampFinite(settings.ringThickness, defaults.ringThickness, 0.5F, 2.5F);
        settings.ringGlowStrength = clampFinite(settings.ringGlowStrength, defaults.ringGlowStrength, 0.0F, 1.5F);

        return settings;
    }

    private static String select(String value, String fallback, String... allowedValues) {
        if (value != null) {
            String normalized = value.toLowerCase(Locale.ROOT);
            for (String allowed : allowedValues) {
                if (allowed.equals(normalized)) {
                    return allowed;
                }
            }
        }
        return fallback;
    }

    private static double clampFinite(double value, double fallback, double minimum, double maximum) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float clampFinite(float value, float fallback, float minimum, float maximum) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return fallback;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int clampColor(int value, int fallback) {
        if (value < 0 || value > 0xFFFFFF) {
            return fallback;
        }
        return value;
    }
}

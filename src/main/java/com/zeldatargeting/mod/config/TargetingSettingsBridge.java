package com.zeldatargeting.mod.config;

/**
 * Copies settings between the transactional configuration model and the legacy
 * static runtime facade used by the rest of the client.
 */
public final class TargetingSettingsBridge {

    private TargetingSettingsBridge() {
    }

    public static TargetingSettings captureRuntime() {
        TargetingSettings settings = new TargetingSettings();

        settings.targetingRange = TargetingConfig.targetingRange;
        settings.maxTrackingDistance = TargetingConfig.maxTrackingDistance;
        settings.maxAngle = TargetingConfig.maxAngle;
        settings.requireLineOfSight = TargetingConfig.requireLineOfSight;
        settings.targetPriority = TargetingConfig.targetPriority;

        settings.showReticle = TargetingConfig.showReticle;
        settings.showHealthBar = TargetingConfig.showHealthBar;
        settings.showDistance = TargetingConfig.showDistance;
        settings.showTargetName = TargetingConfig.showTargetName;
        settings.reticleScale = TargetingConfig.reticleScale;
        settings.reticleColor = TargetingConfig.reticleColor;
        settings.compactHudMode = TargetingConfig.compactHudMode;
        settings.softAimIndicator = TargetingConfig.softAimIndicator;
        settings.targetHistoryEnabled = TargetingConfig.targetHistoryEnabled;
        settings.bossStylePanel = TargetingConfig.bossStylePanel;
        settings.hudAnchor = TargetingConfig.hudAnchor;
        settings.hudOffsetX = TargetingConfig.hudOffsetX;
        settings.hudOffsetY = TargetingConfig.hudOffsetY;

        settings.enableCameraLockOn = TargetingConfig.enableCameraLockOn;
        settings.cameraSmoothness = TargetingConfig.cameraSmoothness;
        settings.maxPitchAdjustment = TargetingConfig.maxPitchAdjustment;
        settings.maxYawAdjustment = TargetingConfig.maxYawAdjustment;
        settings.autoThirdPerson = TargetingConfig.autoThirdPerson;
        settings.ssrCompensationEnabled = TargetingConfig.ssrCompensationEnabled;
        settings.cameraFocusYOffset = TargetingConfig.cameraFocusYOffset;
        settings.lockOnPreset = TargetingConfig.lockOnPreset;
        settings.debugCompatibility = TargetingConfig.debugCompatibility;
        settings.perModeSmoothingEnabled = TargetingConfig.perModeSmoothingEnabled;

        settings.targetHostileMobs = TargetingConfig.targetHostileMobs;
        settings.targetNeutralMobs = TargetingConfig.targetNeutralMobs;
        settings.targetPassiveMobs = TargetingConfig.targetPassiveMobs;
        settings.targetPlayers = TargetingConfig.targetPlayers;

        settings.enableSounds = TargetingConfig.enableSounds;
        settings.soundVolume = TargetingConfig.soundVolume;
        settings.enableTargetLockSound = TargetingConfig.enableTargetLockSound;
        settings.enableTargetSwitchSound = TargetingConfig.enableTargetSwitchSound;
        settings.enableLethalTargetSound = TargetingConfig.enableLethalTargetSound;
        settings.enableTargetLostSound = TargetingConfig.enableTargetLostSound;
        settings.targetLockVolume = TargetingConfig.targetLockVolume;
        settings.targetSwitchVolume = TargetingConfig.targetSwitchVolume;
        settings.lethalTargetVolume = TargetingConfig.lethalTargetVolume;
        settings.targetLostVolume = TargetingConfig.targetLostVolume;
        settings.targetLockPitch = TargetingConfig.targetLockPitch;
        settings.targetSwitchPitch = TargetingConfig.targetSwitchPitch;
        settings.lethalTargetPitch = TargetingConfig.lethalTargetPitch;
        settings.targetLostPitch = TargetingConfig.targetLostPitch;
        settings.soundTheme = TargetingConfig.soundTheme;
        settings.enableSoundVariety = TargetingConfig.enableSoundVariety;

        settings.enableDamageNumbers = TargetingConfig.enableDamageNumbers;
        settings.damageNumbersScale = TargetingConfig.damageNumbersScale;
        settings.damageNumbersDuration = TargetingConfig.damageNumbersDuration;
        settings.damageNumbersCrits = TargetingConfig.damageNumbersCrits;
        settings.damageNumbersColors = TargetingConfig.damageNumbersColors;
        settings.damageNumbersColor = TargetingConfig.damageNumbersColor;
        settings.criticalDamageColor = TargetingConfig.criticalDamageColor;
        settings.lethalDamageColor = TargetingConfig.lethalDamageColor;
        settings.damageNumbersFadeOut = TargetingConfig.damageNumbersFadeOut;
        settings.damageNumbersOffset = TargetingConfig.damageNumbersOffset;
        settings.damageNumbersMotion = TargetingConfig.damageNumbersMotion;
        settings.critEmphasis = TargetingConfig.critEmphasis;

        settings.showDamagePrediction = TargetingConfig.showDamagePrediction;
        settings.showHitsToKill = TargetingConfig.showHitsToKill;
        settings.showVulnerabilities = TargetingConfig.showVulnerabilities;
        settings.highlightLethalTargets = TargetingConfig.highlightLethalTargets;
        settings.damagePredictionScale = TargetingConfig.damagePredictionScale;

        settings.updateFrequency = TargetingConfig.updateFrequency;
        settings.validationInterval = TargetingConfig.validationInterval;

        settings.presentationPalette = TargetingConfig.presentationPalette;
        settings.reducedMotion = TargetingConfig.reducedMotion;
        settings.hudScale = TargetingConfig.hudScale;
        settings.hudOpacity = TargetingConfig.hudOpacity;
        settings.ringEnabled = TargetingConfig.ringEnabled;
        settings.ringThickness = TargetingConfig.ringThickness;
        settings.ringGlowStrength = TargetingConfig.ringGlowStrength;
        settings.ringHealthArcEnabled = TargetingConfig.ringHealthArcEnabled;

        return TargetingSettingsValidator.sanitize(settings);
    }

    public static void applyToRuntime(TargetingSettings source) {
        TargetingSettings settings = TargetingSettingsValidator.sanitize(source);

        TargetingConfig.targetingRange = settings.targetingRange;
        TargetingConfig.maxTrackingDistance = settings.maxTrackingDistance;
        TargetingConfig.maxAngle = settings.maxAngle;
        TargetingConfig.requireLineOfSight = settings.requireLineOfSight;
        TargetingConfig.targetPriority = settings.targetPriority;

        TargetingConfig.showReticle = settings.showReticle;
        TargetingConfig.showHealthBar = settings.showHealthBar;
        TargetingConfig.showDistance = settings.showDistance;
        TargetingConfig.showTargetName = settings.showTargetName;
        TargetingConfig.reticleScale = settings.reticleScale;
        TargetingConfig.reticleColor = settings.reticleColor;
        TargetingConfig.compactHudMode = settings.compactHudMode;
        TargetingConfig.softAimIndicator = settings.softAimIndicator;
        TargetingConfig.targetHistoryEnabled = settings.targetHistoryEnabled;
        TargetingConfig.bossStylePanel = settings.bossStylePanel;
        TargetingConfig.hudAnchor = settings.hudAnchor;
        TargetingConfig.hudOffsetX = settings.hudOffsetX;
        TargetingConfig.hudOffsetY = settings.hudOffsetY;

        TargetingConfig.enableCameraLockOn = settings.enableCameraLockOn;
        TargetingConfig.cameraSmoothness = settings.cameraSmoothness;
        TargetingConfig.maxPitchAdjustment = settings.maxPitchAdjustment;
        TargetingConfig.maxYawAdjustment = settings.maxYawAdjustment;
        TargetingConfig.autoThirdPerson = settings.autoThirdPerson;
        TargetingConfig.ssrCompensationEnabled = settings.ssrCompensationEnabled;
        TargetingConfig.cameraFocusYOffset = settings.cameraFocusYOffset;
        TargetingConfig.lockOnPreset = settings.lockOnPreset;
        TargetingConfig.debugCompatibility = settings.debugCompatibility;
        TargetingConfig.perModeSmoothingEnabled = settings.perModeSmoothingEnabled;

        TargetingConfig.targetHostileMobs = settings.targetHostileMobs;
        TargetingConfig.targetNeutralMobs = settings.targetNeutralMobs;
        TargetingConfig.targetPassiveMobs = settings.targetPassiveMobs;
        TargetingConfig.targetPlayers = settings.targetPlayers;

        TargetingConfig.enableSounds = settings.enableSounds;
        TargetingConfig.soundVolume = settings.soundVolume;
        TargetingConfig.enableTargetLockSound = settings.enableTargetLockSound;
        TargetingConfig.enableTargetSwitchSound = settings.enableTargetSwitchSound;
        TargetingConfig.enableLethalTargetSound = settings.enableLethalTargetSound;
        TargetingConfig.enableTargetLostSound = settings.enableTargetLostSound;
        TargetingConfig.targetLockVolume = settings.targetLockVolume;
        TargetingConfig.targetSwitchVolume = settings.targetSwitchVolume;
        TargetingConfig.lethalTargetVolume = settings.lethalTargetVolume;
        TargetingConfig.targetLostVolume = settings.targetLostVolume;
        TargetingConfig.targetLockPitch = settings.targetLockPitch;
        TargetingConfig.targetSwitchPitch = settings.targetSwitchPitch;
        TargetingConfig.lethalTargetPitch = settings.lethalTargetPitch;
        TargetingConfig.targetLostPitch = settings.targetLostPitch;
        TargetingConfig.soundTheme = settings.soundTheme;
        TargetingConfig.enableSoundVariety = settings.enableSoundVariety;

        TargetingConfig.enableDamageNumbers = settings.enableDamageNumbers;
        TargetingConfig.damageNumbersScale = settings.damageNumbersScale;
        TargetingConfig.damageNumbersDuration = settings.damageNumbersDuration;
        TargetingConfig.damageNumbersCrits = settings.damageNumbersCrits;
        TargetingConfig.damageNumbersColors = settings.damageNumbersColors;
        TargetingConfig.damageNumbersColor = settings.damageNumbersColor;
        TargetingConfig.criticalDamageColor = settings.criticalDamageColor;
        TargetingConfig.lethalDamageColor = settings.lethalDamageColor;
        TargetingConfig.damageNumbersFadeOut = settings.damageNumbersFadeOut;
        TargetingConfig.damageNumbersOffset = settings.damageNumbersOffset;
        TargetingConfig.damageNumbersMotion = settings.damageNumbersMotion;
        TargetingConfig.critEmphasis = settings.critEmphasis;

        TargetingConfig.showDamagePrediction = settings.showDamagePrediction;
        TargetingConfig.showHitsToKill = settings.showHitsToKill;
        TargetingConfig.showVulnerabilities = settings.showVulnerabilities;
        TargetingConfig.highlightLethalTargets = settings.highlightLethalTargets;
        TargetingConfig.damagePredictionScale = settings.damagePredictionScale;

        TargetingConfig.updateFrequency = settings.updateFrequency;
        TargetingConfig.validationInterval = settings.validationInterval;

        TargetingConfig.presentationPalette = settings.presentationPalette;
        TargetingConfig.reducedMotion = settings.reducedMotion;
        TargetingConfig.hudScale = settings.hudScale;
        TargetingConfig.hudOpacity = settings.hudOpacity;
        TargetingConfig.ringEnabled = settings.ringEnabled;
        TargetingConfig.ringThickness = settings.ringThickness;
        TargetingConfig.ringGlowStrength = settings.ringGlowStrength;
        TargetingConfig.ringHealthArcEnabled = settings.ringHealthArcEnabled;
    }
}

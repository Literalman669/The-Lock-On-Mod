package com.zeldatargeting.mod.config;

/**
 * A complete, runtime-independent snapshot of the client's targeting settings.
 *
 * <p>This deliberately contains no Forge or Minecraft types so it can safely be
 * copied, validated, previewed, and persisted before any live client setting is
 * changed.</p>
 */
public class TargetingSettings {

    public int schemaVersion = 4;

    public double targetingRange = 16.0D;
    public double maxTrackingDistance = 20.0D;
    public double maxAngle = 60.0D;
    public boolean requireLineOfSight = true;
    public String targetPriority = "nearest";

    public boolean showReticle = true;
    public boolean showHealthBar = true;
    public boolean showDistance = true;
    public boolean showTargetName = true;
    public float reticleScale = 1.0F;
    public int reticleColor = 0xFF0000;
    public boolean compactHudMode = false;
    public boolean softAimIndicator = false;
    public boolean targetHistoryEnabled = false;
    public boolean bossStylePanel = true;
    public String hudAnchor = "top-right";
    public int hudOffsetX = 0;
    public int hudOffsetY = 0;

    public boolean enableCameraLockOn = true;
    public float cameraSmoothness = 0.4F;
    public float maxPitchAdjustment = 60.0F;
    public float maxYawAdjustment = 90.0F;
    public boolean autoThirdPerson = false;
    public boolean ssrCompensationEnabled = true;
    public float cameraFocusYOffset = 0.0F;
    public String lockOnPreset = "balanced";
    public boolean debugCompatibility = false;
    public boolean perModeSmoothingEnabled = false;

    public boolean targetHostileMobs = true;
    public boolean targetNeutralMobs = true;
    public boolean targetPassiveMobs = true;
    public boolean targetPlayers = false;

    public boolean enableSounds = true;
    public float soundVolume = 1.0F;
    public boolean enableTargetLockSound = true;
    public boolean enableTargetSwitchSound = true;
    public boolean enableLethalTargetSound = true;
    public boolean enableTargetLostSound = true;
    public float targetLockVolume = 0.7F;
    public float targetSwitchVolume = 0.4F;
    public float lethalTargetVolume = 0.8F;
    public float targetLostVolume = 0.5F;
    public float targetLockPitch = 1.2F;
    public float targetSwitchPitch = 1.0F;
    public float lethalTargetPitch = 1.5F;
    public float targetLostPitch = 0.8F;
    public String soundTheme = "default";
    public boolean enableSoundVariety = false;

    public boolean enableDamageNumbers = true;
    public float damageNumbersScale = 1.0F;
    public int damageNumbersDuration = 60;
    public boolean damageNumbersCrits = true;
    public boolean damageNumbersColors = true;
    public int damageNumbersColor = 0xFFFFFF;
    public int criticalDamageColor = 0xFFFF00;
    public int lethalDamageColor = 0xFF0000;
    public boolean damageNumbersFadeOut = true;
    public float damageNumbersOffset = 0.5F;
    public String damageNumbersMotion = "default";
    public boolean critEmphasis = true;

    public boolean showDamagePrediction = true;
    public boolean showHitsToKill = true;
    public boolean showVulnerabilities = true;
    public boolean highlightLethalTargets = true;
    public float damagePredictionScale = 1.0F;

    public int updateFrequency = 1;
    public int validationInterval = 10;

    public String presentationPalette = "default";
    public boolean reducedMotion = false;
    public float hudScale = 1.0F;
    public float hudOpacity = 1.0F;
    public boolean ringEnabled = true;
    public float ringThickness = 1.0F;
    public float ringGlowStrength = 1.0F;
    public boolean ringHealthArcEnabled = true;

    public TargetingSettings copy() {
        TargetingSettings copy = new TargetingSettings();

        copy.schemaVersion = schemaVersion;

        copy.targetingRange = targetingRange;
        copy.maxTrackingDistance = maxTrackingDistance;
        copy.maxAngle = maxAngle;
        copy.requireLineOfSight = requireLineOfSight;
        copy.targetPriority = targetPriority;

        copy.showReticle = showReticle;
        copy.showHealthBar = showHealthBar;
        copy.showDistance = showDistance;
        copy.showTargetName = showTargetName;
        copy.reticleScale = reticleScale;
        copy.reticleColor = reticleColor;
        copy.compactHudMode = compactHudMode;
        copy.softAimIndicator = softAimIndicator;
        copy.targetHistoryEnabled = targetHistoryEnabled;
        copy.bossStylePanel = bossStylePanel;
        copy.hudAnchor = hudAnchor;
        copy.hudOffsetX = hudOffsetX;
        copy.hudOffsetY = hudOffsetY;

        copy.enableCameraLockOn = enableCameraLockOn;
        copy.cameraSmoothness = cameraSmoothness;
        copy.maxPitchAdjustment = maxPitchAdjustment;
        copy.maxYawAdjustment = maxYawAdjustment;
        copy.autoThirdPerson = autoThirdPerson;
        copy.ssrCompensationEnabled = ssrCompensationEnabled;
        copy.cameraFocusYOffset = cameraFocusYOffset;
        copy.lockOnPreset = lockOnPreset;
        copy.debugCompatibility = debugCompatibility;
        copy.perModeSmoothingEnabled = perModeSmoothingEnabled;

        copy.targetHostileMobs = targetHostileMobs;
        copy.targetNeutralMobs = targetNeutralMobs;
        copy.targetPassiveMobs = targetPassiveMobs;
        copy.targetPlayers = targetPlayers;

        copy.enableSounds = enableSounds;
        copy.soundVolume = soundVolume;
        copy.enableTargetLockSound = enableTargetLockSound;
        copy.enableTargetSwitchSound = enableTargetSwitchSound;
        copy.enableLethalTargetSound = enableLethalTargetSound;
        copy.enableTargetLostSound = enableTargetLostSound;
        copy.targetLockVolume = targetLockVolume;
        copy.targetSwitchVolume = targetSwitchVolume;
        copy.lethalTargetVolume = lethalTargetVolume;
        copy.targetLostVolume = targetLostVolume;
        copy.targetLockPitch = targetLockPitch;
        copy.targetSwitchPitch = targetSwitchPitch;
        copy.lethalTargetPitch = lethalTargetPitch;
        copy.targetLostPitch = targetLostPitch;
        copy.soundTheme = soundTheme;
        copy.enableSoundVariety = enableSoundVariety;

        copy.enableDamageNumbers = enableDamageNumbers;
        copy.damageNumbersScale = damageNumbersScale;
        copy.damageNumbersDuration = damageNumbersDuration;
        copy.damageNumbersCrits = damageNumbersCrits;
        copy.damageNumbersColors = damageNumbersColors;
        copy.damageNumbersColor = damageNumbersColor;
        copy.criticalDamageColor = criticalDamageColor;
        copy.lethalDamageColor = lethalDamageColor;
        copy.damageNumbersFadeOut = damageNumbersFadeOut;
        copy.damageNumbersOffset = damageNumbersOffset;
        copy.damageNumbersMotion = damageNumbersMotion;
        copy.critEmphasis = critEmphasis;

        copy.showDamagePrediction = showDamagePrediction;
        copy.showHitsToKill = showHitsToKill;
        copy.showVulnerabilities = showVulnerabilities;
        copy.highlightLethalTargets = highlightLethalTargets;
        copy.damagePredictionScale = damagePredictionScale;

        copy.updateFrequency = updateFrequency;
        copy.validationInterval = validationInterval;

        copy.presentationPalette = presentationPalette;
        copy.reducedMotion = reducedMotion;
        copy.hudScale = hudScale;
        copy.hudOpacity = hudOpacity;
        copy.ringEnabled = ringEnabled;
        copy.ringThickness = ringThickness;
        copy.ringGlowStrength = ringGlowStrength;
        copy.ringHealthArcEnabled = ringHealthArcEnabled;

        return copy;
    }
}

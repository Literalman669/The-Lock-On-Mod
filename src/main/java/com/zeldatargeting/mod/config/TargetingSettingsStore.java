package com.zeldatargeting.mod.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Owns the schema-4 configuration file and its legacy-detection policy. */
public final class TargetingSettingsStore {

    public static final int SCHEMA_VERSION = 4;

    private final File configFile;
    private final LegacyConfigBackup legacyBackup;

    public TargetingSettingsStore(File configFile) {
        this(configFile, new LegacyConfigBackup());
    }

    TargetingSettingsStore(File configFile, LegacyConfigBackup legacyBackup) {
        this.configFile = configFile;
        this.legacyBackup = legacyBackup;
    }

    public LoadResult load() {
        try {
            if (!configFile.isFile()) {
                return LoadResult.presetSelectionRequired(TargetingPreset.BALANCED.createSettings(), false, null);
            }

            Properties configuration = readProperties();
            String schema = readString(configuration, "meta", "schemaVersion", "");
            if (!String.valueOf(SCHEMA_VERSION).equals(schema)) {
                LegacyConfigBackup.BackupResult backup = legacyBackup.backup(configFile);
                return LoadResult.presetSelectionRequired(
                        TargetingPreset.BALANCED.createSettings(), backup.isFailure(), backup.getBackupFile());
            }

            TargetingSettings settings = readSettings(configuration);
            return LoadResult.loaded(settings);
        } catch (IOException exception) {
            LegacyConfigBackup.BackupResult backup = legacyBackup.backup(configFile);
            return LoadResult.presetSelectionRequired(
                    TargetingPreset.BALANCED.createSettings(), backup.isFailure(), backup.getBackupFile());
        }
    }

    public void save(TargetingSettings source) {
        TargetingSettings settings = TargetingSettingsValidator.sanitize(source);
        Properties configuration;
        try {
            configuration = readProperties();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read Zelda Targeting configuration", exception);
        }

        write(configuration, "meta", "schemaVersion", SCHEMA_VERSION);
        write(configuration, "preset", "lockOnPreset", settings.lockOnPreset);

        write(configuration, "targeting", "targetingRange", settings.targetingRange);
        write(configuration, "targeting", "maxTrackingDistance", settings.maxTrackingDistance);
        write(configuration, "targeting", "maxAngle", settings.maxAngle);
        write(configuration, "targeting", "requireLineOfSight", settings.requireLineOfSight);
        write(configuration, "targeting", "targetPriority", settings.targetPriority);

        write(configuration, "camera", "enableCameraLockOn", settings.enableCameraLockOn);
        write(configuration, "camera", "cameraSmoothness", settings.cameraSmoothness);
        write(configuration, "camera", "maxPitchAdjustment", settings.maxPitchAdjustment);
        write(configuration, "camera", "maxYawAdjustment", settings.maxYawAdjustment);
        write(configuration, "camera", "autoThirdPerson", settings.autoThirdPerson);
        write(configuration, "camera", "cameraFocusYOffset", settings.cameraFocusYOffset);
        write(configuration, "camera", "perModeSmoothingEnabled", settings.perModeSmoothingEnabled);

        write(configuration, "hud", "showReticle", settings.showReticle);
        write(configuration, "hud", "showHealthBar", settings.showHealthBar);
        write(configuration, "hud", "showDistance", settings.showDistance);
        write(configuration, "hud", "showTargetName", settings.showTargetName);
        write(configuration, "hud", "reticleScale", settings.reticleScale);
        write(configuration, "hud", "reticleColor", settings.reticleColor);
        write(configuration, "hud", "compactHudMode", settings.compactHudMode);
        write(configuration, "hud", "softAimIndicator", settings.softAimIndicator);
        write(configuration, "hud", "targetHistoryEnabled", settings.targetHistoryEnabled);
        write(configuration, "hud", "bossStylePanel", settings.bossStylePanel);
        write(configuration, "hud", "hudAnchor", settings.hudAnchor);
        write(configuration, "hud", "hudOffsetX", settings.hudOffsetX);
        write(configuration, "hud", "hudOffsetY", settings.hudOffsetY);

        write(configuration, "ring", "ringEnabled", settings.ringEnabled);
        write(configuration, "ring", "ringThickness", settings.ringThickness);
        write(configuration, "ring", "ringGlowStrength", settings.ringGlowStrength);
        write(configuration, "ring", "ringHealthArcEnabled", settings.ringHealthArcEnabled);

        write(configuration, "audio", "enableSounds", settings.enableSounds);
        write(configuration, "audio", "soundVolume", settings.soundVolume);
        write(configuration, "audio", "enableTargetLockSound", settings.enableTargetLockSound);
        write(configuration, "audio", "enableTargetSwitchSound", settings.enableTargetSwitchSound);
        write(configuration, "audio", "enableLethalTargetSound", settings.enableLethalTargetSound);
        write(configuration, "audio", "enableTargetLostSound", settings.enableTargetLostSound);
        write(configuration, "audio", "targetLockVolume", settings.targetLockVolume);
        write(configuration, "audio", "targetSwitchVolume", settings.targetSwitchVolume);
        write(configuration, "audio", "lethalTargetVolume", settings.lethalTargetVolume);
        write(configuration, "audio", "targetLostVolume", settings.targetLostVolume);
        write(configuration, "audio", "targetLockPitch", settings.targetLockPitch);
        write(configuration, "audio", "targetSwitchPitch", settings.targetSwitchPitch);
        write(configuration, "audio", "lethalTargetPitch", settings.lethalTargetPitch);
        write(configuration, "audio", "targetLostPitch", settings.targetLostPitch);
        write(configuration, "audio", "soundTheme", settings.soundTheme);
        write(configuration, "audio", "enableSoundVariety", settings.enableSoundVariety);

        write(configuration, "entities", "targetHostileMobs", settings.targetHostileMobs);
        write(configuration, "entities", "targetNeutralMobs", settings.targetNeutralMobs);
        write(configuration, "entities", "targetPassiveMobs", settings.targetPassiveMobs);
        write(configuration, "entities", "targetPlayers", settings.targetPlayers);

        write(configuration, "damageNumbers", "enableDamageNumbers", settings.enableDamageNumbers);
        write(configuration, "damageNumbers", "damageNumbersScale", settings.damageNumbersScale);
        write(configuration, "damageNumbers", "damageNumbersDuration", settings.damageNumbersDuration);
        write(configuration, "damageNumbers", "damageNumbersCrits", settings.damageNumbersCrits);
        write(configuration, "damageNumbers", "damageNumbersColors", settings.damageNumbersColors);
        write(configuration, "damageNumbers", "damageNumbersColor", settings.damageNumbersColor);
        write(configuration, "damageNumbers", "criticalDamageColor", settings.criticalDamageColor);
        write(configuration, "damageNumbers", "lethalDamageColor", settings.lethalDamageColor);
        write(configuration, "damageNumbers", "damageNumbersFadeOut", settings.damageNumbersFadeOut);
        write(configuration, "damageNumbers", "damageNumbersOffset", settings.damageNumbersOffset);
        write(configuration, "damageNumbers", "damageNumbersMotion", settings.damageNumbersMotion);
        write(configuration, "damageNumbers", "critEmphasis", settings.critEmphasis);
        write(configuration, "damageNumbers", "showDamagePrediction", settings.showDamagePrediction);
        write(configuration, "damageNumbers", "showHitsToKill", settings.showHitsToKill);
        write(configuration, "damageNumbers", "showVulnerabilities", settings.showVulnerabilities);
        write(configuration, "damageNumbers", "highlightLethalTargets", settings.highlightLethalTargets);
        write(configuration, "damageNumbers", "damagePredictionScale", settings.damagePredictionScale);

        configuration.remove("compatibility.btpCompatibilityMode");
        configuration.remove("compatibility.btpCameraIntensity");
        configuration.remove("compatibility.ssrXOffset");
        write(configuration, "compatibility", "ssrCompensationEnabled", settings.ssrCompensationEnabled);
        write(configuration, "compatibility", "debugCompatibility", settings.debugCompatibility);

        write(configuration, "accessibility", "presentationPalette", settings.presentationPalette);
        write(configuration, "accessibility", "reducedMotion", settings.reducedMotion);
        write(configuration, "accessibility", "hudScale", settings.hudScale);
        write(configuration, "accessibility", "hudOpacity", settings.hudOpacity);

        write(configuration, "performance", "updateFrequency", settings.updateFrequency);
        write(configuration, "performance", "validationInterval", settings.validationInterval);

        try {
            writeProperties(configuration);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save Zelda Targeting configuration", exception);
        }
    }

    private TargetingSettings readSettings(Properties configuration) {
        TargetingSettings defaults = TargetingPreset.BALANCED.createSettings();
        TargetingSettings settings = defaults.copy();

        settings.schemaVersion = readInt(configuration, "meta", "schemaVersion", SCHEMA_VERSION);
        settings.lockOnPreset = readString(configuration, "preset", "lockOnPreset", defaults.lockOnPreset);

        settings.targetingRange = readDouble(configuration, "targeting", "targetingRange", defaults.targetingRange);
        settings.maxTrackingDistance = readDouble(configuration, "targeting", "maxTrackingDistance", defaults.maxTrackingDistance);
        settings.maxAngle = readDouble(configuration, "targeting", "maxAngle", defaults.maxAngle);
        settings.requireLineOfSight = readBoolean(configuration, "targeting", "requireLineOfSight", defaults.requireLineOfSight);
        settings.targetPriority = readString(configuration, "targeting", "targetPriority", defaults.targetPriority);

        settings.enableCameraLockOn = readBoolean(configuration, "camera", "enableCameraLockOn", defaults.enableCameraLockOn);
        settings.cameraSmoothness = readFloat(configuration, "camera", "cameraSmoothness", defaults.cameraSmoothness);
        settings.maxPitchAdjustment = readFloat(configuration, "camera", "maxPitchAdjustment", defaults.maxPitchAdjustment);
        settings.maxYawAdjustment = readFloat(configuration, "camera", "maxYawAdjustment", defaults.maxYawAdjustment);
        settings.autoThirdPerson = readBoolean(configuration, "camera", "autoThirdPerson", defaults.autoThirdPerson);
        settings.cameraFocusYOffset = readFloat(configuration, "camera", "cameraFocusYOffset", defaults.cameraFocusYOffset);
        settings.perModeSmoothingEnabled = readBoolean(configuration, "camera", "perModeSmoothingEnabled", defaults.perModeSmoothingEnabled);

        settings.showReticle = readBoolean(configuration, "hud", "showReticle", defaults.showReticle);
        settings.showHealthBar = readBoolean(configuration, "hud", "showHealthBar", defaults.showHealthBar);
        settings.showDistance = readBoolean(configuration, "hud", "showDistance", defaults.showDistance);
        settings.showTargetName = readBoolean(configuration, "hud", "showTargetName", defaults.showTargetName);
        settings.reticleScale = readFloat(configuration, "hud", "reticleScale", defaults.reticleScale);
        settings.reticleColor = readInt(configuration, "hud", "reticleColor", defaults.reticleColor);
        settings.compactHudMode = readBoolean(configuration, "hud", "compactHudMode", defaults.compactHudMode);
        settings.softAimIndicator = readBoolean(configuration, "hud", "softAimIndicator", defaults.softAimIndicator);
        settings.targetHistoryEnabled = readBoolean(configuration, "hud", "targetHistoryEnabled", defaults.targetHistoryEnabled);
        settings.bossStylePanel = readBoolean(configuration, "hud", "bossStylePanel", defaults.bossStylePanel);
        settings.hudAnchor = readString(configuration, "hud", "hudAnchor", defaults.hudAnchor);
        settings.hudOffsetX = readInt(configuration, "hud", "hudOffsetX", defaults.hudOffsetX);
        settings.hudOffsetY = readInt(configuration, "hud", "hudOffsetY", defaults.hudOffsetY);

        settings.ringEnabled = readBoolean(configuration, "ring", "ringEnabled", defaults.ringEnabled);
        settings.ringThickness = readFloat(configuration, "ring", "ringThickness", defaults.ringThickness);
        settings.ringGlowStrength = readFloat(configuration, "ring", "ringGlowStrength", defaults.ringGlowStrength);
        settings.ringHealthArcEnabled = readBoolean(configuration, "ring", "ringHealthArcEnabled", defaults.ringHealthArcEnabled);

        settings.enableSounds = readBoolean(configuration, "audio", "enableSounds", defaults.enableSounds);
        settings.soundVolume = readFloat(configuration, "audio", "soundVolume", defaults.soundVolume);
        settings.enableTargetLockSound = readBoolean(configuration, "audio", "enableTargetLockSound", defaults.enableTargetLockSound);
        settings.enableTargetSwitchSound = readBoolean(configuration, "audio", "enableTargetSwitchSound", defaults.enableTargetSwitchSound);
        settings.enableLethalTargetSound = readBoolean(configuration, "audio", "enableLethalTargetSound", defaults.enableLethalTargetSound);
        settings.enableTargetLostSound = readBoolean(configuration, "audio", "enableTargetLostSound", defaults.enableTargetLostSound);
        settings.targetLockVolume = readFloat(configuration, "audio", "targetLockVolume", defaults.targetLockVolume);
        settings.targetSwitchVolume = readFloat(configuration, "audio", "targetSwitchVolume", defaults.targetSwitchVolume);
        settings.lethalTargetVolume = readFloat(configuration, "audio", "lethalTargetVolume", defaults.lethalTargetVolume);
        settings.targetLostVolume = readFloat(configuration, "audio", "targetLostVolume", defaults.targetLostVolume);
        settings.targetLockPitch = readFloat(configuration, "audio", "targetLockPitch", defaults.targetLockPitch);
        settings.targetSwitchPitch = readFloat(configuration, "audio", "targetSwitchPitch", defaults.targetSwitchPitch);
        settings.lethalTargetPitch = readFloat(configuration, "audio", "lethalTargetPitch", defaults.lethalTargetPitch);
        settings.targetLostPitch = readFloat(configuration, "audio", "targetLostPitch", defaults.targetLostPitch);
        settings.soundTheme = readString(configuration, "audio", "soundTheme", defaults.soundTheme);
        settings.enableSoundVariety = readBoolean(configuration, "audio", "enableSoundVariety", defaults.enableSoundVariety);

        settings.targetHostileMobs = readBoolean(configuration, "entities", "targetHostileMobs", defaults.targetHostileMobs);
        settings.targetNeutralMobs = readBoolean(configuration, "entities", "targetNeutralMobs", defaults.targetNeutralMobs);
        settings.targetPassiveMobs = readBoolean(configuration, "entities", "targetPassiveMobs", defaults.targetPassiveMobs);
        settings.targetPlayers = readBoolean(configuration, "entities", "targetPlayers", defaults.targetPlayers);

        settings.enableDamageNumbers = readBoolean(configuration, "damageNumbers", "enableDamageNumbers", defaults.enableDamageNumbers);
        settings.damageNumbersScale = readFloat(configuration, "damageNumbers", "damageNumbersScale", defaults.damageNumbersScale);
        settings.damageNumbersDuration = readInt(configuration, "damageNumbers", "damageNumbersDuration", defaults.damageNumbersDuration);
        settings.damageNumbersCrits = readBoolean(configuration, "damageNumbers", "damageNumbersCrits", defaults.damageNumbersCrits);
        settings.damageNumbersColors = readBoolean(configuration, "damageNumbers", "damageNumbersColors", defaults.damageNumbersColors);
        settings.damageNumbersColor = readInt(configuration, "damageNumbers", "damageNumbersColor", defaults.damageNumbersColor);
        settings.criticalDamageColor = readInt(configuration, "damageNumbers", "criticalDamageColor", defaults.criticalDamageColor);
        settings.lethalDamageColor = readInt(configuration, "damageNumbers", "lethalDamageColor", defaults.lethalDamageColor);
        settings.damageNumbersFadeOut = readBoolean(configuration, "damageNumbers", "damageNumbersFadeOut", defaults.damageNumbersFadeOut);
        settings.damageNumbersOffset = readFloat(configuration, "damageNumbers", "damageNumbersOffset", defaults.damageNumbersOffset);
        settings.damageNumbersMotion = readString(configuration, "damageNumbers", "damageNumbersMotion", defaults.damageNumbersMotion);
        settings.critEmphasis = readBoolean(configuration, "damageNumbers", "critEmphasis", defaults.critEmphasis);
        settings.showDamagePrediction = readBoolean(configuration, "damageNumbers", "showDamagePrediction", defaults.showDamagePrediction);
        settings.showHitsToKill = readBoolean(configuration, "damageNumbers", "showHitsToKill", defaults.showHitsToKill);
        settings.showVulnerabilities = readBoolean(configuration, "damageNumbers", "showVulnerabilities", defaults.showVulnerabilities);
        settings.highlightLethalTargets = readBoolean(configuration, "damageNumbers", "highlightLethalTargets", defaults.highlightLethalTargets);
        settings.damagePredictionScale = readFloat(configuration, "damageNumbers", "damagePredictionScale", defaults.damagePredictionScale);

        settings.ssrCompensationEnabled = readBoolean(configuration, "compatibility", "ssrCompensationEnabled", defaults.ssrCompensationEnabled);
        settings.debugCompatibility = readBoolean(configuration, "compatibility", "debugCompatibility", defaults.debugCompatibility);

        settings.presentationPalette = readString(configuration, "accessibility", "presentationPalette", defaults.presentationPalette);
        settings.reducedMotion = readBoolean(configuration, "accessibility", "reducedMotion", defaults.reducedMotion);
        settings.hudScale = readFloat(configuration, "accessibility", "hudScale", defaults.hudScale);
        settings.hudOpacity = readFloat(configuration, "accessibility", "hudOpacity", defaults.hudOpacity);

        settings.updateFrequency = readInt(configuration, "performance", "updateFrequency", defaults.updateFrequency);
        settings.validationInterval = readInt(configuration, "performance", "validationInterval", defaults.validationInterval);

        return TargetingSettingsValidator.sanitize(settings);
    }

    private Properties readProperties() throws IOException {
        Properties properties = new Properties();
        if (!configFile.isFile()) {
            return properties;
        }

        FileInputStream input = new FileInputStream(configFile);
        try {
            properties.load(input);
        } finally {
            input.close();
        }
        return properties;
    }

    private void writeProperties(Properties properties) throws IOException {
        File parent = configFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create configuration directory: " + parent);
        }

        FileOutputStream output = new FileOutputStream(configFile);
        try {
            properties.store(output, "Zelda Targeting 1.4 configuration");
        } finally {
            output.close();
        }
    }

    private static void write(Properties configuration, String category, String key, Object value) {
        configuration.setProperty(category + "." + key, String.valueOf(value));
    }

    private static String readString(Properties configuration, String category, String key, String defaultValue) {
        return configuration.getProperty(category + "." + key, defaultValue);
    }

    private static boolean readBoolean(Properties configuration, String category, String key, boolean defaultValue) {
        String value = readString(configuration, category, key, String.valueOf(defaultValue));
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return defaultValue;
    }

    private static int readInt(Properties configuration, String category, String key, int defaultValue) {
        try {
            return Integer.parseInt(readString(configuration, category, key, String.valueOf(defaultValue)));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static double readDouble(Properties configuration, String category, String key, double defaultValue) {
        try {
            return Double.parseDouble(readString(configuration, category, key, String.valueOf(defaultValue)));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static float readFloat(Properties configuration, String category, String key, float defaultValue) {
        try {
            return Float.parseFloat(readString(configuration, category, key, String.valueOf(defaultValue)));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public static final class LoadResult {
        private final TargetingSettings settings;
        private final boolean presetSelectionRequired;
        private final boolean backupFailure;
        private final File backupFile;

        private LoadResult(TargetingSettings settings, boolean presetSelectionRequired, boolean backupFailure, File backupFile) {
            this.settings = TargetingSettingsValidator.sanitize(settings);
            this.presetSelectionRequired = presetSelectionRequired;
            this.backupFailure = backupFailure;
            this.backupFile = backupFile;
        }

        public static LoadResult loaded(TargetingSettings settings) {
            return new LoadResult(settings, false, false, null);
        }

        public static LoadResult presetSelectionRequired(TargetingSettings settings, boolean backupFailure, File backupFile) {
            return new LoadResult(settings, true, backupFailure, backupFile);
        }

        public TargetingSettings getSettings() {
            return settings.copy();
        }

        public boolean requiresPresetSelection() {
            return presetSelectionRequired;
        }

        public boolean hasBackupFailure() {
            return backupFailure;
        }

        public File getBackupFile() {
            return backupFile;
        }
    }
}

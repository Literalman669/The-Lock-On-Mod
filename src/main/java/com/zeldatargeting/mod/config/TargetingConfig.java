package com.zeldatargeting.mod.config;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.core.PresentationPalette;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * Runtime compatibility facade for configuration values.
 *
 * <p>Gameplay and rendering code still read the static fields below. Editing,
 * validation, and persistence now flow through {@link TargetingSettings} and
 * {@link TargetingSettingsStore}.</p>
 */
public final class TargetingConfig {

    private static TargetingSettingsStore settingsStore;
    private static boolean presetSelectionRequired;
    private static boolean legacyBackupFailure;
    private static File legacyBackupFile;

    // Targeting settings
    public static double targetingRange = 16.0;
    public static double maxTrackingDistance = 20.0;
    public static double maxAngle = 60.0;
    public static boolean requireLineOfSight = true;
    public static String targetPriority = "nearest";

    // HUD and visual settings
    public static boolean showReticle = true;
    public static boolean showHealthBar = true;
    public static boolean showDistance = true;
    public static boolean showTargetName = true;
    public static float reticleScale = 1.0f;
    public static int reticleColor = 0xFF0000;
    public static boolean compactHudMode = false;
    public static boolean softAimIndicator = false;
    public static boolean targetHistoryEnabled = false;
    public static boolean bossStylePanel = true;
    public static String hudAnchor = "top-right";
    public static int hudOffsetX = 0;
    public static int hudOffsetY = 0;

    // Camera settings
    public static float cameraSmoothness = 0.4f;
    public static float maxPitchAdjustment = 60.0f;
    public static float maxYawAdjustment = 90.0f;
    public static boolean enableCameraLockOn = true;
    public static boolean autoThirdPerson = false;
    public static boolean ssrCompensationEnabled = true;
    public static float cameraFocusYOffset = 0.0f;
    public static String lockOnPreset = "balanced";
    public static boolean debugCompatibility = false;
    public static boolean perModeSmoothingEnabled = false;

    // Entity filtering
    public static boolean targetHostileMobs = true;
    public static boolean targetNeutralMobs = true;
    public static boolean targetPassiveMobs = true;
    public static boolean targetPlayers = false;

    // Audio settings
    public static boolean enableSounds = true;
    public static float soundVolume = 1.0f;
    public static boolean enableTargetLockSound = true;
    public static boolean enableTargetSwitchSound = true;
    public static boolean enableLethalTargetSound = true;
    public static boolean enableTargetLostSound = true;
    public static float targetLockVolume = 0.7f;
    public static float targetSwitchVolume = 0.4f;
    public static float lethalTargetVolume = 0.8f;
    public static float targetLostVolume = 0.5f;
    public static float targetLockPitch = 1.2f;
    public static float targetSwitchPitch = 1.0f;
    public static float lethalTargetPitch = 1.5f;
    public static float targetLostPitch = 0.8f;
    public static String soundTheme = "default";
    public static boolean enableSoundVariety = false;

    // Damage and combat feedback settings
    public static boolean enableDamageNumbers = true;
    public static float damageNumbersScale = 1.0f;
    public static int damageNumbersDuration = 60;
    public static boolean damageNumbersCrits = true;
    public static boolean damageNumbersColors = true;
    public static int damageNumbersColor = 0xFFFFFF;
    public static int criticalDamageColor = 0xFFFF00;
    public static int lethalDamageColor = 0xFF0000;
    public static boolean damageNumbersFadeOut = true;
    public static float damageNumbersOffset = 0.5f;
    public static String damageNumbersMotion = "default";
    public static boolean critEmphasis = true;
    public static boolean showDamagePrediction = true;
    public static boolean showHitsToKill = true;
    public static boolean showVulnerabilities = true;
    public static boolean highlightLethalTargets = true;
    public static float damagePredictionScale = 1.0f;

    // Performance settings
    public static int updateFrequency = 1;
    public static int validationInterval = 10;

    // Presentation and accessibility settings
    public static String presentationPalette = "default";
    public static boolean reducedMotion = false;
    public static float hudScale = 1.0f;
    public static float hudOpacity = 1.0f;
    public static boolean ringEnabled = true;
    public static float ringThickness = 1.0f;
    public static float ringGlowStrength = 1.0f;
    public static boolean ringHealthArcEnabled = true;

    private TargetingConfig() {
    }

    public static TargetingSettings captureActiveSettings() {
        return TargetingSettingsBridge.captureRuntime();
    }

    public static void previewSettings(TargetingSettings settings) {
        TargetingSettingsBridge.applyToRuntime(TargetingSettingsValidator.sanitize(settings));
    }

    public static void restoreSettings(TargetingSettings settings) {
        TargetingSettingsBridge.applyToRuntime(TargetingSettingsValidator.sanitize(settings));
    }

    public static void saveSettings(TargetingSettings settings) {
        TargetingSettings sanitized = TargetingSettingsValidator.sanitize(settings);
        TargetingSettingsBridge.applyToRuntime(sanitized);

        if (settingsStore == null) {
            return;
        }

        try {
            settingsStore.save(sanitized);
            presetSelectionRequired = false;
            legacyBackupFailure = false;
            legacyBackupFile = null;
        } catch (IllegalStateException exception) {
            ZeldaTargetingMod.getLogger().error("Could not save Zelda Targeting configuration", exception);
        }
    }

    public static PresentationPalette getPresentationPalette() {
        return PresentationPalette.fromName(presentationPalette);
    }

    public static void init(FMLPreInitializationEvent event) {
        initialize(new File(event.getModConfigurationDirectory(), "zeldatargeting.cfg"));
    }

    static void initialize(File configFile) {
        settingsStore = new TargetingSettingsStore(configFile);
        loadConfig();
    }

    public static void loadConfig() {
        if (settingsStore == null) {
            return;
        }

        TargetingSettingsStore.LoadResult result = settingsStore.load();
        TargetingSettingsBridge.applyToRuntime(result.getSettings());
        presetSelectionRequired = result.requiresPresetSelection();
        legacyBackupFailure = result.hasBackupFailure();
        legacyBackupFile = result.getBackupFile();

        if (legacyBackupFailure) {
            ZeldaTargetingMod.getLogger().warn("Could not create a backup of the previous Zelda Targeting configuration");
        }
    }

    public static void saveConfig() {
        saveSettings(captureActiveSettings());
    }

    public static void resetToDefaults() {
        saveSettings(TargetingPreset.BALANCED.createSettings());
    }

    public static boolean isPresetSelectionRequired() {
        return presetSelectionRequired;
    }

    public static boolean requiresPresetSelection() {
        return presetSelectionRequired;
    }

    public static void confirmPreset(TargetingPreset preset) {
        TargetingPreset safePreset = preset == null ? TargetingPreset.BALANCED : preset;
        saveSettings(safePreset.createSettings());
    }

    public static boolean hasLegacyBackupFailure() {
        return legacyBackupFailure;
    }

    public static File getLegacyBackupFile() {
        return legacyBackupFile;
    }

    public static double getTargetingRange() {
        return targetingRange;
    }

    public static double getMaxTrackingDistance() {
        return maxTrackingDistance;
    }

    public static double getMaxAngle() {
        return maxAngle;
    }

    public static boolean shouldRequireLineOfSight() {
        return requireLineOfSight;
    }

    public static float getCameraSmoothness() {
        return cameraSmoothness;
    }

    public static boolean isCameraLockOnEnabled() {
        return enableCameraLockOn;
    }

    public static boolean shouldTargetHostileMobs() {
        return targetHostileMobs;
    }

    public static boolean shouldTargetNeutralMobs() {
        return targetNeutralMobs;
    }

    public static boolean shouldTargetPassiveMobs() {
        return targetPassiveMobs;
    }

    public static boolean shouldTargetPlayers() {
        return targetPlayers;
    }
}

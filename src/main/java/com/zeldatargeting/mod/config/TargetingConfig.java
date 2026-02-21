package com.zeldatargeting.mod.config;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class TargetingConfig {
    
    private static Configuration config;
    
    // Targeting Settings
    public static double targetingRange = 16.0;
    public static double maxTrackingDistance = 20.0;
    public static double maxAngle = 60.0;
    public static boolean requireLineOfSight = true;
    public static String targetPriority = "nearest"; // "nearest", "health", "threat", "angle"
    
    // Visual Settings
    public static boolean showReticle = true;
    public static boolean showHealthBar = true;
    public static boolean showDistance = true;
    public static boolean showTargetName = true;
    public static float reticleScale = 1.0f;
    public static int reticleColor = 0xFF0000; // Red
    
    // Camera Settings
    public static float cameraSmoothness = 0.4f;
    public static float maxPitchAdjustment = 60.0f;
    public static float maxYawAdjustment = 90.0f;
    public static boolean enableCameraLockOn = true;
    public static boolean autoThirdPerson = false;
    public static String btpCompatibilityMode = "gentle"; // "disabled", "gentle", "visual_only"
    public static float btpCameraIntensity = 0.3f; // 0.0 to 1.0 for gentle mode
    public static boolean ssrCompensationEnabled = true;
    public static float ssrXOffset = -0.875f; // SSR camera x-offset (negative = left shoulder)
    public static float cameraFocusYOffset = 0.0f; // -1.0 to 1.0: shifts vertical focus point relative to entity height
    public static String lockOnPreset = "balanced"; // "cinematic", "balanced", "snappy"
    public static boolean debugCompatibility = false; // Log SSR/BTP detection details each tick
    public static boolean perModeSmoothingEnabled = false; // Gentler smoothing in first-person view
    
    // Entity Filtering
    public static boolean targetHostileMobs = true;
    public static boolean targetNeutralMobs = true;
    public static boolean targetPassiveMobs = true;
    public static boolean targetPlayers = false;
    
    // Audio Settings
    public static boolean enableSounds = true;
    public static float soundVolume = 1.0f;
    
    // Enhanced Audio Settings
    public static boolean enableTargetLockSound = true;
    public static boolean enableTargetSwitchSound = true;
    public static boolean enableLethalTargetSound = true;
    public static boolean enableTargetLostSound = true;
    public static float targetLockVolume = 0.7f;
    public static float targetSwitchVolume = 0.4f;
    public static float lethalTargetVolume = 0.8f;
    public static float targetLostVolume = 0.5f;
    
    // Enhanced Sound Customization Settings
    public static float targetLockPitch = 1.2f;
    public static float targetSwitchPitch = 1.0f;
    public static float lethalTargetPitch = 1.5f;
    public static float targetLostPitch = 0.8f;
    public static String soundTheme = "default"; // "default", "zelda", "modern", "subtle"
    public static boolean enableSoundVariety = false; // Cycle through different sounds
    
    // Damage Numbers Display Settings
    public static boolean enableDamageNumbers = true;
    public static float damageNumbersScale = 1.0f;
    public static int damageNumbersDuration = 60; // ticks (3 seconds)
    public static boolean damageNumbersCrits = true;
    public static boolean damageNumbersColors = true;
    public static int damageNumbersColor = 0xFFFFFF; // Default white
    public static int criticalDamageColor = 0xFFFF00; // Yellow for crits
    public static int lethalDamageColor = 0xFF0000; // Red for lethal hits
    public static boolean damageNumbersFadeOut = true;
    public static float damageNumbersOffset = 0.5f; // Y offset above entity
    public static String damageNumbersMotion = "default"; // "default", "subtle", "arcade"
    public static boolean critEmphasis = true; // Extra pop scale + flash on crits
    
    // Enhanced Visual Feedback Settings
    public static boolean showDamagePrediction = true;
    public static boolean showHitsToKill = true;
    public static boolean showVulnerabilities = true;
    public static boolean highlightLethalTargets = true;
    public static float damagePredictionScale = 1.0f;
    
    // Performance Settings
    public static int updateFrequency = 1; // ticks between updates
    public static int validationInterval = 10; // ticks between target validation
    
    public static void init(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), "zeldatargeting.cfg");
        config = new Configuration(configFile);
        
        loadConfig();
    }
    
    public static void loadConfig() {
        try {
            config.load();
            
            // Targeting Settings
            targetingRange = config.getFloat("targetingRange", "targeting", (float) targetingRange, 5.0f, 50.0f,
                "Maximum range for target detection");
            maxTrackingDistance = config.getFloat("maxTrackingDistance", "targeting", (float) maxTrackingDistance, 5.0f, 100.0f,
                "Maximum distance to maintain target lock");
            maxAngle = config.getFloat("maxAngle", "targeting", (float) maxAngle, 15.0f, 180.0f,
                "Maximum angle from look direction to detect targets (degrees)");
            requireLineOfSight = config.getBoolean("requireLineOfSight", "targeting", requireLineOfSight,
                "Require line of sight to target entities");
            targetPriority = config.getString("targetPriority", "targeting", targetPriority,
                "Target priority: nearest, health, threat, angle");
            
            // Visual Settings
            showReticle = config.getBoolean("showReticle", "visual", showReticle,
                "Show targeting reticle around locked target");
            showHealthBar = config.getBoolean("showHealthBar", "visual", showHealthBar,
                "Show health bar for targeted entities");
            showDistance = config.getBoolean("showDistance", "visual", showDistance,
                "Show distance to target");
            showTargetName = config.getBoolean("showTargetName", "visual", showTargetName,
                "Show name of targeted entity");
            reticleScale = config.getFloat("reticleScale", "visual", reticleScale, 0.5f, 3.0f,
                "Scale of the targeting reticle");
            reticleColor = config.getInt("reticleColor", "visual", reticleColor, 0x000000, 0xFFFFFF,
                "Color of the targeting reticle (hex RGB, default 0xFF0000 = red)");
            
            // Camera Settings
            cameraSmoothness = config.getFloat("cameraSmoothness", "camera", cameraSmoothness, 0.01f, 1.0f,
                "Smoothness of camera transitions (lower = smoother)");
            maxPitchAdjustment = config.getFloat("maxPitchAdjustment", "camera", maxPitchAdjustment, 0.0f, 90.0f,
                "Maximum pitch adjustment for camera lock-on (degrees)");
            maxYawAdjustment = config.getFloat("maxYawAdjustment", "camera", maxYawAdjustment, 0.0f, 180.0f,
                "Maximum yaw adjustment for camera lock-on (degrees)");
            enableCameraLockOn = config.getBoolean("enableCameraLockOn", "camera", enableCameraLockOn,
                "Enable camera lock-on to targets");
            autoThirdPerson = config.getBoolean("autoThirdPerson", "camera", autoThirdPerson,
                "Automatically switch to third person when locking on to targets");
            btpCompatibilityMode = config.getString("btpCompatibilityMode", "camera", btpCompatibilityMode,
                "Better Third Person compatibility mode: disabled, gentle, visual_only");
            btpCameraIntensity = config.getFloat("btpCameraIntensity", "camera", btpCameraIntensity, 0.0f, 1.0f,
                "Camera movement intensity when in BTP gentle mode (0.0 = no movement, 1.0 = full movement)");
            ssrCompensationEnabled = config.getBoolean("ssrCompensationEnabled", "camera", ssrCompensationEnabled,
                "Enable crosshair alignment compensation for Shoulder Surfing Reloaded");
            ssrXOffset = config.getFloat("ssrXOffset", "camera", ssrXOffset, -3.0f, 3.0f,
                "SSR camera x-offset (match your SSR config, default -0.875 = left shoulder)");
            cameraFocusYOffset = config.getFloat("cameraFocusYOffset", "camera", cameraFocusYOffset, -1.0f, 1.0f,
                "Vertical camera focus offset (0=entity center, positive=look higher, negative=look lower)");
            lockOnPreset = config.getString("lockOnPreset", "camera", lockOnPreset,
                "Lock-on feel preset: cinematic, balanced, snappy");
            debugCompatibility = config.getBoolean("debugCompatibility", "camera", debugCompatibility,
                "Log mod compatibility diagnostics (SSR/BTP) to game log");
            perModeSmoothingEnabled = config.getBoolean("perModeSmoothingEnabled", "camera", perModeSmoothingEnabled,
                "Apply a gentler smoothing multiplier in first-person view");
            
            // Entity Filtering
            targetHostileMobs = config.getBoolean("targetHostileMobs", "entities", targetHostileMobs,
                "Allow targeting hostile mobs");
            targetNeutralMobs = config.getBoolean("targetNeutralMobs", "entities", targetNeutralMobs,
                "Allow targeting neutral mobs");
            targetPassiveMobs = config.getBoolean("targetPassiveMobs", "entities", targetPassiveMobs,
                "Allow targeting passive mobs");
            targetPlayers = config.getBoolean("targetPlayers", "entities", targetPlayers,
                "Allow targeting other players");
            
            // Audio Settings
            enableSounds = config.getBoolean("enableSounds", "audio", enableSounds,
                "Enable targeting sound effects");
            soundVolume = config.getFloat("soundVolume", "audio", soundVolume, 0.0f, 1.0f,
                "Volume of targeting sound effects");
            
            // Enhanced Audio Settings
            enableTargetLockSound = config.getBoolean("enableTargetLockSound", "audio", enableTargetLockSound,
                "Enable sound when locking onto targets");
            enableTargetSwitchSound = config.getBoolean("enableTargetSwitchSound", "audio", enableTargetSwitchSound,
                "Enable sound when switching between targets");
            enableLethalTargetSound = config.getBoolean("enableLethalTargetSound", "audio", enableLethalTargetSound,
                "Enable special sound for lethal targets");
            enableTargetLostSound = config.getBoolean("enableTargetLostSound", "audio", enableTargetLostSound,
                "Enable sound when losing target lock");
            targetLockVolume = config.getFloat("targetLockVolume", "audio", targetLockVolume, 0.0f, 1.0f,
                "Volume of target lock sound");
            targetSwitchVolume = config.getFloat("targetSwitchVolume", "audio", targetSwitchVolume, 0.0f, 1.0f,
                "Volume of target switch sound");
            lethalTargetVolume = config.getFloat("lethalTargetVolume", "audio", lethalTargetVolume, 0.0f, 1.0f,
                "Volume of lethal target sound");
            targetLostVolume = config.getFloat("targetLostVolume", "audio", targetLostVolume, 0.0f, 1.0f,
                "Volume of target lost sound");
            
            // Enhanced Sound Customization Settings
            targetLockPitch = config.getFloat("targetLockPitch", "audio", targetLockPitch, 0.5f, 2.0f,
                "Pitch of target lock sound");
            targetSwitchPitch = config.getFloat("targetSwitchPitch", "audio", targetSwitchPitch, 0.5f, 2.0f,
                "Pitch of target switch sound");
            lethalTargetPitch = config.getFloat("lethalTargetPitch", "audio", lethalTargetPitch, 0.5f, 2.0f,
                "Pitch of lethal target sound");
            targetLostPitch = config.getFloat("targetLostPitch", "audio", targetLostPitch, 0.5f, 2.0f,
                "Pitch of target lost sound");
            soundTheme = config.getString("soundTheme", "audio", soundTheme,
                "Sound theme: default, zelda, modern, subtle");
            enableSoundVariety = config.getBoolean("enableSoundVariety", "audio", enableSoundVariety,
                "Enable cycling through different sound variants");
            
            // Damage Numbers Display Settings
            enableDamageNumbers = config.getBoolean("enableDamageNumbers", "damage_numbers", enableDamageNumbers,
                "Show damage numbers when hitting targeted entities");
            damageNumbersScale = config.getFloat("damageNumbersScale", "damage_numbers", damageNumbersScale, 0.5f, 3.0f,
                "Scale of damage number text");
            damageNumbersDuration = config.getInt("damageNumbersDuration", "damage_numbers", damageNumbersDuration, 20, 200,
                "Duration damage numbers stay visible (ticks)");
            damageNumbersCrits = config.getBoolean("damageNumbersCrits", "damage_numbers", damageNumbersCrits,
                "Show special effects for critical hits");
            damageNumbersColors = config.getBoolean("damageNumbersColors", "damage_numbers", damageNumbersColors,
                "Use colored damage numbers based on damage type");
            damageNumbersColor = config.getInt("damageNumbersColor", "damage_numbers", damageNumbersColor,
                0x000000, 0xFFFFFF, "Default color for damage numbers (hex format)");
            criticalDamageColor = config.getInt("criticalDamageColor", "damage_numbers", criticalDamageColor,
                0x000000, 0xFFFFFF, "Color for critical damage numbers (hex format)");
            lethalDamageColor = config.getInt("lethalDamageColor", "damage_numbers", lethalDamageColor,
                0x000000, 0xFFFFFF, "Color for lethal damage numbers (hex format)");
            damageNumbersFadeOut = config.getBoolean("damageNumbersFadeOut", "damage_numbers", damageNumbersFadeOut,
                "Enable smooth fade-out animation for damage numbers");
            damageNumbersOffset = config.getFloat("damageNumbersOffset", "damage_numbers", damageNumbersOffset, 0.0f, 2.0f,
                "Vertical offset for damage numbers above entities");
            damageNumbersMotion = config.getString("damageNumbersMotion", "damage_numbers", damageNumbersMotion,
                "Damage number motion style: default, subtle, arcade");
            critEmphasis = config.getBoolean("critEmphasis", "damage_numbers", critEmphasis,
                "Extra scale pop and color flash on critical hits");
            
            // Enhanced Visual Feedback Settings
            showDamagePrediction = config.getBoolean("showDamagePrediction", "visual_feedback", showDamagePrediction,
                "Show damage prediction when targeting entities");
            showHitsToKill = config.getBoolean("showHitsToKill", "visual_feedback", showHitsToKill,
                "Show number of hits required to kill target");
            showVulnerabilities = config.getBoolean("showVulnerabilities", "visual_feedback", showVulnerabilities,
                "Show target vulnerabilities and resistances");
            highlightLethalTargets = config.getBoolean("highlightLethalTargets", "visual_feedback", highlightLethalTargets,
                "Highlight targets that can be killed in one hit");
            damagePredictionScale = config.getFloat("damagePredictionScale", "visual_feedback", damagePredictionScale, 0.5f, 2.0f,
                "Scale of damage prediction text");
            
            // Performance Settings
            updateFrequency = config.getInt("updateFrequency", "performance", updateFrequency, 1, 20,
                "Ticks between targeting system updates (higher = better performance, lower responsiveness)");
            validationInterval = config.getInt("validationInterval", "performance", validationInterval, 1, 60,
                "Ticks between target validation checks");
            
        } catch (Exception e) {
            ZeldaTargetingMod.getLogger().error("Error loading configuration", e);
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    public static void saveConfig() {
        if (config != null) {
            try {
                // Update all config properties with current values
                config.get("targeting", "targetingRange", (float) targetingRange).set((float) targetingRange);
                config.get("targeting", "maxTrackingDistance", (float) maxTrackingDistance).set((float) maxTrackingDistance);
                config.get("targeting", "maxAngle", (float) maxAngle).set((float) maxAngle);
                config.get("targeting", "requireLineOfSight", requireLineOfSight).set(requireLineOfSight);
                config.get("targeting", "targetPriority", targetPriority).set(targetPriority);
                
                config.get("visual", "showReticle", showReticle).set(showReticle);
                config.get("visual", "showHealthBar", showHealthBar).set(showHealthBar);
                config.get("visual", "showDistance", showDistance).set(showDistance);
                config.get("visual", "showTargetName", showTargetName).set(showTargetName);
                config.get("visual", "reticleScale", reticleScale).set(reticleScale);
                config.get("visual", "reticleColor", reticleColor).set(reticleColor);
                
                config.get("camera", "cameraSmoothness", cameraSmoothness).set(cameraSmoothness);
                config.get("camera", "maxPitchAdjustment", maxPitchAdjustment).set(maxPitchAdjustment);
                config.get("camera", "maxYawAdjustment", maxYawAdjustment).set(maxYawAdjustment);
                config.get("camera", "enableCameraLockOn", enableCameraLockOn).set(enableCameraLockOn);
                config.get("camera", "autoThirdPerson", autoThirdPerson).set(autoThirdPerson);
                config.get("camera", "btpCompatibilityMode", btpCompatibilityMode).set(btpCompatibilityMode);
                config.get("camera", "btpCameraIntensity", btpCameraIntensity).set(btpCameraIntensity);
                config.get("camera", "ssrCompensationEnabled", ssrCompensationEnabled).set(ssrCompensationEnabled);
                config.get("camera", "ssrXOffset", ssrXOffset).set(ssrXOffset);
                config.get("camera", "cameraFocusYOffset", cameraFocusYOffset).set(cameraFocusYOffset);
                config.get("camera", "lockOnPreset", lockOnPreset).set(lockOnPreset);
                config.get("camera", "debugCompatibility", debugCompatibility).set(debugCompatibility);
                config.get("camera", "perModeSmoothingEnabled", perModeSmoothingEnabled).set(perModeSmoothingEnabled);
                
                config.get("entities", "targetHostileMobs", targetHostileMobs).set(targetHostileMobs);
                config.get("entities", "targetNeutralMobs", targetNeutralMobs).set(targetNeutralMobs);
                config.get("entities", "targetPassiveMobs", targetPassiveMobs).set(targetPassiveMobs);
                config.get("entities", "targetPlayers", targetPlayers).set(targetPlayers);
                
                config.get("audio", "enableSounds", enableSounds).set(enableSounds);
                config.get("audio", "soundVolume", soundVolume).set(soundVolume);
                config.get("audio", "enableTargetLockSound", enableTargetLockSound).set(enableTargetLockSound);
                config.get("audio", "enableTargetSwitchSound", enableTargetSwitchSound).set(enableTargetSwitchSound);
                config.get("audio", "enableLethalTargetSound", enableLethalTargetSound).set(enableLethalTargetSound);
                config.get("audio", "enableTargetLostSound", enableTargetLostSound).set(enableTargetLostSound);
                config.get("audio", "targetLockVolume", targetLockVolume).set(targetLockVolume);
                config.get("audio", "targetSwitchVolume", targetSwitchVolume).set(targetSwitchVolume);
                config.get("audio", "lethalTargetVolume", lethalTargetVolume).set(lethalTargetVolume);
                config.get("audio", "targetLostVolume", targetLostVolume).set(targetLostVolume);
                config.get("audio", "targetLockPitch", targetLockPitch).set(targetLockPitch);
                config.get("audio", "targetSwitchPitch", targetSwitchPitch).set(targetSwitchPitch);
                config.get("audio", "lethalTargetPitch", lethalTargetPitch).set(lethalTargetPitch);
                config.get("audio", "targetLostPitch", targetLostPitch).set(targetLostPitch);
                config.get("audio", "soundTheme", soundTheme).set(soundTheme);
                config.get("audio", "enableSoundVariety", enableSoundVariety).set(enableSoundVariety);
                
                config.get("damage_numbers", "enableDamageNumbers", enableDamageNumbers).set(enableDamageNumbers);
                config.get("damage_numbers", "damageNumbersScale", damageNumbersScale).set(damageNumbersScale);
                config.get("damage_numbers", "damageNumbersDuration", damageNumbersDuration).set(damageNumbersDuration);
                config.get("damage_numbers", "damageNumbersCrits", damageNumbersCrits).set(damageNumbersCrits);
                config.get("damage_numbers", "damageNumbersColors", damageNumbersColors).set(damageNumbersColors);
                config.get("damage_numbers", "damageNumbersColor", damageNumbersColor).set(damageNumbersColor);
                config.get("damage_numbers", "criticalDamageColor", criticalDamageColor).set(criticalDamageColor);
                config.get("damage_numbers", "lethalDamageColor", lethalDamageColor).set(lethalDamageColor);
                config.get("damage_numbers", "damageNumbersFadeOut", damageNumbersFadeOut).set(damageNumbersFadeOut);
                config.get("damage_numbers", "damageNumbersOffset", damageNumbersOffset).set(damageNumbersOffset);
                config.get("damage_numbers", "damageNumbersMotion", damageNumbersMotion).set(damageNumbersMotion);
                config.get("damage_numbers", "critEmphasis", critEmphasis).set(critEmphasis);
                
                config.get("visual_feedback", "showDamagePrediction", showDamagePrediction).set(showDamagePrediction);
                config.get("visual_feedback", "showHitsToKill", showHitsToKill).set(showHitsToKill);
                config.get("visual_feedback", "showVulnerabilities", showVulnerabilities).set(showVulnerabilities);
                config.get("visual_feedback", "highlightLethalTargets", highlightLethalTargets).set(highlightLethalTargets);
                config.get("visual_feedback", "damagePredictionScale", damagePredictionScale).set(damagePredictionScale);
                
                config.get("performance", "updateFrequency", updateFrequency).set(updateFrequency);
                config.get("performance", "validationInterval", validationInterval).set(validationInterval);
                
                // Force save
                config.save();
                
                ZeldaTargetingMod.getLogger().info("Configuration saved successfully");
            } catch (Exception e) {
                ZeldaTargetingMod.getLogger().error("Error saving configuration", e);
            }
        }
    }
    
    public static void resetToDefaults() {
        // Reset all values to their defaults
        targetingRange = 16.0;
        maxTrackingDistance = 20.0;
        maxAngle = 60.0;
        requireLineOfSight = true;
        targetPriority = "nearest";
        
        showReticle = true;
        showHealthBar = true;
        showDistance = true;
        showTargetName = true;
        reticleScale = 1.0f;
        reticleColor = 0xFF0000;
        
        cameraSmoothness = 0.4f;
        maxPitchAdjustment = 60.0f;
        maxYawAdjustment = 90.0f;
        enableCameraLockOn = true;
        autoThirdPerson = false;
        btpCompatibilityMode = "gentle";
        btpCameraIntensity = 0.3f;
        ssrCompensationEnabled = true;
        ssrXOffset = -0.875f;
        cameraFocusYOffset = 0.0f;
        lockOnPreset = "balanced";
        debugCompatibility = false;
        perModeSmoothingEnabled = false;
        
        targetHostileMobs = true;
        targetNeutralMobs = true;
        targetPassiveMobs = true;
        targetPlayers = false;
        
        enableSounds = true;
        soundVolume = 1.0f;
        enableTargetLockSound = true;
        enableTargetSwitchSound = true;
        enableLethalTargetSound = true;
        enableTargetLostSound = true;
        targetLockVolume = 0.7f;
        targetSwitchVolume = 0.4f;
        lethalTargetVolume = 0.8f;
        targetLostVolume = 0.5f;
        
        // Enhanced Sound Customization defaults
        targetLockPitch = 1.2f;
        targetSwitchPitch = 1.0f;
        lethalTargetPitch = 1.5f;
        targetLostPitch = 0.8f;
        soundTheme = "default";
        enableSoundVariety = false;
        
        // Damage Numbers defaults
        enableDamageNumbers = true;
        damageNumbersScale = 1.0f;
        damageNumbersDuration = 60;
        damageNumbersCrits = true;
        damageNumbersColors = true;
        damageNumbersColor = 0xFFFFFF;
        criticalDamageColor = 0xFFFF00;
        lethalDamageColor = 0xFF0000;
        damageNumbersFadeOut = true;
        damageNumbersOffset = 0.5f;
        damageNumbersMotion = "default";
        critEmphasis = true;
        
        showDamagePrediction = true;
        showHitsToKill = true;
        showVulnerabilities = true;
        highlightLethalTargets = true;
        damagePredictionScale = 1.0f;
        
        updateFrequency = 1;
        validationInterval = 10;
        
        // Save the reset values
        saveConfig();
    }
    
    // Getters for commonly used values
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
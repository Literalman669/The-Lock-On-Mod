package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiTargetingConfig extends GuiScreen {
    private final GuiScreen parentScreen;
    private int currentPage = 0;
    private final int totalPages = 6;
    private int sectionLabelY1 = -1;
    private int sectionLabelY2 = -1;
    private String sectionLabel1 = null;
    private String sectionLabel2 = null;

    private static final String[] PAGE_TITLES = {
        "§6Targeting",
        "§6HUD & Visuals",
        "§6Camera",
        "§6Entity Filtering & Audio",
        "§6Advanced Sound",
        "§6Damage Numbers"
    };
    private static final String[] PAGE_DESCRIPTIONS = {
        "§7Lock-on range, angle, and target priority",
        "§7HUD elements, reticle, and display modes",
        "§7Camera feel, smoothness, and compatibility",
        "§7What can be targeted and audio controls",
        "§7Sound theme, volume, and pitch tuning",
        "§7Floating damage text visuals and colors"
    };

    // Button IDs
    private static final int DONE_BUTTON = 0;
    private static final int RESET_BUTTON = 1;
    private static final int NEXT_PAGE_BUTTON = 2;
    private static final int PREV_PAGE_BUTTON = 3;

    // Config button IDs
    private static final int TARGETING_RANGE_BUTTON = 100;
    private static final int MAX_TRACKING_DISTANCE_BUTTON = 101;
    private static final int DETECTION_ANGLE_BUTTON = 102;
    private static final int REQUIRE_LOS_TOGGLE = 103;
    private static final int SHOW_RETICLE_TOGGLE = 104;
    private static final int SHOW_HEALTH_TOGGLE = 105;
    private static final int SHOW_DISTANCE_TOGGLE = 106;
    private static final int SHOW_NAME_TOGGLE = 107;
    private static final int RETICLE_SCALE_BUTTON = 108;
    private static final int ENABLE_CAMERA_LOCKON_TOGGLE = 109;
    private static final int CAMERA_SMOOTHNESS_BUTTON = 110;
    private static final int MAX_PITCH_BUTTON = 111;
    private static final int MAX_YAW_BUTTON = 112;
    private static final int AUTO_THIRD_PERSON_TOGGLE = 113;
    private static final int BTP_MODE_TOGGLE = 114;
    private static final int BTP_INTENSITY_BUTTON = 115;
    private static final int TARGET_HOSTILES_TOGGLE = 116;
    private static final int TARGET_NEUTRALS_TOGGLE = 117;
    private static final int TARGET_PASSIVES_TOGGLE = 118;
    private static final int ENABLE_SOUNDS_TOGGLE = 119;
    private static final int SOUND_VOLUME_BUTTON = 120;
    private static final int UPDATE_FREQUENCY_BUTTON = 121;
    private static final int VALIDATION_INTERVAL_BUTTON = 122;

    // Enhanced Visual Feedback button IDs
    private static final int SHOW_DAMAGE_PREDICTION_TOGGLE = 123;
    private static final int SHOW_HITS_TO_KILL_TOGGLE = 124;
    private static final int SHOW_VULNERABILITIES_TOGGLE = 125;
    private static final int HIGHLIGHT_LETHAL_TARGETS_TOGGLE = 126;
    private static final int DAMAGE_PREDICTION_SCALE_BUTTON = 127;

    // Enhanced Audio Settings button IDs
    private static final int ENABLE_TARGET_LOCK_SOUND_TOGGLE = 128;
    private static final int ENABLE_TARGET_SWITCH_SOUND_TOGGLE = 129;
    private static final int ENABLE_LETHAL_TARGET_SOUND_TOGGLE = 130;
    private static final int ENABLE_TARGET_LOST_SOUND_TOGGLE = 131;
    private static final int TARGET_LOCK_VOLUME_BUTTON = 132;
    private static final int TARGET_SWITCH_VOLUME_BUTTON = 133;
    private static final int LETHAL_TARGET_VOLUME_BUTTON = 134;
    private static final int TARGET_LOST_VOLUME_BUTTON = 135;

    // Advanced Sound Tweaking button IDs
    private static final int SOUND_THEME_BUTTON = 136;
    private static final int TARGET_LOCK_PITCH_BUTTON = 137;
    private static final int TARGET_SWITCH_PITCH_BUTTON = 138;
    private static final int LETHAL_TARGET_PITCH_BUTTON = 139;
    private static final int TARGET_LOST_PITCH_BUTTON = 140;
    private static final int ENABLE_SOUND_VARIETY_TOGGLE = 141;

    // Damage Numbers Configuration button IDs
    private static final int ENABLE_DAMAGE_NUMBERS_TOGGLE = 142;
    private static final int DAMAGE_NUMBERS_SCALE_BUTTON = 143;
    private static final int DAMAGE_NUMBERS_DURATION_BUTTON = 144;
    private static final int DAMAGE_NUMBERS_CRITS_TOGGLE = 145;
    private static final int DAMAGE_NUMBERS_COLORS_TOGGLE = 146;
    private static final int DAMAGE_NUMBERS_COLOR_BUTTON = 147;
    private static final int CRITICAL_DAMAGE_COLOR_BUTTON = 148;
    private static final int LETHAL_DAMAGE_COLOR_BUTTON = 149;
    private static final int DAMAGE_NUMBERS_FADEOUT_TOGGLE = 150;
    private static final int DAMAGE_NUMBERS_OFFSET_BUTTON = 151;
    private static final int CAMERA_PRESET_BUTTON = 152;
    private static final int CAMERA_FOCUS_OFFSET_BUTTON = 153;
    private static final int DEBUG_COMPAT_TOGGLE = 154;
    private static final int TARGET_PRIORITY_BUTTON = 155;
    private static final int PER_MODE_SMOOTHING_TOGGLE = 156;
    private static final int DAMAGE_NUMBERS_MOTION_BUTTON = 157;
    private static final int CRIT_EMPHASIS_TOGGLE = 158;
    private static final int COMPACT_HUD_TOGGLE = 159;
    private static final int SOFT_AIM_TOGGLE = 160;
    private static final int TARGET_HISTORY_TOGGLE = 161;
    private static final int BOSS_STYLE_PANEL_TOGGLE = 162;
    private static final int HUD_ANCHOR_BUTTON = 163;
    private static final int HUD_OFFSET_X_BUTTON = 164;
    private static final int HUD_OFFSET_Y_BUTTON = 165;

    public GuiTargetingConfig(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = this.width / 2;
        int startY = 52;
        int buttonWidth = Math.min(240, this.width - 60);
        int buttonHeight = 22;
        int navY = this.height - 28;

        // Page navigation buttons
        if (currentPage > 0) {
            this.buttonList.add(new GuiButton(PREV_PAGE_BUTTON, centerX - 155, navY, 90, 20, "< Previous"));
        }
        if (currentPage < totalPages - 1) {
            this.buttonList.add(new GuiButton(NEXT_PAGE_BUTTON, centerX + 65, navY, 90, 20, "Next >"));
        }

        // Control buttons
        String doneLabel = I18n.format("gui.done");
        if (doneLabel == null || doneLabel.isEmpty()) {
            doneLabel = "Done";
        }
        this.buttonList.add(new GuiButton(DONE_BUTTON, centerX - 60, navY, 55, 20, doneLabel));
        this.buttonList.add(new GuiButton(RESET_BUTTON, centerX + 5, navY, 55, 20, "Reset"));

        int currentY = startY;
        sectionLabelY1 = -1;
        sectionLabelY2 = -1;
        sectionLabel1 = null;
        sectionLabel2 = null;

        switch (currentPage) {
            case 0: // Targeting
                addValueButton(TARGETING_RANGE_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Targeting Range", (float)TargetingConfig.targetingRange, 5.0f, 50.0f, 1.0f);
                currentY += 24;

                addValueButton(MAX_TRACKING_DISTANCE_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Max Tracking Distance", (float)TargetingConfig.maxTrackingDistance, 5.0f, 100.0f, 5.0f);
                currentY += 24;

                addValueButton(DETECTION_ANGLE_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Detection Angle", (float)TargetingConfig.maxAngle, 15.0f, 180.0f, 5.0f);
                currentY += 24;

                addToggleButton(REQUIRE_LOS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Require Line of Sight", TargetingConfig.requireLineOfSight);
                currentY += 24;

                addTargetPriorityButton(TARGET_PRIORITY_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        TargetingConfig.targetPriority);
                break;

            case 1: // HUD & Visuals
                addToggleButton(SHOW_RETICLE_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Reticle", TargetingConfig.showReticle);
                currentY += 24;

                addToggleButton(SHOW_HEALTH_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Health Bar", TargetingConfig.showHealthBar);
                currentY += 24;

                addToggleButton(SHOW_DISTANCE_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Distance", TargetingConfig.showDistance);
                currentY += 24;

                addToggleButton(SHOW_NAME_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Target Name", TargetingConfig.showTargetName);
                currentY += 24;

                addValueButton(RETICLE_SCALE_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Reticle Scale", TargetingConfig.reticleScale, 0.5f, 3.0f, 0.1f);
                currentY += 24 + 10;
                sectionLabel1 = "§8— Combat Info —";
                sectionLabelY1 = currentY - 6;
                currentY += 4;

                addToggleButton(SHOW_DAMAGE_PREDICTION_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Damage Prediction", TargetingConfig.showDamagePrediction);
                currentY += 24;

                addToggleButton(SHOW_HITS_TO_KILL_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Hits to Kill", TargetingConfig.showHitsToKill);
                currentY += 24;

                addToggleButton(SHOW_VULNERABILITIES_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Show Vulnerabilities", TargetingConfig.showVulnerabilities);
                currentY += 24;

                addToggleButton(HIGHLIGHT_LETHAL_TARGETS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Highlight Lethal Targets", TargetingConfig.highlightLethalTargets);
                currentY += 24;

                addValueButton(DAMAGE_PREDICTION_SCALE_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Damage Text Scale", TargetingConfig.damagePredictionScale, 0.5f, 2.0f, 0.1f);
                currentY += 24 + 10;
                sectionLabel2 = "§8— Display Modes —";
                sectionLabelY2 = currentY - 6;
                currentY += 4;

                addToggleButton(COMPACT_HUD_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Compact HUD Mode", TargetingConfig.compactHudMode);
                currentY += 24;

                addToggleButton(SOFT_AIM_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Soft Aim Indicator", TargetingConfig.softAimIndicator);
                currentY += 24;

                addToggleButton(TARGET_HISTORY_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target History Ring", TargetingConfig.targetHistoryEnabled);
                currentY += 24;

                addToggleButton(BOSS_STYLE_PANEL_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Boss-Style Panel", TargetingConfig.bossStylePanel);
                currentY += 24 + 10;
                sectionLabel2 = "\u00a78\u2014 HUD Position \u2014";
                sectionLabelY2 = currentY - 6;
                currentY += 4;

                addHudAnchorButton(HUD_ANCHOR_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        TargetingConfig.hudAnchor);
                currentY += 24;

                addValueButton(HUD_OFFSET_X_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "HUD Offset X", (float)TargetingConfig.hudOffsetX, -500.0f, 500.0f, 5.0f);
                currentY += 24;

                addValueButton(HUD_OFFSET_Y_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "HUD Offset Y", (float)TargetingConfig.hudOffsetY, -500.0f, 500.0f, 5.0f);
                break;

            case 2: // Camera
                addToggleButton(ENABLE_CAMERA_LOCKON_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Enable Camera Lock-On", TargetingConfig.enableCameraLockOn);
                currentY += 24;

                addValueButton(CAMERA_SMOOTHNESS_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Camera Smoothness", TargetingConfig.cameraSmoothness, 0.01f, 1.0f, 0.05f);
                currentY += 24;

                addValueButton(MAX_PITCH_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Max Pitch", TargetingConfig.maxPitchAdjustment, 0.0f, 90.0f, 5.0f);
                currentY += 24;

                addValueButton(MAX_YAW_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Max Yaw", TargetingConfig.maxYawAdjustment, 0.0f, 180.0f, 10.0f);
                currentY += 24;

                addToggleButton(AUTO_THIRD_PERSON_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Auto Third Person", TargetingConfig.autoThirdPerson);
                currentY += 24 + 10;

                addBtpModeButton(BTP_MODE_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "BTP Mode", TargetingConfig.btpCompatibilityMode);
                currentY += 24;

                if ("gentle".equals(TargetingConfig.btpCompatibilityMode)) {
                    addValueButton(BTP_INTENSITY_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                            "BTP Camera Intensity", TargetingConfig.btpCameraIntensity, 0.0f, 1.0f, 0.05f);
                }
                currentY += 24;

                addCameraPresetButton(CAMERA_PRESET_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        TargetingConfig.lockOnPreset);
                currentY += 24;

                addValueButton(CAMERA_FOCUS_OFFSET_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Focus Y Offset", TargetingConfig.cameraFocusYOffset, -1.0f, 1.0f, 0.1f);
                currentY += 24;

                addToggleButton(DEBUG_COMPAT_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Debug Compatibility Log", TargetingConfig.debugCompatibility);
                currentY += 24;

                addToggleButton(PER_MODE_SMOOTHING_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Gentler 1st-Person Smoothing", TargetingConfig.perModeSmoothingEnabled);
                break;

            case 3: // Entity Filtering & Audio
                addToggleButton(TARGET_HOSTILES_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Hostile Mobs", TargetingConfig.targetHostileMobs);
                currentY += 24;

                addToggleButton(TARGET_NEUTRALS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Neutral Mobs", TargetingConfig.targetNeutralMobs);
                currentY += 24;

                addToggleButton(TARGET_PASSIVES_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Passive Mobs", TargetingConfig.targetPassiveMobs);
                currentY += 24 + 10;
                sectionLabel1 = "§8— Audio —";
                sectionLabelY1 = currentY - 6;
                currentY += 4;

                addToggleButton(ENABLE_SOUNDS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Enable Sounds", TargetingConfig.enableSounds);
                currentY += 24;

                addValueButton(SOUND_VOLUME_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Master Sound Volume", TargetingConfig.soundVolume, 0.0f, 1.0f, 0.05f);
                currentY += 24 + 10;

                // Basic sound toggle controls
                addToggleButton(ENABLE_TARGET_LOCK_SOUND_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Lock Sound", TargetingConfig.enableTargetLockSound);
                currentY += 24;

                addToggleButton(ENABLE_TARGET_SWITCH_SOUND_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Switch Sound", TargetingConfig.enableTargetSwitchSound);
                currentY += 24;

                addToggleButton(ENABLE_LETHAL_TARGET_SOUND_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Lethal Target Sound", TargetingConfig.enableLethalTargetSound);
                currentY += 24;

                addToggleButton(ENABLE_TARGET_LOST_SOUND_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Lost Sound", TargetingConfig.enableTargetLostSound);
                currentY += 24 + 10;

                addValueButton(UPDATE_FREQUENCY_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Update Frequency", (float)TargetingConfig.updateFrequency, 1.0f, 20.0f, 1.0f);
                currentY += 24;

                addValueButton(VALIDATION_INTERVAL_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Validation Interval", (float)TargetingConfig.validationInterval, 1.0f, 60.0f, 1.0f);
                break;

            case 4: // Advanced Sound
                addSoundThemeButton(SOUND_THEME_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Sound Theme", TargetingConfig.soundTheme);
                currentY += 24;

                addToggleButton(ENABLE_SOUND_VARIETY_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Sound Variety", TargetingConfig.enableSoundVariety);
                currentY += 24 + 10;

                // Volume Controls
                addValueButton(TARGET_LOCK_VOLUME_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Lock Volume", TargetingConfig.targetLockVolume, 0.0f, 1.0f, 0.05f);
                currentY += 24;

                addValueButton(TARGET_SWITCH_VOLUME_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Switch Volume", TargetingConfig.targetSwitchVolume, 0.0f, 1.0f, 0.05f);
                currentY += 24;

                addValueButton(LETHAL_TARGET_VOLUME_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Lethal Target Volume", TargetingConfig.lethalTargetVolume, 0.0f, 1.0f, 0.05f);
                currentY += 24;

                addValueButton(TARGET_LOST_VOLUME_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Lost Volume", TargetingConfig.targetLostVolume, 0.0f, 1.0f, 0.05f);
                currentY += 24 + 10;

                // Pitch Controls
                addValueButton(TARGET_LOCK_PITCH_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Lock Pitch", TargetingConfig.targetLockPitch, 0.5f, 2.0f, 0.1f);
                currentY += 24;

                addValueButton(TARGET_SWITCH_PITCH_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Switch Pitch", TargetingConfig.targetSwitchPitch, 0.5f, 2.0f, 0.1f);
                currentY += 24;

                addValueButton(LETHAL_TARGET_PITCH_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Lethal Target Pitch", TargetingConfig.lethalTargetPitch, 0.5f, 2.0f, 0.1f);
                currentY += 24;

                addValueButton(TARGET_LOST_PITCH_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Target Lost Pitch", TargetingConfig.targetLostPitch, 0.5f, 2.0f, 0.1f);
                break;

            case 5: // Damage Numbers
                addToggleButton(ENABLE_DAMAGE_NUMBERS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Enable Damage Numbers", TargetingConfig.enableDamageNumbers);
                currentY += 24;

                addValueButton(DAMAGE_NUMBERS_SCALE_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Damage Numbers Scale", TargetingConfig.damageNumbersScale, 0.5f, 3.0f, 0.1f);
                currentY += 24;

                addValueButton(DAMAGE_NUMBERS_DURATION_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Duration (ticks)", (float)TargetingConfig.damageNumbersDuration, 20.0f, 200.0f, 10.0f);
                currentY += 24;

                addValueButton(DAMAGE_NUMBERS_OFFSET_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Vertical Offset", TargetingConfig.damageNumbersOffset, 0.0f, 2.0f, 0.1f);
                currentY += 24 + 10;

                addToggleButton(DAMAGE_NUMBERS_CRITS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Critical Hit Effects", TargetingConfig.damageNumbersCrits);
                currentY += 24;

                addToggleButton(DAMAGE_NUMBERS_COLORS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Colored Damage Numbers", TargetingConfig.damageNumbersColors);
                currentY += 24;

                addToggleButton(DAMAGE_NUMBERS_FADEOUT_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Fade-Out Animation", TargetingConfig.damageNumbersFadeOut);
                currentY += 24;

                addDamageMotionButton(DAMAGE_NUMBERS_MOTION_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        TargetingConfig.damageNumbersMotion);
                currentY += 24;

                addToggleButton(CRIT_EMPHASIS_TOGGLE, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Crit Pop Emphasis", TargetingConfig.critEmphasis);
                currentY += 24 + 10;

                // Color configuration buttons (simplified for now)
                addColorButton(DAMAGE_NUMBERS_COLOR_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Default Color", TargetingConfig.damageNumbersColor);
                currentY += 24;

                addColorButton(CRITICAL_DAMAGE_COLOR_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Critical Color", TargetingConfig.criticalDamageColor);
                currentY += 24;

                addColorButton(LETHAL_DAMAGE_COLOR_BUTTON, centerX - buttonWidth/2, currentY, buttonWidth, buttonHeight,
                        "Lethal Color", TargetingConfig.lethalDamageColor);
                break;
        }

        updateButtonStates();
    }

    private String formatToggleText(String name, boolean currentValue) {
        return name + ": " + (currentValue ? "§aON" : "§cOFF");
    }

    private String formatValue(float value) {
        if (Math.abs(value - Math.round(value)) < 0.001f) {
            return String.format("%.0f", value);
        }
        if (Math.abs(value * 10.0f - Math.round(value * 10.0f)) < 0.001f) {
            return String.format("%.1f", value);
        }
        return String.format("%.2f", value);
    }

    private String formatValue(double value) {
        return formatValue((float) value);
    }

    private void addToggleButton(int id, int x, int y, int width, int height, String name, boolean currentValue) {
        String displayText = formatToggleText(name, currentValue);
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addValueButton(int id, int x, int y, int width, int height, String name, float currentValue, float minValue, float maxValue, float increment) {
        String displayText = name + ": " + formatValue(currentValue);
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addBtpModeButton(int id, int x, int y, int width, int height, String name, String currentMode) {
        String displayText = name + ": " + currentMode.toUpperCase();
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addSoundThemeButton(int id, int x, int y, int width, int height, String name, String currentTheme) {
        String displayText = name + ": " + currentTheme.toUpperCase();
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addCameraPresetButton(int id, int x, int y, int width, int height, String currentPreset) {
        String displayText = "Camera Feel: " + currentPreset.toUpperCase();
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addTargetPriorityButton(int id, int x, int y, int width, int height, String currentPriority) {
        String displayText = "Target Priority: " + currentPriority.toUpperCase();
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addHudAnchorButton(int id, int x, int y, int width, int height, String currentAnchor) {
        String displayText = "HUD Position: " + currentAnchor.toUpperCase().replace("-", " ");
        this.buttonList.add(new GuiButton(id, x, y, width, height, displayText));
    }

    private void addDamageMotionButton(int id, int x, int y, int width, int height, String currentMotion) {
        String displayText = "Number Motion: " + currentMotion.toUpperCase();
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private void addColorButton(int id, int x, int y, int width, int height, String name, int currentColor) {
        String colorHex = String.format("#%06X", currentColor & 0xFFFFFF);
        String displayText = name + ": " + colorHex;
        GuiButton button = new GuiButton(id, x, y, width, height, displayText);
        this.buttonList.add(button);
    }

    private GuiButton getButtonById(int id) {
        for (GuiButton button : this.buttonList) {
            if (button.id == id) {
                return button;
            }
        }
        return null;
    }

    private void setButtonEnabled(int id, boolean enabled) {
        GuiButton button = getButtonById(id);
        if (button != null) {
            button.enabled = enabled;
        }
    }

    private void updateButtonStates() {
        setButtonEnabled(BTP_INTENSITY_BUTTON, "gentle".equals(TargetingConfig.btpCompatibilityMode));

        boolean soundsEnabled = TargetingConfig.enableSounds;
        setButtonEnabled(SOUND_VOLUME_BUTTON, soundsEnabled);
        setButtonEnabled(SOUND_THEME_BUTTON, soundsEnabled);
        setButtonEnabled(ENABLE_SOUND_VARIETY_TOGGLE, soundsEnabled);
        setButtonEnabled(TARGET_LOCK_VOLUME_BUTTON, soundsEnabled);
        setButtonEnabled(TARGET_SWITCH_VOLUME_BUTTON, soundsEnabled);
        setButtonEnabled(LETHAL_TARGET_VOLUME_BUTTON, soundsEnabled);
        setButtonEnabled(TARGET_LOST_VOLUME_BUTTON, soundsEnabled);
        setButtonEnabled(TARGET_LOCK_PITCH_BUTTON, soundsEnabled);
        setButtonEnabled(TARGET_SWITCH_PITCH_BUTTON, soundsEnabled);
        setButtonEnabled(LETHAL_TARGET_PITCH_BUTTON, soundsEnabled);
        setButtonEnabled(TARGET_LOST_PITCH_BUTTON, soundsEnabled);

        boolean damageNumbersEnabled = TargetingConfig.enableDamageNumbers;
        setButtonEnabled(DAMAGE_NUMBERS_SCALE_BUTTON, damageNumbersEnabled);
        setButtonEnabled(DAMAGE_NUMBERS_DURATION_BUTTON, damageNumbersEnabled);
        setButtonEnabled(DAMAGE_NUMBERS_OFFSET_BUTTON, damageNumbersEnabled);
        setButtonEnabled(DAMAGE_NUMBERS_CRITS_TOGGLE, damageNumbersEnabled);
        setButtonEnabled(DAMAGE_NUMBERS_COLORS_TOGGLE, damageNumbersEnabled);
        setButtonEnabled(DAMAGE_NUMBERS_FADEOUT_TOGGLE, damageNumbersEnabled);
        setButtonEnabled(DAMAGE_NUMBERS_MOTION_BUTTON, damageNumbersEnabled);
        setButtonEnabled(CRIT_EMPHASIS_TOGGLE, damageNumbersEnabled && TargetingConfig.damageNumbersCrits);

        boolean damageColorControlsEnabled = damageNumbersEnabled && TargetingConfig.damageNumbersColors;
        setButtonEnabled(DAMAGE_NUMBERS_COLOR_BUTTON, damageColorControlsEnabled);
        setButtonEnabled(CRITICAL_DAMAGE_COLOR_BUTTON, damageColorControlsEnabled);
        setButtonEnabled(LETHAL_DAMAGE_COLOR_BUTTON, damageColorControlsEnabled);
    }

    private void cycleSoundTheme() {
        switch (TargetingConfig.soundTheme.toLowerCase()) {
            case "default":
                TargetingConfig.soundTheme = "zelda";
                break;
            case "zelda":
                TargetingConfig.soundTheme = "modern";
                break;
            case "modern":
                TargetingConfig.soundTheme = "subtle";
                break;
            case "subtle":
                TargetingConfig.soundTheme = "cinematic";
                break;
            case "cinematic":
                TargetingConfig.soundTheme = "default";
                break;
            default:
                TargetingConfig.soundTheme = "default";
                break;
        }
        TargetingConfig.saveConfig();
    }

    private void handleConfigButton(GuiButton button, boolean decrease) {
        // Check for shift key for fine adjustment
        boolean isShiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ||
                                Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        switch (button.id) {
            case TARGETING_RANGE_BUTTON:
                float targetingIncrement = isShiftPressed ? 0.5f : 1.0f;
                TargetingConfig.targetingRange = cycleValue((float)TargetingConfig.targetingRange, 5.0f, 50.0f, targetingIncrement, decrease);
                button.displayString = "Targeting Range: " + formatValue(TargetingConfig.targetingRange);
                break;
            case MAX_TRACKING_DISTANCE_BUTTON:
                float trackingIncrement = isShiftPressed ? 1.0f : 5.0f;
                TargetingConfig.maxTrackingDistance = cycleValue((float)TargetingConfig.maxTrackingDistance, 5.0f, 100.0f, trackingIncrement, decrease);
                button.displayString = "Max Tracking Distance: " + formatValue(TargetingConfig.maxTrackingDistance);
                break;
            case DETECTION_ANGLE_BUTTON:
                float angleIncrement = isShiftPressed ? 1.0f : 5.0f;
                TargetingConfig.maxAngle = cycleValue((float)TargetingConfig.maxAngle, 15.0f, 180.0f, angleIncrement, decrease);
                button.displayString = "Detection Angle: " + formatValue(TargetingConfig.maxAngle);
                break;
            case RETICLE_SCALE_BUTTON:
                float scaleIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.reticleScale = cycleValue(TargetingConfig.reticleScale, 0.5f, 3.0f, scaleIncrement, decrease);
                button.displayString = "Reticle Scale: " + formatValue(TargetingConfig.reticleScale);
                break;
            case CAMERA_SMOOTHNESS_BUTTON:
                float smoothnessIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.cameraSmoothness = cycleValue(TargetingConfig.cameraSmoothness, 0.01f, 1.0f, smoothnessIncrement, decrease);
                button.displayString = "Camera Smoothness: " + formatValue(TargetingConfig.cameraSmoothness);
                break;
            case MAX_PITCH_BUTTON:
                float pitchIncrement = isShiftPressed ? 1.0f : 5.0f;
                TargetingConfig.maxPitchAdjustment = cycleValue(TargetingConfig.maxPitchAdjustment, 0.0f, 90.0f, pitchIncrement, decrease);
                button.displayString = "Max Pitch: " + formatValue(TargetingConfig.maxPitchAdjustment);
                break;
            case MAX_YAW_BUTTON:
                float yawIncrement = isShiftPressed ? 5.0f : 10.0f;
                TargetingConfig.maxYawAdjustment = cycleValue(TargetingConfig.maxYawAdjustment, 0.0f, 180.0f, yawIncrement, decrease);
                button.displayString = "Max Yaw: " + formatValue(TargetingConfig.maxYawAdjustment);
                break;
            case BTP_INTENSITY_BUTTON:
                float intensityIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.btpCameraIntensity = cycleValue(TargetingConfig.btpCameraIntensity, 0.0f, 1.0f, intensityIncrement, decrease);
                button.displayString = "BTP Camera Intensity: " + formatValue(TargetingConfig.btpCameraIntensity);
                break;
            case SOUND_VOLUME_BUTTON:
                float volumeIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.soundVolume = cycleValue(TargetingConfig.soundVolume, 0.0f, 1.0f, volumeIncrement, decrease);
                button.displayString = "Sound Volume: " + formatValue(TargetingConfig.soundVolume);
                break;
            case UPDATE_FREQUENCY_BUTTON:
                TargetingConfig.updateFrequency = (int)cycleValue((float)TargetingConfig.updateFrequency, 1.0f, 20.0f, 1.0f, decrease);
                button.displayString = "Update Frequency: " + TargetingConfig.updateFrequency;
                break;
            case VALIDATION_INTERVAL_BUTTON:
                float intervalIncrement = isShiftPressed ? 1.0f : 5.0f;
                TargetingConfig.validationInterval = (int)cycleValue((float)TargetingConfig.validationInterval, 1.0f, 60.0f, intervalIncrement, decrease);
                button.displayString = "Validation Interval: " + TargetingConfig.validationInterval;
                break;

            // Toggle buttons
            case REQUIRE_LOS_TOGGLE:
                TargetingConfig.requireLineOfSight = !TargetingConfig.requireLineOfSight;
                button.displayString = formatToggleText("Require Line of Sight", TargetingConfig.requireLineOfSight);
                break;
            case SHOW_RETICLE_TOGGLE:
                TargetingConfig.showReticle = !TargetingConfig.showReticle;
                button.displayString = formatToggleText("Show Reticle", TargetingConfig.showReticle);
                break;
            case SHOW_HEALTH_TOGGLE:
                TargetingConfig.showHealthBar = !TargetingConfig.showHealthBar;
                button.displayString = formatToggleText("Show Health Bar", TargetingConfig.showHealthBar);
                break;
            case SHOW_DISTANCE_TOGGLE:
                TargetingConfig.showDistance = !TargetingConfig.showDistance;
                button.displayString = formatToggleText("Show Distance", TargetingConfig.showDistance);
                break;
            case SHOW_NAME_TOGGLE:
                TargetingConfig.showTargetName = !TargetingConfig.showTargetName;
                button.displayString = formatToggleText("Show Target Name", TargetingConfig.showTargetName);
                break;
            case ENABLE_CAMERA_LOCKON_TOGGLE:
                TargetingConfig.enableCameraLockOn = !TargetingConfig.enableCameraLockOn;
                button.displayString = formatToggleText("Enable Camera Lock-On", TargetingConfig.enableCameraLockOn);
                break;
            case AUTO_THIRD_PERSON_TOGGLE:
                TargetingConfig.autoThirdPerson = !TargetingConfig.autoThirdPerson;
                button.displayString = formatToggleText("Auto Third Person", TargetingConfig.autoThirdPerson);
                break;
            case BTP_MODE_TOGGLE:
                cycleBtpMode();
                button.displayString = "BTP Mode: " + TargetingConfig.btpCompatibilityMode.toUpperCase();
                this.initGui(); // Refresh GUI to show/hide intensity slider
                break;
            case CAMERA_PRESET_BUTTON:
                applyCameraPreset(decrease);
                button.displayString = "Camera Feel: " + TargetingConfig.lockOnPreset.toUpperCase();
                this.initGui();
                break;
            case CAMERA_FOCUS_OFFSET_BUTTON: {
                float focusIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.cameraFocusYOffset = cycleValue(TargetingConfig.cameraFocusYOffset, -1.0f, 1.0f, focusIncrement, decrease);
                button.displayString = "Focus Y Offset: " + formatValue(TargetingConfig.cameraFocusYOffset);
                break;
            }
            case DEBUG_COMPAT_TOGGLE:
                TargetingConfig.debugCompatibility = !TargetingConfig.debugCompatibility;
                button.displayString = formatToggleText("Debug Compatibility Log", TargetingConfig.debugCompatibility);
                break;
            case PER_MODE_SMOOTHING_TOGGLE:
                TargetingConfig.perModeSmoothingEnabled = !TargetingConfig.perModeSmoothingEnabled;
                button.displayString = formatToggleText("Gentler 1st-Person Smoothing", TargetingConfig.perModeSmoothingEnabled);
                break;
            case TARGET_PRIORITY_BUTTON:
                cycleTargetPriority(decrease);
                button.displayString = "Target Priority: " + TargetingConfig.targetPriority.toUpperCase();
                break;
            case TARGET_HOSTILES_TOGGLE:
                TargetingConfig.targetHostileMobs = !TargetingConfig.targetHostileMobs;
                button.displayString = formatToggleText("Target Hostile Mobs", TargetingConfig.targetHostileMobs);
                break;
            case TARGET_NEUTRALS_TOGGLE:
                TargetingConfig.targetNeutralMobs = !TargetingConfig.targetNeutralMobs;
                button.displayString = formatToggleText("Target Neutral Mobs", TargetingConfig.targetNeutralMobs);
                break;
            case TARGET_PASSIVES_TOGGLE:
                TargetingConfig.targetPassiveMobs = !TargetingConfig.targetPassiveMobs;
                button.displayString = formatToggleText("Target Passive Mobs", TargetingConfig.targetPassiveMobs);
                break;
            case ENABLE_SOUNDS_TOGGLE:
                TargetingConfig.enableSounds = !TargetingConfig.enableSounds;
                button.displayString = formatToggleText("Enable Sounds", TargetingConfig.enableSounds);
                break;

            // Enhanced Audio Settings toggles
            case ENABLE_TARGET_LOCK_SOUND_TOGGLE:
                TargetingConfig.enableTargetLockSound = !TargetingConfig.enableTargetLockSound;
                button.displayString = formatToggleText("Target Lock Sound", TargetingConfig.enableTargetLockSound);
                break;
            case ENABLE_TARGET_SWITCH_SOUND_TOGGLE:
                TargetingConfig.enableTargetSwitchSound = !TargetingConfig.enableTargetSwitchSound;
                button.displayString = formatToggleText("Target Switch Sound", TargetingConfig.enableTargetSwitchSound);
                break;
            case ENABLE_LETHAL_TARGET_SOUND_TOGGLE:
                TargetingConfig.enableLethalTargetSound = !TargetingConfig.enableLethalTargetSound;
                button.displayString = formatToggleText("Lethal Target Sound", TargetingConfig.enableLethalTargetSound);
                break;
            case ENABLE_TARGET_LOST_SOUND_TOGGLE:
                TargetingConfig.enableTargetLostSound = !TargetingConfig.enableTargetLostSound;
                button.displayString = formatToggleText("Target Lost Sound", TargetingConfig.enableTargetLostSound);
                break;

            // Enhanced Audio Volume buttons
            case TARGET_LOCK_VOLUME_BUTTON:
                float lockVolumeIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.targetLockVolume = cycleValue(TargetingConfig.targetLockVolume, 0.0f, 1.0f, lockVolumeIncrement, decrease);
                button.displayString = "Target Lock Volume: " + formatValue(TargetingConfig.targetLockVolume);
                break;
            case TARGET_SWITCH_VOLUME_BUTTON:
                float switchVolumeIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.targetSwitchVolume = cycleValue(TargetingConfig.targetSwitchVolume, 0.0f, 1.0f, switchVolumeIncrement, decrease);
                button.displayString = "Target Switch Volume: " + formatValue(TargetingConfig.targetSwitchVolume);
                break;
            case LETHAL_TARGET_VOLUME_BUTTON:
                float lethalVolumeIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.lethalTargetVolume = cycleValue(TargetingConfig.lethalTargetVolume, 0.0f, 1.0f, lethalVolumeIncrement, decrease);
                button.displayString = "Lethal Target Volume: " + formatValue(TargetingConfig.lethalTargetVolume);
                break;
            case TARGET_LOST_VOLUME_BUTTON:
                float lostVolumeIncrement = isShiftPressed ? 0.01f : 0.05f;
                TargetingConfig.targetLostVolume = cycleValue(TargetingConfig.targetLostVolume, 0.0f, 1.0f, lostVolumeIncrement, decrease);
                button.displayString = "Target Lost Volume: " + formatValue(TargetingConfig.targetLostVolume);
                break;

            // Enhanced Visual Feedback toggles
            case SHOW_DAMAGE_PREDICTION_TOGGLE:
                TargetingConfig.showDamagePrediction = !TargetingConfig.showDamagePrediction;
                button.displayString = formatToggleText("Show Damage Prediction", TargetingConfig.showDamagePrediction);
                break;
            case SHOW_HITS_TO_KILL_TOGGLE:
                TargetingConfig.showHitsToKill = !TargetingConfig.showHitsToKill;
                button.displayString = formatToggleText("Show Hits to Kill", TargetingConfig.showHitsToKill);
                break;
            case SHOW_VULNERABILITIES_TOGGLE:
                TargetingConfig.showVulnerabilities = !TargetingConfig.showVulnerabilities;
                button.displayString = formatToggleText("Show Vulnerabilities", TargetingConfig.showVulnerabilities);
                break;
            case HIGHLIGHT_LETHAL_TARGETS_TOGGLE:
                TargetingConfig.highlightLethalTargets = !TargetingConfig.highlightLethalTargets;
                button.displayString = formatToggleText("Highlight Lethal Targets", TargetingConfig.highlightLethalTargets);
                break;
            case DAMAGE_PREDICTION_SCALE_BUTTON:
                float scaleIncrement2 = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.damagePredictionScale = cycleValue(TargetingConfig.damagePredictionScale, 0.5f, 2.0f, scaleIncrement2, decrease);
                button.displayString = "Damage Text Scale: " + formatValue(TargetingConfig.damagePredictionScale);
                break;

            // Advanced Sound Tweaking controls
            case SOUND_THEME_BUTTON:
                cycleSoundTheme();
                button.displayString = "Sound Theme: " + TargetingConfig.soundTheme.toUpperCase();
                break;
            case ENABLE_SOUND_VARIETY_TOGGLE:
                TargetingConfig.enableSoundVariety = !TargetingConfig.enableSoundVariety;
                button.displayString = formatToggleText("Sound Variety", TargetingConfig.enableSoundVariety);
                break;

            // Pitch controls
            case TARGET_LOCK_PITCH_BUTTON:
                float lockPitchIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.targetLockPitch = cycleValue(TargetingConfig.targetLockPitch, 0.5f, 2.0f, lockPitchIncrement, decrease);
                button.displayString = "Target Lock Pitch: " + formatValue(TargetingConfig.targetLockPitch);
                break;
            case TARGET_SWITCH_PITCH_BUTTON:
                float switchPitchIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.targetSwitchPitch = cycleValue(TargetingConfig.targetSwitchPitch, 0.5f, 2.0f, switchPitchIncrement, decrease);
                button.displayString = "Target Switch Pitch: " + formatValue(TargetingConfig.targetSwitchPitch);
                break;
            case LETHAL_TARGET_PITCH_BUTTON:
                float lethalPitchIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.lethalTargetPitch = cycleValue(TargetingConfig.lethalTargetPitch, 0.5f, 2.0f, lethalPitchIncrement, decrease);
                button.displayString = "Lethal Target Pitch: " + formatValue(TargetingConfig.lethalTargetPitch);
                break;
            case TARGET_LOST_PITCH_BUTTON:
                float lostPitchIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.targetLostPitch = cycleValue(TargetingConfig.targetLostPitch, 0.5f, 2.0f, lostPitchIncrement, decrease);
                button.displayString = "Target Lost Pitch: " + formatValue(TargetingConfig.targetLostPitch);
                break;

            // Damage Numbers Configuration handlers
            case ENABLE_DAMAGE_NUMBERS_TOGGLE:
                TargetingConfig.enableDamageNumbers = !TargetingConfig.enableDamageNumbers;
                button.displayString = formatToggleText("Enable Damage Numbers", TargetingConfig.enableDamageNumbers);
                break;
            case DAMAGE_NUMBERS_SCALE_BUTTON:
                float damageScaleIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.damageNumbersScale = cycleValue(TargetingConfig.damageNumbersScale, 0.5f, 3.0f, damageScaleIncrement, decrease);
                button.displayString = "Damage Numbers Scale: " + formatValue(TargetingConfig.damageNumbersScale);
                break;
            case DAMAGE_NUMBERS_DURATION_BUTTON:
                float durationIncrement = isShiftPressed ? 5.0f : 10.0f;
                TargetingConfig.damageNumbersDuration = (int)cycleValue((float)TargetingConfig.damageNumbersDuration, 20.0f, 200.0f, durationIncrement, decrease);
                button.displayString = "Duration (ticks): " + TargetingConfig.damageNumbersDuration;
                break;
            case DAMAGE_NUMBERS_OFFSET_BUTTON:
                float offsetIncrement = isShiftPressed ? 0.05f : 0.1f;
                TargetingConfig.damageNumbersOffset = cycleValue(TargetingConfig.damageNumbersOffset, 0.0f, 2.0f, offsetIncrement, decrease);
                button.displayString = "Vertical Offset: " + formatValue(TargetingConfig.damageNumbersOffset);
                break;
            case DAMAGE_NUMBERS_CRITS_TOGGLE:
                TargetingConfig.damageNumbersCrits = !TargetingConfig.damageNumbersCrits;
                button.displayString = formatToggleText("Critical Hit Effects", TargetingConfig.damageNumbersCrits);
                break;
            case DAMAGE_NUMBERS_COLORS_TOGGLE:
                TargetingConfig.damageNumbersColors = !TargetingConfig.damageNumbersColors;
                button.displayString = formatToggleText("Colored Damage Numbers", TargetingConfig.damageNumbersColors);
                break;
            case DAMAGE_NUMBERS_FADEOUT_TOGGLE:
                TargetingConfig.damageNumbersFadeOut = !TargetingConfig.damageNumbersFadeOut;
                button.displayString = formatToggleText("Fade-Out Animation", TargetingConfig.damageNumbersFadeOut);
                break;
            case DAMAGE_NUMBERS_MOTION_BUTTON:
                cycleDamageMotion(decrease);
                button.displayString = "Number Motion: " + TargetingConfig.damageNumbersMotion.toUpperCase();
                break;
            case CRIT_EMPHASIS_TOGGLE:
                TargetingConfig.critEmphasis = !TargetingConfig.critEmphasis;
                button.displayString = formatToggleText("Crit Pop Emphasis", TargetingConfig.critEmphasis);
                break;
            case COMPACT_HUD_TOGGLE:
                TargetingConfig.compactHudMode = !TargetingConfig.compactHudMode;
                button.displayString = formatToggleText("Compact HUD Mode", TargetingConfig.compactHudMode);
                break;
            case SOFT_AIM_TOGGLE:
                TargetingConfig.softAimIndicator = !TargetingConfig.softAimIndicator;
                button.displayString = formatToggleText("Soft Aim Indicator", TargetingConfig.softAimIndicator);
                break;
            case TARGET_HISTORY_TOGGLE:
                TargetingConfig.targetHistoryEnabled = !TargetingConfig.targetHistoryEnabled;
                button.displayString = formatToggleText("Target History Ring", TargetingConfig.targetHistoryEnabled);
                break;
            case BOSS_STYLE_PANEL_TOGGLE:
                TargetingConfig.bossStylePanel = !TargetingConfig.bossStylePanel;
                button.displayString = formatToggleText("Boss-Style Panel", TargetingConfig.bossStylePanel);
                break;
            case HUD_ANCHOR_BUTTON:
                cycleHudAnchor(decrease);
                button.displayString = "HUD Position: " + TargetingConfig.hudAnchor.toUpperCase().replace("-", " ");
                break;
            case HUD_OFFSET_X_BUTTON: {
                float incX = isShiftPressed ? 1.0f : 5.0f;
                TargetingConfig.hudOffsetX = (int) cycleValue((float) TargetingConfig.hudOffsetX, -500.0f, 500.0f, incX, decrease);
                button.displayString = "HUD Offset X: " + TargetingConfig.hudOffsetX;
                break;
            }
            case HUD_OFFSET_Y_BUTTON: {
                float incY = isShiftPressed ? 1.0f : 5.0f;
                TargetingConfig.hudOffsetY = (int) cycleValue((float) TargetingConfig.hudOffsetY, -500.0f, 500.0f, incY, decrease);
                button.displayString = "HUD Offset Y: " + TargetingConfig.hudOffsetY;
                break;
            }
            case DAMAGE_NUMBERS_COLOR_BUTTON:
                cycleDamageNumberColor("default");
                String defaultColorHex = String.format("#%06X", TargetingConfig.damageNumbersColor & 0xFFFFFF);
                button.displayString = "Default Color: " + defaultColorHex;
                break;
            case CRITICAL_DAMAGE_COLOR_BUTTON:
                cycleDamageNumberColor("critical");
                String criticalColorHex = String.format("#%06X", TargetingConfig.criticalDamageColor & 0xFFFFFF);
                button.displayString = "Critical Color: " + criticalColorHex;
                break;
            case LETHAL_DAMAGE_COLOR_BUTTON:
                cycleDamageNumberColor("lethal");
                String lethalColorHex = String.format("#%06X", TargetingConfig.lethalDamageColor & 0xFFFFFF);
                button.displayString = "Lethal Color: " + lethalColorHex;
                break;
        }

        // Automatically save config after any change
        TargetingConfig.saveConfig();
        this.initGui();
    }

    private float cycleValue(float currentValue, float minValue, float maxValue, float increment, boolean decrease) {
        float newValue;
        if (decrease) {
            newValue = currentValue - increment;
            if (newValue < minValue) {
                newValue = maxValue;
            }
        } else {
            newValue = currentValue + increment;
            if (newValue > maxValue) {
                newValue = minValue;
            }
        }
        return newValue;
    }

    private void cycleHudAnchor(boolean reverse) {
        String[] anchors = {"top-right", "top-left", "bottom-left", "bottom-right", "center"};
        int idx = 0;
        for (int i = 0; i < anchors.length; i++) {
            if (anchors[i].equals(TargetingConfig.hudAnchor)) { idx = i; break; }
        }
        idx = reverse ? (idx + anchors.length - 1) % anchors.length : (idx + 1) % anchors.length;
        TargetingConfig.hudAnchor = anchors[idx];
    }

    private void cycleDamageMotion(boolean reverse) {
        String[] modes = {"default", "subtle", "arcade"};
        int idx = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(TargetingConfig.damageNumbersMotion)) { idx = i; break; }
        }
        idx = reverse ? (idx + modes.length - 1) % modes.length : (idx + 1) % modes.length;
        TargetingConfig.damageNumbersMotion = modes[idx];
    }

    private void cycleTargetPriority(boolean reverse) {
        String[] priorities = {"nearest", "health", "threat", "angle"};
        int idx = 0;
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(TargetingConfig.targetPriority)) {
                idx = i;
                break;
            }
        }
        idx = reverse ? (idx + priorities.length - 1) % priorities.length : (idx + 1) % priorities.length;
        TargetingConfig.targetPriority = priorities[idx];
    }

    private void applyCameraPreset(boolean reverse) {
        String[] presets = {"cinematic", "balanced", "snappy"};
        int idx = 0;
        for (int i = 0; i < presets.length; i++) {
            if (presets[i].equals(TargetingConfig.lockOnPreset)) {
                idx = i;
                break;
            }
        }
        idx = reverse ? (idx + presets.length - 1) % presets.length : (idx + 1) % presets.length;
        TargetingConfig.lockOnPreset = presets[idx];
        switch (TargetingConfig.lockOnPreset) {
            case "cinematic":
                TargetingConfig.cameraSmoothness = 0.15f;
                TargetingConfig.maxPitchAdjustment = 40.0f;
                TargetingConfig.maxYawAdjustment = 60.0f;
                break;
            case "snappy":
                TargetingConfig.cameraSmoothness = 0.75f;
                TargetingConfig.maxPitchAdjustment = 75.0f;
                TargetingConfig.maxYawAdjustment = 120.0f;
                break;
            default: // balanced
                TargetingConfig.cameraSmoothness = 0.4f;
                TargetingConfig.maxPitchAdjustment = 60.0f;
                TargetingConfig.maxYawAdjustment = 90.0f;
                break;
        }
    }

    private void cycleBtpMode() {
        switch (TargetingConfig.btpCompatibilityMode) {
            case "disabled":
                TargetingConfig.btpCompatibilityMode = "gentle";
                break;
            case "gentle":
                TargetingConfig.btpCompatibilityMode = "visual_only";
                break;
            case "visual_only":
                TargetingConfig.btpCompatibilityMode = "disabled";
                break;
            default:
                TargetingConfig.btpCompatibilityMode = "gentle";
                break;
        }
        // Save config after BTP mode change
        TargetingConfig.saveConfig();
    }

    private void cycleDamageNumberColor(String colorType) {
        // Define common color options as RGB integers
        int[] colorOptions = {
            0xFFFFFF, // White
            0xFF0000, // Red
            0x00FF00, // Green
            0x0000FF, // Blue
            0xFFFF00, // Yellow
            0xFF8800, // Orange
            0xFF00FF, // Magenta
            0x00FFFF, // Cyan
            0x808080, // Gray
            0xFF6666, // Light Red
            0x66FF66, // Light Green
            0x6666FF, // Light Blue
            0xFFFFAA, // Light Yellow
            0xFFAA66, // Light Orange
            0xAA66FF, // Purple
            0x66FFAA  // Light Cyan
        };

        int currentColor;
        switch (colorType) {
            case "critical":
                currentColor = TargetingConfig.criticalDamageColor;
                break;
            case "lethal":
                currentColor = TargetingConfig.lethalDamageColor;
                break;
            default: // "default"
                currentColor = TargetingConfig.damageNumbersColor;
                break;
        }

        // Find current color index
        int currentIndex = 0;
        for (int i = 0; i < colorOptions.length; i++) {
            if (colorOptions[i] == currentColor) {
                currentIndex = i;
                break;
            }
        }

        // Cycle to next color
        int nextIndex = (currentIndex + 1) % colorOptions.length;
        int nextColor = colorOptions[nextIndex];

        // Apply the new color
        switch (colorType) {
            case "critical":
                TargetingConfig.criticalDamageColor = nextColor;
                break;
            case "lethal":
                TargetingConfig.lethalDamageColor = nextColor;
                break;
            default: // "default"
                TargetingConfig.damageNumbersColor = nextColor;
                break;
        }

        TargetingConfig.saveConfig();
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        if (button.id == DONE_BUTTON) {
            TargetingConfig.saveConfig();
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        if (button.id == RESET_BUTTON) {
            TargetingConfig.resetToDefaults();
            this.initGui();
            return;
        }
        if (button.id == NEXT_PAGE_BUTTON) {
            currentPage++;
            this.initGui();
            return;
        }
        if (button.id == PREV_PAGE_BUTTON) {
            currentPage--;
            this.initGui();
            return;
        }

        handleConfigButton(button, false);
    }

    @SuppressWarnings("null")
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.mc == null) {
            return;
        }

        // Handle right-click for decreasing values
        if (mouseButton == 1) {
            GuiButton clicked = null;
            for (GuiButton button : this.buttonList) {
                if (button.mousePressed(this.mc, mouseX, mouseY)) {
                    clicked = button;
                    break;
                }
            }
            if (clicked != null) {
                handleConfigButton(clicked, true);
            }
        }
    }

    @Nonnull
    private String getCurrentPageTitle() {
        String title = currentPage >= 0 && currentPage < PAGE_TITLES.length
            ? PAGE_TITLES[currentPage] : "§6Configuration";
        return title == null ? "§6Configuration" : title;
    }

    @Nonnull
    private String getCurrentPageDescription() {
        String description = currentPage >= 0 && currentPage < PAGE_DESCRIPTIONS.length
            ? PAGE_DESCRIPTIONS[currentPage] : "§7Adjust settings";
        return description == null ? "§7Adjust settings" : description;
    }

    @SuppressWarnings("null")
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;

        // Title + page dots on one line
        StringBuilder dotsBuf = new StringBuilder("Zelda Targeting  ");
        for (int i = 0; i < totalPages; i++) {
            dotsBuf.append(i == currentPage ? "\u00a7f\u25cf" : "\u00a78\u25cb");
            if (i < totalPages - 1) dotsBuf.append("\u00a7r ");
        }
        this.drawCenteredString(this.fontRenderer, dotsBuf.toString(), centerX, 10, 0xFFFFFF);

        // Page title and description
        this.drawCenteredString(this.fontRenderer, getCurrentPageTitle(), centerX, 24, 0xFFAA00);
        this.drawCenteredString(this.fontRenderer, getCurrentPageDescription(), centerX, 35, 0xAAAAAA);

        // BTP notice if relevant
        if (ZeldaTargetingMod.isBetterThirdPersonLoaded()) {
            String btpMsg = "\u00a7eBTP detected \u00a78\u2014\u00a7e " + TargetingConfig.btpCompatibilityMode.toUpperCase();
            this.drawCenteredString(this.fontRenderer, btpMsg, centerX, 44, 0xFFAA00);
        }

        // Section divider labels
        if (sectionLabel1 != null && sectionLabelY1 >= 0) {
            this.drawCenteredString(this.fontRenderer, sectionLabel1, centerX, sectionLabelY1, 0x888888);
        }
        if (sectionLabel2 != null && sectionLabelY2 >= 0) {
            this.drawCenteredString(this.fontRenderer, sectionLabel2, centerX, sectionLabelY2, 0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawButtonTooltip(mouseX, mouseY);
    }

    private void drawButtonTooltip(int mouseX, int mouseY) {
        for (GuiButton button : this.buttonList) {
            if (button.visible && mouseX >= button.x && mouseX < button.x + button.width
                    && mouseY >= button.y && mouseY < button.y + button.height) {
                List<String> tooltip = getTooltip(button.id);
                if (!tooltip.isEmpty()) {
                    this.drawHoveringText(tooltip, mouseX, mouseY);
                }
                return;
            }
        }
    }

    private List<String> getTooltip(int buttonId) {
        switch (buttonId) {
            case TARGETING_RANGE_BUTTON:
                return Arrays.asList("How far lock-on can acquire targets.", "Higher values may feel less focused.");
            case MAX_TRACKING_DISTANCE_BUTTON:
                return Arrays.asList("Distance where an active lock breaks.", "Set above targeting range for smoother chase behavior.");
            case DETECTION_ANGLE_BUTTON:
                return Arrays.asList("Field-of-view cone used for detection.", "Smaller = stricter forward targeting.");
            case CAMERA_SMOOTHNESS_BUTTON:
                return Arrays.asList("Camera follow responsiveness.", "Lower = smoother / slower, higher = snappier.");
            case BTP_MODE_TOGGLE:
                return Arrays.asList("Better Third Person compatibility mode.", "GENTLE keeps lock-on feel while reducing camera conflict.");
            case BTP_INTENSITY_BUTTON:
                return Arrays.asList("Only active in GENTLE mode.", "Controls how strongly lock-on moves the camera.");
            case ENABLE_SOUNDS_TOGGLE:
                return Arrays.asList("Master audio switch.", "When OFF, detailed audio controls are disabled.");
            case SOUND_THEME_BUTTON:
                return Arrays.asList("Cycles sound style presets.", "Try with Sound Variety for more variation.");
            case ENABLE_DAMAGE_NUMBERS_TOGGLE:
                return Arrays.asList("Master switch for floating damage text.", "When OFF, damage-number options are disabled.");
            case DAMAGE_NUMBERS_COLORS_TOGGLE:
                return Arrays.asList("Use dynamic damage text colors.", "Turn OFF for one consistent color.");
            case SHOW_HITS_TO_KILL_TOGGLE:
                return Arrays.asList("Shows estimated hits needed to defeat target.", "Displayed in polished HUD when available.");
            case DONE_BUTTON:
                return Collections.singletonList("Save configuration and close this menu.");
            case RESET_BUTTON:
                return Collections.singletonList("Reset all targeting settings to default values.");
            default:
                return Collections.singletonList("Tip: LMB increase/toggle, RMB decrease, Shift for finer adjustments.");
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
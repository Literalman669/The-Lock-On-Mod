package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingConfig;
import com.zeldatargeting.mod.config.TargetingPreset;
import com.zeldatargeting.mod.config.TargetingSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Transactional 1.4 configuration screen.
 *
 * <p>Controls always edit a {@link ConfigEditSession} first. The live runtime
 * receives a validated preview so the player can see the result immediately,
 * but no file is written until Save is pressed. Cancel restores the precise
 * settings which were active before the screen opened.</p>
 */
@SideOnly(Side.CLIENT)
public class GuiTargetingConfig extends GuiScreen {
    private static final int SAVE_BUTTON = 1;
    private static final int CANCEL_BUTTON = 2;
    private static final int RESET_BUTTON = 3;
    private static final int PREVIOUS_BUTTON = 4;
    private static final int NEXT_BUTTON = 5;
    private static final int CONTROL_BUTTON_BASE = 100;
    private static final int CONTENT_SCROLL_STEP = 24;

    private final GuiScreen parentScreen;
    private final ConfigPageNavigator pageNavigator = new ConfigPageNavigator();
    private final ConfigEditSession editSession;
    private final ConfigUiRebuildQueue rebuildQueue = new ConfigUiRebuildQueue();
    private final Map<GuiButton, ControlSpec> controlButtons = new IdentityHashMap<GuiButton, ControlSpec>();
    private final Map<GuiButton, Integer> naturalContentButtonY = new IdentityHashMap<GuiButton, Integer>();
    private final List<SectionLabel> sectionLabels = new ArrayList<SectionLabel>();

    private ConfigContentViewport contentViewport;
    private int contentTop;
    private int contentBottom;
    private int contentNaturalBottom;
    private int contentScroll;
    private boolean settled;

    public GuiTargetingConfig(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.editSession = new ConfigEditSession(TargetingConfig.captureActiveSettings());
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.controlButtons.clear();
        this.naturalContentButtonY.clear();
        this.sectionLabels.clear();

        int footerY = this.height - 30;
        this.contentTop = 58;
        this.contentBottom = Math.max(this.contentTop, footerY - 8);

        int currentY = this.contentTop;
        currentY = this.addPageControls(this.pageNavigator.getCurrentConfigPage(), currentY);
        this.contentNaturalBottom = Math.max(this.contentTop, currentY - 4);
        this.contentViewport = new ConfigContentViewport(this.contentTop, this.contentBottom, this.contentNaturalBottom);
        this.contentScroll = this.contentViewport.clampScroll(this.contentScroll);
        this.applyContentViewport();

        this.addFooterButtons(footerY);
    }

    private int addPageControls(ConfigPage page, int y) {
        switch (page) {
            case PRESETS:
                y = this.addSection("Choose a starting profile", y);
                y = this.addPreset(TargetingPreset.CINEMATIC, y);
                y = this.addPreset(TargetingPreset.BALANCED, y);
                y = this.addPreset(TargetingPreset.SNAPPY, y);
                y = this.addSection("Choosing a preset updates this preview only. Save keeps it.", y + 4);
                return y;

            case TARGETING:
                y = this.addSection("Acquisition", y);
                y = this.addControl(ControlSpec.doubleValue("targetingRange", "Targeting Range", 1.0D, 64.0D, 1.0D), y);
                y = this.addControl(ControlSpec.doubleValue("maxTrackingDistance", "Max Tracking Distance", 1.0D, 128.0D, 1.0D), y);
                y = this.addControl(ControlSpec.doubleValue("maxAngle", "Detection Angle", 15.0D, 180.0D, 5.0D), y);
                y = this.addControl(ControlSpec.toggle("requireLineOfSight", "Require Line of Sight"), y);
                y = this.addControl(ControlSpec.choice("targetPriority", "Target Priority", "nearest", "angle", "health", "threat"), y);
                y = this.addSection("Eligible Targets", y + 4);
                y = this.addControl(ControlSpec.toggle("targetHostileMobs", "Target Hostile Mobs"), y);
                y = this.addControl(ControlSpec.toggle("targetNeutralMobs", "Target Neutral Mobs"), y);
                y = this.addControl(ControlSpec.toggle("targetPassiveMobs", "Target Passive Mobs"), y);
                return this.addControl(ControlSpec.toggle("targetPlayers", "Target Players"), y);

            case CAMERA:
                y = this.addSection("Lock-on Camera", y);
                y = this.addControl(ControlSpec.toggle("enableCameraLockOn", "Enable Camera Look-On"), y);
                y = this.addControl(ControlSpec.floatValue("cameraSmoothness", "Camera Smoothness", 0.01F, 1.0F, 0.05F), y);
                y = this.addControl(ControlSpec.floatValue("maxPitchAdjustment", "Max Pitch", 0.0F, 90.0F, 5.0F), y);
                y = this.addControl(ControlSpec.floatValue("maxYawAdjustment", "Max Yaw", 0.0F, 180.0F, 5.0F), y);
                y = this.addControl(ControlSpec.floatValue("cameraFocusYOffset", "Focus Y Offset", -1.0F, 1.0F, 0.05F), y);
                y = this.addControl(ControlSpec.toggle("autoThirdPerson", "Auto Third Person"), y);
                return this.addControl(ControlSpec.toggle("perModeSmoothingEnabled", "Per-Mode Smoothing"), y);

            case HUD:
                y = this.addSection("Target Details", y);
                y = this.addControl(ControlSpec.toggle("showReticle", "Show Reticle"), y);
                y = this.addControl(ControlSpec.toggle("showHealthBar", "Show Health Bar"), y);
                y = this.addControl(ControlSpec.toggle("showDistance", "Show Distance"), y);
                y = this.addControl(ControlSpec.toggle("showTargetName", "Show Target Name"), y);
                y = this.addControl(ControlSpec.floatValue("reticleScale", "Reticle Scale", 0.5F, 3.0F, 0.1F), y);
                y = this.addSection("Panel Position", y + 4);
                y = this.addControl(ControlSpec.choice("hudAnchor", "HUD Anchor", "top-left", "top-right", "bottom-left", "bottom-right", "center"), y);
                y = this.addControl(ControlSpec.integerValue("hudOffsetX", "HUD Offset X", -500, 500, 5), y);
                y = this.addControl(ControlSpec.integerValue("hudOffsetY", "HUD Offset Y", -500, 500, 5), y);
                y = this.addControl(ControlSpec.floatValue("hudScale", "HUD Scale", 0.75F, 1.5F, 0.05F), y);
                y = this.addControl(ControlSpec.floatValue("hudOpacity", "HUD Opacity", 0.35F, 1.0F, 0.05F), y);
                y = this.addSection("Classic Target Ring", y + 4);
                y = this.addControl(ControlSpec.toggle("ringEnabled", "Enable Target Ring"), y);
                y = this.addControl(ControlSpec.floatValue("ringThickness", "Ring Thickness", 0.5F, 2.5F, 0.1F), y);
                y = this.addControl(ControlSpec.floatValue("ringGlowStrength", "Ring Glow", 0.0F, 1.5F, 0.1F), y);
                return this.addControl(ControlSpec.toggle("ringHealthArcEnabled", "Show Ring Health Arc"), y);

            case AUDIO:
                y = this.addSection("Main Audio", y);
                y = this.addControl(ControlSpec.toggle("enableSounds", "Enable Sounds"), y);
                y = this.addControl(ControlSpec.floatValue("soundVolume", "Master Sound Volume", 0.0F, 1.0F, 0.1F), y);
                y = this.addControl(ControlSpec.choice("soundTheme", "Sound Theme", "default", "zelda", "modern", "subtle"), y);
                y = this.addControl(ControlSpec.toggle("enableSoundVariety", "Sound Variety"), y);
                y = this.addSection("Events", y + 4);
                y = this.addControl(ControlSpec.toggle("enableTargetLockSound", "Target Lock Sound"), y);
                y = this.addControl(ControlSpec.floatValue("targetLockVolume", "Target Lock Volume", 0.0F, 1.0F, 0.1F), y);
                y = this.addControl(ControlSpec.floatValue("targetLockPitch", "Target Lock Pitch", 0.5F, 2.0F, 0.1F), y);
                y = this.addControl(ControlSpec.toggle("enableTargetSwitchSound", "Target Switch Sound"), y);
                y = this.addControl(ControlSpec.floatValue("targetSwitchVolume", "Target Switch Volume", 0.0F, 1.0F, 0.1F), y);
                y = this.addControl(ControlSpec.floatValue("targetSwitchPitch", "Target Switch Pitch", 0.5F, 2.0F, 0.1F), y);
                y = this.addControl(ControlSpec.toggle("enableLethalTargetSound", "Lethal Target Sound"), y);
                y = this.addControl(ControlSpec.floatValue("lethalTargetVolume", "Lethal Target Volume", 0.0F, 1.0F, 0.1F), y);
                y = this.addControl(ControlSpec.floatValue("lethalTargetPitch", "Lethal Target Pitch", 0.5F, 2.0F, 0.1F), y);
                y = this.addControl(ControlSpec.toggle("enableTargetLostSound", "Target Lost Sound"), y);
                y = this.addControl(ControlSpec.floatValue("targetLostVolume", "Target Lost Volume", 0.0F, 1.0F, 0.1F), y);
                return this.addControl(ControlSpec.floatValue("targetLostPitch", "Target Lost Pitch", 0.5F, 2.0F, 0.1F), y);

            case COMPATIBILITY:
                y = this.addSection("Vanilla is active by default. Optional adapters stay safe when absent.", y);
                y = this.addControl(ControlSpec.toggle("ssrCompensationEnabled", "Align Aim With Shoulder"), y);
                return this.addControl(ControlSpec.toggle("debugCompatibility", "Compatibility Debug Output"), y);

            case ACCESSIBILITY:
                y = this.addSection("Presentation", y);
                y = this.addControl(ControlSpec.choice("presentationPalette", "Color Palette", "default", "deuteranopia", "protanopia", "tritanopia"), y);
                y = this.addControl(ControlSpec.toggle("reducedMotion", "Reduced Motion"), y);
                y = this.addControl(ControlSpec.toggle("compactHudMode", "Compact HUD"), y);
                y = this.addControl(ControlSpec.toggle("softAimIndicator", "Soft Aim Indicator"), y);
                y = this.addControl(ControlSpec.toggle("targetHistoryEnabled", "Target History"), y);
                y = this.addControl(ControlSpec.toggle("bossStylePanel", "Boss Style Panel"), y);
                y = this.addSection("Combat Feedback", y + 4);
                y = this.addControl(ControlSpec.toggle("showDamagePrediction", "Show Damage Prediction"), y);
                y = this.addControl(ControlSpec.toggle("showHitsToKill", "Show Hits to Kill"), y);
                y = this.addControl(ControlSpec.toggle("showVulnerabilities", "Show Vulnerabilities"), y);
                y = this.addControl(ControlSpec.toggle("highlightLethalTargets", "Highlight Lethal Targets"), y);
                y = this.addControl(ControlSpec.floatValue("damagePredictionScale", "Damage Prediction Scale", 0.5F, 2.0F, 0.1F), y);
                y = this.addSection("Damage Numbers", y + 4);
                y = this.addControl(ControlSpec.toggle("enableDamageNumbers", "Enable Damage Numbers"), y);
                y = this.addControl(ControlSpec.floatValue("damageNumbersScale", "Damage Number Scale", 0.5F, 3.0F, 0.1F), y);
                y = this.addControl(ControlSpec.integerValue("damageNumbersDuration", "Damage Number Duration", 20, 200, 10), y);
                y = this.addControl(ControlSpec.toggle("damageNumbersCrits", "Critical Hit Effects"), y);
                y = this.addControl(ControlSpec.toggle("damageNumbersColors", "Colored Damage Numbers"), y);
                y = this.addControl(ControlSpec.toggle("damageNumbersFadeOut", "Damage Number Fade-Out"), y);
                y = this.addControl(ControlSpec.floatValue("damageNumbersOffset", "Damage Number Vertical Offset", 0.0F, 2.0F, 0.1F), y);
                y = this.addControl(ControlSpec.choice("damageNumbersMotion", "Damage Number Motion", "default", "subtle", "arcade"), y);
                return this.addControl(ControlSpec.toggle("critEmphasis", "Critical Pop Emphasis"), y);

            default:
                return y;
        }
    }

    private int addSection(String text, int y) {
        this.sectionLabels.add(new SectionLabel(text, y));
        return y + 16;
    }

    private int addPreset(TargetingPreset preset, int y) {
        return this.addControl(ControlSpec.preset(preset), y);
    }

    private int addControl(ControlSpec spec, int y) {
        int buttonWidth = Math.min(420, Math.max(150, this.width - 48));
        GuiButton button = new ConfigGuiButton(CONTROL_BUTTON_BASE + this.controlButtons.size(), (this.width - buttonWidth) / 2, y,
                buttonWidth, 20, this.getDisplayString(spec));
        this.buttonList.add(button);
        this.controlButtons.put(button, spec);
        this.naturalContentButtonY.put(button, y);
        return y + 24;
    }

    private void addFooterButtons(int footerY) {
        int buttonWidth = Math.max(52, Math.min(104, (this.width - 32) / 5));
        int totalWidth = buttonWidth * 5 + 16;
        int x = Math.max(8, (this.width - totalWidth) / 2);
        GuiButton previous = new ConfigGuiButton(PREVIOUS_BUTTON, x, footerY, buttonWidth, 20, "< Prev");
        GuiButton cancel = new ConfigGuiButton(CANCEL_BUTTON, x + buttonWidth + 4, footerY, buttonWidth, 20, "Cancel");
        GuiButton save = new ConfigGuiButton(SAVE_BUTTON, x + (buttonWidth + 4) * 2, footerY, buttonWidth, 20, "Save");
        GuiButton reset = new ConfigGuiButton(RESET_BUTTON, x + (buttonWidth + 4) * 3, footerY, buttonWidth, 20, "Reset");
        GuiButton next = new ConfigGuiButton(NEXT_BUTTON, x + (buttonWidth + 4) * 4, footerY, buttonWidth, 20, "Next >");
        previous.enabled = this.pageNavigator.getCurrentPage() > 0;
        next.enabled = this.pageNavigator.getCurrentPage() < ConfigPage.values().length - 1;
        this.buttonList.add(previous);
        this.buttonList.add(cancel);
        this.buttonList.add(save);
        this.buttonList.add(reset);
        this.buttonList.add(next);
    }

    private void applyContentViewport() {
        for (Map.Entry<GuiButton, Integer> entry : this.naturalContentButtonY.entrySet()) {
            GuiButton button = entry.getKey();
            int naturalY = entry.getValue();
            button.y = this.contentViewport.translateY(naturalY, this.contentScroll);
            button.visible = this.contentViewport.isFullyVisible(naturalY, button.height, this.contentScroll);
        }
    }

    private void changeControl(ControlSpec spec, boolean reverse) {
        if (spec.kind == ControlKind.PRESET) {
            this.editSession.reset(spec.preset);
        } else {
            TargetingSettings working = this.editSession.workingCopy();
            try {
                Field field = TargetingSettings.class.getField(spec.fieldName);
                switch (spec.kind) {
                    case TOGGLE:
                        field.setBoolean(working, !field.getBoolean(working));
                        break;
                    case DOUBLE:
                        field.setDouble(working, this.cycleDouble(field.getDouble(working), spec, reverse));
                        break;
                    case FLOAT:
                        field.setFloat(working, (float) this.cycleDouble(field.getFloat(working), spec, reverse));
                        break;
                    case INTEGER:
                        field.setInt(working, (int) this.cycleDouble(field.getInt(working), spec, reverse));
                        break;
                    case CHOICE:
                        field.set(working, this.cycleChoice((String) field.get(working), spec, reverse));
                        break;
                    default:
                        break;
                }
            } catch (ReflectiveOperationException ignored) {
                return;
            }
        }

        TargetingConfig.previewSettings(this.editSession.preview());
        this.rebuildQueue.requestRebuild(false);
    }

    private double cycleDouble(double current, ControlSpec spec, boolean reverse) {
        double next = current + (reverse ? -spec.step : spec.step);
        if (next > spec.maximum + 0.0001D) {
            next = spec.minimum;
        } else if (next < spec.minimum - 0.0001D) {
            next = spec.maximum;
        }
        return Math.round(next * 1000.0D) / 1000.0D;
    }

    private String cycleChoice(String current, ControlSpec spec, boolean reverse) {
        int index = 0;
        for (int i = 0; i < spec.choices.length; i++) {
            if (spec.choices[i].equalsIgnoreCase(current)) {
                index = i;
                break;
            }
        }
        int offset = reverse ? -1 : 1;
        index = (index + offset + spec.choices.length) % spec.choices.length;
        return spec.choices[index];
    }

    private String getDisplayString(ControlSpec spec) {
        if (spec.kind == ControlKind.PRESET) {
            String selected = this.editSession.workingCopy().lockOnPreset;
            String marker = spec.preset.getId().equalsIgnoreCase(selected) ? "§a* " : "§7  ";
            return marker + spec.preset.name().substring(0, 1) + spec.preset.name().substring(1).toLowerCase(Locale.ROOT) + " Profile";
        }

        try {
            Object value = TargetingSettings.class.getField(spec.fieldName).get(this.editSession.workingCopy());
            if (spec.kind == ControlKind.TOGGLE) {
                return spec.label + ": " + ((Boolean) value ? "§aON" : "§cOFF");
            }
            if (spec.kind == ControlKind.CHOICE) {
                return spec.label + ": " + String.valueOf(value).toUpperCase(Locale.ROOT).replace('-', ' ');
            }
            if (spec.kind == ControlKind.INTEGER) {
                return spec.label + ": " + value;
            }
            return spec.label + ": " + this.formatNumber(((Number) value).doubleValue());
        } catch (ReflectiveOperationException ignored) {
            return spec.label;
        }
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == SAVE_BUTTON) {
            this.settled = true;
            TargetingConfig.saveSettings(this.editSession.save());
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        if (button.id == CANCEL_BUTTON) {
            this.cancelAndClose();
            return;
        }
        if (button.id == RESET_BUTTON) {
            this.editSession.reset(TargetingPreset.fromId(this.editSession.workingCopy().lockOnPreset));
            TargetingConfig.previewSettings(this.editSession.preview());
            this.rebuildQueue.requestRebuild(true);
            return;
        }
        if (button.id == PREVIOUS_BUTTON) {
            this.pageNavigator.requestPrevious();
            this.rebuildQueue.requestRebuild(true);
            return;
        }
        if (button.id == NEXT_BUTTON) {
            this.pageNavigator.requestNext();
            this.rebuildQueue.requestRebuild(true);
            return;
        }

        ControlSpec spec = this.controlButtons.get(button);
        if (spec != null) {
            this.changeControl(spec, false);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.rebuildQueue.consumeRebuildRequest()) {
            this.pageNavigator.applyPendingPage();
            if (this.rebuildQueue.consumeScrollResetRequest()) {
                this.contentScroll = 0;
            }
            this.initGui();
        }
    }

    private void cancelAndClose() {
        this.settled = true;
        TargetingConfig.restoreSettings(this.editSession.cancel());
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 1) {
            for (Map.Entry<GuiButton, ControlSpec> entry : this.controlButtons.entrySet()) {
                GuiButton button = entry.getKey();
                if (button.visible && button.enabled && button.mousePressed(this.mc, mouseX, mouseY)) {
                    this.changeControl(entry.getValue(), true);
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.contentViewport == null || this.contentViewport.getMaxScroll() == 0) {
            return;
        }
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            this.contentScroll = this.contentViewport.clampScroll(this.contentScroll + (wheel < 0 ? CONTENT_SCROLL_STEP : -CONTENT_SCROLL_STEP));
            this.applyContentViewport();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.cancelAndClose();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        if (!this.settled) {
            this.settled = true;
            TargetingConfig.restoreSettings(this.editSession.cancel());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        ConfigPage page = this.pageNavigator.getCurrentConfigPage();
        this.drawCenteredString(this.fontRenderer, "Zelda Targeting", this.width / 2, 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "§6" + page.getTitle(), this.width / 2, 24, 0xFFAA00);
        this.drawCenteredString(this.fontRenderer, "§7" + page.getDescription(), this.width / 2, 40, 0xA0A0A0);

        for (SectionLabel section : this.sectionLabels) {
            int translatedY = this.contentViewport.translateY(section.naturalY, this.contentScroll);
            if (translatedY >= this.contentTop && translatedY + 10 <= this.contentBottom) {
                this.drawCenteredString(this.fontRenderer, "§8- " + section.text + " -", this.width / 2, translatedY, 0x808080);
            }
        }

        int markerY = 10;
        String pageMarker = "§7" + (this.pageNavigator.getCurrentPage() + 1) + " / " + ConfigPage.values().length;
        this.drawString(this.fontRenderer, pageMarker, this.width - this.fontRenderer.getStringWidth(pageMarker) - 10, markerY, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private enum ControlKind {
        TOGGLE,
        DOUBLE,
        FLOAT,
        INTEGER,
        CHOICE,
        PRESET
    }

    private static final class ControlSpec {
        private final ControlKind kind;
        private final String fieldName;
        private final String label;
        private final double minimum;
        private final double maximum;
        private final double step;
        private final String[] choices;
        private final TargetingPreset preset;

        private ControlSpec(ControlKind kind, String fieldName, String label, double minimum, double maximum, double step,
                            String[] choices, TargetingPreset preset) {
            this.kind = kind;
            this.fieldName = fieldName;
            this.label = label;
            this.minimum = minimum;
            this.maximum = maximum;
            this.step = step;
            this.choices = choices;
            this.preset = preset;
        }

        private static ControlSpec toggle(String fieldName, String label) {
            return new ControlSpec(ControlKind.TOGGLE, fieldName, label, 0.0D, 0.0D, 0.0D, new String[0], null);
        }

        private static ControlSpec doubleValue(String fieldName, String label, double minimum, double maximum, double step) {
            return new ControlSpec(ControlKind.DOUBLE, fieldName, label, minimum, maximum, step, new String[0], null);
        }

        private static ControlSpec floatValue(String fieldName, String label, float minimum, float maximum, float step) {
            return new ControlSpec(ControlKind.FLOAT, fieldName, label, minimum, maximum, step, new String[0], null);
        }

        private static ControlSpec integerValue(String fieldName, String label, int minimum, int maximum, int step) {
            return new ControlSpec(ControlKind.INTEGER, fieldName, label, minimum, maximum, step, new String[0], null);
        }

        private static ControlSpec choice(String fieldName, String label, String... choices) {
            return new ControlSpec(ControlKind.CHOICE, fieldName, label, 0.0D, 0.0D, 0.0D, choices, null);
        }

        private static ControlSpec preset(TargetingPreset preset) {
            return new ControlSpec(ControlKind.PRESET, null, null, 0.0D, 0.0D, 0.0D, new String[0], preset);
        }
    }

    private static final class SectionLabel {
        private final String text;
        private final int naturalY;

        private SectionLabel(String text, int naturalY) {
            this.text = text;
            this.naturalY = naturalY;
        }
    }
}

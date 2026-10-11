package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.config.TargetingConfig;
import com.zeldatargeting.mod.config.TargetingPreset;
import com.zeldatargeting.mod.config.TargetingSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Transactional configuration screen.
 *
 * <p>Controls always edit a {@link ConfigEditSession} first. The live runtime
 * receives a validated preview so the player can see the result immediately,
 * but no file is written until Save is pressed. Cancel restores the precise
 * settings which were active before the screen opened.</p>
 *
 * <p>Pages, labels, controls, and help text come from {@link ConfigLayout}.</p>
 */
@SideOnly(Side.CLIENT)
public class GuiTargetingConfig extends GuiScreen {
    private static final int SAVE_BUTTON = 1;
    private static final int CANCEL_BUTTON = 2;
    private static final int RESET_BUTTON = 3;
    private static final int PREVIOUS_BUTTON = 4;
    private static final int NEXT_BUTTON = 5;
    private static final int TAB_BUTTON_BASE = 50;
    private static final int CONTROL_BUTTON_BASE = 100;
    private static final int CONTENT_SCROLL_STEP = 24;
    private static final int TITLE_Y = 8;
    private static final int TAB_Y = 22;
    private static final int DESCRIPTION_Y = 40;
    private static final int CONTENT_TOP = 54;
    private static final int LINE_HEIGHT = 10;

    private final GuiScreen parentScreen;
    private final ConfigPageNavigator pageNavigator = new ConfigPageNavigator();
    private final ConfigEditSession editSession;
    private final ConfigUiRebuildQueue rebuildQueue = new ConfigUiRebuildQueue();
    private final ConfigTooltipTimer tooltipTimer = new ConfigTooltipTimer();
    private final Map<GuiButton, ConfigEntry> controls = new IdentityHashMap<GuiButton, ConfigEntry>();
    private final Map<GuiButton, Integer> naturalContentButtonY = new IdentityHashMap<GuiButton, Integer>();
    private final List<TextRow> sectionLabels = new ArrayList<TextRow>();
    private final List<TextRow> presetNotes = new ArrayList<TextRow>();

    private ConfigContentViewport contentViewport;
    private ConfigTabLayout.Mode tabMode = ConfigTabLayout.Mode.FULL;
    private int contentBottom;
    private int contentScroll;
    private int footerY;
    private int columnLeft;
    private int columnRight;
    private boolean settled;
    private boolean openingChildScreen;

    public GuiTargetingConfig(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.editSession = new ConfigEditSession(TargetingConfig.captureActiveSettings());
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.controls.clear();
        this.naturalContentButtonY.clear();
        this.sectionLabels.clear();
        this.presetNotes.clear();
        this.tooltipTimer.interact();

        this.footerY = this.height - 30;
        this.contentBottom = Math.max(CONTENT_TOP, this.footerY - 8);
        int columnWidth = Math.min(420, Math.max(150, this.width - 48));
        this.columnLeft = (this.width - columnWidth) / 2;
        this.columnRight = this.columnLeft + columnWidth;

        this.addTabs();
        int currentY = CONTENT_TOP;
        for (ConfigLayout.Item item : ConfigLayout.page(this.pageNavigator.getCurrentConfigPage())) {
            currentY = item.isSection()
                ? this.addSection(item.getSectionTitle(), currentY == CONTENT_TOP ? currentY : currentY + 4)
                : this.addControl(item.getEntry(), currentY, columnWidth);
        }
        int contentNaturalBottom = Math.max(CONTENT_TOP, currentY - 4);
        this.contentViewport = new ConfigContentViewport(CONTENT_TOP, this.contentBottom, contentNaturalBottom);
        this.contentScroll = this.contentViewport.clampScroll(this.contentScroll);
        this.applyContentViewport();

        this.addFooterButtons();
        this.refreshControls();
    }

    private void addTabs() {
        ConfigPage[] pages = ConfigPage.values();
        List<String> fullNames = new ArrayList<String>();
        List<String> shortNames = new ArrayList<String>();
        for (ConfigPage page : pages) {
            fullNames.add(page.getTitle());
            shortNames.add(page.getShortTitle());
        }
        ConfigTabLayout.Result tabs = ConfigTabLayout.layout(
            this.width, TAB_Y, fullNames, shortNames, this.fontRenderer::getStringWidth);
        this.tabMode = tabs.getMode();
        List<String> names = this.tabMode == ConfigTabLayout.Mode.SHORT ? shortNames : fullNames;
        for (int i = 0; i < tabs.getTabs().size(); i++) {
            ConfigRect rect = tabs.getTabs().get(i);
            ConfigGuiButton tab = new ConfigGuiButton(
                TAB_BUTTON_BASE + i, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), names.get(i));
            tab.setSelected(i == this.pageNavigator.getCurrentPage());
            this.buttonList.add(tab);
        }
    }

    private int addSection(String text, int y) {
        this.sectionLabels.add(new TextRow(text, y));
        return y + 16;
    }

    private int addControl(ConfigEntry entry, int y, int columnWidth) {
        GuiButton button;
        int id = CONTROL_BUTTON_BASE + this.controls.size();
        if (entry.getKind() == ConfigEntry.Kind.SLIDER) {
            button = new ConfigSlider(id, this.columnLeft, y, columnWidth, entry, this.readNumber(entry),
                value -> this.onSliderDrag(entry, value),
                value -> entry.getLabel() + ": " + ConfigValueFormat.format(value, entry.getUnit()));
        } else {
            button = new ConfigGuiButton(id, this.columnLeft, y, columnWidth, 20, "");
        }
        this.buttonList.add(button);
        this.controls.put(button, entry);
        this.naturalContentButtonY.put(button, y);
        if (entry.getKind() == ConfigEntry.Kind.PRESET) {
            this.presetNotes.add(new TextRow(entry.getHelp(), y + 23));
            return y + 38;
        }
        return y + 24;
    }

    private void addFooterButtons() {
        int buttonWidth = Math.max(52, Math.min(104, (this.width - 32) / 5));
        int totalWidth = buttonWidth * 5 + 16;
        int x = Math.max(8, (this.width - totalWidth) / 2);
        GuiButton previous = new ConfigGuiButton(PREVIOUS_BUTTON, x, this.footerY, buttonWidth, 20, "< Prev");
        GuiButton cancel = new ConfigGuiButton(CANCEL_BUTTON, x + buttonWidth + 4, this.footerY, buttonWidth, 20, "Cancel");
        GuiButton save = new ConfigGuiButton(SAVE_BUTTON, x + (buttonWidth + 4) * 2, this.footerY, buttonWidth, 20, "Save");
        GuiButton reset = new ConfigGuiButton(RESET_BUTTON, x + (buttonWidth + 4) * 3, this.footerY, buttonWidth, 20, "Reset Page");
        GuiButton next = new ConfigGuiButton(NEXT_BUTTON, x + (buttonWidth + 4) * 4, this.footerY, buttonWidth, 20, "Next >");
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

    /** Updates every control's label and greying from the working settings, without rebuilding. */
    private void refreshControls() {
        TargetingSettings working = this.editSession.workingCopy();
        for (Map.Entry<GuiButton, ConfigEntry> control : this.controls.entrySet()) {
            GuiButton button = control.getKey();
            ConfigEntry entry = control.getValue();
            if (button instanceof ConfigSlider) {
                ConfigSlider slider = (ConfigSlider) button;
                if (!slider.isDragging()) {
                    slider.setDisplayedValue(this.readNumber(entry));
                }
            } else {
                button.displayString = this.getDisplayString(entry);
            }
            button.enabled = ConfigDependencies.blockingDependency(entry, working) == null;
        }
    }

    private void onSliderDrag(ConfigEntry entry, double value) {
        this.tooltipTimer.interact();
        this.writeNumber(entry, value);
        TargetingConfig.previewSettings(this.editSession.preview());
        this.refreshControls();
    }

    private void changeControl(ConfigEntry entry, boolean reverse) {
        switch (entry.getKind()) {
            case PRESET:
                this.editSession.reset(TargetingPreset.fromId(entry.getPresetId()));
                break;
            case TEXT:
                this.openTextEditor(entry);
                return;
            case SLIDER:
                return;
            default:
                TargetingSettings working = this.editSession.workingCopy();
                try {
                    Field field = TargetingSettings.class.getField(entry.getFieldName());
                    if (entry.getKind() == ConfigEntry.Kind.TOGGLE) {
                        field.setBoolean(working, !field.getBoolean(working));
                    } else if (entry.getKind() == ConfigEntry.Kind.CHOICE) {
                        field.set(working, this.cycleChoice((String) field.get(working), entry, reverse));
                    } else if (entry.getKind() == ConfigEntry.Kind.COLOR) {
                        field.setInt(working, ReticleColor.cycle(field.getInt(working), reverse).getRgb());
                    }
                } catch (ReflectiveOperationException ignored) {
                    return;
                }
                break;
        }
        TargetingConfig.previewSettings(this.editSession.preview());
        this.refreshControls();
    }

    private void openTextEditor(final ConfigEntry entry) {
        final Field field;
        String current;
        try {
            field = TargetingSettings.class.getField(entry.getFieldName());
            current = (String) field.get(this.editSession.workingCopy());
        } catch (ReflectiveOperationException ignored) {
            return;
        }
        GuiTextSettingEdit editor = new GuiTextSettingEdit(this, entry.getLabel(), entry.getTextHints(), current, value -> {
            try {
                field.set(this.editSession.workingCopy(), value);
            } catch (ReflectiveOperationException ignored) {
                return;
            }
            TargetingConfig.previewSettings(this.editSession.preview());
        });
        // The edit stays inside this screen's Save/Cancel session, so leaving for the
        // editor must not cancel it.
        this.openingChildScreen = true;
        this.mc.displayGuiScreen(editor);
    }

    private String cycleChoice(String current, ConfigEntry entry, boolean reverse) {
        String[] values = entry.getChoiceValues();
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(current)) {
                index = i;
                break;
            }
        }
        index = (index + (reverse ? -1 : 1) + values.length) % values.length;
        return values[index];
    }

    private double readNumber(ConfigEntry entry) {
        return this.readNumber(entry, this.editSession.workingCopy());
    }

    private double readNumber(ConfigEntry entry, TargetingSettings settings) {
        try {
            return ((Number) TargetingSettings.class.getField(entry.getFieldName()).get(settings)).doubleValue();
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return entry.getMin();
        }
    }

    private void writeNumber(ConfigEntry entry, double value) {
        TargetingSettings working = this.editSession.workingCopy();
        try {
            Field field = TargetingSettings.class.getField(entry.getFieldName());
            if (field.getType() == double.class) {
                field.setDouble(working, value);
            } else if (field.getType() == float.class) {
                field.setFloat(working, (float) value);
            } else if (field.getType() == int.class) {
                field.setInt(working, (int) Math.round(value));
            }
        } catch (ReflectiveOperationException ignored) {
            // Not a numeric setting; nothing to write.
        }
    }

    private String getDisplayString(ConfigEntry entry) {
        if (entry.getKind() == ConfigEntry.Kind.PRESET) {
            boolean selected = entry.getPresetId().equalsIgnoreCase(this.editSession.workingCopy().lockOnPreset);
            return (selected ? "§a* " : "§7  ") + entry.getLabel();
        }
        Object value;
        try {
            value = TargetingSettings.class.getField(entry.getFieldName()).get(this.editSession.workingCopy());
        } catch (ReflectiveOperationException ignored) {
            return entry.getLabel();
        }
        switch (entry.getKind()) {
            case TOGGLE:
                return entry.getLabel() + ": " + ((Boolean) value ? "§aON" : "§cOFF");
            case CHOICE:
                return entry.getLabel() + ": " + entry.choiceLabel(String.valueOf(value));
            case COLOR:
                return entry.getLabel() + ": " + ReticleColor.describe((Integer) value);
            case TEXT:
                String text = String.valueOf(value);
                return entry.getLabel() + ": " + (text.isEmpty() ? "§7none" : this.summarize(text, entry.getLabel()));
            default:
                return entry.getLabel() + ": " + ConfigValueFormat.format(((Number) value).doubleValue(), entry.getUnit());
        }
    }

    private String summarize(String text, String label) {
        int available = this.columnRight - this.columnLeft - 16 - this.fontRenderer.getStringWidth(label + ": ");
        if (this.fontRenderer.getStringWidth(text) <= available) {
            return text;
        }
        return this.fontRenderer.trimStringToWidth(text, Math.max(0, available - this.fontRenderer.getStringWidth("..."))) + "...";
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
            this.resetPage();
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
        if (button.id >= TAB_BUTTON_BASE && button.id < CONTROL_BUTTON_BASE) {
            this.pageNavigator.requestPage(button.id - TAB_BUTTON_BASE);
            this.rebuildQueue.requestRebuild(true);
            return;
        }
        ConfigEntry entry = this.controls.get(button);
        if (entry != null) {
            this.changeControl(entry, false);
        }
    }

    private void resetPage() {
        ConfigPage page = this.pageNavigator.getCurrentConfigPage();
        TargetingPreset preset = TargetingPreset.fromId(this.editSession.workingCopy().lockOnPreset);
        if (page == ConfigPage.PRESETS) {
            this.editSession.reset(preset);
        } else {
            // Every setting on the page, including greyed-out ones, goes back to the preset.
            List<String> pageFields = new ArrayList<String>();
            for (ConfigLayout.Item item : ConfigLayout.page(page)) {
                if (!item.isSection() && item.getEntry().getFieldName() != null) {
                    pageFields.add(item.getEntry().getFieldName());
                }
            }
            this.editSession.resetFields(preset, pageFields);
        }
        TargetingConfig.previewSettings(this.editSession.preview());
        this.rebuildQueue.requestRebuild(true);
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
        this.tooltipTimer.interact();
        if (mouseButton == 1) {
            for (Map.Entry<GuiButton, ConfigEntry> control : this.controls.entrySet()) {
                GuiButton button = control.getKey();
                ConfigEntry.Kind kind = control.getValue().getKind();
                boolean cycles = kind == ConfigEntry.Kind.TOGGLE
                    || kind == ConfigEntry.Kind.CHOICE
                    || kind == ConfigEntry.Kind.COLOR;
                if (cycles && button.visible && button.enabled && button.mousePressed(this.mc, mouseX, mouseY)) {
                    button.playPressSound(this.mc.getSoundHandler());
                    this.changeControl(control.getValue(), true);
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
            this.tooltipTimer.interact();
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
        if (this.openingChildScreen) {
            this.openingChildScreen = false;
            return;
        }
        if (!this.settled) {
            this.settled = true;
            TargetingConfig.restoreSettings(this.editSession.cancel());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        ConfigPage page = this.pageNavigator.getCurrentConfigPage();
        this.drawCenteredString(this.fontRenderer, ZeldaTargetingMod.NAME, this.width / 2, TITLE_Y, 0xFFFFFF);
        if (this.tabMode == ConfigTabLayout.Mode.SINGLE_LINE) {
            String marker = (this.pageNavigator.getCurrentPage() + 1) + " / " + ConfigPage.values().length
                + " - " + page.getTitle();
            this.drawCenteredString(this.fontRenderer, "§6" + marker, this.width / 2, TAB_Y + 2, 0xFFAA00);
        }
        this.drawCenteredString(this.fontRenderer, "§7" + page.getDescription(), this.width / 2, DESCRIPTION_Y, 0xA0A0A0);
        for (TextRow section : this.sectionLabels) {
            this.drawContentText("§8- " + section.text + " -", section.naturalY, 0x808080);
        }
        for (TextRow note : this.presetNotes) {
            this.drawContentText("§7" + note.text, note.naturalY, 0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawTooltip(mouseX, mouseY);
    }

    private void drawContentText(String text, int naturalY, int color) {
        int translatedY = this.contentViewport.translateY(naturalY, this.contentScroll);
        if (translatedY >= CONTENT_TOP && translatedY + LINE_HEIGHT <= this.contentBottom) {
            this.drawCenteredString(this.fontRenderer, text, this.width / 2, translatedY, color);
        }
    }

    private void drawTooltip(int mouseX, int mouseY) {
        GuiButton hoveredButton = null;
        for (GuiButton button : this.controls.keySet()) {
            if (button.visible && mouseX >= button.x && mouseY >= button.y
                    && mouseX < button.x + button.width && mouseY < button.y + button.height) {
                hoveredButton = button;
                break;
            }
        }
        final ConfigEntry entry = hoveredButton == null ? null : this.controls.get(hoveredButton);
        long nowMillis = System.nanoTime() / 1_000_000L;
        this.tooltipTimer.hover(entry, nowMillis);
        if (entry == null || !this.tooltipTimer.isVisible(nowMillis)) {
            return;
        }

        ConfigRect buttonRect = new ConfigRect(hoveredButton.x, hoveredButton.y, hoveredButton.width, hoveredButton.height);
        ConfigRect box = ConfigTooltipPlacement.place(this.width, this.footerY, buttonRect,
            this.columnLeft, this.columnRight, width -> this.tooltipLines(entry, width).size() * LINE_HEIGHT + 8);
        List<String> lines = this.tooltipLines(entry, box.getWidth());

        GlStateManager.disableDepth();
        this.drawGradientRect(box.getX(), box.getY(), box.getRight(), box.getBottom(), 0xF0100010, 0xF0100010);
        this.drawGradientRect(box.getX(), box.getY(), box.getRight(), box.getY() + 1, 0x505000FF, 0x505000FF);
        this.drawGradientRect(box.getX(), box.getBottom() - 1, box.getRight(), box.getBottom(), 0x5028007F, 0x5028007F);
        this.drawGradientRect(box.getX(), box.getY() + 1, box.getX() + 1, box.getBottom() - 1, 0x505000FF, 0x5028007F);
        this.drawGradientRect(box.getRight() - 1, box.getY() + 1, box.getRight(), box.getBottom() - 1, 0x505000FF, 0x5028007F);
        for (int i = 0; i < lines.size(); i++) {
            this.fontRenderer.drawStringWithShadow(lines.get(i), box.getX() + 4, box.getY() + 4 + i * LINE_HEIGHT, 0xFFFFFF);
        }
        GlStateManager.enableDepth();
    }

    private List<String> tooltipLines(ConfigEntry entry, int width) {
        ConfigEntry.Dependency blocking = ConfigDependencies.blockingDependency(entry, this.editSession.workingCopy());
        String text = blocking != null ? ConfigDependencies.blockedMessage(blocking) : entry.getHelp();
        List<String> lines = new ArrayList<String>(this.fontRenderer.listFormattedStringToWidth(text, width - 8));
        String defaultText = this.defaultText(entry);
        if (defaultText != null) {
            lines.addAll(this.fontRenderer.listFormattedStringToWidth("§7" + defaultText, width - 8));
        }
        return lines;
    }

    private String defaultText(ConfigEntry entry) {
        if (entry.getKind() == ConfigEntry.Kind.PRESET) {
            return null;
        }
        TargetingSettings defaults = TargetingPreset.fromId(this.editSession.workingCopy().lockOnPreset).createSettings();
        Object value;
        try {
            value = TargetingSettings.class.getField(entry.getFieldName()).get(defaults);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        switch (entry.getKind()) {
            case TOGGLE:
                return ConfigValueFormat.defaultLine((Boolean) value ? "ON" : "OFF");
            case CHOICE:
                return ConfigValueFormat.defaultLine(entry.choiceLabel(String.valueOf(value)));
            case COLOR:
                return ConfigValueFormat.defaultLine(ReticleColor.describe((Integer) value));
            case TEXT:
                return ConfigValueFormat.defaultLine("none");
            default:
                return ConfigValueFormat.sliderDefaultLine(
                    ((Number) value).doubleValue(), entry.getMin(), entry.getMax(), entry.getUnit());
        }
    }

    /** A line of text drawn inside the scrolling content area. */
    private static final class TextRow {
        private final String text;
        private final int naturalY;

        private TextRow(String text, int naturalY) {
            this.text = text;
            this.naturalY = naturalY;
        }
    }
}

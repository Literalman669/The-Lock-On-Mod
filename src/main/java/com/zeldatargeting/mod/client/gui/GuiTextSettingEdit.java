package com.zeldatargeting.mod.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Edits one free-text setting. Done hands the text back to the config screen,
 * which keeps it in its own Save/Cancel session; Cancel leaves it unchanged.
 */
@SideOnly(Side.CLIENT)
public class GuiTextSettingEdit extends GuiScreen {
    private static final int DONE_BUTTON = 1;
    private static final int CANCEL_BUTTON = 2;
    private static final int MAX_LENGTH = 1024;

    private final GuiScreen parentScreen;
    private final String title;
    private final String[] hints;
    private final Consumer<String> onDone;
    private String text;
    private GuiTextField textField;

    public GuiTextSettingEdit(GuiScreen parentScreen, String title, String[] hints,
                              String initialText, Consumer<String> onDone) {
        this.parentScreen = parentScreen;
        this.title = title;
        this.hints = hints == null ? new String[0] : hints;
        this.text = initialText == null ? "" : initialText;
        this.onDone = onDone;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int fieldWidth = Math.min(400, this.width - 40);
        int fieldY = 50 + this.hints.length * 12;
        this.textField = new GuiTextField(0, this.fontRenderer, (this.width - fieldWidth) / 2, fieldY, fieldWidth, 20);
        this.textField.setMaxStringLength(MAX_LENGTH);
        this.textField.setText(this.text);
        this.textField.setFocused(true);
        this.textField.setCursorPositionEnd();

        this.buttonList.add(new GuiButton(DONE_BUTTON, this.width / 2 - 104, fieldY + 32, 100, 20, "Done"));
        this.buttonList.add(new GuiButton(CANCEL_BUTTON, this.width / 2 + 4, fieldY + 32, 100, 20, "Cancel"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == DONE_BUTTON) {
            this.finish(true);
        } else if (button.id == CANCEL_BUTTON) {
            this.finish(false);
        }
    }

    private void finish(boolean keep) {
        if (keep && this.onDone != null) {
            this.onDone.accept(this.textField.getText().trim());
        }
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.finish(false);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            this.finish(true);
            return;
        }
        if (this.textField.textboxKeyTyped(typedChar, keyCode)) {
            this.text = this.textField.getText();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        this.textField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Zelda Targeting", this.width / 2, 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "§6" + this.title, this.width / 2, 24, 0xFFAA00);
        for (int i = 0; i < this.hints.length; i++) {
            this.drawCenteredString(this.fontRenderer, "§7" + this.hints[i], this.width / 2, 44 + i * 12, 0xA0A0A0);
        }
        this.textField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

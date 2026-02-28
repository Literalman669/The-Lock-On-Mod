package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiEntityBlacklistEdit extends GuiScreen {
    private final GuiScreen parentScreen;
    private GuiTextField blacklistField;
    private GuiButton doneButton;

    public GuiEntityBlacklistEdit(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int centerX = this.width / 2;
        int fieldWidth = Math.min(400, this.width - 40);

        this.blacklistField = new GuiTextField(0, this.fontRenderer, centerX - fieldWidth / 2, 60, fieldWidth, 20);
        this.blacklistField.setMaxStringLength(2048);
        this.blacklistField.setFocused(true);
        String current = TargetingConfig.entityBlacklist;
        this.blacklistField.setText(current != null ? current : "");
        this.blacklistField.setCursorPositionEnd();

        this.doneButton = new GuiButton(1, centerX - 50, this.height - 28, 100, 20, "Done");
        this.buttonList.add(this.doneButton);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            saveAndClose();
        }
    }

    private void saveAndClose() {
        String text = this.blacklistField.getText();
        TargetingConfig.entityBlacklist = text != null ? text.trim() : "";
        TargetingConfig.saveConfig();
        if (this.parentScreen instanceof GuiTargetingConfig) {
            ((GuiTargetingConfig) this.parentScreen).initGui();
        }
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            saveAndClose();
            return;
        }
        if (this.blacklistField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.blacklistField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        this.blacklistField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "§6Entity Blacklist", this.width / 2, 20, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "§7Comma-separated registry names (e.g. iceandfire:dragon, minecraft:iron_golem)", this.width / 2, 38, 0xAAAAAA);
        this.blacklistField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}

package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingConfig;
import com.zeldatargeting.mod.config.TargetingPreset;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.io.IOException;

/** Main-menu prompt for selecting a curated 1.4 configuration preset. */
@SideOnly(Side.CLIENT)
public final class GuiPresetSelection extends GuiScreen {

    private static final int CINEMATIC_BUTTON = 0;
    private static final int BALANCED_BUTTON = 1;
    private static final int SNAPPY_BUTTON = 2;
    private static final int CONTINUE_BUTTON = 3;

    private final GuiScreen parentScreen;
    private final Runnable onConfirmed;
    private final Runnable onDismissed;
    private boolean closed;

    public GuiPresetSelection(GuiScreen parentScreen, Runnable onConfirmed, Runnable onDismissed) {
        this.parentScreen = parentScreen;
        this.onConfirmed = onConfirmed;
        this.onDismissed = onDismissed;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int buttonWidth = Math.min(300, width - 40);
        int left = (width - buttonWidth) / 2;
        int top = Math.max(64, height / 2 - 80);

        buttonList.add(new GuiButton(CINEMATIC_BUTTON, left, top, buttonWidth, 20, "Cinematic"));
        buttonList.add(new GuiButton(BALANCED_BUTTON, left, top + 44, buttonWidth, 20, "Balanced (Recommended)"));
        buttonList.add(new GuiButton(SNAPPY_BUTTON, left, top + 88, buttonWidth, 20, "Snappy"));
        buttonList.add(new GuiButton(CONTINUE_BUTTON, left, top + 136, buttonWidth, 20, "Continue with Balanced for now"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int centerX = width / 2;
        int top = Math.max(64, height / 2 - 80);
        drawCenteredString(fontRenderer, "Zelda Targeting 1.4", centerX, top - 36, 0xFFFFFF);
        drawCenteredString(fontRenderer, "Choose how lock-on should feel. You can fine-tune it later.", centerX, top - 20, 0xAAAAAA);
        drawCenteredString(fontRenderer, "Smooth camera and automatic third person.", centerX, top + 23, 0xAAAAAA);
        drawCenteredString(fontRenderer, "Responsive default for most play styles.", centerX, top + 67, 0xAAAAAA);
        drawCenteredString(fontRenderer, "Direct camera response with no automatic third person.", centerX, top + 111, 0xAAAAAA);
        drawCenteredString(fontRenderer, "Esc leaves Balanced active for this session without saving.", centerX, top + 164, 0x777777);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        if (button.id == CINEMATIC_BUTTON) {
            confirm(TargetingPreset.CINEMATIC);
        } else if (button.id == BALANCED_BUTTON) {
            confirm(TargetingPreset.BALANCED);
        } else if (button.id == SNAPPY_BUTTON) {
            confirm(TargetingPreset.SNAPPY);
        } else if (button.id == CONTINUE_BUTTON) {
            dismiss();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            dismiss();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        if (!closed) {
            closed = true;
            onDismissed.run();
        }
    }

    private void confirm(TargetingPreset preset) {
        TargetingConfig.confirmPreset(preset);
        closed = true;
        onConfirmed.run();
        mc.displayGuiScreen(parentScreen);
    }

    private void dismiss() {
        TargetingConfig.previewSettings(TargetingPreset.BALANCED.createSettings());
        closed = true;
        onDismissed.run();
        mc.displayGuiScreen(parentScreen);
    }
}

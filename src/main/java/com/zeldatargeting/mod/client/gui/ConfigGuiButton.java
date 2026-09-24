package com.zeldatargeting.mod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Vanilla-looking button which can safely span a configuration page.
 *
 * <p>The stock 1.12 button uses two fixed texture slices and corrupts the
 * middle when stretched beyond its 200-pixel design width. This renderer uses
 * simple shaded rectangles instead, so label width is not limited by that
 * texture.</p>
 */
@SideOnly(Side.CLIENT)
public final class ConfigGuiButton extends GuiButton {
    public ConfigGuiButton(int buttonId, int x, int y, int width, int height, String displayString) {
        super(buttonId, x, y, width, height, displayString);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int borderColor = this.enabled ? 0xFF202020 : 0xFF1A1A1A;
        int fillColor = this.enabled ? (this.hovered ? 0xFF969696 : 0xFF7F7F7F) : 0xFF555555;
        int highlightColor = this.enabled ? (this.hovered ? 0xFFC0C0C0 : 0xFFA0A0A0) : 0xFF707070;

        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, borderColor);
        drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, fillColor);
        drawRect(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + 3, highlightColor);

        int textColor = this.enabled ? (this.hovered ? 0xFFFFFFA0 : 0xFFE0E0E0) : 0xFFA0A0A0;
        drawCenteredString(minecraft.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
    }
}

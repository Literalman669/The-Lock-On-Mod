package com.zeldatargeting.mod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.DoubleConsumer;
import java.util.function.Function;

/**
 * A drag slider for a numeric setting, drawn to match {@link ConfigGuiButton}. The
 * value snaps to the entry's step and stops at its ends. Only dragging changes the
 * setting: a value loaded from the file is shown as-is until the player moves it.
 */
@SideOnly(Side.CLIENT)
public final class ConfigSlider extends GuiButton {
    private static final int HANDLE_WIDTH = 8;

    private final ConfigEntry entry;
    private final DoubleConsumer onDrag;
    private final Function<Double, String> label;
    private double value;
    private boolean dragging;

    public ConfigSlider(int id, int x, int y, int width, ConfigEntry entry, double initialValue,
                        DoubleConsumer onDrag, Function<Double, String> label) {
        super(id, x, y, width, 20, "");
        this.entry = entry;
        this.onDrag = onDrag;
        this.label = label;
        setDisplayedValue(initialValue);
    }

    public double getValue() {
        return value;
    }

    public boolean isDragging() {
        return dragging;
    }

    /** Shows {@code newValue} without reporting a change. */
    public void setDisplayedValue(double newValue) {
        value = newValue;
        displayString = label.apply(newValue);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.mousePressed(minecraft, mouseX, mouseY)) {
            return false;
        }
        dragging = true;
        applyPosition(mouseX);
        return true;
    }

    @Override
    protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
        if (visible && dragging) {
            applyPosition(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        dragging = false;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        mouseDragged(minecraft, mouseX, mouseY);

        drawRect(x, y, x + width, y + height, enabled ? 0xFF202020 : 0xFF1A1A1A);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, enabled ? 0xFF5A5A5A : 0xFF454545);

        int handleX = x + (int) Math.round(ConfigSliderMath.fractionOf(value, entry.getMin(), entry.getMax())
            * (width - HANDLE_WIDTH));
        int handleFill = enabled ? (hovered || dragging ? 0xFF969696 : 0xFF7F7F7F) : 0xFF555555;
        int handleHighlight = enabled ? (hovered || dragging ? 0xFFC0C0C0 : 0xFFA0A0A0) : 0xFF707070;
        drawRect(handleX, y, handleX + HANDLE_WIDTH, y + height, 0xFF202020);
        drawRect(handleX + 1, y + 1, handleX + HANDLE_WIDTH - 1, y + height - 1, handleFill);
        drawRect(handleX + 1, y + 1, handleX + HANDLE_WIDTH - 1, y + 2, handleHighlight);

        int textColor = enabled ? (hovered ? 0xFFFFFFA0 : 0xFFE0E0E0) : 0xFFA0A0A0;
        drawCenteredString(minecraft.fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, textColor);
    }

    private void applyPosition(int mouseX) {
        double fraction = (mouseX - (x + HANDLE_WIDTH / 2.0D)) / (double) (width - HANDLE_WIDTH);
        double next = ConfigSliderMath.valueAt(fraction, entry.getMin(), entry.getMax(), entry.getStep());
        if (Math.abs(next - value) > 1.0E-9) {
            setDisplayedValue(next);
            onDrag.accept(next);
        }
    }
}

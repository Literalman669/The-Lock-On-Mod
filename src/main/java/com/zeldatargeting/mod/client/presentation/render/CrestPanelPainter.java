package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.client.presentation.core.PresentationStyle;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Shared pixel geometry for the regular plaque and boss banner. */
@SideOnly(Side.CLIENT)
final class CrestPanelPainter {
    static final int GOLD = 0xFFD7AE68;
    static final int GOLD_DIM = 0xFF80633C;
    static final int TEXT = 0xFFF5EBD5;
    static final int MUTED = 0xFFB8B8AD;

    private static final int SHADOW = 0x70000000;
    private static final int FRAME = 0xFF80633C;
    private static final int FILL = 0xE0121A1C;
    private static final int INNER = 0xCC202A2B;
    private static final int BAR_TRACK = 0xFF2A3230;

    private CrestPanelPainter() {
    }

    static void frame(int x, int y, int width, int height,
                      boolean boss, PresentationStyle style) {
        if (width < 12 || height < 12) {
            return;
        }
        int cut = boss ? 6 : 4;
        notched(x + 2, y + 3, width, height, cut,
            style.applyHudOpacity(SHADOW));
        notched(x, y, width, height, cut,
            style.applyHudOpacity(FRAME));
        notched(x + 1, y + 1, width - 2, height - 2, cut - 1,
            style.applyHudOpacity(FILL));
        Gui.drawRect(x + cut + 2, y + 2, x + width - cut - 2, y + 3,
            style.applyHudOpacity(boss ? GOLD : GOLD_DIM));
        Gui.drawRect(x + 5, y + height - 4, x + width - 5, y + height - 3,
            style.applyHudOpacity(INNER));
        if (boss) {
            Gui.drawRect(x + 2, y + 12, x + 4, y + height - 11,
                style.applyHudOpacity(GOLD));
            Gui.drawRect(x + width - 4, y + 12, x + width - 2, y + height - 11,
                style.applyHudOpacity(GOLD));
        }
    }

    static void diamond(int centerX, int centerY, int color) {
        Gui.drawRect(centerX, centerY - 3, centerX + 1, centerY - 2, color);
        Gui.drawRect(centerX - 1, centerY - 2, centerX + 2, centerY - 1, color);
        Gui.drawRect(centerX - 2, centerY - 1, centerX + 3, centerY + 1, color);
        Gui.drawRect(centerX - 1, centerY + 1, centerX + 2, centerY + 2, color);
        Gui.drawRect(centerX, centerY + 2, centerX + 1, centerY + 3, color);
    }

    static void healthBar(int x, int y, int width, int height,
                          float healthRatio, int healthColor,
                          boolean quarters, PresentationStyle style) {
        if (width < 4 || height < 4) {
            return;
        }
        Gui.drawRect(x, y, x + width, y + height,
            style.applyHudOpacity(GOLD_DIM));
        Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1,
            style.applyHudOpacity(BAR_TRACK));
        int innerWidth = width - 2;
        float ratio = Math.max(0.0F, Math.min(1.0F, healthRatio));
        int filled = Math.round(innerWidth * ratio);
        if (filled > 0) {
            Gui.drawRect(x + 1, y + 1, x + 1 + filled, y + height - 1,
                style.applyHudOpacity(healthColor));
            Gui.drawRect(x + 1, y + 1, x + 1 + filled, y + 2,
                style.applyHudOpacity(0x55FFFFFF));
        }
        if (quarters) {
            for (int division = 1; division < 4; division++) {
                int tickX = x + 1 + innerWidth * division / 4;
                Gui.drawRect(tickX, y + 1, tickX + 1, y + height - 1,
                    style.applyHudOpacity(0x990E1618));
            }
        }
    }

    private static void notched(int x, int y, int width, int height,
                                int cut, int color) {
        if (width <= cut * 2 || height <= cut * 2) {
            return;
        }
        Gui.drawRect(x + cut, y, x + width - cut, y + height, color);
        Gui.drawRect(x + 2, y + 2, x + width - 2, y + height - 2, color);
        Gui.drawRect(x, y + cut, x + width, y + height - cut, color);
    }
}

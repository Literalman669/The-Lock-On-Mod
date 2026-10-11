package com.zeldatargeting.mod.client.gui;

import java.util.function.IntUnaryOperator;

/**
 * Places a setting's tooltip where it does not get in the way: beside the column of
 * settings (right first, then left), lined up with the hovered row; on narrow
 * screens, just below the hovered row, or above it when below would reach the
 * footer. It never covers the hovered setting or the footer.
 */
public final class ConfigTooltipPlacement {
    public static final int GAP = 8;
    public static final int MAX_WIDTH = 200;
    public static final int MIN_WIDTH = 120;
    public static final int MARGIN = 4;
    /** Space between the hovered row and a tooltip placed below or above it. */
    private static final int ROW_GAP = 4;

    private ConfigTooltipPlacement() {
    }

    /**
     * @param heightForWidth the tooltip's height once its text is wrapped to a width
     */
    public static ConfigRect place(int screenWidth, int footerTop, ConfigRect hovered,
                                   int columnLeft, int columnRight, IntUnaryOperator heightForWidth) {
        int rightSpace = screenWidth - MARGIN - (columnRight + GAP);
        if (rightSpace >= MIN_WIDTH) {
            int width = Math.min(MAX_WIDTH, rightSpace);
            return beside(columnRight + GAP, width, footerTop, hovered, heightForWidth);
        }
        int leftSpace = columnLeft - GAP - MARGIN;
        if (leftSpace >= MIN_WIDTH) {
            int width = Math.min(MAX_WIDTH, leftSpace);
            return beside(columnLeft - GAP - width, width, footerTop, hovered, heightForWidth);
        }

        int width = Math.min(MAX_WIDTH, screenWidth - 2 * MARGIN);
        int height = heightForWidth.applyAsInt(width);
        int centered = hovered.getX() + hovered.getWidth() / 2 - width / 2;
        int x = clamp(centered, MARGIN, screenWidth - MARGIN - width);
        int below = hovered.getBottom() + ROW_GAP;
        int y = below + height <= footerTop - MARGIN
            ? below
            : Math.max(MARGIN, hovered.getY() - ROW_GAP - height);
        return new ConfigRect(x, y, width, height);
    }

    private static ConfigRect beside(int x, int width, int footerTop, ConfigRect hovered,
                                     IntUnaryOperator heightForWidth) {
        int height = heightForWidth.applyAsInt(width);
        int y = clamp(hovered.getY(), MARGIN, footerTop - MARGIN - height);
        return new ConfigRect(x, y, width, height);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

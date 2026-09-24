package com.zeldatargeting.mod.client.presentation.core;

public final class PanelLayoutCalculator {
    private PanelLayoutCalculator() {
    }

    public static PanelLayout calculate(
            int screenWidth, int screenHeight, int panelWidth, int panelHeight,
            PanelAnchor anchor, int offsetX, int offsetY, int margin) {
        int safeScreenWidth = Math.max(0, screenWidth);
        int safeScreenHeight = Math.max(0, screenHeight);
        int requestedMargin = Math.max(0, margin);
        int safeMargin = Math.min(requestedMargin, Math.min(safeScreenWidth, safeScreenHeight) / 2);
        int width = Math.min(Math.max(0, panelWidth), safeScreenWidth - safeMargin * 2);
        int height = Math.min(Math.max(0, panelHeight), safeScreenHeight - safeMargin * 2);
        int maxX = Math.max(safeMargin, safeScreenWidth - safeMargin - width);
        int maxY = Math.max(safeMargin, safeScreenHeight - safeMargin - height);
        PanelAnchor safeAnchor = anchor == null ? PanelAnchor.TOP_RIGHT : anchor;
        int baseX = isRight(safeAnchor) ? maxX : safeAnchor == PanelAnchor.CENTER
            ? (safeScreenWidth - width) / 2 : safeMargin;
        int baseY = isBottom(safeAnchor) ? maxY : safeAnchor == PanelAnchor.CENTER
            ? (safeScreenHeight - height) / 2 : safeMargin;

        return new PanelLayout(
            clamp((long) baseX + offsetX, safeMargin, maxX),
            clamp((long) baseY + offsetY, safeMargin, maxY),
            width,
            height
        );
    }

    private static boolean isRight(PanelAnchor anchor) {
        return anchor == PanelAnchor.TOP_RIGHT || anchor == PanelAnchor.BOTTOM_RIGHT;
    }

    private static boolean isBottom(PanelAnchor anchor) {
        return anchor == PanelAnchor.BOTTOM_LEFT || anchor == PanelAnchor.BOTTOM_RIGHT;
    }

    private static int clamp(long value, int minimum, int maximum) {
        return (int) Math.max(minimum, Math.min(value, maximum));
    }
}

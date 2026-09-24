package com.zeldatargeting.mod.client.presentation.core;

public enum PanelAnchor {
    TOP_RIGHT,
    TOP_LEFT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER;

    public static PanelAnchor fromConfig(String value) {
        String normalized = value == null ? "" : value.trim();
        if ("top-left".equalsIgnoreCase(normalized)) {
            return TOP_LEFT;
        }
        if ("bottom-left".equalsIgnoreCase(normalized)) {
            return BOTTOM_LEFT;
        }
        if ("bottom-right".equalsIgnoreCase(normalized)) {
            return BOTTOM_RIGHT;
        }
        if ("center".equalsIgnoreCase(normalized)) {
            return CENTER;
        }
        return TOP_RIGHT;
    }
}

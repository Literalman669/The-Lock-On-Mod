package com.zeldatargeting.mod.client.gui;

/**
 * Calculates the usable settings area between the configuration header and
 * footer. It is independent of Minecraft so every GUI scale follows the same
 * bounds.
 */
public final class ConfigContentViewport {
    private final int top;
    private final int bottom;
    private final int maxScroll;

    public ConfigContentViewport(int top, int bottom, int contentBottom) {
        if (bottom < top) {
            throw new IllegalArgumentException("bottom must not be above top");
        }
        this.top = top;
        this.bottom = bottom;
        this.maxScroll = Math.max(0, contentBottom - bottom);
    }

    public int getMaxScroll() {
        return maxScroll;
    }

    public int clampScroll(int requestedScroll) {
        return Math.max(0, Math.min(requestedScroll, maxScroll));
    }

    public int translateY(int naturalY, int scroll) {
        return naturalY - clampScroll(scroll);
    }

    public boolean isFullyVisible(int naturalY, int height, int scroll) {
        int y = translateY(naturalY, scroll);
        return y >= top && y + height <= bottom;
    }
}

package com.zeldatargeting.mod.client.gui;

/** An immutable screen rectangle in GUI pixels. */
public final class ConfigRect {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public ConfigRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRight() {
        return x + width;
    }

    public int getBottom() {
        return y + height;
    }

    /** Whether the two rectangles share any area; touching edges do not count. */
    public boolean intersects(ConfigRect other) {
        return x < other.getRight() && other.x < getRight()
            && y < other.getBottom() && other.y < getBottom();
    }
}

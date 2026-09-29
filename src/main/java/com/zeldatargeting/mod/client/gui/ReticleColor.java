package com.zeldatargeting.mod.client.gui;

import java.util.Locale;

/** The reticle colors the config screen cycles through, using Minecraft's text colors. */
public enum ReticleColor {
    WHITE("White", 0xFFFFFF, 'f'),
    GOLD("Gold", 0xFFAA00, '6'),
    YELLOW("Yellow", 0xFFFF55, 'e'),
    GREEN("Green", 0x55FF55, 'a'),
    AQUA("Aqua", 0x55FFFF, 'b'),
    BLUE("Blue", 0x5555FF, '9'),
    PINK("Pink", 0xFF55FF, 'd'),
    RED("Red", 0xFF5555, 'c');

    private final String label;
    private final int rgb;
    private final char formattingCode;

    ReticleColor(String label, int rgb, char formattingCode) {
        this.label = label;
        this.rgb = rgb;
        this.formattingCode = formattingCode;
    }

    public int getRgb() {
        return rgb;
    }

    /** The color after (or before) {@code rgb}; a custom value starts over at white. */
    public static ReticleColor cycle(int rgb, boolean reverse) {
        ReticleColor current = fromRgb(rgb);
        if (current == null) {
            return WHITE;
        }
        ReticleColor[] values = values();
        int offset = reverse ? values.length - 1 : 1;
        return values[(current.ordinal() + offset) % values.length];
    }

    /** A button label for {@code rgb}, drawn in that color. */
    public static String describe(int rgb) {
        ReticleColor color = fromRgb(rgb);
        if (color == null) {
            return String.format(Locale.ROOT, "Custom #%06X", rgb & 0xFFFFFF);
        }
        return "§" + color.formattingCode + color.label;
    }

    static ReticleColor fromRgb(int rgb) {
        for (ReticleColor color : values()) {
            if (color.rgb == (rgb & 0xFFFFFF)) {
                return color;
            }
        }
        return null;
    }
}

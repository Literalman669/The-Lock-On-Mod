package com.zeldatargeting.mod.client.gui;

import java.util.Locale;

/** Turns stored setting values into the text the configuration screen shows. */
public final class ConfigValueFormat {
    private ConfigValueFormat() {
    }

    public static String format(double value, ConfigUnit unit) {
        switch (unit) {
            case BLOCKS:
                String blocks = isWhole(value)
                    ? String.valueOf(Math.round(value))
                    : String.format(Locale.ROOT, "%.1f", value);
                return blocks + (isWhole(value) && Math.round(value) == 1 ? " block" : " blocks");
            case DEGREES:
                return Math.round(value) + "°";
            case PERCENT:
                return Math.round(value * 100.0D) + "%";
            case MULTIPLIER:
                return String.format(Locale.ROOT, "%.1fx", value);
            case PIXELS:
                return Math.round(value) + " px";
            case TICKS_AS_SECONDS:
                return String.format(Locale.ROOT, "%.1f s", value / 20.0D);
            case TICK_INTERVAL:
                long ticks = Math.round(value);
                return "every " + ticks + (ticks == 1 ? " tick" : " ticks");
            case SIGNED:
                return String.format(Locale.ROOT, "%+.2f", value);
            case NONE:
            default:
                return isWhole(value)
                    ? String.valueOf(Math.round(value))
                    : String.format(Locale.ROOT, "%.2f", value);
        }
    }

    public static String defaultLine(String defaultText) {
        return "Default: " + defaultText;
    }

    public static String sliderDefaultLine(double defaultValue, double min, double max, ConfigUnit unit) {
        return defaultLine(format(defaultValue, unit))
            + " - Range: " + format(min, unit) + " to " + format(max, unit);
    }

    private static boolean isWhole(double value) {
        return Math.abs(value - Math.rint(value)) < 0.0001D;
    }
}

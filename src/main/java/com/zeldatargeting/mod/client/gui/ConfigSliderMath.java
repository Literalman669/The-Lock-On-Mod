package com.zeldatargeting.mod.client.gui;

/** Maps between a slider's handle position (0 to 1) and a snapped setting value. */
public final class ConfigSliderMath {
    private ConfigSliderMath() {
    }

    /**
     * The value at {@code fraction} along the track, snapped to a whole multiple of
     * {@code step} (so 0.40 stays reachable on a 0.01-1.0 track) and kept in range.
     */
    public static double valueAt(double fraction, double min, double max, double step) {
        double position = Math.max(0.0D, Math.min(1.0D, fraction));
        double raw = min + position * (max - min);
        double snapped = step > 0.0D ? Math.round(raw / step) * step : raw;
        double clamped = Math.max(min, Math.min(max, snapped));
        return Math.round(clamped * 1000.0D) / 1000.0D;
    }

    /** Where {@code value} sits along the track, from 0 to 1. Used for drawing only. */
    public static double fractionOf(double value, double min, double max) {
        if (max <= min) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, (value - min) / (max - min)));
    }
}

package com.zeldatargeting.mod.client.gui;

/**
 * Decides when a setting's tooltip shows: only after the mouse rests on it, never
 * while moving across settings, and not again on a setting the player just clicked
 * or dragged until the mouse leaves it. Call {@link #hover} every frame.
 */
public final class ConfigTooltipTimer {
    public static final long REST_DELAY_MILLIS = 600L;

    private Object target;
    private long enteredAtMillis;
    private Object suppressed;

    /** @param hovered the setting under the mouse this frame, or null */
    public void hover(Object hovered, long nowMillis) {
        if (hovered == target) {
            return;
        }
        target = hovered;
        enteredAtMillis = nowMillis;
        if (hovered != suppressed) {
            suppressed = null;
        }
    }

    /** A click, drag, scroll, or page change: hide now, and keep the current setting quiet until left. */
    public void interact() {
        // A second interaction in a row (a slider press is a click and then a drag) keeps
        // the first one's suppression instead of clearing it.
        if (target != null) {
            suppressed = target;
        }
        // Moving onto another setting after this (such as on a new page) restarts the full delay.
        target = null;
    }

    public boolean isVisible(long nowMillis) {
        return target != null
            && target != suppressed
            && nowMillis - enteredAtMillis >= REST_DELAY_MILLIS;
    }

    public Object getTarget() {
        return target;
    }
}

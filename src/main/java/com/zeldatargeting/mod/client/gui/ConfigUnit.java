package com.zeldatargeting.mod.client.gui;

/** How a numeric setting is shown to the player. See {@link ConfigValueFormat}. */
public enum ConfigUnit {
    NONE,
    BLOCKS,
    DEGREES,
    /** A 0-1 fraction shown as a whole percentage. */
    PERCENT,
    MULTIPLIER,
    PIXELS,
    /** Game ticks shown as seconds (20 ticks per second). */
    TICKS_AS_SECONDS,
    /** Game ticks between checks, shown as "every N ticks". */
    TICK_INTERVAL,
    SIGNED
}

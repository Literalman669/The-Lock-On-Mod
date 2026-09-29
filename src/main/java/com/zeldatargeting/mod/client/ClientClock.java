package com.zeldatargeting.mod.client;

/**
 * Monotonic millisecond clock for lock-on timing and camera smoothing.
 *
 * <p>{@link System#currentTimeMillis()} follows the wall clock: it can step when the
 * system time changes and on some Windows setups only advances every ~15.6 ms, so
 * frames read as 0 ms and then as a double step. {@link System#nanoTime()} is
 * steady and fine-grained. Its origin is arbitrary (values may be negative), so only
 * differences between two readings are meaningful.</p>
 */
public final class ClientClock {
    private ClientClock() {
    }

    public static long nowMillis() {
        return System.nanoTime() / 1_000_000L;
    }
}

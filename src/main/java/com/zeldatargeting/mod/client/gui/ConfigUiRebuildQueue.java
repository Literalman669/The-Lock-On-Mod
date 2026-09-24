package com.zeldatargeting.mod.client.gui;

/**
 * Defers configuration control rebuilding until the next client UI tick.
 *
 * <p>Minecraft processes a click against the current button list. Replacing
 * that list inside the click handler can let the same event reach a newly
 * created button, so requests are coalesced and consumed after the event.</p>
 */
public final class ConfigUiRebuildQueue {
    private boolean rebuildPending;
    private boolean scrollResetPending;

    public void requestRebuild(boolean resetScroll) {
        rebuildPending = true;
        scrollResetPending |= resetScroll;
    }

    public boolean consumeRebuildRequest() {
        boolean requested = rebuildPending;
        rebuildPending = false;
        return requested;
    }

    public boolean consumeScrollResetRequest() {
        boolean requested = scrollResetPending;
        scrollResetPending = false;
        return requested;
    }
}

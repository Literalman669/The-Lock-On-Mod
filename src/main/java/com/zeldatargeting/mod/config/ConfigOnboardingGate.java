package com.zeldatargeting.mod.config;

/** Controls one main-menu preset prompt for the current client session. */
public final class ConfigOnboardingGate {

    private boolean opened;
    private boolean dismissed;
    private boolean confirmed;

    public boolean shouldOpen(boolean selectionRequired, boolean mainMenuVisible, boolean competingScreenOpen) {
        return selectionRequired
                && mainMenuVisible
                && !competingScreenOpen
                && !opened
                && !dismissed
                && !confirmed;
    }

    public void markOpened() {
        opened = true;
    }

    public void dismiss() {
        dismissed = true;
    }

    public void confirm() {
        confirmed = true;
    }
}

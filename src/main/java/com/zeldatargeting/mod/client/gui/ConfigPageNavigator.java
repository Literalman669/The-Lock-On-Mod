package com.zeldatargeting.mod.client.gui;

/**
 * Holds a requested configuration page until the current mouse event has
 * finished. This prevents Minecraft from processing a newly-created Next
 * button a second time during the same click.
 */
public final class ConfigPageNavigator {
    private final int totalPages;
    private int currentPage;
    private int pendingPage;

    public ConfigPageNavigator() {
        this(ConfigPage.values().length);
    }

    public ConfigPageNavigator(int totalPages) {
        if (totalPages < 1) {
            throw new IllegalArgumentException("totalPages must be positive");
        }
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public ConfigPage getCurrentConfigPage() {
        return ConfigPage.values()[currentPage];
    }

    public void requestNext() {
        pendingPage = Math.min(currentPage + 1, totalPages - 1);
    }

    public void requestPrevious() {
        pendingPage = Math.max(currentPage - 1, 0);
    }

    public boolean applyPendingPage() {
        if (pendingPage == currentPage) {
            return false;
        }
        currentPage = pendingPage;
        return true;
    }
}

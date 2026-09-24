package com.zeldatargeting.mod.client.presentation.core;

public final class PresentationFeedbackEvent {
    public static final int NONE = 0;
    public static final int LOCK = 1;
    public static final int SWITCH = 1 << 1;
    public static final int LOST = 1 << 2;
    public static final int LOW_HEALTH = 1 << 3;
    public static final int CRITICAL = 1 << 4;
    public static final int LETHAL = 1 << 5;

    private PresentationFeedbackEvent() {
    }
}

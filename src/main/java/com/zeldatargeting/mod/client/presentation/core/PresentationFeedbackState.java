package com.zeldatargeting.mod.client.presentation.core;

import com.zeldatargeting.mod.client.session.LockPhase;

public final class PresentationFeedbackState {
    private long lastTransitionId = Long.MIN_VALUE;
    private PresentationStatus lastStatus = PresentationStatus.NORMAL;

    public int advance(long transitionId, LockPhase phase, PresentationStatus status) {
        int events = PresentationFeedbackEvent.NONE;
        LockPhase safePhase = phase == null ? LockPhase.IDLE : phase;
        PresentationStatus safeStatus = status == null ? PresentationStatus.NORMAL : status;

        if (transitionId != lastTransitionId) {
            if (safePhase == LockPhase.ACQUIRING) {
                events |= PresentationFeedbackEvent.LOCK;
            } else if (safePhase == LockPhase.SWITCHING) {
                events |= PresentationFeedbackEvent.SWITCH;
            } else if (safePhase == LockPhase.RELEASING) {
                events |= PresentationFeedbackEvent.LOST;
            }
            lastTransitionId = transitionId;
        }
        if (safeStatus == PresentationStatus.LOW_HEALTH && lastStatus != PresentationStatus.LOW_HEALTH) {
            events |= PresentationFeedbackEvent.LOW_HEALTH;
        }
        lastStatus = safeStatus;
        return events;
    }

    public int damage(boolean critical, boolean lethal) {
        return (critical ? PresentationFeedbackEvent.CRITICAL : PresentationFeedbackEvent.NONE)
            | (lethal ? PresentationFeedbackEvent.LETHAL : PresentationFeedbackEvent.NONE);
    }

    public void clear() {
        lastTransitionId = Long.MIN_VALUE;
        lastStatus = PresentationStatus.NORMAL;
    }
}

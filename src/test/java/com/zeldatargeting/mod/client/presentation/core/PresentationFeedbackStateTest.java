package com.zeldatargeting.mod.client.presentation.core;

import com.zeldatargeting.mod.client.session.LockPhase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PresentationFeedbackStateTest {
    @Test
    public void stableFramesDoNotReplayFeedbackButNewTransitionsDo() {
        PresentationFeedbackState state = new PresentationFeedbackState();

        assertEquals(PresentationFeedbackEvent.LOCK,
            state.advance(4L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
        assertEquals(PresentationFeedbackEvent.NONE,
            state.advance(4L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
        assertEquals(PresentationFeedbackEvent.SWITCH,
            state.advance(5L, LockPhase.SWITCHING, PresentationStatus.NORMAL));
        assertEquals(PresentationFeedbackEvent.LOST,
            state.advance(6L, LockPhase.RELEASING, PresentationStatus.NORMAL));
    }

    @Test
    public void lowHealthAndDamageEventsOnlyFireOnTheirEnteringEdge() {
        PresentationFeedbackState state = new PresentationFeedbackState();

        assertEquals(PresentationFeedbackEvent.LOW_HEALTH,
            state.advance(6L, LockPhase.LOCKED, PresentationStatus.LOW_HEALTH));
        assertEquals(PresentationFeedbackEvent.NONE,
            state.advance(6L, LockPhase.LOCKED, PresentationStatus.LOW_HEALTH));
        assertEquals(PresentationFeedbackEvent.CRITICAL | PresentationFeedbackEvent.LETHAL,
            state.damage(true, true));
    }

    @Test
    public void clearAllowsTheNextLockSessionToEmitItsInitialLockEvent() {
        PresentationFeedbackState state = new PresentationFeedbackState();

        assertEquals(PresentationFeedbackEvent.LOCK,
            state.advance(9L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
        state.clear();
        assertEquals(PresentationFeedbackEvent.LOCK,
            state.advance(9L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
    }
}

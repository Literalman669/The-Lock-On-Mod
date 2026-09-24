package com.zeldatargeting.mod.client.presentation;

import com.zeldatargeting.mod.client.presentation.core.PresentationFeedbackEvent;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.session.LockPhase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PresentationFeedbackControllerTest {
    @Test
    public void visualFeedbackIsConsumedOnceAfterStatusAndDamageEdges() {
        PresentationFeedbackController controller = new PresentationFeedbackController();

        controller.onTransition(snapshot(8L, LockPhase.ACQUIRING));
        controller.onPresentationStatus(8L, PresentationStatus.LOW_HEALTH);
        controller.onDamage(true, true);

        assertEquals(PresentationFeedbackEvent.LOW_HEALTH
                | PresentationFeedbackEvent.CRITICAL
                | PresentationFeedbackEvent.LETHAL,
            controller.consumeVisualEvents());
        assertEquals(PresentationFeedbackEvent.NONE, controller.consumeVisualEvents());
    }

    private static TargetPresentationSnapshot snapshot(long transitionId, LockPhase phase) {
        return new TargetPresentationSnapshot(
            null, "", 0.0D, 0.0D, 0.0D, 1.0F, 1.0F, 20.0F, 20.0F,
            1.0F, 1.0D, 1.0F, 2, "", false, transitionId, phase, PresentationStatus.NORMAL
        );
    }
}

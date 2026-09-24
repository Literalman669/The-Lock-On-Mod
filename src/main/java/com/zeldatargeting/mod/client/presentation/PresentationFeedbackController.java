package com.zeldatargeting.mod.client.presentation;

import com.zeldatargeting.mod.client.audio.TargetingSounds;
import com.zeldatargeting.mod.client.presentation.core.PresentationFeedbackEvent;
import com.zeldatargeting.mod.client.presentation.core.PresentationFeedbackState;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.session.LockPhase;
import net.minecraft.entity.EntityLivingBase;

public final class PresentationFeedbackController {
    private final PresentationFeedbackState state = new PresentationFeedbackState();
    private int pendingVisualEvents;

    public void onTransition(TargetPresentationSnapshot snapshot) {
        onTransition(snapshot, true);
    }

    public void onTransition(TargetPresentationSnapshot snapshot, boolean playLostSound) {
        if (snapshot == null) {
            return;
        }
        int events = state.advance(
            snapshot.getTransitionId(), snapshot.getLockPhase(), PresentationStatus.NORMAL
        );
        EntityLivingBase target = snapshot.getTarget();
        if ((events & PresentationFeedbackEvent.LOCK) != 0 && target != null) {
            TargetingSounds.playTargetLockSound(target);
        }
        if ((events & PresentationFeedbackEvent.SWITCH) != 0 && target != null) {
            TargetingSounds.playTargetSwitchSound();
        }
        if ((events & PresentationFeedbackEvent.LOST) != 0 && playLostSound) {
            TargetingSounds.playTargetLostSound();
        }
    }

    public void onPresentationStatus(long transitionId, PresentationStatus status) {
        pendingVisualEvents |= state.advance(transitionId, LockPhase.LOCKED, status)
            & PresentationFeedbackEvent.LOW_HEALTH;
    }

    public void onDamage(boolean critical, boolean lethal) {
        pendingVisualEvents |= state.damage(critical, lethal);
    }

    public int consumeVisualEvents() {
        int events = pendingVisualEvents;
        pendingVisualEvents = PresentationFeedbackEvent.NONE;
        return events;
    }

    public void clear() {
        state.clear();
        pendingVisualEvents = PresentationFeedbackEvent.NONE;
    }
}

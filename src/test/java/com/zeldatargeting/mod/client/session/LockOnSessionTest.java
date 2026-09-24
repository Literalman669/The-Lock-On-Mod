package com.zeldatargeting.mod.client.session;

import com.zeldatargeting.mod.client.targeting.core.TargetAnchor;
import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPoint;
import com.zeldatargeting.mod.client.targeting.core.TargetPriority;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LockOnSessionTest {
    @Test
    public void acquisitionRetainsPriorityAndProfileAcrossIncreasingTransitions() {
        LockOnSession<String> session = new LockOnSession<>();

        assertTrue(session.beginAcquire(
            observation("pig", 4, true, TargetAnchor.HEAD),
            TargetPriority.HEALTH,
            "CINEMATIC",
            100L
        ));
        LockOnSnapshot<String> acquiring = session.snapshot();
        assertEquals(LockPhase.ACQUIRING, acquiring.getPhase());
        assertEquals(1L, acquiring.getTransitionId());
        assertEquals(100L, acquiring.getTransitionStartedAtMillis());
        assertEquals(TargetPriority.HEALTH, acquiring.getPriority());
        assertEquals("cinematic", acquiring.getCameraProfileName());

        assertTrue(session.completeAcquire(120L));
        LockOnSnapshot<String> locked = session.snapshot();
        assertEquals(LockPhase.LOCKED, locked.getPhase());
        assertTrue(locked.getTransitionId() > acquiring.getTransitionId());
        assertEquals(120L, locked.getTransitionStartedAtMillis());
        assertEquals(TargetPriority.HEALTH, locked.getPriority());
        assertEquals("cinematic", locked.getCameraProfileName());
        assertTrue(locked.isTracking());

        assertTrue(session.beginSwitch(
            observation("cow", 5, true, TargetAnchor.CENTER),
            TargetPriority.NEAREST,
            130L
        ));
        assertEquals("cinematic", session.snapshot().getCameraProfileName());
    }

    @Test
    public void invalidTransitionsLeaveIdleSnapshotUnchanged() {
        LockOnSession<String> session = new LockOnSession<>();
        LockOnSnapshot<String> before = session.snapshot();

        assertFalse(session.completeAcquire(10L));
        assertFalse(session.beginSwitch(observation("pig", 4, true, TargetAnchor.HEAD), TargetPriority.ANGLE, 11L));
        assertFalse(session.completeSwitch(12L));
        assertFalse(session.beginRelease(LockReleaseReason.MANUAL, 13L));

        LockOnSnapshot<String> after = session.snapshot();
        assertEquals(before.getPhase(), after.getPhase());
        assertEquals(before.getTransitionId(), after.getTransitionId());
        assertEquals(before.getTransitionStartedAtMillis(), after.getTransitionStartedAtMillis());
        assertEquals(before.getReleaseReason(), after.getReleaseReason());
        assertNull(after.getTarget());
    }

    @Test
    public void switchingReplacesTheTargetOnceAndReturnsToLocked() {
        LockOnSession<String> session = lockedSession("pig", 4);
        long beforeSwitch = session.snapshot().getTransitionId();

        assertTrue(session.beginSwitch(observation("zombie", 9, true, TargetAnchor.HEAD), TargetPriority.THREAT, 30L));
        LockOnSnapshot<String> switching = session.snapshot();
        assertEquals(LockPhase.SWITCHING, switching.getPhase());
        assertEquals(9, switching.getTarget().getEntityId());
        assertEquals(TargetPriority.THREAT, switching.getPriority());
        assertTrue(switching.getTransitionId() > beforeSwitch);

        assertTrue(session.completeSwitch(40L));
        LockOnSnapshot<String> locked = session.snapshot();
        assertEquals(LockPhase.LOCKED, locked.getPhase());
        assertEquals(9, locked.getTarget().getEntityId());
        assertEquals(switching.getTransitionId() + 1L, locked.getTransitionId());
    }

    @Test
    public void invisibleTargetBeginsOcclusionGraceAtZeroDuration() {
        LockOnSession<String> session = lockedSession("pig", 4);

        assertTrue(session.updateObservation(observation("pig", 4, false, TargetAnchor.HEAD), 100L, 750L));

        LockOnSnapshot<String> snapshot = session.snapshot();
        assertEquals(LockPhase.OCCLUDED_GRACE, snapshot.getPhase());
        assertEquals(0L, snapshot.getOcclusionDurationMillis());
        assertFalse(snapshot.getTarget().isVisible());
    }

    @Test
    public void visibilityRecoveryReturnsDirectlyToLocked() {
        LockOnSession<String> session = lockedSession("pig", 4);
        session.updateObservation(observation("pig", 4, false, TargetAnchor.HEAD), 100L, 750L);

        assertTrue(session.updateObservation(observation("pig", 4, true, TargetAnchor.HEAD), 700L, 750L));

        LockOnSnapshot<String> snapshot = session.snapshot();
        assertEquals(LockPhase.LOCKED, snapshot.getPhase());
        assertTrue(snapshot.getTarget().isVisible());
        assertEquals(0L, snapshot.getOcclusionDurationMillis());
    }

    @Test
    public void graceExpiryReleasesDetachedPresentationData() {
        LockOnSession<String> session = lockedSession("pig", 4);
        session.updateObservation(observation("pig", 4, false, TargetAnchor.HEAD), 100L, 750L);
        assertTrue(session.updateObservation(observation("pig", 4, false, TargetAnchor.HEAD), 850L, 750L));

        LockOnSnapshot<String> snapshot = session.snapshot();
        assertEquals(LockPhase.RELEASING, snapshot.getPhase());
        assertEquals(LockReleaseReason.OCCLUDED, snapshot.getReleaseReason());
        assertNull(snapshot.getTarget().getReference());
        assertEquals(4, snapshot.getTarget().getEntityId());
        assertEquals("pig", snapshot.getTarget().getName());
        assertEquals(8.0F, snapshot.getTarget().getHealth(), 0.0001F);
        assertTrue(snapshot.isPresentationVisible());
    }

    @Test
    public void manualReleaseKeepsPresentationUntilFadeBoundary() {
        LockOnSession<String> session = lockedSession("pig", 4);
        assertTrue(session.beginRelease(LockReleaseReason.MANUAL, 100L));

        assertFalse(session.advanceRelease(299L, 200L));
        assertEquals(LockPhase.RELEASING, session.snapshot().getPhase());
        assertTrue(session.snapshot().isPresentationVisible());

        assertTrue(session.advanceRelease(300L, 200L));
        LockOnSnapshot<String> idle = session.snapshot();
        assertEquals(LockPhase.IDLE, idle.getPhase());
        assertNull(idle.getTarget());
        assertEquals(LockReleaseReason.MANUAL, idle.getReleaseReason());
        assertEquals("balanced", idle.getCameraProfileName());
    }

    @Test
    public void anchorChangeUpdatesTargetWithoutLeavingLocked() {
        LockOnSession<String> session = lockedSession("pig", 4);
        long before = session.snapshot().getTransitionId();

        assertTrue(session.changeAnchor(observation("pig", 4, true, TargetAnchor.CENTER), 50L));

        LockOnSnapshot<String> snapshot = session.snapshot();
        assertEquals(LockPhase.LOCKED, snapshot.getPhase());
        assertEquals(TargetAnchor.CENTER, snapshot.getTarget().getAnchor());
        assertEquals(before + 1L, snapshot.getTransitionId());
        assertEquals(50L, snapshot.getTransitionStartedAtMillis());
    }

    @Test
    public void lifecycleClearImmediatelyRemovesTarget() {
        LockOnSession<String> session = lockedSession("pig", 4);

        session.clear(LockReleaseReason.PLAYER_DEAD, 80L);

        LockOnSnapshot<String> snapshot = session.snapshot();
        assertEquals(LockPhase.IDLE, snapshot.getPhase());
        assertNull(snapshot.getTarget());
        assertEquals(LockReleaseReason.PLAYER_DEAD, snapshot.getReleaseReason());
        assertEquals("balanced", snapshot.getCameraProfileName());
        assertFalse(snapshot.isPresentationVisible());
    }

    private static LockOnSession<String> lockedSession(String reference, int entityId) {
        LockOnSession<String> session = new LockOnSession<>();
        session.beginAcquire(
            observation(reference, entityId, true, TargetAnchor.HEAD),
            TargetPriority.NEAREST,
            "balanced",
            10L
        );
        session.completeAcquire(20L);
        return session;
    }

    private static TargetObservation<String> observation(
            String reference,
            int entityId,
            boolean visible,
            TargetAnchor anchor) {
        return new TargetObservation<>(
            reference, entityId, reference, 8.0F, 10.0F, true, true, true,
            6.0D, 90.0D, visible, anchor,
            new TargetPoint(1.0D, 2.0D, 3.0D), 0.9D, 1.0D
        );
    }
}

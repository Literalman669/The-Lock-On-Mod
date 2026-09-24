package com.zeldatargeting.mod.client.targeting.core;

import com.zeldatargeting.mod.client.session.LockOnSession;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;
import com.zeldatargeting.mod.client.session.LockPhase;
import com.zeldatargeting.mod.client.session.LockReleaseReason;

import java.util.ArrayList;

public final class TargetingService<T> {
    private final TargetProvider<T> provider;
    private final LockOnSession<T> session;
    private final TargetHistory history;
    private final ArrayList<TargetCandidate<T>> candidateBuffer = new ArrayList<>(16);
    private long lastCycleMillis = Long.MIN_VALUE;

    public TargetingService(
            TargetProvider<T> provider,
            LockOnSession<T> session,
            TargetHistory history) {
        if (provider == null || session == null || history == null) {
            throw new IllegalArgumentException("provider, session, and history must not be null");
        }
        this.provider = provider;
        this.session = session;
        this.history = history;
    }

    public LockOnSnapshot<T> snapshot() {
        return session.snapshot();
    }

    public LockOnSnapshot<T> acquire(long nowMillis, TargetingOptions options) {
        requireOptions(options);
        if (session.snapshot().getPhase() != LockPhase.IDLE) {
            return session.snapshot();
        }

        collectCandidates(true);
        TargetCandidate<T> winner = TargetScorer.selectBest(candidateBuffer, options.getPriority());
        if (winner != null) {
            session.beginAcquire(
                winner.getObservation(),
                options.getPriority(),
                options.getCameraProfileName(),
                nowMillis
            );
        }
        return session.snapshot();
    }

    public LockOnSnapshot<T> cycle(
            boolean forward,
            long nowMillis,
            TargetingOptions options) {
        requireOptions(options);
        LockOnSnapshot<T> current = session.snapshot();
        if (!current.isTracking()
                || current.getTarget() == null
                || !TargetCycleRules.isCooldownElapsed(
                    lastCycleMillis,
                    nowMillis,
                    options.getCycleCooldownMillis())) {
            return current;
        }

        collectCandidates(false);
        TargetCycleRules.sortByBearing(candidateBuffer);
        TargetCandidate<T> adjacent = TargetCycleRules.selectAdjacent(
            candidateBuffer,
            current.getTarget().getEntityId(),
            forward
        );
        if (adjacent == null || adjacent.getEntityId() == current.getTarget().getEntityId()) {
            return session.snapshot();
        }

        history.record(current.getTarget().getEntityId());
        if (session.beginSwitch(adjacent.getObservation(), options.getPriority(), nowMillis)) {
            lastCycleMillis = nowMillis;
        }
        return session.snapshot();
    }

    public LockOnSnapshot<T> cycleAnchor(boolean forward, long nowMillis) {
        LockOnSnapshot<T> current = session.snapshot();
        if (!current.isTracking()
                || current.getTarget() == null
                || current.getTarget().getReference() == null) {
            return current;
        }

        TargetAnchor preferredAnchor = current.getTarget().getAnchor().cycle(forward);
        TargetObservation<T> observed = provider.observe(
            current.getTarget().getReference(),
            preferredAnchor
        );
        if (observed != null) {
            session.changeAnchor(observed, nowMillis);
        }
        return session.snapshot();
    }

    public LockOnSnapshot<T> tick(long nowMillis, TargetingOptions options) {
        requireOptions(options);
        LockOnSnapshot<T> current = session.snapshot();
        if (current.getPhase() == LockPhase.ACQUIRING) {
            session.completeAcquire(nowMillis);
            current = session.snapshot();
        } else if (current.getPhase() == LockPhase.SWITCHING) {
            session.completeSwitch(nowMillis);
            current = session.snapshot();
        } else if (current.getPhase() == LockPhase.RELEASING) {
            session.advanceRelease(nowMillis, options.getReleaseFadeMillis());
            return session.snapshot();
        }

        if (!current.isTracking()
                || current.getTarget() == null
                || current.getTarget().getReference() == null) {
            return current;
        }

        TargetObservation<T> observed = provider.observe(
            current.getTarget().getReference(),
            current.getTarget().getAnchor()
        );
        if (observed == null || !observed.isPresent()) {
            session.beginRelease(LockReleaseReason.TARGET_REMOVED, nowMillis);
            return session.snapshot();
        }
        if (!observed.isSameDimension()) {
            return clear(LockReleaseReason.DIMENSION_CHANGED, nowMillis);
        }
        if (!observed.isAlive()) {
            return handleDeadTarget(observed, nowMillis, options);
        }
        if (observed.getDistance() > options.getMaxTrackingDistance()) {
            session.beginRelease(LockReleaseReason.OUT_OF_RANGE, nowMillis);
            return session.snapshot();
        }

        session.updateObservation(observed, nowMillis, options.getOcclusionGraceMillis());
        return session.snapshot();
    }

    public LockOnSnapshot<T> release(LockReleaseReason reason, long nowMillis) {
        session.beginRelease(reason, nowMillis);
        return session.snapshot();
    }

    public LockOnSnapshot<T> clear(LockReleaseReason reason, long nowMillis) {
        session.clear(reason, nowMillis);
        history.clear();
        lastCycleMillis = Long.MIN_VALUE;
        return session.snapshot();
    }

    public TargetHistory getHistory() {
        return history;
    }

    private LockOnSnapshot<T> handleDeadTarget(
            TargetObservation<T> observed,
            long nowMillis,
            TargetingOptions options) {
        int droppedEntityId = observed.getEntityId();
        if (options.isQuickSwitchEnabled()) {
            collectCandidates(false);
            TargetCandidate<T> replacement = TargetCycleRules.selectAutomaticReplacement(
                candidateBuffer,
                options.getPriority(),
                droppedEntityId
            );
            if (replacement != null && replacement.getEntityId() != droppedEntityId) {
                history.record(droppedEntityId);
                session.beginSwitch(replacement.getObservation(), options.getPriority(), nowMillis);
                return session.snapshot();
            }
            session.beginRelease(LockReleaseReason.QUICK_SWITCH_FAILED, nowMillis);
            return session.snapshot();
        }

        session.beginRelease(LockReleaseReason.TARGET_DEAD, nowMillis);
        return session.snapshot();
    }

    private void collectCandidates(boolean acquisitionCone) {
        candidateBuffer.clear();
        provider.collectCandidates(candidateBuffer, acquisitionCone);
    }

    private static void requireOptions(TargetingOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
    }
}

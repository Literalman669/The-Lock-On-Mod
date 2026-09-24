package com.zeldatargeting.mod.client.session;

import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPriority;

import java.util.Locale;

public final class LockOnSession<T> {
    private LockPhase phase = LockPhase.IDLE;
    private long transitionId;
    private long transitionStartedAtMillis;
    private TargetObservation<T> target;
    private long occlusionDurationMillis;
    private LockReleaseReason releaseReason = LockReleaseReason.NONE;
    private TargetPriority priority = TargetPriority.NEAREST;
    private String cameraProfileName = "balanced";
    private long occlusionStartedAtMillis = Long.MIN_VALUE;

    public boolean beginAcquire(
            TargetObservation<T> target,
            TargetPriority priority,
            String cameraProfileName,
            long nowMillis) {
        if (phase != LockPhase.IDLE || !hasReference(target)) {
            return false;
        }
        this.target = target;
        this.priority = normalizePriority(priority);
        this.cameraProfileName = normalizeCameraProfileName(cameraProfileName);
        resetOcclusion();
        releaseReason = LockReleaseReason.NONE;
        transitionTo(LockPhase.ACQUIRING, nowMillis);
        return true;
    }

    public boolean completeAcquire(long nowMillis) {
        if (phase != LockPhase.ACQUIRING || target == null) {
            return false;
        }
        transitionTo(LockPhase.LOCKED, nowMillis);
        return true;
    }

    public boolean beginSwitch(
            TargetObservation<T> target,
            TargetPriority priority,
            long nowMillis) {
        if (!isTrackingPhase() || !hasReference(target)) {
            return false;
        }
        this.target = target;
        this.priority = normalizePriority(priority);
        resetOcclusion();
        releaseReason = LockReleaseReason.NONE;
        transitionTo(LockPhase.SWITCHING, nowMillis);
        return true;
    }

    public boolean completeSwitch(long nowMillis) {
        if (phase != LockPhase.SWITCHING || target == null) {
            return false;
        }
        transitionTo(LockPhase.LOCKED, nowMillis);
        return true;
    }

    public boolean updateObservation(
            TargetObservation<T> target,
            long nowMillis,
            long occlusionGraceMillis) {
        if (target == null
                || this.target == null
                || target.getEntityId() != this.target.getEntityId()
                || (phase != LockPhase.LOCKED && phase != LockPhase.OCCLUDED_GRACE)) {
            return false;
        }

        this.target = target;
        if (phase == LockPhase.LOCKED) {
            if (target.isVisible()) {
                resetOcclusion();
            } else {
                occlusionStartedAtMillis = nowMillis;
                occlusionDurationMillis = 0L;
                transitionTo(LockPhase.OCCLUDED_GRACE, nowMillis);
            }
            return true;
        }

        if (target.isVisible()) {
            resetOcclusion();
            transitionTo(LockPhase.LOCKED, nowMillis);
            return true;
        }

        occlusionDurationMillis = Math.max(0L, nowMillis - occlusionStartedAtMillis);
        if (occlusionDurationMillis >= Math.max(0L, occlusionGraceMillis)) {
            return beginRelease(LockReleaseReason.OCCLUDED, nowMillis);
        }
        return true;
    }

    public boolean changeAnchor(TargetObservation<T> target, long nowMillis) {
        if (!isTrackingPhase()
                || target == null
                || this.target == null
                || target.getEntityId() != this.target.getEntityId()
                || target.getAnchor() == this.target.getAnchor()) {
            return false;
        }
        this.target = target;
        markTransition(nowMillis);
        return true;
    }

    public boolean beginRelease(LockReleaseReason reason, long nowMillis) {
        if (!isTrackingPhase() || target == null) {
            return false;
        }
        target = target.withoutReference();
        releaseReason = normalizeReleaseReason(reason);
        transitionTo(LockPhase.RELEASING, nowMillis);
        return true;
    }

    public boolean advanceRelease(long nowMillis, long releaseFadeMillis) {
        if (phase != LockPhase.RELEASING) {
            return false;
        }
        long elapsed = Math.max(0L, nowMillis - transitionStartedAtMillis);
        if (elapsed < Math.max(0L, releaseFadeMillis)) {
            return false;
        }
        target = null;
        cameraProfileName = "balanced";
        resetOcclusion();
        transitionTo(LockPhase.IDLE, nowMillis);
        return true;
    }

    public void clear(LockReleaseReason reason, long nowMillis) {
        boolean hadSession = phase != LockPhase.IDLE || target != null;
        target = null;
        cameraProfileName = "balanced";
        resetOcclusion();
        releaseReason = normalizeReleaseReason(reason);
        if (hadSession) {
            transitionTo(LockPhase.IDLE, nowMillis);
        }
    }

    public LockOnSnapshot<T> snapshot() {
        return new LockOnSnapshot<>(
            phase,
            transitionId,
            transitionStartedAtMillis,
            target,
            occlusionDurationMillis,
            releaseReason,
            priority,
            cameraProfileName
        );
    }

    private boolean isTrackingPhase() {
        return phase == LockPhase.ACQUIRING
            || phase == LockPhase.LOCKED
            || phase == LockPhase.OCCLUDED_GRACE
            || phase == LockPhase.SWITCHING;
    }

    private static boolean hasReference(TargetObservation<?> target) {
        return target != null && target.getReference() != null;
    }

    private static TargetPriority normalizePriority(TargetPriority priority) {
        return priority == null ? TargetPriority.NEAREST : priority;
    }

    private static String normalizeCameraProfileName(String value) {
        if (value == null) {
            return "balanced";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("cinematic".equals(normalized) || "snappy".equals(normalized)) {
            return normalized;
        }
        return "balanced";
    }

    private static LockReleaseReason normalizeReleaseReason(LockReleaseReason reason) {
        return reason == null ? LockReleaseReason.TARGET_UNAVAILABLE : reason;
    }

    private void resetOcclusion() {
        occlusionStartedAtMillis = Long.MIN_VALUE;
        occlusionDurationMillis = 0L;
    }

    private void transitionTo(LockPhase phase, long nowMillis) {
        this.phase = phase;
        markTransition(nowMillis);
    }

    private void markTransition(long nowMillis) {
        transitionId++;
        transitionStartedAtMillis = nowMillis;
    }
}

package com.zeldatargeting.mod.client.session;

import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPriority;

public final class LockOnSnapshot<T> {
    private final LockPhase phase;
    private final long transitionId;
    private final long transitionStartedAtMillis;
    private final TargetObservation<T> target;
    private final long occlusionDurationMillis;
    private final LockReleaseReason releaseReason;
    private final TargetPriority priority;
    private final String cameraProfileName;

    public LockOnSnapshot(
            LockPhase phase,
            long transitionId,
            long transitionStartedAtMillis,
            TargetObservation<T> target,
            long occlusionDurationMillis,
            LockReleaseReason releaseReason,
            TargetPriority priority,
            String cameraProfileName) {
        this.phase = phase;
        this.transitionId = transitionId;
        this.transitionStartedAtMillis = transitionStartedAtMillis;
        this.target = target;
        this.occlusionDurationMillis = occlusionDurationMillis;
        this.releaseReason = releaseReason;
        this.priority = priority;
        this.cameraProfileName = cameraProfileName;
    }

    public LockPhase getPhase() {
        return phase;
    }

    public long getTransitionId() {
        return transitionId;
    }

    public long getTransitionStartedAtMillis() {
        return transitionStartedAtMillis;
    }

    public TargetObservation<T> getTarget() {
        return target;
    }

    public long getOcclusionDurationMillis() {
        return occlusionDurationMillis;
    }

    public LockReleaseReason getReleaseReason() {
        return releaseReason;
    }

    public TargetPriority getPriority() {
        return priority;
    }

    public String getCameraProfileName() {
        return cameraProfileName;
    }

    public boolean isTracking() {
        return phase == LockPhase.ACQUIRING
            || phase == LockPhase.LOCKED
            || phase == LockPhase.OCCLUDED_GRACE
            || phase == LockPhase.SWITCHING;
    }

    public boolean isPresentationVisible() {
        return target != null;
    }
}

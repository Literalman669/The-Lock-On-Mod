package com.zeldatargeting.mod.client.camera.core;

public final class PerspectiveController {
    private static final long TIMER_INACTIVE = Long.MIN_VALUE;

    private boolean active;
    private int capturedPerspective;
    private int requestedPerspective;
    private float baselineFov;
    private boolean changedPerspective;
    private boolean fallbackEligible;
    private boolean fallbackActive;
    private long belowThresholdSince = TIMER_INACTIVE;
    private long aboveThresholdSince = TIMER_INACTIVE;

    public PerspectiveDecision begin(
            int currentPerspective,
            float currentFov,
            boolean autoThirdPerson,
            long nowMillis) {
        if (active) {
            return noChange();
        }
        active = true;
        capturedPerspective = currentPerspective;
        requestedPerspective = currentPerspective;
        baselineFov = currentFov;
        changedPerspective = autoThirdPerson && currentPerspective == 0;
        fallbackEligible = changedPerspective;
        fallbackActive = false;
        resetTimers();
        if (changedPerspective) {
            requestedPerspective = 1;
            return PerspectiveDecision.apply(requestedPerspective, baselineFov, false);
        }
        return noChange();
    }

    public PerspectiveDecision update(
            double safeDistance,
            CameraProfile profile,
            long nowMillis) {
        if (!active || !fallbackEligible || profile == null) {
            return noChange();
        }
        double distance = isFinite(safeDistance) ? safeDistance : 0.0D;
        if (!fallbackActive) {
            aboveThresholdSince = TIMER_INACTIVE;
            if (distance < profile.getFallbackEnterDistance()) {
                if (belowThresholdSince == TIMER_INACTIVE) {
                    belowThresholdSince = nowMillis;
                }
                if (elapsedSince(belowThresholdSince, nowMillis)
                        >= profile.getFallbackEnterDelayMillis()) {
                    fallbackActive = true;
                    requestedPerspective = capturedPerspective;
                    belowThresholdSince = TIMER_INACTIVE;
                    return PerspectiveDecision.apply(
                        requestedPerspective,
                        baselineFov,
                        true
                    );
                }
            } else {
                belowThresholdSince = TIMER_INACTIVE;
            }
            return noChange();
        }

        belowThresholdSince = TIMER_INACTIVE;
        if (distance > profile.getFallbackExitDistance()) {
            if (aboveThresholdSince == TIMER_INACTIVE) {
                aboveThresholdSince = nowMillis;
            }
            if (elapsedSince(aboveThresholdSince, nowMillis)
                    >= profile.getFallbackExitDelayMillis()) {
                fallbackActive = false;
                requestedPerspective = 1;
                aboveThresholdSince = TIMER_INACTIVE;
                return PerspectiveDecision.apply(requestedPerspective, baselineFov, false);
            }
        } else {
            aboveThresholdSince = TIMER_INACTIVE;
        }
        return noChange();
    }

    public PerspectiveDecision restore() {
        if (!active) {
            return noChange();
        }
        int restorePerspective = capturedPerspective;
        float restoreFov = baselineFov;
        boolean shouldApply = changedPerspective;
        clear();
        if (shouldApply) {
            return PerspectiveDecision.apply(restorePerspective, restoreFov, false);
        }
        return PerspectiveDecision.noChange(restorePerspective, restoreFov, false);
    }

    public void clear() {
        active = false;
        capturedPerspective = 0;
        requestedPerspective = 0;
        baselineFov = 0.0F;
        changedPerspective = false;
        fallbackEligible = false;
        fallbackActive = false;
        resetTimers();
    }

    public boolean isActive() {
        return active;
    }

    public int getCapturedPerspective() {
        return capturedPerspective;
    }

    public int getRequestedPerspective() {
        return requestedPerspective;
    }

    private PerspectiveDecision noChange() {
        return PerspectiveDecision.noChange(
            requestedPerspective,
            baselineFov,
            fallbackActive
        );
    }

    private void resetTimers() {
        belowThresholdSince = TIMER_INACTIVE;
        aboveThresholdSince = TIMER_INACTIVE;
    }

    private static long elapsedSince(long startedAt, long nowMillis) {
        return Math.max(0L, nowMillis - startedAt);
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}

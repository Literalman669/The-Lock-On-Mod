package com.zeldatargeting.mod.client.camera.core;

import com.zeldatargeting.mod.client.math.CameraMath;

public final class CameraDirector {
    private static final float MAX_TRACKED_TARGET_DELTA = 30.0F;
    private static final float MAX_TRACKED_TARGET_LEAD = 45.0F;

    private boolean active;
    private boolean restoring;
    private boolean freeLookHeld;
    private boolean hasPreviousTargetRotation;
    private float currentYaw;
    private float currentPitch;
    private float previousTargetYaw;
    private float previousTargetPitch;
    private double currentDistance = 4.0D;
    private double currentTranslationDelta;
    private double currentFovMultiplier = 1.0D;
    private float currentPlayerAlpha = 1.0F;
    private double restoreStartDistance;
    private double restoreStartTranslationDelta;
    private double restoreStartFovMultiplier;
    private float restoreStartPlayerAlpha;
    private long restoreStartedAtMillis;
    private CameraFrame lastValidFrame;

    public void begin(CameraInput input, CameraProfile profile) {
        if (input == null || profile == null || !hasFiniteRotation(input)) {
            reset();
            return;
        }
        active = true;
        restoring = false;
        freeLookHeld = input.isFreeLook();
        currentYaw = input.getPlayerYaw();
        currentPitch = clamp(input.getPlayerPitch(), -90.0F, 90.0F);
        CameraCollisionResult collision = input.getCollision();
        if (isFiniteCollision(collision)) {
            currentDistance = collision.getSafeDistance();
            currentTranslationDelta = collision.getTranslationDelta();
        } else {
            currentDistance = profile.getBaseDistance();
            currentTranslationDelta = 0.0D;
        }
        currentFovMultiplier = 1.0D;
        currentPlayerAlpha = 1.0F;
        seedTargetRotation(input, profile);
        lastValidFrame = frame(false, input.getRequestedPerspective(), false);
    }

    public CameraFrame update(CameraInput input, CameraProfile profile) {
        if (!active) {
            begin(input, profile);
        }
        if (input == null || profile == null || !isValid(input)) {
            return lastValidFrame == null
                ? CameraFrame.baseline(input == null ? 0 : input.getRequestedPerspective())
                : lastValidFrame;
        }

        double halfLife = profile.getRotationHalfLifeMillis();
        if (input.getRequestedPerspective() == 0) {
            halfLife *= profile.getFirstPersonHalfLifeScale();
        }
        float factor = (float) CameraFraming.interpolationFactor(
            input.getElapsedMillis(),
            halfLife
        );
        updatePresentation(input, profile, factor);
        if (input.isFreeLook()) {
            freeLookHeld = true;
            seedTargetRotation(input, profile);
            lastValidFrame = frame(false, input.getRequestedPerspective(), false);
            return lastValidFrame;
        }

        if (freeLookHeld) {
            currentYaw = input.getPlayerYaw();
            currentPitch = clamp(input.getPlayerPitch(), -90.0F, 90.0F);
            freeLookHeld = false;
            seedTargetRotation(input, profile);
        }

        CameraMath.Rotation target = targetRotation(input, profile);
        CameraMath.Rotation predicted = predictTargetRotation(input, profile, target, factor);
        rememberTargetRotation(target);
        currentYaw = CameraMath.interpolateAngle(currentYaw, predicted.getYaw(), factor);
        currentPitch = clamp(
            CameraMath.interpolate(currentPitch, predicted.getPitch(), factor),
            -90.0F,
            90.0F
        );
        lastValidFrame = frame(true, input.getRequestedPerspective(), false);
        return lastValidFrame;
    }

    public CameraFrame beginRestore(CameraRestoreMode mode, long nowMillis) {
        int perspective = lastValidFrame == null
            ? 0
            : lastValidFrame.getRequestedPerspective();
        if (mode == CameraRestoreMode.IMMEDIATE) {
            CameraFrame baseline = CameraFrame.baseline(perspective);
            reset();
            return baseline;
        }
        if (!active) {
            return CameraFrame.baseline(perspective);
        }
        restoring = true;
        restoreStartedAtMillis = nowMillis;
        restoreStartDistance = currentDistance;
        restoreStartTranslationDelta = currentTranslationDelta;
        restoreStartFovMultiplier = currentFovMultiplier;
        restoreStartPlayerAlpha = currentPlayerAlpha;
        CameraFrame frame = frame(false, perspective, false);
        lastValidFrame = frame;
        return frame;
    }

    public CameraFrame updateRestore(
            long nowMillis,
            CameraProfile profile,
            int requestedPerspective) {
        if (!restoring || profile == null) {
            return CameraFrame.baseline(requestedPerspective);
        }
        long elapsed = Math.max(0L, nowMillis - restoreStartedAtMillis);
        long duration = profile.getReleaseDurationMillis();
        double progress = duration == 0L
            ? 1.0D
            : clamp((double) elapsed / duration, 0.0D, 1.0D);
        currentDistance = interpolate(restoreStartDistance, 4.0D, progress);
        currentTranslationDelta = interpolate(restoreStartTranslationDelta, 0.0D, progress);
        currentFovMultiplier = interpolate(restoreStartFovMultiplier, 1.0D, progress);
        currentPlayerAlpha = (float) interpolate(restoreStartPlayerAlpha, 1.0D, progress);
        boolean complete = progress >= 1.0D;
        CameraFrame frame = frame(false, requestedPerspective, complete);
        if (complete) {
            reset();
        } else {
            lastValidFrame = frame;
        }
        return frame;
    }

    public void reset() {
        active = false;
        restoring = false;
        freeLookHeld = false;
        hasPreviousTargetRotation = false;
        currentYaw = 0.0F;
        currentPitch = 0.0F;
        previousTargetYaw = 0.0F;
        previousTargetPitch = 0.0F;
        currentDistance = 4.0D;
        currentTranslationDelta = 0.0D;
        currentFovMultiplier = 1.0D;
        currentPlayerAlpha = 1.0F;
        restoreStartDistance = 4.0D;
        restoreStartTranslationDelta = 0.0D;
        restoreStartFovMultiplier = 1.0D;
        restoreStartPlayerAlpha = 1.0F;
        restoreStartedAtMillis = 0L;
        lastValidFrame = null;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRestoring() {
        return restoring;
    }

    private void updatePresentation(
            CameraInput input,
            CameraProfile profile,
            double factor) {
        CameraCollisionResult collision = input.getCollision();
        double safeDistance = collision.getSafeDistance();
        if (safeDistance < currentDistance) {
            currentDistance = safeDistance;
        } else {
            currentDistance = interpolate(currentDistance, safeDistance, factor);
        }
        currentTranslationDelta = currentDistance - collision.getVanillaDistance();
        double targetFovMultiplier = CameraFraming.fovMultiplier(
            profile,
            input.getTargetWidth(),
            input.getTargetHeight(),
            input.getTargetDistance(),
            20.0D
        );
        currentFovMultiplier = interpolate(
            currentFovMultiplier,
            targetFovMultiplier,
            factor
        );
        currentPlayerAlpha = CameraFraming.playerAlpha(profile, currentDistance);
    }

    private CameraFrame frame(
            boolean applyRotation,
            int requestedPerspective,
            boolean restoreComplete) {
        return new CameraFrame(
            currentYaw,
            currentPitch,
            currentDistance,
            currentTranslationDelta,
            currentFovMultiplier,
            currentPlayerAlpha,
            applyRotation,
            requestedPerspective,
            restoreComplete
        );
    }

    private static CameraMath.Rotation targetRotation(
            CameraInput input,
            CameraProfile profile) {
        CameraMath.Rotation desired = CameraMath.lookAt(
            input.getTargetDeltaX(),
            input.getTargetDeltaY(),
            input.getTargetDeltaZ()
        );
        float yawDifference = CameraMath.wrapDegrees(desired.getYaw() - input.getPlayerYaw());
        float boundedYaw = input.getPlayerYaw() + clamp(
            yawDifference,
            -profile.getMaxYawAdjustment(),
            profile.getMaxYawAdjustment()
        );
        float pitchDifference = desired.getPitch() - input.getPlayerPitch();
        float boundedPitch = clamp(
            input.getPlayerPitch() + clamp(
                pitchDifference,
                -profile.getMaxPitchAdjustment(),
                profile.getMaxPitchAdjustment()
            ),
            -90.0F,
            90.0F
        );
        return new CameraMath.Rotation(boundedYaw, boundedPitch);
    }

    private CameraMath.Rotation predictTargetRotation(
            CameraInput input,
            CameraProfile profile,
            CameraMath.Rotation target,
            float interpolationFactor) {
        if (!hasPreviousTargetRotation || interpolationFactor <= 0.0F) {
            return target;
        }
        float yawChange = CameraMath.wrapDegrees(
            target.getYaw() - previousTargetYaw
        );
        float pitchChange = target.getPitch() - previousTargetPitch;
        if (Math.abs(yawChange) > MAX_TRACKED_TARGET_DELTA
                || Math.abs(pitchChange) > MAX_TRACKED_TARGET_DELTA) {
            return target;
        }
        float leadFactor = (1.0F - interpolationFactor) / interpolationFactor;
        float yawLead = yawChange * leadFactor;
        float pitchLead = pitchChange * leadFactor;
        if (Math.abs(yawLead) > MAX_TRACKED_TARGET_LEAD
                || Math.abs(pitchLead) > MAX_TRACKED_TARGET_LEAD) {
            return target;
        }
        float predictedYaw = input.getPlayerYaw() + clamp(
            CameraMath.wrapDegrees(
                target.getYaw() + yawLead - input.getPlayerYaw()
            ),
            -profile.getMaxYawAdjustment(),
            profile.getMaxYawAdjustment()
        );
        float predictedPitch = clamp(
            input.getPlayerPitch() + clamp(
                target.getPitch() + pitchLead - input.getPlayerPitch(),
                -profile.getMaxPitchAdjustment(),
                profile.getMaxPitchAdjustment()
            ),
            -90.0F,
            90.0F
        );
        return new CameraMath.Rotation(predictedYaw, predictedPitch);
    }

    private void seedTargetRotation(CameraInput input, CameraProfile profile) {
        if (!hasFiniteTargetDirection(input)) {
            hasPreviousTargetRotation = false;
            return;
        }
        rememberTargetRotation(targetRotation(input, profile));
    }

    private void rememberTargetRotation(CameraMath.Rotation rotation) {
        previousTargetYaw = rotation.getYaw();
        previousTargetPitch = rotation.getPitch();
        hasPreviousTargetRotation = true;
    }

    private static boolean isValid(CameraInput input) {
        return hasFiniteRotation(input)
            && isFinite(input.getTargetDeltaX())
            && isFinite(input.getTargetDeltaY())
            && isFinite(input.getTargetDeltaZ())
            && isFinite(input.getTargetWidth())
            && isFinite(input.getTargetHeight())
            && isFinite(input.getTargetDistance())
            && isFiniteCollision(input.getCollision());
    }

    private static boolean hasFiniteRotation(CameraInput input) {
        return isFinite(input.getPlayerYaw()) && isFinite(input.getPlayerPitch());
    }

    private static boolean hasFiniteTargetDirection(CameraInput input) {
        return input != null
            && isFinite(input.getTargetDeltaX())
            && isFinite(input.getTargetDeltaY())
            && isFinite(input.getTargetDeltaZ());
    }

    private static boolean isFiniteCollision(CameraCollisionResult collision) {
        return collision != null
            && isFinite(collision.getSafeDistance())
            && isFinite(collision.getVanillaDistance())
            && isFinite(collision.getTranslationDelta());
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double interpolate(double start, double end, double factor) {
        return start + (end - start) * factor;
    }
}

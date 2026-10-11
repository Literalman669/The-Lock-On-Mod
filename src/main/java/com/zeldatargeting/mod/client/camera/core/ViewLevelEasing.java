package com.zeldatargeting.mod.client.camera.core;

/**
 * Eases the view's pitch back to the horizon, the Minecraft take on Zelda's
 * camera re-center when the lock key finds no target.
 *
 * <p>{@link #pitchStep(long)} returns how much to add to the pitch this frame,
 * not an absolute pitch, so mouse movement during the ease still counts.</p>
 */
public final class ViewLevelEasing {
    /** Views closer to level than this are left alone. */
    static final float LEVEL_TOLERANCE_DEGREES = 0.5F;

    private final long durationMillis;
    private boolean active;
    private float startPitch;
    private long startedAtMillis;
    private long lastStepMillis;
    private float appliedOffset;

    public ViewLevelEasing(long durationMillis) {
        this.durationMillis = Math.max(1L, durationMillis);
    }

    public void start(float currentPitch, long nowMillis) {
        if (Float.isNaN(currentPitch) || Math.abs(currentPitch) < LEVEL_TOLERANCE_DEGREES) {
            cancel();
            return;
        }
        active = true;
        startPitch = currentPitch;
        startedAtMillis = nowMillis;
        lastStepMillis = nowMillis;
        appliedOffset = 0.0F;
    }

    public float pitchStep(long nowMillis) {
        if (!active || nowMillis < lastStepMillis) {
            return 0.0F;
        }
        lastStepMillis = nowMillis;
        double progress = Math.min(1.0D, (nowMillis - startedAtMillis) / (double) durationMillis);
        double remaining = 1.0D - progress;
        float targetOffset = (float) (-startPitch * (1.0D - remaining * remaining * remaining));
        if (progress >= 1.0D) {
            targetOffset = -startPitch;
            active = false;
        }
        float step = targetOffset - appliedOffset;
        appliedOffset = targetOffset;
        return step;
    }

    public void cancel() {
        active = false;
        appliedOffset = 0.0F;
    }

    public boolean isActive() {
        return active;
    }
}

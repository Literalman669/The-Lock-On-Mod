package com.zeldatargeting.mod.client.presentation.core;

/**
 * Short-lived ring effects driven by {@link PresentationFeedbackEvent} bits:
 * a flash on critical hits, a shockwave when the target drops to low health,
 * and a burst when it dies. Each effect reports its progress from 0 (just
 * started) toward 1 (finished), or {@link #INACTIVE}.
 */
public final class FeedbackEffects {
    public static final float INACTIVE = -1.0F;

    public enum Effect {
        CRITICAL(350L),
        LOW_HEALTH(900L),
        LETHAL(700L);

        private final long durationMillis;

        Effect(long durationMillis) {
            this.durationMillis = durationMillis;
        }

        public long getDurationMillis() {
            return durationMillis;
        }
    }

    private static final long NOT_STARTED = Long.MIN_VALUE;
    private static final int NO_TARGET = Integer.MIN_VALUE;

    private final long[] startedAt = new long[Effect.values().length];
    private int lowHealthTargetId = NO_TARGET;

    public FeedbackEffects() {
        clear();
    }

    /**
     * Starts the effects named by {@code events}. The low-health shockwave plays
     * once per target, so a health estimate that wavers near the threshold does
     * not replay it.
     */
    public void trigger(int events, int targetId, long nowMillis) {
        if ((events & PresentationFeedbackEvent.CRITICAL) != 0) {
            start(Effect.CRITICAL, nowMillis);
        }
        if ((events & PresentationFeedbackEvent.LETHAL) != 0) {
            start(Effect.LETHAL, nowMillis);
        }
        if ((events & PresentationFeedbackEvent.LOW_HEALTH) != 0 && targetId != lowHealthTargetId) {
            lowHealthTargetId = targetId;
            start(Effect.LOW_HEALTH, nowMillis);
        }
    }

    /** Progress of {@code effect} in [0, 1), or {@link #INACTIVE} when it is not playing. */
    public float progress(Effect effect, long nowMillis) {
        long started = startedAt[effect.ordinal()];
        if (started == NOT_STARTED || nowMillis < started) {
            return INACTIVE;
        }
        long elapsed = nowMillis - started;
        if (elapsed >= effect.getDurationMillis()) {
            startedAt[effect.ordinal()] = NOT_STARTED;
            return INACTIVE;
        }
        return elapsed / (float) effect.getDurationMillis();
    }

    public boolean isActive(Effect effect, long nowMillis) {
        return progress(effect, nowMillis) != INACTIVE;
    }

    public void clear() {
        for (int i = 0; i < startedAt.length; i++) {
            startedAt[i] = NOT_STARTED;
        }
        lowHealthTargetId = NO_TARGET;
    }

    private void start(Effect effect, long nowMillis) {
        startedAt[effect.ordinal()] = nowMillis;
    }
}

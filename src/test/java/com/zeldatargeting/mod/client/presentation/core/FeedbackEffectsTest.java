package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FeedbackEffectsTest {
    private static final float EPSILON = 0.0001F;

    @Test
    public void nothingPlaysUntilTriggered() {
        FeedbackEffects effects = new FeedbackEffects();

        for (FeedbackEffects.Effect effect : FeedbackEffects.Effect.values()) {
            assertEquals(FeedbackEffects.INACTIVE, effects.progress(effect, 1000L), EPSILON);
        }
    }

    @Test
    public void eventBitsStartTheMatchingEffects() {
        FeedbackEffects effects = new FeedbackEffects();

        effects.trigger(PresentationFeedbackEvent.CRITICAL | PresentationFeedbackEvent.LETHAL, 7, 1000L);

        assertTrue(effects.isActive(FeedbackEffects.Effect.CRITICAL, 1000L));
        assertTrue(effects.isActive(FeedbackEffects.Effect.LETHAL, 1000L));
        assertFalse(effects.isActive(FeedbackEffects.Effect.LOW_HEALTH, 1000L));
    }

    @Test
    public void progressRunsFromZeroToOneThenStops() {
        FeedbackEffects effects = new FeedbackEffects();
        effects.trigger(PresentationFeedbackEvent.CRITICAL, 7, 1000L);
        long duration = FeedbackEffects.Effect.CRITICAL.getDurationMillis();

        assertEquals(0.0F, effects.progress(FeedbackEffects.Effect.CRITICAL, 1000L), EPSILON);
        assertEquals(0.5F, effects.progress(FeedbackEffects.Effect.CRITICAL, 1000L + duration / 2), EPSILON);
        assertEquals(FeedbackEffects.INACTIVE,
            effects.progress(FeedbackEffects.Effect.CRITICAL, 1000L + duration), EPSILON);
    }

    @Test
    public void retriggeringRestartsTheEffect() {
        FeedbackEffects effects = new FeedbackEffects();
        effects.trigger(PresentationFeedbackEvent.CRITICAL, 7, 1000L);
        effects.trigger(PresentationFeedbackEvent.CRITICAL, 7, 1300L);

        assertEquals(0.0F, effects.progress(FeedbackEffects.Effect.CRITICAL, 1300L), EPSILON);
    }

    @Test
    public void lowHealthShockwavePlaysOncePerTarget() {
        FeedbackEffects effects = new FeedbackEffects();
        long later = 1000L + FeedbackEffects.Effect.LOW_HEALTH.getDurationMillis() + 1L;

        effects.trigger(PresentationFeedbackEvent.LOW_HEALTH, 7, 1000L);
        assertTrue(effects.isActive(FeedbackEffects.Effect.LOW_HEALTH, 1000L));

        effects.trigger(PresentationFeedbackEvent.LOW_HEALTH, 7, later);
        assertFalse(effects.isActive(FeedbackEffects.Effect.LOW_HEALTH, later));

        effects.trigger(PresentationFeedbackEvent.LOW_HEALTH, 8, later);
        assertTrue(effects.isActive(FeedbackEffects.Effect.LOW_HEALTH, later));
    }

    @Test
    public void clearStopsEffectsAndForgetsTheLowHealthTarget() {
        FeedbackEffects effects = new FeedbackEffects();
        effects.trigger(PresentationFeedbackEvent.LOW_HEALTH | PresentationFeedbackEvent.CRITICAL, 7, 1000L);

        effects.clear();
        assertFalse(effects.isActive(FeedbackEffects.Effect.CRITICAL, 1000L));

        effects.trigger(PresentationFeedbackEvent.LOW_HEALTH, 7, 1100L);
        assertTrue(effects.isActive(FeedbackEffects.Effect.LOW_HEALTH, 1100L));
    }
}

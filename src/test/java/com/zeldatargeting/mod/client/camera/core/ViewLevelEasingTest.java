package com.zeldatargeting.mod.client.camera.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ViewLevelEasingTest {
    @Test
    public void stepsAddUpToLevelingThePitchExactly() {
        ViewLevelEasing easing = new ViewLevelEasing(200L);
        easing.start(40.0F, 1000L);

        float total = 0.0F;
        for (long now = 1000L; now <= 1240L; now += 16L) {
            total += easing.pitchStep(now);
        }

        assertEquals(-40.0F, total, 0.0001F);
        assertFalse(easing.isActive());
    }

    @Test
    public void firstHalfMovesFurtherThanSecondHalf() {
        ViewLevelEasing easing = new ViewLevelEasing(200L);
        easing.start(-30.0F, 0L);

        float firstHalf = easing.pitchStep(100L);
        float secondHalf = easing.pitchStep(200L);

        assertTrue(firstHalf > secondHalf);
        assertEquals(30.0F, firstHalf + secondHalf, 0.0001F);
    }

    @Test
    public void aLevelViewNeedsNoMovement() {
        ViewLevelEasing easing = new ViewLevelEasing(200L);
        easing.start(0.2F, 0L);

        assertFalse(easing.isActive());
        assertEquals(0.0F, easing.pitchStep(50L), 0.0F);
    }

    @Test
    public void cancelStopsFurtherSteps() {
        ViewLevelEasing easing = new ViewLevelEasing(200L);
        easing.start(45.0F, 0L);
        easing.pitchStep(50L);

        easing.cancel();

        assertFalse(easing.isActive());
        assertEquals(0.0F, easing.pitchStep(100L), 0.0F);
    }

    @Test
    public void aClockThatStepsBackwardDoesNotReverseTheView() {
        ViewLevelEasing easing = new ViewLevelEasing(200L);
        easing.start(20.0F, 500L);
        easing.pitchStep(600L);

        assertEquals(0.0F, easing.pitchStep(550L), 0.0F);
    }
}

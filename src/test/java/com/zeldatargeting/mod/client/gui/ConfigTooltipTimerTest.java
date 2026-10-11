package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ConfigTooltipTimerTest {
    private final Object a = new Object();
    private final Object b = new Object();

    @Test
    public void showsAfterResting() {
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.hover(a, 0L);

        timer.hover(a, 599L);
        assertFalse(timer.isVisible(599L));
        timer.hover(a, 600L);
        assertTrue(timer.isVisible(600L));
        assertSame(a, timer.getTarget());
    }

    @Test
    public void movingToAnotherTargetRestartsTheDelay() {
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.hover(a, 0L);
        timer.hover(b, 500L);

        timer.hover(b, 1000L);
        assertFalse(timer.isVisible(1000L));
        timer.hover(b, 1100L);
        assertTrue(timer.isVisible(1100L));
    }

    @Test
    public void leavingHidesAtOnce() {
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.hover(a, 0L);
        timer.hover(a, 700L);
        assertTrue(timer.isVisible(700L));

        timer.hover(null, 710L);

        assertFalse(timer.isVisible(710L));
        assertNull(timer.getTarget());
    }

    @Test
    public void interactionHidesAndSuppressesUntilLeave() {
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.hover(a, 0L);
        timer.hover(a, 700L);

        timer.interact();
        timer.hover(a, 5700L);
        assertFalse(timer.isVisible(5700L));

        timer.hover(null, 5800L);
        timer.hover(a, 5900L);
        timer.hover(a, 6499L);
        assertFalse(timer.isVisible(6499L));
        timer.hover(a, 6500L);
        assertTrue(timer.isVisible(6500L));
    }

    @Test
    public void repeatedInteractionKeepsTheClickedSettingQuiet() {
        // A slider press reports a click and then a drag in the same moment.
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.hover(a, 0L);
        timer.hover(a, 700L);

        timer.interact();
        timer.interact();
        timer.hover(a, 800L);
        timer.hover(a, 5000L);

        assertFalse(timer.isVisible(5000L));
    }

    @Test
    public void interactionWhileNothingHoveredIsHarmless() {
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.interact();

        timer.hover(a, 0L);
        timer.hover(a, 600L);
        assertTrue(timer.isVisible(600L));
    }

    @Test
    public void pageChangeWaitsTheFullDelay() {
        ConfigTooltipTimer timer = new ConfigTooltipTimer();
        timer.hover(a, 0L);
        timer.hover(a, 700L);

        timer.interact();
        timer.hover(b, 800L);
        timer.hover(b, 1399L);
        assertFalse(timer.isVisible(1399L));
        timer.hover(b, 1400L);
        assertTrue(timer.isVisible(1400L));
    }
}

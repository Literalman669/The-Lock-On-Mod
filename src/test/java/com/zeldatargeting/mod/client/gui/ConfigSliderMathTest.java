package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigSliderMathTest {
    @Test
    public void ends() {
        assertEquals(1.0, ConfigSliderMath.valueAt(0.0, 1, 64, 1), 0.0);
        assertEquals(64.0, ConfigSliderMath.valueAt(1.0, 1, 64, 1), 0.0);
    }

    @Test
    public void snapsToStep() {
        assertEquals(0.51, ConfigSliderMath.valueAt(0.5, 0.01, 1.0, 0.05), 1.0E-9);
        assertEquals(0.3, ConfigSliderMath.valueAt(0.33, 0.0, 1.0, 0.1), 1.0E-9);
    }

    @Test
    public void clampsOutsideFraction() {
        assertEquals(1.0, ConfigSliderMath.valueAt(-1.0, 1, 64, 1), 0.0);
        assertEquals(64.0, ConfigSliderMath.valueAt(2.0, 1, 64, 1), 0.0);
    }

    @Test
    public void neverOvershootsMax() {
        assertEquals(60.0, ConfigSliderMath.valueAt(1.0, 5, 60, 5), 0.0);
        assertEquals(1.0, ConfigSliderMath.valueAt(1.0, 0.01, 1.0, 0.05), 0.0);
    }

    @Test
    public void fractionClampsBelowMin() {
        assertEquals(0.0, ConfigSliderMath.fractionOf(1, 5, 60), 0.0);
        assertEquals(1.0, ConfigSliderMath.fractionOf(99, 5, 60), 0.0);
    }

    @Test
    public void offStepValueIsNotRewrittenByFraction() {
        double fraction = ConfigSliderMath.fractionOf(0.37, 0.01, 1.0);

        assertTrue(fraction > 0.35 && fraction < 0.37);
    }
}

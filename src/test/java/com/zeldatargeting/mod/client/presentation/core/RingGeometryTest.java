package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RingGeometryTest {
    @Test
    public void ringGeometryClampsSizeAndHealthArc() {
        RingGeometry geometry = RingGeometry.create(0.6D, 12.0D, 2.0D, 0.25F, false);

        assertEquals(0.60D, geometry.getHalfWidth(), 0.000001D);
        assertEquals(2.00D, geometry.getHalfHeight(), 0.000001D);
        assertEquals(90.0F, geometry.getHealthArcDegrees(), 0.0001F);
        assertEquals(1.04F, geometry.getMotionScale(), 0.0001F);
        assertTrue(geometry.getAlpha() > 0.0F);
    }

    @Test
    public void healthArcStartsFullAndDrainsWithTargetHealth() {
        assertEquals(360.0F,
            RingGeometry.create(1.0D, 2.0D, 2.0D, 1.0F, false).getHealthArcDegrees(),
            0.0001F);
        assertEquals(0.0F,
            RingGeometry.create(1.0D, 2.0D, 2.0D, 0.0F, false).getHealthArcDegrees(),
            0.0001F);
    }

    @Test
    public void reducedMotionDisablesRingPulseWithoutChangingGeometry() {
        RingGeometry geometry = RingGeometry.create(1.0D, 2.0D, 0.0D, 1.0F, true);

        assertEquals(1.0D, geometry.getHalfWidth(), 0.000001D);
        assertEquals(1.0D, geometry.getHalfHeight(), 0.000001D);
        assertEquals(1.0F, geometry.getMotionScale(), 0.0001F);
    }

    @Test
    public void invalidRingInputsStayFiniteForTheOpenGlBoundary() {
        RingGeometry geometry = RingGeometry.create(Double.NaN, -1.0D, Double.NaN, Float.NaN, true);

        assertTrue(Double.isFinite(geometry.getHalfWidth()));
        assertTrue(Double.isFinite(geometry.getHalfHeight()));
        assertTrue(Float.isFinite(geometry.getAlpha()));
    }
}

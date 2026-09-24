package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TorusGeometryTest {
    @Test
    public void createsARealTubeCrossSectionAroundTheRing() {
        TorusGeometry geometry = TorusGeometry.create(1.5D, 0.18D);

        assertEquals(1.5D, geometry.getMajorRadius(), 0.000001D);
        assertEquals(0.18D, geometry.getTubeRadius(), 0.000001D);
        assertEquals(1.68D, geometry.getRadialDistance(0.0D), 0.000001D);
        assertEquals(1.5D, geometry.getRadialDistance(Math.PI / 2.0D), 0.000001D);
        assertEquals(0.18D, geometry.getVerticalOffset(Math.PI / 2.0D), 0.000001D);
        assertEquals(-0.18D, geometry.getVerticalOffset(-Math.PI / 2.0D), 0.000001D);
    }

    @Test
    public void keepsInvalidTorusInputsFiniteAndDrawable() {
        TorusGeometry geometry = TorusGeometry.create(Double.NaN, Double.POSITIVE_INFINITY);

        assertTrue(Double.isFinite(geometry.getMajorRadius()));
        assertTrue(Double.isFinite(geometry.getTubeRadius()));
        assertTrue(geometry.getMajorRadius() > 0.0D);
        assertTrue(geometry.getTubeRadius() > 0.0D);
        assertTrue(geometry.getTubeRadius() < geometry.getMajorRadius());
    }
}

package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NeonRingAppearanceTest {
    @Test
    public void derivesACompactGlowAroundOnePrimaryTube() {
        NeonRingAppearance appearance = NeonRingAppearance.from(
            RingGeometry.create(1.0D, 2.0D, 4.0D, 1.0F, false)
        );

        assertTrue(appearance.getAuraHalfThickness() > appearance.getCoreHalfThickness());
        assertTrue(appearance.getAuraHalfThickness() <= appearance.getCoreHalfThickness() * 1.5D);
        assertTrue(appearance.getAuraAlphaMultiplier() < appearance.getCoreAlphaMultiplier());
    }

    @Test
    public void clampsEveryBandAndAlphaForInvalidGeometry() {
        NeonRingAppearance appearance = NeonRingAppearance.from(
            RingGeometry.create(Double.NaN, Double.NaN, Double.NaN, Float.NaN, true)
        );

        assertTrue(Double.isFinite(appearance.getAuraHalfThickness()));
        assertTrue(Double.isFinite(appearance.getCoreHalfThickness()));
        assertTrue(appearance.getAuraAlphaMultiplier() >= 0.0F);
        assertTrue(appearance.getAuraAlphaMultiplier() <= 1.0F);
        assertTrue(appearance.getCoreAlphaMultiplier() >= 0.0F);
        assertTrue(appearance.getCoreAlphaMultiplier() <= 1.0F);
    }

    @Test
    public void changesGlowWithoutChangingTorusDimensions() {
        RingGeometry geometry = RingGeometry.create(1.0D, 2.0D, 4.0D, 1.0F, false);

        NeonRingAppearance dim = NeonRingAppearance.from(geometry, 0.0F);
        NeonRingAppearance bright = NeonRingAppearance.from(geometry, 1.5F);

        assertEquals(dim.getAuraHalfThickness(), bright.getAuraHalfThickness(), 0.0D);
        assertEquals(dim.getCoreHalfThickness(), bright.getCoreHalfThickness(), 0.0D);
        assertTrue(bright.getAuraAlphaMultiplier() > dim.getAuraAlphaMultiplier());
        assertTrue(bright.getCoreAlphaMultiplier() > dim.getCoreAlphaMultiplier());
    }

}

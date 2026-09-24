package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PresentationStatusResolverTest {
    @Test
    public void lethalAndLowHealthUseDistinctNonColorStatuses() {
        assertEquals(PresentationStatus.LETHAL,
            PresentationStatusResolver.resolve(0.50F, 1, false));
        assertEquals(PresentationStatus.LOW_HEALTH,
            PresentationStatusResolver.resolve(0.20F, 4, false));
        assertEquals(PresentationStatus.OCCLUDED,
            PresentationStatusResolver.resolve(0.90F, 4, true));
    }

    @Test
    public void nearLethalTargetsUseWarningBeforeTheLowHealthThreshold() {
        assertEquals(PresentationStatus.WARNING,
            PresentationStatusResolver.resolve(0.80F, 3, false));
        assertEquals(PresentationStatus.NORMAL,
            PresentationStatusResolver.resolve(Float.NaN, 4, false));
    }

    @Test
    public void healthWarningThresholdMatchesTheHudColorBoundary() {
        assertEquals(PresentationStatus.NORMAL,
            PresentationStatusResolver.resolve(0.76F, 4, false));
        assertEquals(PresentationStatus.WARNING,
            PresentationStatusResolver.resolve(0.75F, 4, false));
    }
}

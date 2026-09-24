package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RingColorPolicyTest {
    @Test
    public void usesTheHealthColorForHealthyTargets() {
        assertEquals(PresentationPalette.DEFAULT.getHealthColor(),
            RingColorPolicy.colorFor(PresentationStatus.NORMAL));
    }

    @Test
    public void escalatesTheMainRingColorWithTargetDanger() {
        assertEquals(PresentationPalette.DEFAULT.getWarningColor(),
            RingColorPolicy.colorFor(PresentationStatus.WARNING));
        assertEquals(PresentationPalette.DEFAULT.getWarningColor(),
            RingColorPolicy.colorFor(PresentationStatus.LOW_HEALTH));
        assertEquals(PresentationPalette.DEFAULT.getLethalColor(),
            RingColorPolicy.colorFor(PresentationStatus.LETHAL));
    }

    @Test
    public void preservesTheMutedOccludedState() {
        assertEquals(0xFF9EA6AD, RingColorPolicy.colorFor(PresentationStatus.OCCLUDED));
    }
}

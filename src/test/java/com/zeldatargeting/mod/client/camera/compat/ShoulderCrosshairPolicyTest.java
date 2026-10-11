package com.zeldatargeting.mod.client.camera.compat;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShoulderCrosshairPolicyTest {
    @Test
    public void anyLockUnderAdaptiveCrosshairUsesTheEyeRay() {
        // A wide shoulder offset cannot reach close targets from the shoulder ray,
        // so the lock always asks SSR for its eye-based (dynamic) crosshair.
        assertTrue(ShoulderCrosshairPolicy.useDynamicCrosshair(true, true, true, true, true));
    }

    @Test
    public void noLockKeepsTheShoulderRay() {
        assertFalse(ShoulderCrosshairPolicy.useDynamicCrosshair(true, false, true, true, true));
    }

    @Test
    public void explicitStaticOrDynamicModesAreLeftToSsr() {
        assertFalse(ShoulderCrosshairPolicy.useDynamicCrosshair(true, true, true, true, false));
    }

    @Test
    public void turningOffShoulderAimLeavesSsrAlone() {
        assertFalse(ShoulderCrosshairPolicy.useDynamicCrosshair(false, true, true, true, true));
    }

    @Test
    public void spectatingAnotherEntityLeavesSsrAlone() {
        assertFalse(ShoulderCrosshairPolicy.useDynamicCrosshair(true, true, false, true, true));
    }

    @Test
    public void inactiveShoulderCameraLeavesSsrAlone() {
        assertFalse(ShoulderCrosshairPolicy.useDynamicCrosshair(true, true, true, false, true));
    }
}

package com.zeldatargeting.mod.client.camera.compat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShoulderCameraStateTest {

    @Test
    public void inactiveStateCannotCompensate() {
        ShoulderCameraState state = ShoulderCameraState.inactive();

        assertFalse(state.isActive());
        assertFalse(state.canCompensate());
    }

    @Test
    public void invalidOffsetsCannotCompensate() {
        ShoulderCameraState state = ShoulderCameraState.active(
            Double.NaN,
            0.0D,
            3.0D,
            3.2D
        );

        assertTrue(state.isActive());
        assertFalse(state.canCompensate());
    }

    @Test
    public void nonPositiveCameraDistanceCannotCompensate() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.875D,
            0.0D,
            3.0D,
            0.0D
        );

        assertFalse(state.canCompensate());
    }

    @Test
    public void finiteActiveStatePreservesOffsetsAndCanCompensate() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.875D,
            0.25D,
            3.0D,
            3.125D
        );

        assertTrue(state.isActive());
        assertTrue(state.canCompensate());
        assertEquals(-0.875D, state.getOffsetX(), 0.0D);
        assertEquals(0.25D, state.getOffsetY(), 0.0D);
        assertEquals(3.0D, state.getOffsetZ(), 0.0D);
        assertEquals(3.125D, state.getCameraDistance(), 0.0D);
    }

    @Test
    public void dynamicCrosshairUsesEyeRayWithoutShoulderCompensation() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.875D, 0.25D, 3.0D, 3.125D, false
        );

        assertTrue(state.isActive());
        assertFalse(state.canCompensate());
        ShoulderAimSolver.AimVector aim = ShoulderAimSolver.compensate(
            0.0D, -0.5D, 3.0D, state
        );
        assertEquals(0.0D, aim.getX(), 0.0D);
        assertEquals(-0.5D, aim.getY(), 0.0D);
        assertEquals(3.0D, aim.getZ(), 0.0D);
        assertFalse(aim.isCompensated());
    }
}

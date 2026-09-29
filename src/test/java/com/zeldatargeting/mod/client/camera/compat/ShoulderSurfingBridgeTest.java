package com.zeldatargeting.mod.client.camera.compat;

import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import com.teamderpy.shouldersurfing.client.ShoulderRenderer;
import com.teamderpy.shouldersurfing.config.Config;
import com.teamderpy.shouldersurfing.config.CrosshairType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Runs the real reflection wiring against test stand-ins for SSR 2.9.x classes. */
public class ShoulderSurfingBridgeTest {
    private static final double EPSILON = 1.0E-9D;

    @Before
    public void resetShoulderSurfing() {
        ShoulderInstance.shoulderSurfing = true;
        ShoulderInstance.offsetX = -0.75D;
        ShoulderInstance.offsetY = 0.0D;
        ShoulderInstance.offsetZ = 3.0D;
        ShoulderInstance.offsetXOld = -0.75D;
        ShoulderInstance.offsetYOld = 0.0D;
        ShoulderInstance.offsetZOld = 3.0D;
        ShoulderRenderer.cameraDistance = 3.0D;
        Config.CLIENT.crosshairType = CrosshairType.STATIC;
        CrosshairType.holdingAdaptiveItem = false;
    }

    @Test
    public void detectsShoulderSurfingAndReadsTheActiveOffset() {
        ShoulderSurfingBridge bridge = ShoulderSurfingBridge.detect(true, null);
        ShoulderCameraState state = bridge.captureState();

        assertTrue(bridge.isLoaded());
        assertTrue(state.isActive());
        assertTrue(state.canCompensate());
        assertEquals(-0.75D, state.getOffsetX(), EPSILON);
        assertEquals(3.0D, state.getCameraDistance(), EPSILON);
    }

    @Test
    public void blendsOffsetsBetweenTicksLikeShoulderSurfingRenders() {
        // SSR is sliding its camera to the center (for example when looking down).
        ShoulderInstance.offsetXOld = -0.75D;
        ShoulderInstance.offsetX = -0.25D;
        ShoulderSurfingBridge bridge = ShoulderSurfingBridge.detect(true, null);

        assertEquals(-0.75D, bridge.captureState(0.0F).getOffsetX(), EPSILON);
        assertEquals(-0.5D, bridge.captureState(0.5F).getOffsetX(), EPSILON);
        assertEquals(-0.25D, bridge.captureState(1.0F).getOffsetX(), EPSILON);
        assertEquals(-0.25D, bridge.captureState().getOffsetX(), EPSILON);
    }

    @Test
    public void inactiveWhenShoulderSurfingIsNotTheCurrentView() {
        ShoulderInstance.shoulderSurfing = false;

        assertFalse(ShoulderSurfingBridge.detect(true, null).captureState(0.5F).isActive());
    }

    @Test
    public void dynamicCrosshairTurnsAimCorrectionOff() {
        Config.CLIENT.crosshairType = CrosshairType.ADAPTIVE;
        CrosshairType.holdingAdaptiveItem = true;

        ShoulderCameraState state = ShoulderSurfingBridge.detect(true, null).captureState(1.0F);

        assertTrue(state.isActive());
        assertFalse(state.canCompensate());
    }

    @Test
    public void notInstalledStaysUnavailable() {
        ShoulderSurfingBridge bridge = ShoulderSurfingBridge.detect(false, null);

        assertFalse(bridge.isLoaded());
        assertFalse(bridge.captureState(0.5F).isActive());
    }

    @Test
    public void interpolateClampsPartialTicks() {
        assertEquals(1.0D, ShoulderSurfingBridge.interpolate(1.0D, 3.0D, -1.0F), EPSILON);
        assertEquals(2.0D, ShoulderSurfingBridge.interpolate(1.0D, 3.0D, 0.5F), EPSILON);
        assertEquals(3.0D, ShoulderSurfingBridge.interpolate(1.0D, 3.0D, 2.0F), EPSILON);
        assertEquals(3.0D, ShoulderSurfingBridge.interpolate(1.0D, 3.0D, Float.NaN), EPSILON);
    }
}

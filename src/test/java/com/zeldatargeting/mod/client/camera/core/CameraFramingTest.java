package com.zeldatargeting.mod.client.camera.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CameraFramingTest {
    private static final double EPSILON = 0.000001D;
    private static final float FLOAT_EPSILON = 0.0001F;

    @Test
    public void targetSizeContinuouslyExpandsDistanceWithinProfileCap() {
        assertEquals(4.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, 0.6D, 1.8D), EPSILON);
        assertEquals(5.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, 2.0D, 4.9D), EPSILON);
        assertEquals(6.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, 8.0D, 8.0D), EPSILON);
        assertEquals(4.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, Double.NaN, -1.0D), EPSILON);
    }

    @Test
    public void fovAndAlphaRespectExactBounds() {
        assertEquals(1.06D, CameraFraming.fovMultiplier(
            CameraProfiles.BALANCED, 8.0D, 8.0D, 20.0D, 20.0D
        ), EPSILON);
        assertEquals(1.0D, CameraFraming.fovMultiplier(
            CameraProfiles.SNAPPY, 8.0D, 8.0D, 20.0D, 20.0D
        ), EPSILON);
        assertEquals(1.0D, CameraFraming.fovMultiplier(
            CameraProfiles.BALANCED, 8.0D, 8.0D, Double.NaN, 20.0D
        ), EPSILON);
        assertEquals(1.0F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, 1.25D), FLOAT_EPSILON);
        assertEquals(0.25F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, 0.45D), FLOAT_EPSILON);
        assertEquals(0.625F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, 0.85D), FLOAT_EPSILON);
        assertEquals(0.25F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, Double.NaN), FLOAT_EPSILON);
    }

    @Test
    public void interpolationFactorIsFrameRateIndependentAndBounded() {
        double oneHundredMillis = CameraFraming.interpolationFactor(100L, 100.0D);
        double twoFiftyMillis = CameraFraming.interpolationFactor(250L, 100.0D);

        assertEquals(0.5D, oneHundredMillis, EPSILON);
        assertTrue(twoFiftyMillis > oneHundredMillis);
        assertEquals(0.0D, CameraFraming.interpolationFactor(-1L, 100.0D), EPSILON);
        assertEquals(
            CameraFraming.interpolationFactor(250L, 100.0D),
            CameraFraming.interpolationFactor(900L, 100.0D),
            EPSILON
        );
    }
}

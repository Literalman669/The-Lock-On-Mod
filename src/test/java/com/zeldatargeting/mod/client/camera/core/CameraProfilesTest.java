package com.zeldatargeting.mod.client.camera.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class CameraProfilesTest {
    private static final double EPSILON = 0.000001D;
    private static final float FLOAT_EPSILON = 0.0001F;

    @Test
    public void profileNamesSelectCompleteCuratedValuesWithBalancedFallback() {
        assertEquals("cinematic", CameraProfiles.CINEMATIC.getName());
        assertEquals(180.0D, CameraProfiles.CINEMATIC.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(3.0D, CameraProfiles.CINEMATIC.getSizeDistanceGain(), EPSILON);
        assertEquals(0.12D, CameraProfiles.CINEMATIC.getMaxFovScale(), EPSILON);
        assertEquals(60.0F, CameraProfiles.CINEMATIC.getMaxYawAdjustment(), FLOAT_EPSILON);
        assertEquals(40.0F, CameraProfiles.CINEMATIC.getMaxPitchAdjustment(), FLOAT_EPSILON);

        assertEquals(100.0D, CameraProfiles.BALANCED.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(45.0D, CameraProfiles.SNAPPY.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(0.0D, CameraProfiles.SNAPPY.getMaxFovScale(), EPSILON);
        assertEquals(200L, CameraProfiles.BALANCED.getReleaseDurationMillis());
        assertEquals(4.0D, CameraProfiles.BALANCED.getBaseDistance(), EPSILON);
        assertEquals(1.25D, CameraProfiles.BALANCED.getTransparencyStartDistance(), EPSILON);
        assertEquals(0.45D, CameraProfiles.BALANCED.getTransparencyInnerDistance(), EPSILON);
        assertEquals(0.25F, CameraProfiles.BALANCED.getMinimumPlayerAlpha(), FLOAT_EPSILON);
        assertEquals(0.65D, CameraProfiles.BALANCED.getFallbackEnterDistance(), EPSILON);
        assertEquals(1.25D, CameraProfiles.BALANCED.getFallbackExitDistance(), EPSILON);
        assertEquals(100L, CameraProfiles.BALANCED.getFallbackEnterDelayMillis());
        assertEquals(200L, CameraProfiles.BALANCED.getFallbackExitDelayMillis());

        assertSame(CameraProfiles.BALANCED, CameraProfiles.fromName(null));
        assertSame(CameraProfiles.BALANCED, CameraProfiles.fromName("unknown"));
        assertSame(CameraProfiles.CINEMATIC, CameraProfiles.fromName(" CINEMATIC "));
    }

    @Test
    public void legacyOverridesReturnAValidatedCopyWithoutChangingThePreset() {
        CameraProfile overridden = CameraProfiles.BALANCED.withLegacyOverrides(
            142.5D,
            80.0F,
            55.0F,
            0.1D
        );

        assertEquals(142.5D, overridden.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(80.0F, overridden.getMaxYawAdjustment(), FLOAT_EPSILON);
        assertEquals(55.0F, overridden.getMaxPitchAdjustment(), FLOAT_EPSILON);
        assertEquals(0.1D, overridden.getFocusYOffset(), EPSILON);
        assertEquals(CameraProfiles.BALANCED.getSizeDistanceGain(), overridden.getSizeDistanceGain(), EPSILON);
        assertEquals(100.0D, CameraProfiles.BALANCED.getRotationHalfLifeMillis(), EPSILON);
    }

    @Test
    public void invalidProfileGeometryAndTimingAreRejected() {
        assertInvalid(profile(-1.0D, 1.25D, 0.45D, 0.25F, 0.65D, 1.25D));
        assertInvalid(profile(100.0D, 0.40D, 0.45D, 0.25F, 0.65D, 1.25D));
        assertInvalid(profile(100.0D, 1.25D, 0.45D, 1.1F, 0.65D, 1.25D));
        assertInvalid(profile(100.0D, 1.25D, 0.45D, 0.25F, 1.30D, 1.25D));
        assertInvalid(profile(Double.NaN, 1.25D, 0.45D, 0.25F, 0.65D, 1.25D));
    }

    private static CameraProfile profile(
            double halfLife,
            double alphaStart,
            double alphaInner,
            float minimumAlpha,
            double fallbackEnter,
            double fallbackExit) {
        try {
            return new CameraProfile(
                "test", halfLife, 2.0D, 0.06D, 90.0F, 60.0F, 200L,
                4.0D, alphaStart, alphaInner, minimumAlpha,
                fallbackEnter, fallbackExit, 100L, 200L,
                0.0D, 1.0D
            );
        } catch (IllegalArgumentException expected) {
            return null;
        }
    }

    private static void assertInvalid(CameraProfile profile) {
        if (profile != null) {
            fail("Expected profile validation to reject the supplied values");
        }
    }
}

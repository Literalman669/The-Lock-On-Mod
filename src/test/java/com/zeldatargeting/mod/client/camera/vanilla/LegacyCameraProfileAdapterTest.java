package com.zeldatargeting.mod.client.camera.vanilla;

import com.zeldatargeting.mod.client.camera.core.CameraProfile;
import com.zeldatargeting.mod.config.TargetingConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyCameraProfileAdapterTest {
    private static final double EPSILON = 0.000001D;
    private static final float FLOAT_EPSILON = 0.0001F;

    private float originalSmoothness;
    private float originalMaxYaw;
    private float originalMaxPitch;
    private float originalFocusY;
    private boolean originalPerMode;

    @Before
    public void captureConfig() {
        originalSmoothness = TargetingConfig.cameraSmoothness;
        originalMaxYaw = TargetingConfig.maxYawAdjustment;
        originalMaxPitch = TargetingConfig.maxPitchAdjustment;
        originalFocusY = TargetingConfig.cameraFocusYOffset;
        originalPerMode = TargetingConfig.perModeSmoothingEnabled;
    }

    @After
    public void restoreConfig() {
        TargetingConfig.cameraSmoothness = originalSmoothness;
        TargetingConfig.maxYawAdjustment = originalMaxYaw;
        TargetingConfig.maxPitchAdjustment = originalMaxPitch;
        TargetingConfig.cameraFocusYOffset = originalFocusY;
        TargetingConfig.perModeSmoothingEnabled = originalPerMode;
    }

    @Test
    public void selectedPresetKeepsItsHalfLifeAtTheMatchingLegacyDefault() {
        TargetingConfig.cameraSmoothness = 0.4F;
        TargetingConfig.maxYawAdjustment = 80.0F;
        TargetingConfig.maxPitchAdjustment = 55.0F;
        TargetingConfig.cameraFocusYOffset = 0.1F;
        TargetingConfig.perModeSmoothingEnabled = false;

        CameraProfile profile = new LegacyCameraProfileAdapter().resolve("balanced", 1);

        assertEquals(100.0D, profile.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(80.0F, profile.getMaxYawAdjustment(), FLOAT_EPSILON);
        assertEquals(55.0F, profile.getMaxPitchAdjustment(), FLOAT_EPSILON);
        assertEquals(0.1D, profile.getFocusYOffset(), EPSILON);
    }

    @Test
    public void customSmoothnessAndFirstPersonModeUseTheDocumentedConversion() {
        TargetingConfig.cameraSmoothness = 0.5F;
        TargetingConfig.perModeSmoothingEnabled = true;

        CameraProfile thirdPerson = new LegacyCameraProfileAdapter().resolve("balanced", 1);
        CameraProfile firstPerson = new LegacyCameraProfileAdapter().resolve("balanced", 0);

        assertEquals(142.5D, thirdPerson.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(237.5D, firstPerson.getRotationHalfLifeMillis(), EPSILON);
    }

    @Test
    public void unknownPresetAndNonFiniteAdvancedValuesFallBackSafely() {
        TargetingConfig.cameraSmoothness = 0.4F;
        TargetingConfig.maxYawAdjustment = Float.NaN;
        TargetingConfig.maxPitchAdjustment = Float.POSITIVE_INFINITY;
        TargetingConfig.cameraFocusYOffset = Float.NaN;

        CameraProfile profile = new LegacyCameraProfileAdapter().resolve("missing", 1);

        assertEquals("balanced", profile.getName());
        assertEquals(100.0D, profile.getRotationHalfLifeMillis(), EPSILON);
        assertEquals(90.0F, profile.getMaxYawAdjustment(), FLOAT_EPSILON);
        assertEquals(60.0F, profile.getMaxPitchAdjustment(), FLOAT_EPSILON);
        assertEquals(0.0D, profile.getFocusYOffset(), EPSILON);
    }
}

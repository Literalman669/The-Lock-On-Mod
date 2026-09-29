package com.zeldatargeting.mod.client.camera.compat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CameraRotationPolicyTest {

    @Test
    public void activeShoulderSurfingOwnsOffsetButPlayerOwnsRotation() {
        CameraRotationPolicy.Decision decision = CameraRotationPolicy.resolve(true);

        assertEquals(
            CameraRotationPolicy.CameraHost.SHOULDER_SURFING,
            decision.getCameraHost()
        );
        assertEquals(
            CameraRotationPolicy.RotationOwner.PLAYER,
            decision.getRotationOwner()
        );
        assertEquals(
            CameraRotationPolicy.OffsetOwner.CAMERA_HOST,
            decision.getOffsetOwner()
        );
    }

    @Test
    public void inactiveShoulderSurfingUsesVanillaOwnership() {
        CameraRotationPolicy.Decision decision = CameraRotationPolicy.resolve(false);

        assertEquals(
            CameraRotationPolicy.CameraHost.VANILLA,
            decision.getCameraHost()
        );
        assertEquals(
            CameraRotationPolicy.RotationOwner.PLAYER,
            decision.getRotationOwner()
        );
        assertEquals(
            CameraRotationPolicy.OffsetOwner.ZELDA,
            decision.getOffsetOwner()
        );
    }

    @Test
    public void onlyTheVanillaCameraLetsZeldaFadeThePlayer() {
        assertTrue(CameraRotationPolicy.resolve(false).ownsPlayerTransparency());
        assertFalse(CameraRotationPolicy.resolve(true).ownsPlayerTransparency());
    }

    @Test
    public void activeShoulderSurfingDoesNotRequestInputRemapping() {
        CameraRotationPolicy.Decision decision = CameraRotationPolicy.resolve(true);

        assertFalse(decision.shouldRemapMovement());
    }
}

package com.zeldatargeting.mod.client.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CameraMathTest {
    private static final float EPSILON = 0.0001F;

    @Test
    public void wrapDegreesUsesShortestSignedDifference() {
        assertEquals(-170.0F, CameraMath.wrapDegrees(190.0F), EPSILON);
        assertEquals(170.0F, CameraMath.wrapDegrees(-190.0F), EPSILON);
        assertEquals(45.0F, CameraMath.wrapDegrees(405.0F), EPSILON);
    }

    @Test
    public void interpolateAngleCrossesTheBoundaryByTheShortPath() {
        assertEquals(180.0F, CameraMath.interpolateAngle(170.0F, -170.0F, 0.5F), EPSILON);
        assertEquals(-180.0F, CameraMath.interpolateAngle(-170.0F, 170.0F, 0.5F), EPSILON);
    }

    @Test
    public void lookAtMatchesMinecraftYawAndPitchConventions() {
        CameraMath.Rotation forward = CameraMath.lookAt(0.0D, 0.0D, 1.0D);
        assertEquals(0.0F, forward.getYaw(), EPSILON);
        assertEquals(0.0F, forward.getPitch(), EPSILON);

        CameraMath.Rotation right = CameraMath.lookAt(1.0D, 0.0D, 0.0D);
        assertEquals(-90.0F, right.getYaw(), EPSILON);
        assertEquals(0.0F, right.getPitch(), EPSILON);

        CameraMath.Rotation aboveForward = CameraMath.lookAt(0.0D, 1.0D, 1.0D);
        assertEquals(0.0F, aboveForward.getYaw(), EPSILON);
        assertEquals(-45.0F, aboveForward.getPitch(), EPSILON);
    }

    @Test
    public void zeroLengthLookAtReturnsNeutralRotation() {
        CameraMath.Rotation rotation = CameraMath.lookAt(0.0D, 0.0D, 0.0D);
        assertEquals(0.0F, rotation.getYaw(), EPSILON);
        assertEquals(0.0F, rotation.getPitch(), EPSILON);
    }

    @Test
    public void interpolationAndClampHaveDefinedBounds() {
        assertEquals(4.0F, CameraMath.interpolate(0.0F, 10.0F, 0.4F), EPSILON);
        assertEquals(90.0F, CameraMath.clamp(120.0F, -90.0F, 90.0F), EPSILON);
        assertEquals(-90.0F, CameraMath.clamp(-120.0F, -90.0F, 90.0F), EPSILON);
    }
}

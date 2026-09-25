package com.zeldatargeting.mod.client.camera.compat;

import com.zeldatargeting.mod.client.math.CameraMath;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EpicFightAimPolicyTest {
    private static final ShoulderCameraState LEFT_SHOULDER =
        ShoulderCameraState.active(-1.55D, 0.0D, 3.0D, 3.375D);

    @Test
    public void epicFightBattleFacesTheTargetFromThePlayersEye() {
        ShoulderAimSolver.AimVector aim = EpicFightAimPolicy.aim(
            2.0D, -0.5D, 0.0D, LEFT_SHOULDER, true
        );

        assertEquals(2.0D, aim.getX(), 0.0D);
        assertEquals(-0.5D, aim.getY(), 0.0D);
        assertEquals(0.0D, aim.getZ(), 0.0D);
        assertEquals(-90.0F, CameraMath.lookAt(
            aim.getX(), aim.getY(), aim.getZ()
        ).getYaw(), 0.001F);
    }

    @Test
    public void shoulderOnlyRetainsTheCompensatedAttackRay() {
        ShoulderAimSolver.AimVector aim = EpicFightAimPolicy.aim(
            2.0D, -0.5D, 0.0D, LEFT_SHOULDER, false
        );

        assertTrue(aim.isCompensated());
        assertTrue(Math.abs(CameraMath.lookAt(
            aim.getX(), aim.getY(), aim.getZ()
        ).getYaw() + 90.0F) > 10.0F);
    }

    @Test
    public void epicFightWithoutShoulderSurfingKeepsTheEyeRay() {
        ShoulderAimSolver.AimVector aim = EpicFightAimPolicy.aim(
            2.0D, -0.5D, 0.0D, ShoulderCameraState.inactive(), true
        );

        assertEquals(2.0D, aim.getX(), 0.0D);
        assertEquals(-0.5D, aim.getY(), 0.0D);
        assertEquals(0.0D, aim.getZ(), 0.0D);
    }
}

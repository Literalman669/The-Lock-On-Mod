package com.zeldatargeting.mod.client.camera.compat;

import net.minecraft.util.math.Vec3d;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShoulderAimSolverTest {
    private static final double EPSILON = 0.000001D;
    private static final double MAXIMUM_RAY_MISS = 0.001D;

    @Test
    public void inactiveStateReturnsVanillaTargetVector() {
        ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(
            0.0D,
            0.0D,
            6.0D,
            ShoulderCameraState.inactive()
        );

        assertEquals(0.0D, result.getX(), EPSILON);
        assertEquals(0.0D, result.getY(), EPSILON);
        assertEquals(6.0D, result.getZ(), EPSILON);
        assertFalse(result.isCompensated());
    }

    @Test
    public void leftShoulderRayConvergesOnCloseTarget() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.875D,
            0.0D,
            3.0D,
            3.125D
        );

        ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(
            0.0D,
            -0.25D,
            3.0D,
            state
        );

        assertTrue(result.isCompensated());
        assertRayHitsTarget(result, state, 0.0D, -0.25D, 3.0D);
    }

    @Test
    public void rightShoulderRayConvergesAcrossYawWrap() {
        ShoulderCameraState state = ShoulderCameraState.active(
            0.875D,
            0.2D,
            3.0D,
            3.15D
        );

        ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(
            0.05D,
            0.4D,
            -5.0D,
            state
        );

        assertTrue(result.isCompensated());
        assertRayHitsTarget(result, state, 0.05D, 0.4D, -5.0D);
    }

    @Test
    public void shoulderRayConvergesOnDistantElevatedTarget() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.875D,
            0.4D,
            3.0D,
            3.15D
        );

        ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(
            12.0D,
            4.0D,
            24.0D,
            state
        );

        assertRayHitsTarget(result, state, 12.0D, 4.0D, 24.0D);
    }

    @Test
    public void shoulderRayConvergesOnNegativeXTarget() {
        ShoulderCameraState state = ShoulderCameraState.active(
            0.875D,
            -0.2D,
            3.0D,
            3.15D
        );

        ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(
            -4.0D,
            -0.5D,
            2.0D,
            state
        );

        assertRayHitsTarget(result, state, -4.0D, -0.5D, 2.0D);
    }

    @Test
    public void nonFiniteTargetReturnsSafeZeroVector() {
        ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(
            Double.NaN,
            0.0D,
            4.0D,
            ShoulderCameraState.active(-0.875D, 0.0D, 3.0D, 3.125D)
        );

        assertEquals(0.0D, result.getX(), EPSILON);
        assertEquals(0.0D, result.getY(), EPSILON);
        assertEquals(0.0D, result.getZ(), EPSILON);
        assertFalse(result.isCompensated());
    }

    private static void assertRayHitsTarget(
            ShoulderAimSolver.AimVector aim,
            ShoulderCameraState state,
            double targetX,
            double targetY,
            double targetZ) {
        Vec3d view = new Vec3d(aim.getX(), aim.getY(), aim.getZ()).normalize();
        double horizontal = Math.sqrt(
            aim.getX() * aim.getX() + aim.getZ() * aim.getZ()
        );
        float yaw = (float) Math.toDegrees(Math.atan2(-aim.getX(), aim.getZ()));
        float pitch = (float) -Math.toDegrees(Math.atan2(aim.getY(), horizontal));
        Vec3d localOffset = new Vec3d(
            state.getOffsetX(),
            state.getOffsetY(),
            -state.getOffsetZ()
        );
        Vec3d cameraOffset = localOffset
            .normalize()
            .scale(state.getCameraDistance())
            .rotatePitch((float) Math.toRadians(-pitch))
            .rotateYaw((float) Math.toRadians(-yaw));
        Vec3d lateralOrigin = cameraOffset.subtract(
            view.scale(cameraOffset.dotProduct(view))
        );
        Vec3d targetFromOrigin = new Vec3d(targetX, targetY, targetZ)
            .subtract(lateralOrigin);
        double missDistance = targetFromOrigin.crossProduct(view).lengthVector();

        assertTrue(
            "SSR ray misses target by " + missDistance + " blocks",
            missDistance < MAXIMUM_RAY_MISS
        );
    }
}

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
        assertRayMissesByAtMost(aim, state, targetX, targetY, targetZ, MAXIMUM_RAY_MISS);
    }

    private static double rayMiss(
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
        return targetFromOrigin.crossProduct(view).lengthVector();
    }

    @Test
    public void closeTargetsTurnAtMostTheCapWithoutFlipping() {
        // SSR 2.9.6 defaults: 0.75 block right-shoulder offset, 3 blocks back.
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.75D,
            0.0D,
            3.0D,
            Math.sqrt(0.75D * 0.75D + 9.0D)
        );
        double previousDeviation = 0.0D;
        for (double distance = 3.0D; distance >= 0.1D; distance -= 0.05D) {
            ShoulderAimSolver.AimVector result = ShoulderAimSolver.compensate(0.0D, 0.0D, distance, state);
            double deviation = yawDegrees(result);

            assertTrue("turned " + deviation + " degrees at " + distance + " blocks",
                Math.abs(deviation) <= ShoulderAimSolver.MAX_CORRECTION_DEGREES + 0.01D);
            assertTrue("correction flipped sides at " + distance + " blocks",
                previousDeviation == 0.0D || Math.signum(deviation) == Math.signum(previousDeviation));
            previousDeviation = deviation;
        }
    }

    @Test
    public void tinyTargetMovementCausesOnlyATinyTurnUpClose() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.75D,
            0.0D,
            3.0D,
            Math.sqrt(0.75D * 0.75D + 9.0D)
        );

        double before = yawDegrees(ShoulderAimSolver.compensate(0.0D, 0.0D, 0.7D, state));
        double after = yawDegrees(ShoulderAimSolver.compensate(0.01D, 0.0D, 0.7D, state));

        // Moving 1 cm at 0.7 blocks shifts the direct line by under a degree.
        assertTrue("turned " + Math.abs(after - before) + " degrees", Math.abs(after - before) < 1.0D);
    }

    @Test
    public void closeTargetRayStillPassesWithinAZombieWideHitbox() {
        ShoulderCameraState state = ShoulderCameraState.active(
            -0.75D,
            0.0D,
            3.0D,
            Math.sqrt(0.75D * 0.75D + 9.0D)
        );

        // Zombie hitbox half-width 0.3 plus Minecraft's 0.1 pick border.
        assertRayMissesByAtMost(ShoulderAimSolver.compensate(0.0D, 0.0D, 1.0D, state),
            state, 0.0D, 0.0D, 1.0D, 0.4D);
        assertRayMissesByAtMost(ShoulderAimSolver.compensate(0.0D, 0.0D, 1.5D, state),
            state, 0.0D, 0.0D, 1.5D, 0.4D);
    }

    private static double yawDegrees(ShoulderAimSolver.AimVector aim) {
        return Math.toDegrees(Math.atan2(-aim.getX(), aim.getZ()));
    }

    private static void assertRayMissesByAtMost(
            ShoulderAimSolver.AimVector aim,
            ShoulderCameraState state,
            double targetX,
            double targetY,
            double targetZ,
            double maximumMiss) {
        double missDistance = rayMiss(aim, state, targetX, targetY, targetZ);
        assertTrue(
            "SSR ray misses target by " + missDistance + " blocks",
            missDistance <= maximumMiss
        );
    }
}

package com.zeldatargeting.mod.client.camera.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CameraDirectorTest {
    private static final float FLOAT_EPSILON = 0.0001F;
    private static final double DOUBLE_EPSILON = 0.000001D;

    @Test
    public void twoFiftyMillisecondStepsEqualOneHundredMillisecondStep() {
        CameraDirector oneStep = new CameraDirector();
        oneStep.begin(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);
        CameraFrame one = oneStep.update(
            input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 100L, false),
            CameraProfiles.BALANCED
        );

        CameraDirector twoSteps = new CameraDirector();
        twoSteps.begin(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);
        twoSteps.update(
            input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 50L, false),
            CameraProfiles.BALANCED
        );
        CameraFrame two = twoSteps.update(
            input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 50L, false),
            CameraProfiles.BALANCED
        );

        assertEquals(one.getYaw(), two.getYaw(), FLOAT_EPSILON);
        assertEquals(-45.0F, one.getYaw(), FLOAT_EPSILON);
    }

    @Test
    public void steadyTargetMotionDoesNotLeaveTheCameraBehind() {
        CameraDirector director = new CameraDirector();
        director.begin(
            input(0.0F, 0.0F, 0.0D, 0.0D, 1.0D, 0L, false),
            CameraProfiles.BALANCED
        );

        CameraFrame frame = director.update(
            input(
                0.0F,
                0.0F,
                0.087156D,
                0.0D,
                0.996195D,
                50L,
                false
            ),
            CameraProfiles.BALANCED
        );

        assertEquals(-5.0F, frame.getYaw(), FLOAT_EPSILON);
    }

    @Test
    public void largeTargetJumpStillTurnsTowardTheNewTarget() {
        CameraDirector director = new CameraDirector();
        director.begin(
            input(0.0F, 0.0F, 0.0D, 0.0D, 1.0D, 0L, false),
            CameraProfiles.BALANCED
        );

        CameraFrame frame = director.update(
            input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 50L, false),
            CameraProfiles.BALANCED
        );

        assertTrue(frame.getYaw() < 0.0F);
        assertTrue(frame.getYaw() > -90.0F);
    }

    @Test
    public void wrappedYawTakesTheTwoDegreePathAcrossTheBoundary() {
        CameraDirector director = new CameraDirector();
        director.begin(input(179.0F, 0.0F, 0.017452D, 0.0D, -0.999848D, 0L, false), CameraProfiles.BALANCED);

        CameraFrame frame = director.update(
            input(179.0F, 0.0F, 0.017452D, 0.0D, -0.999848D, 100L, false),
            CameraProfiles.BALANCED
        );

        assertEquals(180.0F, frame.getYaw(), 0.01F);
    }

    @Test
    public void yawPitchAdjustmentAndPitchRangeStayBounded() {
        CameraDirector director = new CameraDirector();
        director.begin(input(0.0F, 80.0F, 0.0D, 100.0D, 0.0D, 0L, false), CameraProfiles.CINEMATIC);

        CameraFrame frame = director.update(
            input(0.0F, 80.0F, 0.0D, 100.0D, 0.0D, 180L, false),
            CameraProfiles.CINEMATIC
        );

        assertEquals(60.0F, frame.getPitch(), FLOAT_EPSILON);
        assertTrue(frame.getPitch() >= -90.0F && frame.getPitch() <= 90.0F);
    }

    @Test
    public void freeLookStopsRotationAndReleaseReseedsFromActualPlayerYaw() {
        CameraDirector director = new CameraDirector();
        director.begin(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);

        CameraFrame held = director.update(
            input(30.0F, 5.0F, 1.0D, 0.0D, 0.0D, 50L, true),
            CameraProfiles.BALANCED
        );
        CameraFrame returned = director.update(
            input(30.0F, 5.0F, 1.0D, 0.0D, 0.0D, 100L, false),
            CameraProfiles.BALANCED
        );

        assertFalse(held.shouldApplyRotation());
        assertTrue(returned.shouldApplyRotation());
        assertEquals(-15.0F, returned.getYaw(), FLOAT_EPSILON);
    }

    @Test
    public void framingValuesAndSnappyZeroFovReachTheOutputFrame() {
        CameraDirector director = new CameraDirector();
        CameraCollisionResult collision = new CameraCollisionResult(0.85D, 1.0D, -0.15D);
        CameraInput sample = input(
            0.0F, 0.0F, 1.0D, 0.0D, 0.0D,
            8.0D, 8.0D, 20.0D, collision, 45L, false, 1
        );
        director.begin(sample, CameraProfiles.SNAPPY);

        CameraFrame frame = director.update(sample, CameraProfiles.SNAPPY);

        assertEquals(0.85D, frame.getCameraDistance(), DOUBLE_EPSILON);
        assertEquals(-0.15D, frame.getTranslationDelta(), DOUBLE_EPSILON);
        assertEquals(1.0D, frame.getFovMultiplier(), DOUBLE_EPSILON);
        assertEquals(0.625F, frame.getPlayerAlpha(), FLOAT_EPSILON);
    }

    @Test
    public void distanceAndFovEaseOutwardButCollisionContractionIsImmediate() {
        CameraDirector director = new CameraDirector();
        CameraInput initial = input(
            0.0F, 0.0F, 1.0D, 0.0D, 0.0D,
            0.6D, 1.8D, 20.0D,
            new CameraCollisionResult(4.0D, 4.0D, 0.0D),
            0L, false, 1
        );
        director.begin(initial, CameraProfiles.BALANCED);

        CameraFrame expanded = director.update(
            input(
                0.0F, 0.0F, 1.0D, 0.0D, 0.0D,
                8.0D, 8.0D, 20.0D,
                new CameraCollisionResult(6.0D, 4.0D, 2.0D),
                100L, false, 1
            ),
            CameraProfiles.BALANCED
        );
        CameraFrame contracted = director.update(
            input(
                0.0F, 0.0F, 1.0D, 0.0D, 0.0D,
                8.0D, 8.0D, 20.0D,
                new CameraCollisionResult(0.5D, 0.6D, -0.1D),
                100L, false, 1
            ),
            CameraProfiles.BALANCED
        );

        assertEquals(5.0D, expanded.getCameraDistance(), DOUBLE_EPSILON);
        assertEquals(1.0D, expanded.getTranslationDelta(), DOUBLE_EPSILON);
        assertEquals(1.03D, expanded.getFovMultiplier(), DOUBLE_EPSILON);
        assertEquals(0.5D, contracted.getCameraDistance(), DOUBLE_EPSILON);
        assertEquals(-0.1D, contracted.getTranslationDelta(), DOUBLE_EPSILON);
    }

    @Test
    public void nonFiniteCandidateRetainsTheLastValidFrame() {
        CameraDirector director = new CameraDirector();
        CameraInput valid = input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 100L, false);
        director.begin(valid, CameraProfiles.BALANCED);
        CameraFrame before = director.update(valid, CameraProfiles.BALANCED);

        CameraFrame after = director.update(
            input(0.0F, 0.0F, Double.NaN, 0.0D, 0.0D, 100L, false),
            CameraProfiles.BALANCED
        );

        assertSame(before, after);
    }

    @Test
    public void normalRestoreEasesPresentationWithoutApplyingOldRotation() {
        CameraDirector director = new CameraDirector();
        CameraCollisionResult collision = new CameraCollisionResult(6.9D, 4.0D, 2.9D);
        CameraInput sample = input(
            10.0F, 2.0F, 1.0D, 0.0D, 0.0D,
            8.0D, 8.0D, 20.0D, collision, 100L, false, 1
        );
        director.begin(sample, CameraProfiles.BALANCED);
        director.update(sample, CameraProfiles.BALANCED);
        director.beginRestore(CameraRestoreMode.EASED, 1000L);

        CameraFrame midpoint = director.updateRestore(1100L, CameraProfiles.BALANCED, 1);
        CameraFrame complete = director.updateRestore(1200L, CameraProfiles.BALANCED, 1);

        assertFalse(midpoint.shouldApplyRotation());
        assertFalse(midpoint.isRestoreComplete());
        assertEquals(5.45D, midpoint.getCameraDistance(), DOUBLE_EPSILON);
        assertEquals(1.45D, midpoint.getTranslationDelta(), DOUBLE_EPSILON);
        assertEquals(1.0D, complete.getFovMultiplier(), DOUBLE_EPSILON);
        assertEquals(0.0D, complete.getTranslationDelta(), DOUBLE_EPSILON);
        assertEquals(1.0F, complete.getPlayerAlpha(), FLOAT_EPSILON);
        assertTrue(complete.isRestoreComplete());
        assertFalse(director.isActive());
    }

    @Test
    public void immediateRestoreReturnsBaselineAndClearsState() {
        CameraDirector director = new CameraDirector();
        CameraInput sample = input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 100L, false);
        director.begin(sample, CameraProfiles.BALANCED);
        director.update(sample, CameraProfiles.BALANCED);

        CameraFrame frame = director.beginRestore(CameraRestoreMode.IMMEDIATE, 1000L);

        assertEquals(4.0D, frame.getCameraDistance(), DOUBLE_EPSILON);
        assertEquals(0.0D, frame.getTranslationDelta(), DOUBLE_EPSILON);
        assertEquals(1.0D, frame.getFovMultiplier(), DOUBLE_EPSILON);
        assertEquals(1.0F, frame.getPlayerAlpha(), FLOAT_EPSILON);
        assertFalse(frame.shouldApplyRotation());
        assertTrue(frame.isRestoreComplete());
        assertFalse(director.isActive());
        assertFalse(director.isRestoring());
    }

    private static CameraInput input(
            float playerYaw,
            float playerPitch,
            double targetDeltaX,
            double targetDeltaY,
            double targetDeltaZ,
            long elapsedMillis,
            boolean freeLook) {
        return input(
            playerYaw, playerPitch, targetDeltaX, targetDeltaY, targetDeltaZ,
            0.6D, 1.8D, 6.0D,
            new CameraCollisionResult(4.0D, 4.0D, 0.0D),
            elapsedMillis, freeLook, 1
        );
    }

    private static CameraInput input(
            float playerYaw,
            float playerPitch,
            double targetDeltaX,
            double targetDeltaY,
            double targetDeltaZ,
            double targetWidth,
            double targetHeight,
            double targetDistance,
            CameraCollisionResult collision,
            long elapsedMillis,
            boolean freeLook,
            int requestedPerspective) {
        return new CameraInput(
            playerYaw,
            playerPitch,
            targetDeltaX,
            targetDeltaY,
            targetDeltaZ,
            targetWidth,
            targetHeight,
            targetDistance,
            collision,
            elapsedMillis,
            freeLook,
            requestedPerspective
        );
    }
}

package com.zeldatargeting.mod.client.camera.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CameraCollisionSolverTest {
    private static final double EPSILON = 0.000001D;

    @Test
    public void shortestOfEightSamplesConstrainsDesiredAndVanillaDistance() {
        CameraCollisionSamples samples = new CameraCollisionSamples(
            7.0D, 6.0D, 5.0D, 3.0D, 7.0D, 7.0D, 7.0D, 7.0D
        );

        CameraCollisionResult result = CameraCollisionSolver.solve(6.0D, samples);

        assertEquals(2.9D, result.getSafeDistance(), EPSILON);
        assertEquals(3.0D, result.getVanillaDistance(), EPSILON);
        assertEquals(-0.1D, result.getTranslationDelta(), EPSILON);
    }

    @Test
    public void unobstructedLargeTargetExtendsBeyondVanillaFourBlocks() {
        CameraCollisionResult result = CameraCollisionSolver.solve(
            7.0D,
            CameraCollisionSamples.uniform(7.0D)
        );

        assertEquals(6.9D, result.getSafeDistance(), EPSILON);
        assertEquals(4.0D, result.getVanillaDistance(), EPSILON);
        assertEquals(2.9D, result.getTranslationDelta(), EPSILON);
    }

    @Test
    public void invalidSampleIsBlockedAndNeverExtendsTheCamera() {
        CameraCollisionSamples samples = new CameraCollisionSamples(
            7.0D, Double.NaN, 7.0D, 7.0D, 7.0D, 7.0D, 7.0D, 7.0D
        );

        CameraCollisionResult result = CameraCollisionSolver.solve(7.0D, samples);

        assertEquals(0.0D, result.getSafeDistance(), EPSILON);
        assertEquals(0.0D, result.getVanillaDistance(), EPSILON);
        assertEquals(0.0D, result.getTranslationDelta(), EPSILON);
    }

    @Test
    public void everyFixedSampleParticipatesInTheMinimum() {
        assertEquals(1.0D, new CameraCollisionSamples(1, 9, 9, 9, 9, 9, 9, 9).minimumValidDistance(), EPSILON);
        assertEquals(2.0D, new CameraCollisionSamples(9, 2, 9, 9, 9, 9, 9, 9).minimumValidDistance(), EPSILON);
        assertEquals(3.0D, new CameraCollisionSamples(9, 9, 3, 9, 9, 9, 9, 9).minimumValidDistance(), EPSILON);
        assertEquals(4.0D, new CameraCollisionSamples(9, 9, 9, 4, 9, 9, 9, 9).minimumValidDistance(), EPSILON);
        assertEquals(5.0D, new CameraCollisionSamples(9, 9, 9, 9, 5, 9, 9, 9).minimumValidDistance(), EPSILON);
        assertEquals(6.0D, new CameraCollisionSamples(9, 9, 9, 9, 9, 6, 9, 9).minimumValidDistance(), EPSILON);
        assertEquals(7.0D, new CameraCollisionSamples(9, 9, 9, 9, 9, 9, 7, 9).minimumValidDistance(), EPSILON);
        assertEquals(8.0D, new CameraCollisionSamples(9, 9, 9, 9, 9, 9, 9, 8).minimumValidDistance(), EPSILON);
    }

    @Test
    public void negativeDesiredDistanceAndMissingSamplesFailClosed() {
        CameraCollisionResult negative = CameraCollisionSolver.solve(
            -4.0D,
            CameraCollisionSamples.uniform(7.0D)
        );
        CameraCollisionResult missing = CameraCollisionSolver.solve(7.0D, null);

        assertEquals(0.0D, negative.getSafeDistance(), EPSILON);
        assertEquals(4.0D, negative.getVanillaDistance(), EPSILON);
        assertEquals(-4.0D, negative.getTranslationDelta(), EPSILON);
        assertEquals(0.0D, missing.getSafeDistance(), EPSILON);
        assertEquals(0.0D, missing.getVanillaDistance(), EPSILON);
        assertEquals(0.0D, missing.getTranslationDelta(), EPSILON);
    }
}

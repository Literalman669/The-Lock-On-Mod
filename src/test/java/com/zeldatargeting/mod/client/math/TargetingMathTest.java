package com.zeldatargeting.mod.client.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TargetingMathTest {
    private static final double EPSILON = 0.0001D;

    @Test
    public void horizontalBearingUsesStableClockwiseMinecraftDirections() {
        assertEquals(0.0D, TargetingMath.horizontalBearing(0.0D, -1.0D), EPSILON);
        assertEquals(90.0D, TargetingMath.horizontalBearing(1.0D, 0.0D), EPSILON);
        assertEquals(180.0D, TargetingMath.horizontalBearing(0.0D, 1.0D), EPSILON);
        assertEquals(270.0D, TargetingMath.horizontalBearing(-1.0D, 0.0D), EPSILON);
    }

    @Test
    public void angleDegreesHandlesNormalizedAndUnnormalizedVectors() {
        assertEquals(0.0D, TargetingMath.angleDegrees(0, 0, 2, 0, 0, 10), EPSILON);
        assertEquals(90.0D, TargetingMath.angleDegrees(0, 0, 1, 1, 0, 0), EPSILON);
        assertEquals(180.0D, TargetingMath.angleDegrees(0, 0, 1, 0, 0, -5), EPSILON);
    }

    @Test
    public void angleDegreesRejectsZeroLengthInputAsOutsideTheCone() {
        assertEquals(180.0D, TargetingMath.angleDegrees(0, 0, 0, 1, 0, 0), EPSILON);
        assertEquals(180.0D, TargetingMath.angleDegrees(0, 0, 1, 0, 0, 0), EPSILON);
    }

    @Test
    public void adjacentIndexWrapsInBothDirections() {
        assertEquals(1, TargetingMath.adjacentIndex(0, 3, true));
        assertEquals(0, TargetingMath.adjacentIndex(2, 3, true));
        assertEquals(2, TargetingMath.adjacentIndex(0, 3, false));
        assertEquals(1, TargetingMath.adjacentIndex(2, 3, false));
    }

    @Test
    public void adjacentIndexDefinesEmptyAndMissingCurrentBehavior() {
        assertEquals(-1, TargetingMath.adjacentIndex(0, 0, true));
        assertEquals(0, TargetingMath.adjacentIndex(-1, 3, true));
        assertEquals(2, TargetingMath.adjacentIndex(-1, 3, false));
    }

    @Test
    public void meleeReachMatchesVanillaAndAddsModdedBonus() {
        assertEquals(3.0D, TargetingMath.meleeReach(false, 0.0D), EPSILON);
        assertEquals(6.0D, TargetingMath.meleeReach(true, 0.0D), EPSILON);
        assertEquals(5.0D, TargetingMath.meleeReach(false, 2.0D), EPSILON);
        assertEquals(3.0D, TargetingMath.meleeReach(false, -1.0D), EPSILON);
        assertEquals(3.0D, TargetingMath.meleeReach(false, Double.NaN), EPSILON);
    }

    @Test
    public void distanceToBoxMeasuresToTheNearestFaceEdgeOrCorner() {
        // Iron golem sized box (1.4 wide, 2.7 tall) centered 3.5 blocks ahead on z.
        assertEquals(2.8D, TargetingMath.distanceToBox(0, 1.62, 0, -0.7, 0, 2.8, 0.7, 2.7, 4.2), EPSILON);
        assertEquals(5.0D, TargetingMath.distanceToBox(-3, 0, -4, 0, 0, 0, 1, 1, 1), EPSILON);
        assertEquals(0.0D, TargetingMath.distanceToBox(0.5, 0.5, 0.5, 0, 0, 0, 1, 1, 1), EPSILON);
    }
}

package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TargetAnchorResolverTest {
    private static final double EPSILON = 0.0001D;

    @Test
    public void headUsesEyeHeightAndClampsToBoundingBoxTop() {
        TargetPoint normal = TargetAnchorResolver.resolve(TargetAnchor.HEAD, 4, 10, 8, 12, 1.62D);
        TargetPoint clamped = TargetAnchorResolver.resolve(TargetAnchor.HEAD, 4, 10, 8, 11, 2.5D);
        assertEquals(4.0D, normal.getX(), EPSILON);
        assertEquals(11.62D, normal.getY(), EPSILON);
        assertEquals(8.0D, normal.getZ(), EPSILON);
        assertEquals(11.0D, clamped.getY(), EPSILON);
    }

    @Test
    public void centerUsesBoundingBoxMidpoint() {
        assertEquals(11.0D,
            TargetAnchorResolver.resolve(TargetAnchor.CENTER, 4, 10, 8, 12, 1.62D).getY(), EPSILON);
    }

    @Test
    public void lowerBodyUsesFirstQuarterOfBoundingBoxHeight() {
        assertEquals(10.5D,
            TargetAnchorResolver.resolve(TargetAnchor.LOWER_BODY, 4, 10, 8, 12, 1.62D).getY(), EPSILON);
    }

    @Test
    public void missingAnchorFallsBackToCenter() {
        assertEquals(11.0D,
            TargetAnchorResolver.resolve(null, 4, 10, 8, 12, 1.62D).getY(), EPSILON);
    }

    @Test
    public void combatDefaultCentersTheTargetForReliableAttackAim() {
        TargetPoint focus = TargetAnchorResolver.resolve(
            TargetAnchor.combatDefault(),
            4,
            10,
            8,
            12,
            1.62D
        );

        assertEquals(11.0D, focus.getY(), EPSILON);
    }

    @Test
    public void positiveVerticalOffsetStaysBelowHitboxTop() {
        assertEquals(11.8D,
            TargetAnchorResolver.offsetWithinHitbox(11.62D, 10.0D, 12.0D, 0.2D),
            EPSILON);
    }

    @Test
    public void negativeVerticalOffsetStaysAboveHitboxBottom() {
        assertEquals(10.2D,
            TargetAnchorResolver.offsetWithinHitbox(11.0D, 10.0D, 12.0D, -1.0D),
            EPSILON);
    }

    @Test
    public void anchorCyclingWrapsInBothDirections() {
        assertEquals(TargetAnchor.CENTER, TargetAnchor.HEAD.cycle(true));
        assertEquals(TargetAnchor.LOWER_BODY, TargetAnchor.CENTER.cycle(true));
        assertEquals(TargetAnchor.HEAD, TargetAnchor.LOWER_BODY.cycle(true));
        assertEquals(TargetAnchor.LOWER_BODY, TargetAnchor.HEAD.cycle(false));
    }
}

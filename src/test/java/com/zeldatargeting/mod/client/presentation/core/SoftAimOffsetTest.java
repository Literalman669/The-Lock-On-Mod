package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SoftAimOffsetTest {
    @Test
    public void targetToTheRightShowsRightOfTheCrosshairWhateverTheFacing() {
        // Facing south (+Z), the player's right is west (-X).
        SoftAimOffset south = SoftAimOffset.compute(0.0F, 0.0F, -0.3D, 0.0D, 1.0D);
        // Facing north (-Z), the player's right is east (+X).
        SoftAimOffset north = SoftAimOffset.compute(180.0F, 0.0F, 0.3D, 0.0D, -1.0D);
        // Facing west (-X), the player's right is north (-Z).
        SoftAimOffset west = SoftAimOffset.compute(90.0F, 0.0F, -1.0D, 0.0D, -0.3D);

        for (SoftAimOffset offset : new SoftAimOffset[] { south, north, west }) {
            assertTrue("x=" + offset.getX(), offset.getX() > 0);
            assertEquals(0, offset.getY());
        }
    }

    @Test
    public void targetAboveShowsAboveTheCrosshair() {
        SoftAimOffset offset = SoftAimOffset.compute(0.0F, 0.0F, 0.0D, 0.3D, 1.0D);

        assertEquals(0, offset.getX());
        assertTrue("y=" + offset.getY(), offset.getY() < 0);
    }

    @Test
    public void lookingStraightDownStillHasAStableRightDirection() {
        // Looking straight down while facing south: a target a little west is to the right.
        SoftAimOffset offset = SoftAimOffset.compute(0.0F, 90.0F, -0.3D, -1.0D, 0.0D);

        assertTrue("x=" + offset.getX(), offset.getX() > 0);
    }

    @Test
    public void centeredTargetSitsOnTheCrosshairAndIsInvisible() {
        SoftAimOffset offset = SoftAimOffset.compute(0.0F, 0.0F, 0.0D, 0.0D, 5.0D);

        assertEquals(0, offset.getX());
        assertEquals(0, offset.getY());
        assertEquals(0, offset.getAlpha());
    }

    @Test
    public void targetsFarOffScreenOrDegenerateAreHidden() {
        assertNull(SoftAimOffset.compute(0.0F, 0.0F, 0.0D, 0.0D, -1.0D));
        assertNull(SoftAimOffset.compute(0.0F, 0.0F, 0.0D, 0.0D, 0.0D));
        assertNull(SoftAimOffset.compute(0.0F, 0.0F, Double.NaN, 0.0D, 1.0D));
    }
}

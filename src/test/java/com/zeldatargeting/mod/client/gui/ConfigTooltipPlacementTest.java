package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import java.util.function.IntUnaryOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigTooltipPlacementTest {
    private static final IntUnaryOperator HEIGHT_30 = width -> 30;

    @Test
    public void prefersTheRightOfTheColumn() {
        ConfigRect hovered = new ConfigRect(217, 100, 420, 20);

        ConfigRect placed = place(854, 450, hovered, 217, 637, HEIGHT_30);

        assertEquals(645, placed.getX());
        assertEquals(200, placed.getWidth());
        assertEquals(100, placed.getY());
    }

    @Test
    public void fallsBackToTheLeft() {
        ConfigRect hovered = new ConfigRect(330, 100, 420, 20);

        ConfigRect placed = place(854, 450, hovered, 330, 750, HEIGHT_30);

        assertEquals(322, placed.getRight());
    }

    @Test
    public void narrowSideSpaceShrinksToMinimum() {
        ConfigRect hovered = new ConfigRect(302, 100, 420, 20);

        ConfigRect placed = place(854, 450, hovered, 302, 722, HEIGHT_30);

        assertEquals(730, placed.getX());
        assertEquals(ConfigTooltipPlacement.MIN_WIDTH, placed.getWidth());
    }

    @Test
    public void goesBelowWithoutSideRoom() {
        ConfigRect hovered = new ConfigRect(24, 80, 272, 20);

        ConfigRect placed = place(320, 210, hovered, 24, 296, HEIGHT_30);

        assertEquals(104, placed.getY());
        assertEquals(60, placed.getX());
    }

    @Test
    public void goesAboveWhenBelowWouldReachTheFooter() {
        ConfigRect hovered = new ConfigRect(24, 180, 272, 20);

        ConfigRect placed = place(320, 210, hovered, 24, 296, HEIGHT_30);

        assertEquals(176, placed.getBottom());
    }

    @Test
    public void sideTooltipNearTheFooterIsPushedUp() {
        ConfigRect hovered = new ConfigRect(217, 190, 420, 20);

        ConfigRect placed = place(854, 210, hovered, 217, 637, width -> 40);

        assertEquals(206, placed.getBottom());
    }

    /** Places the tooltip and checks the guarantees every placement must keep. */
    private static ConfigRect place(int screenWidth, int footerTop, ConfigRect hovered,
                                    int columnLeft, int columnRight, IntUnaryOperator heightForWidth) {
        ConfigRect placed = ConfigTooltipPlacement.place(
            screenWidth, footerTop, hovered, columnLeft, columnRight, heightForWidth);
        assertFalse("covers the hovered setting", placed.intersects(hovered));
        assertTrue("reaches the footer", placed.getBottom() <= footerTop - ConfigTooltipPlacement.MARGIN);
        assertTrue("off the left edge", placed.getX() >= ConfigTooltipPlacement.MARGIN);
        assertTrue("off the right edge", placed.getRight() <= screenWidth - ConfigTooltipPlacement.MARGIN);
        assertEquals(heightForWidth.applyAsInt(placed.getWidth()), placed.getHeight());
        return placed;
    }
}

package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PanelLayoutCalculatorTest {
    @Test
    public void topRightLayoutHonorsOffsetButStaysInsideTheSafeArea() {
        PanelLayout layout = PanelLayoutCalculator.calculate(
            320, 240, 130, 68, PanelAnchor.TOP_RIGHT, 500, -500, 8
        );

        assertEquals(182, layout.getX());
        assertEquals(8, layout.getY());
        assertEquals(130, layout.getWidth());
        assertEquals(68, layout.getHeight());
    }

    @Test
    public void missingAnchorUsesTopRightSafeAreaPlacement() {
        PanelLayout layout = PanelLayoutCalculator.calculate(
            320, 240, 130, 68, null, 0, 0, 8
        );

        assertEquals(182, layout.getX());
        assertEquals(8, layout.getY());
    }

    @Test
    public void configAnchorFallsBackToTopRightForUnknownValues() {
        assertEquals(PanelAnchor.BOTTOM_LEFT, PanelAnchor.fromConfig("bottom-left"));
        assertEquals(PanelAnchor.TOP_RIGHT, PanelAnchor.fromConfig("unknown"));
        assertEquals(PanelAnchor.TOP_RIGHT, PanelAnchor.fromConfig(null));
    }

    @Test
    public void bottomLeftLayoutUsesTheSameSafeMarginAtHighGuiScale() {
        PanelLayout layout = PanelLayoutCalculator.calculate(
            320, 240, 160, 80, PanelAnchor.BOTTOM_LEFT, -400, 400, 8
        );

        assertEquals(8, layout.getX());
        assertEquals(152, layout.getY());
    }

    @Test
    public void configAnchorIgnoresAccidentalWhitespace() {
        assertEquals(PanelAnchor.BOTTOM_LEFT, PanelAnchor.fromConfig(" bottom-left "));
    }
}

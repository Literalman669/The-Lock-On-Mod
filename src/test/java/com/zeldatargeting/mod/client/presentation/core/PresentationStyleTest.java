package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PresentationStyleTest {
    @Test
    public void mapsEveryStatusThroughTheSelectedPalette() {
        PresentationStyle style = PresentationStyle.of(PresentationPalette.TRITANOPIA, 0.5F, 1.0F, true);

        assertEquals(PresentationPalette.TRITANOPIA.getHealthColor(), style.getStatusColor(PresentationStatus.NORMAL));
        assertEquals(PresentationPalette.TRITANOPIA.getWarningColor(), style.getStatusColor(PresentationStatus.WARNING));
        assertEquals(PresentationPalette.TRITANOPIA.getLethalColor(), style.getStatusColor(PresentationStatus.LETHAL));
        assertEquals(0xFF9EA6AD, style.getStatusColor(PresentationStatus.OCCLUDED));
    }

    @Test
    public void appliesHudOpacityWithoutChangingTheRgbChannels() {
        PresentationStyle style = PresentationStyle.of(PresentationPalette.DEFAULT, 0.5F, 1.0F, false);

        assertEquals(0x58101016, style.applyHudOpacity(0xB0101016));
    }

    @Test
    public void sanitizesExternalPresentationValues() {
        PresentationStyle style = PresentationStyle.of(null, 0.1F, 5.0F, true);

        assertSame(PresentationPalette.DEFAULT, style.getPalette());
        assertEquals(0.35F, style.getHudOpacity(), 0.0F);
        assertEquals(1.5F, style.getRingGlowStrength(), 0.0F);
        assertTrue(style.isReducedMotion());
    }
}

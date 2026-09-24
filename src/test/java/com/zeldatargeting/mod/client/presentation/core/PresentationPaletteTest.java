package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class PresentationPaletteTest {
    @Test
    public void namedPalettesHaveDefaultFallbackAndFiniteArgbColors() {
        assertSame(PresentationPalette.DEFAULT, PresentationPalette.fromName(null));
        assertSame(PresentationPalette.DEUTERANOPIA, PresentationPalette.fromName("deuteranopia"));
        assertNotEquals(0, PresentationPalette.TRITANOPIA.getWarningColor());
    }
}

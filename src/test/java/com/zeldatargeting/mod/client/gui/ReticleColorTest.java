package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReticleColorTest {
    @Test
    public void cyclesForwardAndBackwardWithWraparound() {
        assertEquals(ReticleColor.GOLD, ReticleColor.cycle(0xFFFFFF, false));
        assertEquals(ReticleColor.RED, ReticleColor.cycle(0xFFFFFF, true));
        assertEquals(ReticleColor.WHITE, ReticleColor.cycle(0xFF5555, false));
    }

    @Test
    public void customColorsStartOverAtWhite() {
        assertEquals(ReticleColor.WHITE, ReticleColor.cycle(0x123456, false));
        assertEquals(ReticleColor.WHITE, ReticleColor.cycle(0x123456, true));
    }

    @Test
    public void describeUsesTheColorsOwnFormattingCode() {
        assertEquals("§bAqua", ReticleColor.describe(0x55FFFF));
        assertEquals("Custom #123456", ReticleColor.describe(0x123456));
    }
}

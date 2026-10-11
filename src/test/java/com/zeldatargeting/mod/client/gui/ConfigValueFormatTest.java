package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigValueFormatTest {
    @Test
    public void blocksArePluralizedAndKeepHalves() {
        assertEquals("16 blocks", ConfigValueFormat.format(16, ConfigUnit.BLOCKS));
        assertEquals("1 block", ConfigValueFormat.format(1, ConfigUnit.BLOCKS));
        assertEquals("0.5 blocks", ConfigValueFormat.format(0.5, ConfigUnit.BLOCKS));
    }

    @Test
    public void anglesUseTheDegreeSign() {
        assertEquals("60°", ConfigValueFormat.format(60, ConfigUnit.DEGREES));
    }

    @Test
    public void fractionsShowAsWholePercentages() {
        assertEquals("40%", ConfigValueFormat.format(0.4, ConfigUnit.PERCENT));
        assertEquals("100%", ConfigValueFormat.format(1.0, ConfigUnit.PERCENT));
    }

    @Test
    public void multipliersKeepOneDecimal() {
        assertEquals("1.2x", ConfigValueFormat.format(1.2, ConfigUnit.MULTIPLIER));
        assertEquals("1.0x", ConfigValueFormat.format(1.0, ConfigUnit.MULTIPLIER));
    }

    @Test
    public void pixelsKeepTheirSign() {
        assertEquals("-5 px", ConfigValueFormat.format(-5, ConfigUnit.PIXELS));
    }

    @Test
    public void ticksShowAsSecondsOrIntervals() {
        assertEquals("3.0 s", ConfigValueFormat.format(60, ConfigUnit.TICKS_AS_SECONDS));
        assertEquals("every 1 tick", ConfigValueFormat.format(1, ConfigUnit.TICK_INTERVAL));
        assertEquals("every 10 ticks", ConfigValueFormat.format(10, ConfigUnit.TICK_INTERVAL));
    }

    @Test
    public void signedValuesAlwaysShowTheirSign() {
        assertEquals("+0.00", ConfigValueFormat.format(0, ConfigUnit.SIGNED));
        assertEquals("-0.25", ConfigValueFormat.format(-0.25, ConfigUnit.SIGNED));
    }

    @Test
    public void toggleStateIsColoredOnlyWhileTheOptionApplies() {
        assertEquals("§aON", ConfigValueFormat.toggleText(true, false));
        assertEquals("§cOFF", ConfigValueFormat.toggleText(false, false));
        assertEquals("§7ON", ConfigValueFormat.toggleText(true, true));
        assertEquals("§7OFF", ConfigValueFormat.toggleText(false, true));
    }

    @Test
    public void defaultLines() {
        assertEquals("Default: 16 blocks - Range: 1 block to 64 blocks",
            ConfigValueFormat.sliderDefaultLine(16, 1, 64, ConfigUnit.BLOCKS));
        assertEquals("Default: ON", ConfigValueFormat.defaultLine("ON"));
    }
}

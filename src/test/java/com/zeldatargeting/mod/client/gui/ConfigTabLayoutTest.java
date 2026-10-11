package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigTabLayoutTest {
    private static final ToIntFunction<String> SIX_PER_CHAR = text -> 6 * text.length();

    @Test
    public void fullNamesWhenTheyFit() {
        ConfigTabLayout.Result result = layout(854);

        assertEquals(ConfigTabLayout.Mode.FULL, result.getMode());
        List<ConfigRect> tabs = result.getTabs();
        assertEquals(8, tabs.size());
        for (int i = 1; i < tabs.size(); i++) {
            assertFalse(tabs.get(i - 1).intersects(tabs.get(i)));
            assertTrue(tabs.get(i - 1).getRight() <= tabs.get(i).getX());
        }
        int left = tabs.get(0).getX();
        int right = 854 - tabs.get(tabs.size() - 1).getRight();
        assertTrue("centered", Math.abs(left - right) <= 1);
        assertEquals(22, tabs.get(0).getY());
        assertEquals(ConfigTabLayout.HEIGHT, tabs.get(0).getHeight());
    }

    @Test
    public void shortNamesWhenFullDoNotFit() {
        ConfigTabLayout.Result result = layout(480);

        assertEquals(ConfigTabLayout.Mode.SHORT, result.getMode());
        assertEquals(8, result.getTabs().size());
    }

    @Test
    public void singleLineWhenNothingFits() {
        ConfigTabLayout.Result result = layout(320);

        assertEquals(ConfigTabLayout.Mode.SINGLE_LINE, result.getMode());
        assertTrue(result.getTabs().isEmpty());
    }

    private static ConfigTabLayout.Result layout(int screenWidth) {
        List<String> full = new ArrayList<>();
        List<String> shortNames = new ArrayList<>();
        for (ConfigPage page : ConfigPage.values()) {
            full.add(page.getTitle());
            shortNames.add(page.getShortTitle());
        }
        return ConfigTabLayout.layout(screenWidth, 22, full, shortNames, SIX_PER_CHAR);
    }
}

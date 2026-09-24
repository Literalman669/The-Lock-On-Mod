package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigContentViewportTest {
    @Test
    public void scrollingRevealsAControlWithoutLettingItOverlapTheFooter() throws Exception {
        Class<?> viewportType = loadViewport();
        Constructor<?> constructor = viewportType.getConstructor(int.class, int.class, int.class);
        Object viewport = constructor.newInstance(52, 232, 412);
        Method getMaxScroll = viewportType.getMethod("getMaxScroll");
        Method isFullyVisible = viewportType.getMethod("isFullyVisible", int.class, int.class, int.class);
        Method translateY = viewportType.getMethod("translateY", int.class, int.class);

        assertEquals(180, ((Integer) getMaxScroll.invoke(viewport)).intValue());
        assertFalse((Boolean) isFullyVisible.invoke(viewport, 390, 22, 0));
        assertTrue((Boolean) isFullyVisible.invoke(viewport, 390, 22, 180));
        assertEquals(210, ((Integer) translateY.invoke(viewport, 390, 180)).intValue());
    }

    @Test
    public void smallWindowKeepsTheLastControlAboveTheFooter() throws Exception {
        Class<?> viewportType = loadViewport();
        Constructor<?> constructor = viewportType.getConstructor(int.class, int.class, int.class);
        int footerTop = 212;
        Object viewport = constructor.newInstance(58, footerTop - 8, 430);
        Method getMaxScroll = viewportType.getMethod("getMaxScroll");
        Method isFullyVisible = viewportType.getMethod("isFullyVisible", int.class, int.class, int.class);
        Method translateY = viewportType.getMethod("translateY", int.class, int.class);

        int scroll = ((Integer) getMaxScroll.invoke(viewport)).intValue();
        int translatedY = ((Integer) translateY.invoke(viewport, 410, scroll)).intValue();

        assertTrue((Boolean) isFullyVisible.invoke(viewport, 410, 20, scroll));
        assertTrue("The final control must remain above the footer", translatedY + 20 <= footerTop - 8);
    }

    private Class<?> loadViewport() {
        try {
            return Class.forName("com.zeldatargeting.mod.client.gui.ConfigContentViewport");
        } catch (ClassNotFoundException error) {
            fail("Configuration content needs a bounded scroll viewport.");
            throw new AssertionError(error);
        }
    }
}

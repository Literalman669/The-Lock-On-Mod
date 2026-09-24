package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigPageNavigatorTest {
    @Test
    public void repeatedNextRequestsBeforeTheNextUiFrameAdvanceOnlyOnePage() throws Exception {
        Class<?> navigatorType = loadNavigator();
        Constructor<?> constructor = navigatorType.getConstructor(int.class);
        Object navigator = constructor.newInstance(7);
        Method requestNext = navigatorType.getMethod("requestNext");
        Method applyPendingPage = navigatorType.getMethod("applyPendingPage");
        Method getCurrentPage = navigatorType.getMethod("getCurrentPage");

        requestNext.invoke(navigator);
        requestNext.invoke(navigator);

        assertTrue((Boolean) applyPendingPage.invoke(navigator));
        assertEquals(1, ((Integer) getCurrentPage.invoke(navigator)).intValue());
    }

    private Class<?> loadNavigator() {
        try {
            return Class.forName("com.zeldatargeting.mod.client.gui.ConfigPageNavigator");
        } catch (ClassNotFoundException error) {
            fail("Config page navigation must queue duplicate clicks until the next UI frame.");
            throw new AssertionError(error);
        }
    }
}

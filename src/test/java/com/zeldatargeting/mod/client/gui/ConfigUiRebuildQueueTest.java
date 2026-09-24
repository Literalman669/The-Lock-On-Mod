package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigUiRebuildQueueTest {
    @Test
    public void rebuildRequestsWaitForTheNextUiTickAndKeepTheScrollReset() throws Exception {
        Class<?> queueType = loadQueue();
        Constructor<?> constructor = queueType.getConstructor();
        Object queue = constructor.newInstance();
        Method requestRebuild = queueType.getMethod("requestRebuild", boolean.class);
        Method consumeRebuild = queueType.getMethod("consumeRebuildRequest");
        Method consumeScrollReset = queueType.getMethod("consumeScrollResetRequest");

        assertFalse((Boolean) consumeRebuild.invoke(queue));

        requestRebuild.invoke(queue, false);
        requestRebuild.invoke(queue, true);

        assertTrue((Boolean) consumeRebuild.invoke(queue));
        assertTrue((Boolean) consumeScrollReset.invoke(queue));
        assertFalse((Boolean) consumeRebuild.invoke(queue));
        assertFalse((Boolean) consumeScrollReset.invoke(queue));
    }

    private Class<?> loadQueue() {
        try {
            return Class.forName("com.zeldatargeting.mod.client.gui.ConfigUiRebuildQueue");
        } catch (ClassNotFoundException error) {
            fail("Configuration rebuilds must be deferred until the next UI tick.");
            throw new AssertionError(error);
        }
    }
}

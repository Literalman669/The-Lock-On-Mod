package com.zeldatargeting.mod.client;

import org.junit.Test;
import org.lwjgl.input.Keyboard;

import static org.junit.Assert.assertEquals;

public class KeyBindingsTest {
    @Test
    public void targetCyclingDefaultsDoNotReplaceVanillaDropOrUseKeys() {
        assertEquals(Keyboard.KEY_Z, KeyBindings.DEFAULT_CYCLE_LEFT_KEY);
        assertEquals(Keyboard.KEY_X, KeyBindings.DEFAULT_CYCLE_RIGHT_KEY);
    }
}

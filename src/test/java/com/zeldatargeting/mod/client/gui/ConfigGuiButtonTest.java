package com.zeldatargeting.mod.client.gui;

import net.minecraft.client.gui.GuiButton;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigGuiButtonTest {
    @Test
    public void configurationButtonsSupportWideControlsWithoutUsingTheLegacyButtonSlices() throws Exception {
        Class<?> buttonType = loadButtonType();
        Constructor<?> constructor = buttonType.getConstructor(int.class, int.class, int.class, int.class, int.class, String.class);
        GuiButton button = (GuiButton) constructor.newInstance(42, 10, 20, 420, 20, "Wide control");

        assertTrue(GuiButton.class.isAssignableFrom(buttonType));
        assertEquals(420, button.width);
    }

    private Class<?> loadButtonType() {
        try {
            return Class.forName("com.zeldatargeting.mod.client.gui.ConfigGuiButton");
        } catch (ClassNotFoundException error) {
            fail("Configuration controls need a wide-button renderer instead of the 200-pixel vanilla texture slices.");
            throw new AssertionError(error);
        }
    }
}

package com.zeldatargeting.mod.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {
    
    public static final String CATEGORY = "key.categories.zeldatargeting";
    public static final int DEFAULT_CYCLE_LEFT_KEY = Keyboard.KEY_Z;
    public static final int DEFAULT_CYCLE_RIGHT_KEY = Keyboard.KEY_X;
    
    public static KeyBinding lockOnToggle;
    public static KeyBinding cycleTargetLeft;
    public static KeyBinding cycleTargetRight;
    public static KeyBinding cameraFreeLook;
    
    public static void init() {
        // Lock-on toggle key (default: R)
        lockOnToggle = new KeyBinding(
            "key.zeldatargeting.lockon_toggle",
            Keyboard.KEY_R,
            CATEGORY
        );
        
        // Cycle target left key (default: Z)
        cycleTargetLeft = new KeyBinding(
            "key.zeldatargeting.cycle_left",
            DEFAULT_CYCLE_LEFT_KEY,
            CATEGORY
        );
        
        // Cycle target right key (default: X)
        cycleTargetRight = new KeyBinding(
            "key.zeldatargeting.cycle_right",
            DEFAULT_CYCLE_RIGHT_KEY,
            CATEGORY
        );

        cameraFreeLook = new KeyBinding(
            "key.zeldatargeting.camera_free_look",
            Keyboard.KEY_LMENU,
            CATEGORY
        );
        
        // Register key bindings
        ClientRegistry.registerKeyBinding(lockOnToggle);
        ClientRegistry.registerKeyBinding(cycleTargetLeft);
        ClientRegistry.registerKeyBinding(cycleTargetRight);
        ClientRegistry.registerKeyBinding(cameraFreeLook);
    }
}
package com.zeldatargeting.mod.client;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.audio.TargetingSounds;
import com.zeldatargeting.mod.compat.CompatRiding;
import com.zeldatargeting.mod.client.targeting.EntityDetector;
import com.zeldatargeting.mod.client.targeting.CameraController;
import com.zeldatargeting.mod.client.targeting.TargetTracker;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TargetingManager {
    
    private static TargetingManager instance;
    
    private EntityDetector entityDetector;
    private CameraController cameraController;
    private TargetTracker targetTracker;
    
    private boolean isActive;
    private Entity currentTarget;
    private int previousPerspective;
    private long lastCycleTime = 0;
    private static final long CYCLE_COOLDOWN_MS = 250;
    
    private TargetingManager() {
        this.entityDetector = new EntityDetector();
        this.cameraController = new CameraController();
        this.targetTracker = new TargetTracker();
    }
    
    public static void init() {
        if (instance == null) {
            instance = new TargetingManager();
            MinecraftForge.EVENT_BUS.register(instance);
            ZeldaTargetingMod.getLogger().info("Targeting Manager initialized");
        }
    }
    
    public static TargetingManager getInstance() {
        return instance;
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) {
            return; // Don't process input when GUI is open
        }
        
        if (KeyBindings.lockOnToggle.isPressed()) {
            toggleLockOn();
        } else if (KeyBindings.cycleTargetLeft.isPressed()) {
            cycleTarget(false);
        } else if (KeyBindings.cycleTargetRight.isPressed()) {
            cycleTarget(true);
        }
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        // Auto-release lock-on when player starts riding (if config enabled)
        if (isActive && CompatRiding.shouldReleaseLockOn()) {
            disableLockOn();
            return;
        }
        
        if (isActive && currentTarget != null) {
            // Update targeting system
            targetTracker.update(currentTarget);
            
            // Check if target is still valid
            if (!targetTracker.isTargetValid()) {
                // Try to find a new target or disable lock-on
                Entity newTarget = entityDetector.findNearestTarget();
                if (newTarget != null) {
                    setTarget(newTarget);
                    // Play target switch sound for automatic retargeting
                    TargetingSounds.playTargetSwitchSound();
                } else {
                    disableLockOn();
                }
            } else {
                // Update camera to follow target
                cameraController.updateCamera(currentTarget);
            }
        }
    }
    
    private void toggleLockOn() {
        if (isActive) {
            disableLockOn();
        } else {
            enableLockOn();
        }
    }
    
    private void enableLockOn() {
        if (CompatRiding.shouldBlockLockOn()) {
            return;
        }
        Entity target = entityDetector.findNearestTarget();
        if (target != null) {
            isActive = true;
            setTarget(target);
            
            // Play target lock sound
            TargetingSounds.playTargetLockSound(target);
            
            // Handle auto third-person switching
            if (TargetingConfig.autoThirdPerson) {
                Minecraft mc = Minecraft.getMinecraft();
                previousPerspective = mc.gameSettings.thirdPersonView;
                if (mc.gameSettings.thirdPersonView == 0) { // First person
                    mc.gameSettings.thirdPersonView = 1; // Switch to third person
                }
            }
            
            ZeldaTargetingMod.getLogger().debug("Lock-on enabled on target: " + target.getName());
            if (TargetingConfig.debugCompatibility) {
                ZeldaTargetingMod.getLogger().info("[ZT Debug] Lock-on activated | SSR active: "
                        + ZeldaTargetingMod.isShoulderSurfingActive()
                        + " | SSR offsetX: " + ZeldaTargetingMod.getShoulderSurfingOffsetX()
                        + " | BTP loaded: " + ZeldaTargetingMod.isBetterThirdPersonLoaded()
                        + " | BTP mode: " + TargetingConfig.btpCompatibilityMode
                        + " | Preset: " + TargetingConfig.lockOnPreset
                        + " | Smoothness: " + TargetingConfig.cameraSmoothness);
            }
        }
    }
    
    private void disableLockOn() {
        if (isActive) {
            // Play target lost sound
            TargetingSounds.playTargetLostSound();
            
            // Restore previous perspective if auto third-person was used
            if (TargetingConfig.autoThirdPerson) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.gameSettings.thirdPersonView = previousPerspective;
            }
            
            isActive = false;
            currentTarget = null;
            cameraController.resetCamera();
            ZeldaTargetingMod.getLogger().debug("Lock-on disabled");
        }
    }
    
    private void cycleTarget(boolean forward) {
        if (!isActive) {
            enableLockOn();
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCycleTime < CYCLE_COOLDOWN_MS) {
            return;
        }
        
        lastCycleTime = currentTime;
        
        Entity newTarget = entityDetector.findNextTarget(currentTarget, forward);
        if (newTarget != null && newTarget != currentTarget) {
            setTarget(newTarget);
            
            // Play target switch sound
            TargetingSounds.playTargetSwitchSound();
            
            ZeldaTargetingMod.getLogger().debug("Cycled to new target: " + newTarget.getName());
        }
    }
    
    private void setTarget(Entity target) {
        this.currentTarget = target;
        targetTracker.setTarget(target);
        cameraController.setTarget(target);
    }
    
    // Getters
    public boolean isActive() {
        return isActive;
    }
    
    public Entity getCurrentTarget() {
        return currentTarget;
    }
}
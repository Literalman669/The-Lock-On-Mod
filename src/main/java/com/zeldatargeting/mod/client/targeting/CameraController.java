package com.zeldatargeting.mod.client.targeting;

import com.zeldatargeting.mod.compat.CompatBTP;
import com.zeldatargeting.mod.compat.CompatSSR;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CameraController {
    
    private static final float MIN_TRACKING_SMOOTHING  = 0.3f;
    private static final float VELOCITY_BOOST_SCALE    = 0.08f;
    private static final float MAX_VELOCITY_BOOST      = 0.45f;
    private static final float SMOOTHING_CEILING       = 0.95f;
    private static final float LARGE_DIFF_THRESHOLD    = 10.0f;
    private static final float LARGE_DIFF_MIN_SMOOTHING = 0.7f;

    private final Minecraft mc;
    private Entity currentTarget;
    
    // Store original camera settings
    private float originalYaw;
    private float originalPitch;
    private boolean hasStoredOriginal = false;
    
    // Smooth interpolation values
    private float targetYaw;
    private float targetPitch;
    private float currentYaw;
    private float currentPitch;
    
    // Velocity tracking for feed-forward lag compensation
    private float prevTargetYaw;
    private float prevTargetPitch;

    // Suppress large-diff boost for a few ticks after deliberate target switch
    // so cycling feels smooth rather than lurching.
    private int switchCooldown = 0;
    private static final int SWITCH_COOLDOWN_TICKS = 4;
    
    public CameraController() {
        this.mc = Minecraft.getMinecraft();
    }
    
    public void setTarget(Entity target) {
        this.currentTarget = target;
        
        if (!hasStoredOriginal && mc.player != null) {
            // Store original camera rotation
            originalYaw = mc.player.rotationYaw;
            originalPitch = mc.player.rotationPitch;
            currentYaw = originalYaw;
            currentPitch = originalPitch;
            hasStoredOriginal = true;
        }
        
        if (target != null) {
            calculateTargetRotation();
            prevTargetYaw = targetYaw;
            prevTargetPitch = targetPitch;
            switchCooldown = SWITCH_COOLDOWN_TICKS;
        }
    }
    
    public void updateCamera(Entity target) {
        if (target == null || mc.player == null || !TargetingConfig.isCameraLockOnEnabled()) {
            return;
        }
        
        if (!CompatBTP.shouldApplyCameraRotation()) {
            return;
        }
        
        this.currentTarget = target;
        calculateTargetRotation();
        
        // Compute angular velocity of the target for feed-forward compensation
        float yawVelocity = MathHelper.wrapDegrees(targetYaw - prevTargetYaw);
        float pitchVelocity = targetPitch - prevTargetPitch;
        prevTargetYaw = targetYaw;
        prevTargetPitch = targetPitch;
        
        // Feed-forward: extrapolate one frame ahead so the camera leads the target
        // instead of chasing it, eliminating the lag-behind on moving targets
        float feedTargetYaw = targetYaw + yawVelocity;
        float feedTargetPitch = MathHelper.clamp(targetPitch + pitchVelocity, -90.0f, 90.0f);
        
        // Calculate smoothing based on BTP compatibility mode
        float baseSmoothing = Math.max(TargetingConfig.getCameraSmoothness(), MIN_TRACKING_SMOOTHING);
        float smoothing = CompatBTP.getEffectiveSmoothing(baseSmoothing);

        // Per-mode smoothing: gentler (0.6x) in first-person for tighter precision feel
        if (TargetingConfig.perModeSmoothingEnabled && mc.gameSettings.thirdPersonView == 0) {
            smoothing = Math.max(smoothing * 0.6f, MIN_TRACKING_SMOOTHING);
        }

        float yawDiff = MathHelper.wrapDegrees(feedTargetYaw - currentYaw);
        float pitchDiff = feedTargetPitch - currentPitch;

        float angularSpeed = Math.abs(yawVelocity) + Math.abs(pitchVelocity);
        float velocityBoost = Math.min(angularSpeed * VELOCITY_BOOST_SCALE, MAX_VELOCITY_BOOST);
        float adaptiveSmoothing = Math.min(smoothing + velocityBoost, SMOOTHING_CEILING);
        if (switchCooldown > 0) {
            switchCooldown--;
        } else if (Math.abs(yawDiff) > LARGE_DIFF_THRESHOLD || Math.abs(pitchDiff) > LARGE_DIFF_THRESHOLD) {
            adaptiveSmoothing = Math.max(adaptiveSmoothing, LARGE_DIFF_MIN_SMOOTHING);
        }
        
        currentYaw = interpolateAngle(currentYaw, feedTargetYaw, adaptiveSmoothing);
        currentPitch = MathHelper.clamp(
            interpolateFloat(currentPitch, feedTargetPitch, adaptiveSmoothing),
            -90.0f, 90.0f
        );
        
        // Apply the camera rotation
        applyCameraRotation();
    }
    
    public void resetCamera() {
        // Leave the camera where it is — the player was looking at the target.
        // Restoring to originalYaw/originalPitch would snap the view back to where
        // they were when lock-on started, causing a jarring flick when the target dies.
        currentTarget = null;
        hasStoredOriginal = false;
    }
    
    private void calculateTargetRotation() {
        if (currentTarget == null || mc.player == null) {
            return;
        }
        
        EntityPlayer player = mc.player;
        
        // Use interpolated positions for smoother tracking of moving targets
        float partialTicks = mc.getRenderPartialTicks();
        
        // Get interpolated player position
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks + player.getEyeHeight();
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        
        // Get interpolated target position
        double targetX = currentTarget.lastTickPosX + (currentTarget.posX - currentTarget.lastTickPosX) * partialTicks;
        double targetY = currentTarget.lastTickPosY + (currentTarget.posY - currentTarget.lastTickPosY) * partialTicks
                + currentTarget.height * (0.5 + TargetingConfig.cameraFocusYOffset);
        double targetZ = currentTarget.lastTickPosZ + (currentTarget.posZ - currentTarget.lastTickPosZ) * partialTicks;
        
        Vec3d direction = new Vec3d(targetX - playerX, targetY - playerY, targetZ - playerZ).normalize();
        
        // Calculate yaw (horizontal rotation)
        float newYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        
        // Calculate pitch (vertical rotation)
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float newPitch = (float) -Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        
        // Get current player rotation for reference
        float currentPlayerYaw = player.rotationYaw;
        float currentPlayerPitch = player.rotationPitch;
        
        // Limit the camera adjustment to prevent jarring movements
        float yawDiff = MathHelper.wrapDegrees(newYaw - currentPlayerYaw);
        float pitchDiff = newPitch - currentPlayerPitch;
        
        float maxYawAdj = TargetingConfig.maxYawAdjustment;
        float maxPitchAdj = TargetingConfig.maxPitchAdjustment;
        
        yawDiff = MathHelper.clamp(yawDiff, -maxYawAdj, maxYawAdj);
        pitchDiff = MathHelper.clamp(pitchDiff, -maxPitchAdj, maxPitchAdj);
        
        targetYaw = currentPlayerYaw + yawDiff;
        targetPitch = MathHelper.clamp(currentPlayerPitch + pitchDiff, -90.0f, 90.0f);
        
        // Shoulder Surfing Reloaded compensation:
        // SSR shifts the visual camera by xOffset in the screen-right direction.
        // The crosshair (screen center) corresponds to a ray from the shoulder camera
        // position, not the player's eye. We recompute targetYaw/Pitch from the
        // shoulder camera position so the crosshair lands on the locked target.
        if (CompatSSR.isActive()
                && TargetingConfig.ssrCompensationEnabled) {
            float yawRad = (float) Math.toRadians(targetYaw);
            // Read SSR's actual runtime x-offset (handles left/right shoulder, aiming, etc.)
            double xOffset = CompatSSR.getOffsetX();
            double camX = playerX + xOffset * Math.cos(yawRad);
            double camZ = playerZ + xOffset * Math.sin(yawRad);
            double dX = targetX - camX;
            double dZ = targetZ - camZ;
            double dY = targetY - playerY;
            double horizDist = Math.sqrt(dX * dX + dZ * dZ);
            targetYaw = (float) Math.toDegrees(Math.atan2(-dX, dZ));
            targetPitch = MathHelper.clamp((float) -Math.toDegrees(Math.atan2(dY, horizDist)), -90.0f, 90.0f);
        }
    }
    
    private void applyCameraRotation() {
        if (mc.player == null) {
            return;
        }
        
        // Store previous rotation for smooth rendering
        mc.player.prevRotationYaw = mc.player.rotationYaw;
        mc.player.prevRotationPitch = mc.player.rotationPitch;
        
        // Apply new rotation
        mc.player.rotationYaw = currentYaw;
        mc.player.rotationPitch = currentPitch;
    }
    
    private float interpolateAngle(float current, float target, float factor) {
        float diff = MathHelper.wrapDegrees(target - current);
        return current + diff * factor;
    }
    
    private float interpolateFloat(float current, float target, float factor) {
        return current + (target - current) * factor;
    }
    
    public Entity getCurrentTarget() {
        return currentTarget;
    }
    
    public boolean isActive() {
        return currentTarget != null;
    }
}
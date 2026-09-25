package com.zeldatargeting.mod.client.camera.vanilla;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.camera.CameraRuntimeAdapter;
import com.zeldatargeting.mod.client.camera.compat.CameraRotationPolicy;
import com.zeldatargeting.mod.client.camera.compat.ShoulderAimSolver;
import com.zeldatargeting.mod.client.camera.compat.ShoulderCameraState;
import com.zeldatargeting.mod.client.camera.compat.ShoulderSurfingBridge;
import com.zeldatargeting.mod.client.camera.CameraRuntimeState;
import com.zeldatargeting.mod.client.camera.core.CameraCollisionResult;
import com.zeldatargeting.mod.client.camera.core.CameraCollisionSamples;
import com.zeldatargeting.mod.client.camera.core.CameraCollisionSolver;
import com.zeldatargeting.mod.client.camera.core.CameraFrame;
import com.zeldatargeting.mod.client.camera.core.CameraFraming;
import com.zeldatargeting.mod.client.camera.core.CameraInput;
import com.zeldatargeting.mod.client.camera.core.CameraProfile;
import com.zeldatargeting.mod.client.camera.core.PerspectiveDecision;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;
import com.zeldatargeting.mod.config.TargetingConfig;
import com.zeldatargeting.mod.client.targeting.core.TargetAnchorResolver;
import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class VanillaCameraAdapter implements CameraRuntimeAdapter<EntityLivingBase> {
    private final Minecraft minecraft;
    private final VanillaCameraOffsetApplier offsetApplier;
    private final CloseCameraTransparency transparency;
    private final ShoulderSurfingBridge shoulderSurfing;
    private CameraRuntimeState capturedState;
    private CameraFrame currentFrame;
    private boolean translationDisabled;
    private boolean applicationDisabled;
    private boolean translationWarningLogged;
    private boolean applicationWarningLogged;

    public VanillaCameraAdapter() {
        this(
            Minecraft.getMinecraft(),
            new VanillaCameraOffsetApplier(),
            new CloseCameraTransparency(),
            ZeldaTargetingMod.getShoulderSurfingBridge()
        );
    }

    VanillaCameraAdapter(
            Minecraft minecraft,
            VanillaCameraOffsetApplier offsetApplier,
            CloseCameraTransparency transparency) {
        this(
            minecraft,
            offsetApplier,
            transparency,
            ZeldaTargetingMod.getShoulderSurfingBridge()
        );
    }

    VanillaCameraAdapter(
            Minecraft minecraft,
            VanillaCameraOffsetApplier offsetApplier,
            CloseCameraTransparency transparency,
            ShoulderSurfingBridge shoulderSurfing) {
        this.minecraft = minecraft;
        this.offsetApplier = offsetApplier;
        this.transparency = transparency;
        this.shoulderSurfing = shoulderSurfing == null
            ? ShoulderSurfingBridge.unavailable()
            : shoulderSurfing;
    }

    @Override
    public CameraRuntimeState captureState() {
        capturedState = new CameraRuntimeState(
            minecraft.gameSettings.thirdPersonView,
            minecraft.gameSettings.fovSetting
        );
        return capturedState;
    }

    @Override
    public CameraCollisionResult sampleCollision(
            LockOnSnapshot<EntityLivingBase> snapshot,
            CameraProfile profile) {
        TargetObservation<EntityLivingBase> observation = snapshot == null
            ? null
            : snapshot.getTarget();
        double desiredDistance = observation == null
            ? profile.getBaseDistance()
            : CameraFraming.desiredDistance(
                profile,
                observation.getWidth(),
                observation.getHeight()
            );
        if (minecraft.player == null || minecraft.world == null) {
            return CameraCollisionSolver.solve(
                desiredDistance,
                CameraCollisionSamples.uniform(0.0D)
            );
        }

        EntityPlayer player = minecraft.player;
        Vec3d eye = new Vec3d(
            player.posX,
            player.posY + player.getEyeHeight(),
            player.posZ
        );
        double sampleDistance = Math.max(4.0D, desiredDistance);
        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;
        if (minecraft.gameSettings.thirdPersonView == 2) {
            pitch += 180.0F;
        }
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double directionX = -Math.sin(yawRadians) * Math.cos(pitchRadians) * sampleDistance;
        double directionZ = Math.cos(yawRadians) * Math.cos(pitchRadians) * sampleDistance;
        double directionY = -Math.sin(pitchRadians) * sampleDistance;
        CameraCollisionSamples samples = new CameraCollisionSamples(
            sampleRay(0, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(1, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(2, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(3, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(4, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(5, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(6, eye, directionX, directionY, directionZ, sampleDistance),
            sampleRay(7, eye, directionX, directionY, directionZ, sampleDistance)
        );
        return CameraCollisionSolver.solve(desiredDistance, samples);
    }

    @Override
    public CameraInput sampleFrame(
            LockOnSnapshot<EntityLivingBase> snapshot,
            CameraProfile profile,
            CameraCollisionResult collision,
            long elapsedMillis,
            boolean freeLook,
            int requestedPerspective,
            float partialTicks) {
        TargetObservation<EntityLivingBase> observation = snapshot == null
            ? null
            : snapshot.getTarget();
        EntityLivingBase target = observation == null ? null : observation.getReference();
        EntityPlayer player = minecraft.player;
        if (player == null || target == null) {
            return invalidFrameInput(
                collision,
                elapsedMillis,
                freeLook,
                requestedPerspective
            );
        }

        double playerX = interpolate(player.lastTickPosX, player.posX, partialTicks);
        double playerY = interpolate(player.lastTickPosY, player.posY, partialTicks)
            + player.getEyeHeight();
        double playerZ = interpolate(player.lastTickPosZ, player.posZ, partialTicks);
        double targetX = interpolate(target.lastTickPosX, target.posX, partialTicks);
        double targetY = interpolate(target.lastTickPosY, target.posY, partialTicks);
        double targetZ = interpolate(target.lastTickPosZ, target.posZ, partialTicks);
        double targetHeight = observation.getHeight();
        TargetPoint focus = TargetAnchorResolver.resolve(
            observation.getAnchor(),
            targetX,
            targetY,
            targetZ,
            targetY + targetHeight,
            target.getEyeHeight()
        );
        double focusY = TargetAnchorResolver.offsetWithinHitbox(
            focus.getY(),
            targetY,
            targetY + targetHeight,
            profile.getFocusYOffset()
        );
        double targetDeltaX = focus.getX() - playerX;
        double targetDeltaY = focusY - playerY;
        double targetDeltaZ = focus.getZ() - playerZ;
        if (TargetingConfig.ssrCompensationEnabled) {
            ShoulderCameraState shoulder = shoulderSurfing.captureState();
            ShoulderAimSolver.AimVector compensated = ShoulderAimSolver.compensate(
                targetDeltaX, targetDeltaY, targetDeltaZ, shoulder
            );
            targetDeltaX = compensated.getX();
            targetDeltaY = compensated.getY();
            targetDeltaZ = compensated.getZ();
        }
        return new CameraInput(
            player.rotationYaw,
            player.rotationPitch,
            targetDeltaX,
            targetDeltaY,
            targetDeltaZ,
            observation.getWidth(),
            targetHeight,
            observation.getDistance(),
            collision,
            elapsedMillis,
            freeLook,
            requestedPerspective
        );
    }

    @Override
    public void applyPerspective(PerspectiveDecision decision) {
        if (decision == null || !decision.shouldApply()) {
            return;
        }
        try {
            minecraft.gameSettings.thirdPersonView = decision.getPerspective();
        } catch (RuntimeException failure) {
            handleApplicationFailure("perspective", failure);
        }
    }

    @Override
    public void applyFrame(CameraFrame frame) {
        if (frame == null || applicationDisabled) {
            return;
        }
        try {
            currentFrame = frame;
            if (frame.shouldApplyRotation()
                    && minecraft.player != null) {
                minecraft.player.prevRotationYaw = minecraft.player.rotationYaw;
                minecraft.player.prevRotationPitch = minecraft.player.rotationPitch;
                minecraft.player.rotationYaw = frame.getYaw();
                minecraft.player.rotationPitch = frame.getPitch();
            }
            transparency.setAlpha(frame.getPlayerAlpha());
        } catch (RuntimeException failure) {
            handleApplicationFailure("frame", failure);
        }
    }

    @Override
    public void restoreImmediate(PerspectiveDecision decision) {
        if (decision != null && decision.shouldApply()) {
            try {
                minecraft.gameSettings.thirdPersonView = decision.getPerspective();
            } catch (RuntimeException failure) {
                warnApplicationOnce("immediate perspective restoration", failure);
            }
        }
        int perspective = capturedState == null
                ? minecraft.gameSettings.thirdPersonView
                : capturedState.getPerspective();
        currentFrame = CameraFrame.baseline(perspective);
        transparency.reset();
    }

    @Override
    public void clearFrame() {
        currentFrame = null;
        capturedState = null;
        transparency.reset();
    }

    @Override
    public void resetWorldSession() {
        clearFrame();
        capturedState = null;
        translationDisabled = false;
        applicationDisabled = false;
        translationWarningLogged = false;
        applicationWarningLogged = false;
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (applicationDisabled
                || currentFrame == null
                || event.getEntity() != minecraft.getRenderViewEntity()) {
            return;
        }

        if (compatibilityDecision().getOffsetOwner()
                == CameraRotationPolicy.OffsetOwner.CAMERA_HOST) {
            return;
        }

        if (translationDisabled) {
            return;
        }

        try {
            offsetApplier.apply(currentFrame, minecraft.gameSettings.thirdPersonView);
        } catch (RuntimeException failure) {
            if (!translationWarningLogged) {
                logWarning("Custom camera distance disabled for this world", failure);
                translationWarningLogged = true;
            }
            translationDisabled = true;
        }
    }

    @SubscribeEvent
    public void onFovModifier(EntityViewRenderEvent.FOVModifier event) {
        if (applicationDisabled
                || currentFrame == null
                || event.getEntity() != minecraft.getRenderViewEntity()) {
            return;
        }
        try {
            event.setFOV((float) (event.getFOV() * currentFrame.getFovMultiplier()));
        } catch (RuntimeException failure) {
            handleApplicationFailure("FOV", failure);
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (applicationDisabled || currentFrame == null) {
            return;
        }
        try {
            transparency.onPre(event, minecraft.player);
        } catch (RuntimeException failure) {
            handleApplicationFailure("player transparency", failure);
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        try {
            transparency.onPost(event, minecraft.player);
        } catch (RuntimeException failure) {
            warnApplicationOnce("player transparency cleanup", failure);
        }
    }

    private double sampleRay(
            int index,
            Vec3d eye,
            double directionX,
            double directionY,
            double directionZ,
            double sampleDistance) {
        double offsetX = ((index & 1) * 2 - 1) * 0.1D;
        double offsetY = ((index >> 1 & 1) * 2 - 1) * 0.1D;
        double offsetZ = ((index >> 2 & 1) * 2 - 1) * 0.1D;
        Vec3d start = new Vec3d(
            eye.x + offsetX,
            eye.y + offsetY,
            eye.z + offsetZ
        );
        Vec3d end = new Vec3d(
            eye.x - directionX + offsetX + offsetZ,
            eye.y - directionY + offsetY,
            eye.z - directionZ + offsetZ
        );
        RayTraceResult hit = minecraft.world.rayTraceBlocks(start, end, false, true, false);
        return hit == null || hit.hitVec == null
            ? sampleDistance
            : hit.hitVec.distanceTo(eye);
    }

    private CameraRotationPolicy.Decision compatibilityDecision() {
        return CameraRotationPolicy.resolve(shoulderSurfing.isActive());
    }

    private CameraInput invalidFrameInput(
            CameraCollisionResult collision,
            long elapsedMillis,
            boolean freeLook,
            int requestedPerspective) {
        return new CameraInput(
            0.0F,
            0.0F,
            Double.NaN,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            collision,
            elapsedMillis,
            freeLook,
            requestedPerspective
        );
    }

    private void handleApplicationFailure(String operation, RuntimeException failure) {
        warnApplicationOnce(operation, failure);
        applicationDisabled = true;
        try {
            if (capturedState != null) {
                minecraft.gameSettings.thirdPersonView = capturedState.getPerspective();
                currentFrame = CameraFrame.baseline(capturedState.getPerspective());
            } else {
                currentFrame = null;
            }
        } catch (RuntimeException recoveryFailure) {
            currentFrame = null;
        }
        try {
            transparency.reset();
        } catch (RuntimeException recoveryFailure) {
            warnApplicationOnce("failure recovery", recoveryFailure);
        }
    }

    private void warnApplicationOnce(String operation, RuntimeException failure) {
        if (!applicationWarningLogged) {
            logWarning("Vanilla camera application failed during " + operation, failure);
            applicationWarningLogged = true;
        }
    }

    private static void logWarning(String message, RuntimeException failure) {
        if (ZeldaTargetingMod.getLogger() != null) {
            ZeldaTargetingMod.getLogger().warn(message, failure);
        }
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }
}

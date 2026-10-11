package com.zeldatargeting.mod.client;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.camera.CameraCoordinator;
import com.zeldatargeting.mod.client.camera.compat.ShoulderCameraState;
import com.zeldatargeting.mod.client.camera.core.CameraDirector;
import com.zeldatargeting.mod.client.camera.core.PerspectiveController;
import com.zeldatargeting.mod.client.camera.core.ViewLevelEasing;
import com.zeldatargeting.mod.client.camera.vanilla.LegacyCameraProfileAdapter;
import com.zeldatargeting.mod.client.camera.vanilla.VanillaCameraAdapter;
import com.zeldatargeting.mod.client.audio.TargetingSounds;
import com.zeldatargeting.mod.client.diagnostics.AttackAimDiagnostics;
import com.zeldatargeting.mod.client.diagnostics.MouseTurnDiagnostics;
import com.zeldatargeting.mod.client.math.TargetingMath;
import com.zeldatargeting.mod.client.session.LifecycleGuard;
import com.zeldatargeting.mod.client.session.LockOnSession;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;
import com.zeldatargeting.mod.client.session.LockPhase;
import com.zeldatargeting.mod.client.session.LockReleaseReason;
import com.zeldatargeting.mod.client.presentation.PresentationFeedbackController;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.targeting.EntityDetector;
import com.zeldatargeting.mod.client.targeting.core.TargetHistory;
import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPriority;
import com.zeldatargeting.mod.client.targeting.core.TargetingOptions;
import com.zeldatargeting.mod.client.targeting.core.TargetingService;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Locale;

public final class TargetingManager {
    private static TargetingManager instance;

    private final VanillaCameraAdapter cameraAdapter;
    private final EntityDetector entityDetector;
    private final CameraCoordinator<EntityLivingBase> cameraCoordinator;
    private final TargetingService<EntityLivingBase> targetingService;
    private final PresentationFeedbackController presentationFeedbackController;
    private final ViewLevelEasing viewLeveling = new ViewLevelEasing(220L);
    private final MouseTurnDiagnostics mouseDiagnostics = new MouseTurnDiagnostics();
    private final AttackAimDiagnostics attackDiagnostics = new AttackAimDiagnostics();
    private int ticksSinceValidation;
    // A target killed this tick that the lock already switched away from, so hit
    // feedback (the kill burst) still recognises it. Cleared at the next tick.
    private int justKilledTargetId = -1;

    private TargetingManager() {
        cameraAdapter = new VanillaCameraAdapter();
        cameraCoordinator = new CameraCoordinator<>(
            new CameraDirector(),
            new PerspectiveController(),
            cameraAdapter,
            new LegacyCameraProfileAdapter()
        );
        entityDetector = new EntityDetector();
        targetingService = new TargetingService<>(
            entityDetector,
            new LockOnSession<EntityLivingBase>(),
            new TargetHistory(3)
        );
        presentationFeedbackController = new PresentationFeedbackController();
    }

    public static void init() {
        if (instance == null) {
            instance = new TargetingManager();
            MinecraftForge.EVENT_BUS.register(instance);
            MinecraftForge.EVENT_BUS.register(instance.cameraAdapter);
            ZeldaTargetingMod.getLogger().info("Targeting Manager initialized");
        }
    }

    public static TargetingManager getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        handleInput();
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        handleInput();
    }

    private void handleInput() {
        if (Minecraft.getMinecraft().currentScreen != null) {
            return;
        }

        if (KeyBindings.lockOnToggle.isPressed()) {
            toggleLockOn();
        } else if (KeyBindings.cycleTargetLeft.isPressed()) {
            cycleTarget(false);
        } else if (KeyBindings.cycleTargetRight.isPressed()) {
            cycleTarget(true);
        } else if (KeyBindings.cycleAimPoint.isPressed()) {
            cycleAimPoint();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        justKilledTargetId = -1;
        attackDiagnostics.onClientTick(isActive() ? referencedTarget(targetingService.snapshot()) : null);
        long nowMillis = ClientClock.nowMillis();
        LockOnSnapshot<EntityLivingBase> before = targetingService.snapshot();
        if (before.isPresentationVisible()) {
            Minecraft mc = Minecraft.getMinecraft();
            TargetObservation<EntityLivingBase> observation = before.getTarget();
            EntityLivingBase target = observation == null ? null : observation.getReference();
            boolean playerAvailable = mc.player != null;
            boolean detachedRelease = before.getPhase() == LockPhase.RELEASING;
            boolean targetAvailable = detachedRelease || target != null;
            boolean sameDimension = detachedRelease || (playerAvailable
                && targetAvailable
                && mc.player.dimension == target.dimension);
            LockReleaseReason releaseReason = LifecycleGuard.evaluate(
                mc.world != null,
                playerAvailable,
                playerAvailable && mc.player.isEntityAlive(),
                targetAvailable,
                sameDimension
            );
            if (releaseReason != LockReleaseReason.NONE) {
                LockOnSnapshot<EntityLivingBase> after = targetingService.clear(
                    releaseReason,
                    nowMillis
                );
                handleTransition(before, after, true);
                cameraCoordinator.onClientTick(
                    after,
                    false,
                    false,
                    true,
                    TargetingConfig.autoThirdPerson,
                    nowMillis
                );
                ZeldaTargetingMod.getLogger().debug("Lock-on cleared: " + releaseReason);
                return;
            }
        }

        LockReleaseReason filterReason = validateTarget(before);
        if (filterReason != LockReleaseReason.NONE) {
            releaseLockOn(filterReason);
        }

        LockOnSnapshot<EntityLivingBase> tickBefore = targetingService.snapshot();
        LockOnSnapshot<EntityLivingBase> after = targetingService.tick(
            nowMillis,
            currentOptions()
        );
        handleTransition(tickBefore, after, false);
        cameraCoordinator.onClientTick(
            after,
            KeyBindings.cameraFreeLook.isKeyDown(),
            Minecraft.getMinecraft().currentScreen != null
                || !TargetingConfig.isCameraLockOnEnabled(),
            false,
            TargetingConfig.autoThirdPerson,
            nowMillis
        );
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        long nowMillis = ClientClock.nowMillis();
        if (event.phase == TickEvent.Phase.START) {
            mouseDiagnostics.beforeModCamera(nowMillis);
            cameraCoordinator.onRenderTick(
                event.renderTickTime,
                nowMillis
            );
            applyViewLeveling(nowMillis);
            mouseDiagnostics.afterModCamera();
        } else {
            mouseDiagnostics.onFrameEnd(nowMillis);
        }
    }

    /** Eases the pitch toward level, adding to the view like mouse movement does. */
    private void applyViewLeveling(long nowMillis) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (!viewLeveling.isActive()) {
            return;
        }
        if (player == null || minecraft.currentScreen != null || cameraCoordinator.isActive()) {
            viewLeveling.cancel();
            return;
        }
        float step = viewLeveling.pitchStep(nowMillis);
        player.rotationPitch += step;
        player.prevRotationPitch += step;
    }

    private void toggleLockOn() {
        LockOnSnapshot<EntityLivingBase> snapshot = targetingService.snapshot();
        if (snapshot.isTracking()) {
            releaseLockOn(LockReleaseReason.MANUAL);
        } else {
            acquireTarget();
        }
    }

    private void acquireTarget() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (isRidingBlocked()) {
            minecraft.player.sendStatusMessage(
                new TextComponentTranslation("zeldatargeting.message.riding_blocked"),
                true
            );
            TargetingSounds.playNoTargetSound();
            return;
        }
        long nowMillis = ClientClock.nowMillis();
        LockOnSnapshot<EntityLivingBase> before = targetingService.snapshot();
        LockOnSnapshot<EntityLivingBase> after = targetingService.acquire(
            nowMillis,
            currentOptions()
        );
        handleTransition(before, after, false);
        if (!after.isTracking() && minecraft.player != null) {
            TargetingSounds.playNoTargetSound();
            if (TargetingConfig.recenterWithoutTarget) {
                viewLeveling.start(minecraft.player.rotationPitch, nowMillis);
            }
        }
    }

    private void cycleAimPoint() {
        long nowMillis = ClientClock.nowMillis();
        LockOnSnapshot<EntityLivingBase> before = targetingService.snapshot();
        if (!before.isTracking()) {
            return;
        }
        LockOnSnapshot<EntityLivingBase> after = targetingService.cycleAnchor(true, nowMillis);
        handleTransition(before, after, false);
        TargetObservation<EntityLivingBase> target = after.getTarget();
        if (target != null && after.getTransitionId() != before.getTransitionId()) {
            Minecraft.getMinecraft().player.sendStatusMessage(
                new TextComponentTranslation(
                    "zeldatargeting.message.aim_point."
                        + target.getAnchor().name().toLowerCase(Locale.ROOT)
                ),
                true
            );
        }
    }

    private void releaseLockOn(LockReleaseReason reason) {
        long nowMillis = ClientClock.nowMillis();
        LockOnSnapshot<EntityLivingBase> before = targetingService.snapshot();
        LockOnSnapshot<EntityLivingBase> after = targetingService.release(reason, nowMillis);
        handleTransition(before, after, false);
    }

    private void cycleTarget(boolean forward) {
        LockOnSnapshot<EntityLivingBase> before = targetingService.snapshot();
        if (!before.isTracking()) {
            acquireTarget();
            return;
        }

        long nowMillis = ClientClock.nowMillis();
        LockOnSnapshot<EntityLivingBase> after = targetingService.cycle(
            forward,
            nowMillis,
            currentOptions()
        );
        handleTransition(before, after, false);
    }

    /**
     * Drops the lock while riding (when enabled), and every validation interval
     * re-checks that the target still passes the player's filters.
     */
    private LockReleaseReason validateTarget(LockOnSnapshot<EntityLivingBase> snapshot) {
        if (!snapshot.isTracking()) {
            ticksSinceValidation = 0;
            return LockReleaseReason.NONE;
        }
        if (isRidingBlocked()) {
            return LockReleaseReason.RIDING;
        }
        if (++ticksSinceValidation < TargetingConfig.validationInterval) {
            return LockReleaseReason.NONE;
        }
        ticksSinceValidation = 0;
        EntityLivingBase target = referencedTarget(snapshot);
        return target != null && !entityDetector.isEligible(target)
            ? LockReleaseReason.FILTERED
            : LockReleaseReason.NONE;
    }

    private static boolean isRidingBlocked() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return TargetingConfig.disableLockOnWhenRiding
            && minecraft.player != null
            && minecraft.player.isRiding();
    }

    private TargetingOptions currentOptions() {
        return new TargetingOptions(
            TargetPriority.fromConfig(TargetingConfig.targetPriority),
            TargetingConfig.lockOnPreset,
            TargetingMath.effectiveTrackingDistance(
                TargetingConfig.getMaxTrackingDistance(),
                entityDetector.currentTargetingRange(),
                TargetingConfig.syncTargetingRangeWithReach
            ),
            750L,
            200L,
            250L,
            TargetingConfig.quickSwitchOnKill,
            TargetingConfig.updateFrequency
        );
    }

    private void handleTransition(
            LockOnSnapshot<EntityLivingBase> before,
            LockOnSnapshot<EntityLivingBase> after,
            boolean lifecycleClear) {
        if (after.getTransitionId() == before.getTransitionId()) {
            return;
        }

        // Switch Target on Kill moves on in the same tick the target dies, before
        // HitTracker sees the killing blow; remember the dead target for it.
        EntityLivingBase previous = referencedTarget(before);
        if (after.getPhase() == LockPhase.SWITCHING
                && previous != null
                && !previous.isEntityAlive()) {
            justKilledTargetId = previous.getEntityId();
        }

        presentationFeedbackController.onTransition(toPresentationSnapshot(after), !lifecycleClear);

        if (after.getPhase() == LockPhase.ACQUIRING) {
            beginTrackingFeedback(after);
        } else if (after.getPhase() == LockPhase.SWITCHING) {
            switchTrackingFeedback(after);
        } else if (before.isTracking() && after.getPhase() == LockPhase.RELEASING) {
            ZeldaTargetingMod.getLogger().debug(
                "Lock-on released: " + after.getReleaseReason()
            );
        }
        if (lifecycleClear || after.getPhase() == LockPhase.IDLE) {
            presentationFeedbackController.clear();
        }
    }

    private void beginTrackingFeedback(LockOnSnapshot<EntityLivingBase> snapshot) {
        EntityLivingBase target = referencedTarget(snapshot);
        if (target == null) {
            return;
        }
        ZeldaTargetingMod.getLogger().debug("Lock-on enabled on target: " + target.getName());
        if (TargetingConfig.debugCompatibility) {
            ShoulderCameraState shoulder = ZeldaTargetingMod
                .getShoulderSurfingBridge()
                .captureState();
            ZeldaTargetingMod.getLogger().info(
                "[ZT Debug] Lock-on activated | SSR loaded: "
                    + ZeldaTargetingMod.isShoulderSurfingLoaded()
                    + " | SSR active: " + shoulder.isActive()
                    + " | SSR state valid: " + shoulder.canCompensate()
                    + " | Preset: " + TargetingConfig.lockOnPreset
                    + " | Smoothness: " + TargetingConfig.cameraSmoothness
            );
        }
    }

    private void switchTrackingFeedback(LockOnSnapshot<EntityLivingBase> snapshot) {
        EntityLivingBase target = referencedTarget(snapshot);
        if (target == null) {
            return;
        }
        ZeldaTargetingMod.getLogger().debug("Switched to target: " + target.getName());
    }

    private static EntityLivingBase referencedTarget(
            LockOnSnapshot<EntityLivingBase> snapshot) {
        TargetObservation<EntityLivingBase> observation = snapshot.getTarget();
        return observation == null ? null : observation.getReference();
    }

    public boolean isActive() {
        return targetingService.snapshot().isTracking();
    }

    public LockOnSnapshot<EntityLivingBase> getLockSnapshot() {
        return targetingService.snapshot();
    }

    public PresentationFeedbackController getPresentationFeedbackController() {
        return presentationFeedbackController;
    }

    /**
     * Entity id of the locked target, still reported while the lock fades out
     * (such as right after a kill), or -1 without a lock.
     */
    public int getFeedbackTargetId() {
        TargetObservation<EntityLivingBase> target = targetingService.snapshot().getTarget();
        return target == null ? -1 : target.getEntityId();
    }

    /** Entity id of a target killed this tick after the lock switched away from it, or -1. */
    public int getJustKilledTargetId() {
        return justKilledTargetId;
    }

    /**
     * Whether hits on this entity drive the ring's feedback: the locked target,
     * or one killed this tick that Switch Target on Kill already moved away from.
     */
    public boolean isFeedbackTarget(int entityId) {
        return entityId >= 0
            && (entityId == getFeedbackTargetId() || entityId == justKilledTargetId);
    }

    public Entity getCurrentTarget() {
        return referencedTarget(targetingService.snapshot());
    }

    private static TargetPresentationSnapshot toPresentationSnapshot(
            LockOnSnapshot<EntityLivingBase> snapshot) {
        EntityLivingBase target = referencedTarget(snapshot);
        return new TargetPresentationSnapshot(
            target,
            target == null ? "" : target.getName(),
            0.0D,
            0.0D,
            0.0D,
            1.0F,
            1.0F,
            0.0F,
            1.0F,
            0.0F,
            0.0D,
            0.0F,
            -1,
            "",
            false,
            snapshot.getTransitionId(),
            snapshot.getPhase(),
            PresentationStatus.NORMAL
        );
    }
}

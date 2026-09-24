package com.zeldatargeting.mod.client.camera;

import com.zeldatargeting.mod.client.camera.core.CameraCollisionResult;
import com.zeldatargeting.mod.client.camera.core.CameraDirector;
import com.zeldatargeting.mod.client.camera.core.CameraFrame;
import com.zeldatargeting.mod.client.camera.core.CameraInput;
import com.zeldatargeting.mod.client.camera.core.CameraProfile;
import com.zeldatargeting.mod.client.camera.core.CameraProfiles;
import com.zeldatargeting.mod.client.camera.core.PerspectiveController;
import com.zeldatargeting.mod.client.camera.core.PerspectiveDecision;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;
import com.zeldatargeting.mod.client.session.LockPhase;
import com.zeldatargeting.mod.client.session.LockReleaseReason;
import com.zeldatargeting.mod.client.targeting.core.TargetAnchor;
import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPoint;
import com.zeldatargeting.mod.client.targeting.core.TargetPriority;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CameraCoordinatorTest {
    @Test
    public void acquisitionCapturesOnceAndSwitchDoesNotRecapture() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);

        coordinator.onClientTick(tracking("pig", 1L), false, false, false, true, 100L);
        coordinator.onRenderTick(0.5F, 100L);
        coordinator.onClientTick(tracking("cow", 2L), false, false, false, true, 120L);
        coordinator.onRenderTick(0.5F, 120L);

        assertEquals(1, runtime.captureCount);
        assertEquals(2, runtime.collisionCount);
        assertEquals(2, runtime.appliedFrames.size());
        assertEquals(0L, runtime.elapsedSamples.get(0).longValue());
        assertEquals(20L, runtime.elapsedSamples.get(1).longValue());
    }

    @Test
    public void normalReleaseEasesThenRestoresPerspectiveOnce() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);
        coordinator.onClientTick(tracking("pig", 1L), false, false, false, true, 0L);
        coordinator.onRenderTick(0.0F, 0L);

        coordinator.onClientTick(releasing(2L), false, false, false, true, 100L);
        coordinator.onRenderTick(0.0F, 200L);
        assertEquals(0, runtime.clearedFrameCount);
        coordinator.onClientTick(idle(3L), false, false, false, true, 250L);
        coordinator.onRenderTick(0.0F, 300L);

        assertEquals(1, runtime.clearedFrameCount);
        assertEquals(1, runtime.restorePerspectiveCount);
        assertEquals(0, runtime.immediateRestoreCount);
        assertFalse(coordinator.isActive());
    }

    @Test
    public void lifecycleClearRestoresImmediatelyAndIsIdempotent() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);
        coordinator.onClientTick(tracking("pig", 1L), false, false, false, true, 0L);

        coordinator.onClientTick(idle(2L), false, false, true, true, 10L);
        coordinator.onClientTick(idle(2L), false, false, true, true, 20L);

        assertEquals(1, runtime.immediateRestoreCount);
        assertEquals(1, runtime.clearedFrameCount);
        assertEquals(1, runtime.worldResetCount);
        assertFalse(coordinator.isActive());
    }

    @Test
    public void guiSuspendsAndClosingGuiStartsFreshWithoutReacquiring() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);
        LockOnSnapshot<String> locked = tracking("pig", 1L);

        coordinator.onClientTick(locked, false, false, false, true, 0L);
        coordinator.onClientTick(locked, false, true, false, true, 10L);
        coordinator.onRenderTick(0.0F, 15L);
        coordinator.onClientTick(locked, false, false, false, true, 20L);
        coordinator.onRenderTick(0.0F, 20L);

        assertEquals(2, runtime.captureCount);
        assertEquals(1, runtime.immediateRestoreCount);
        assertEquals(1, runtime.sampleFrameCount);
        assertEquals(0L, runtime.elapsedSamples.get(0).longValue());
    }

    @Test
    public void freeLookIsForwardedWithoutChangingTheLockSnapshot() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);
        LockOnSnapshot<String> locked = tracking("pig", 1L);

        coordinator.onClientTick(locked, true, false, false, false, 0L);
        coordinator.onRenderTick(0.0F, 0L);

        assertTrue(runtime.lastFreeLook);
        assertEquals("pig", locked.getTarget().getReference());
        assertEquals(LockPhase.LOCKED, locked.getPhase());
    }

    @Test
    public void idleRenderingPerformsNoSamplingOrApplication() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);

        coordinator.onClientTick(idle(0L), false, false, false, true, 0L);
        coordinator.onRenderTick(0.5F, 20L);

        assertEquals(0, runtime.captureCount);
        assertEquals(0, runtime.collisionCount);
        assertEquals(0, runtime.sampleFrameCount);
        assertEquals(0, runtime.appliedFrames.size());
    }

    @Test
    public void autoThirdPersonChangesApplyOnlyToTheNextCameraSession() {
        FakeRuntime runtime = new FakeRuntime();
        CameraCoordinator<String> coordinator = coordinator(runtime);
        LockOnSnapshot<String> locked = tracking("pig", 1L);

        coordinator.onClientTick(locked, false, false, false, false, 0L);
        assertEquals(0, runtime.appliedPerspectiveCount);

        coordinator.onClientTick(locked, false, false, false, true, 10L);
        assertEquals(0, runtime.appliedPerspectiveCount);

        coordinator.onClientTick(locked, false, true, false, true, 20L);
        coordinator.onClientTick(locked, false, false, false, true, 30L);
        assertEquals(1, runtime.appliedPerspectiveCount);
    }

    private static CameraCoordinator<String> coordinator(FakeRuntime runtime) {
        return new CameraCoordinator<>(
            new CameraDirector(),
            new PerspectiveController(),
            runtime,
            new CameraProfileResolver() {
                @Override
                public CameraProfile resolve(String profileName, int perspective) {
                    return CameraProfiles.fromName(profileName);
                }
            }
        );
    }

    private static LockOnSnapshot<String> tracking(String reference, long transitionId) {
        return snapshot(
            LockPhase.LOCKED,
            transitionId,
            observation(reference),
            LockReleaseReason.NONE
        );
    }

    private static LockOnSnapshot<String> releasing(long transitionId) {
        return snapshot(
            LockPhase.RELEASING,
            transitionId,
            observation(null),
            LockReleaseReason.MANUAL
        );
    }

    private static LockOnSnapshot<String> idle(long transitionId) {
        return snapshot(LockPhase.IDLE, transitionId, null, LockReleaseReason.MANUAL);
    }

    private static LockOnSnapshot<String> snapshot(
            LockPhase phase,
            long transitionId,
            TargetObservation<String> target,
            LockReleaseReason reason) {
        return new LockOnSnapshot<>(
            phase,
            transitionId,
            transitionId,
            target,
            0L,
            reason,
            TargetPriority.NEAREST,
            "balanced"
        );
    }

    private static TargetObservation<String> observation(String reference) {
        return new TargetObservation<>(
            reference,
            1,
            reference == null ? "pig" : reference,
            8.0F,
            10.0F,
            true,
            true,
            true,
            6.0D,
            0.0D,
            true,
            TargetAnchor.HEAD,
            new TargetPoint(1.0D, 2.0D, 3.0D),
            0.9D,
            1.8D
        );
    }

    private static final class FakeRuntime implements CameraRuntimeAdapter<String> {
        private final List<CameraFrame> appliedFrames = new ArrayList<>();
        private final List<Long> elapsedSamples = new ArrayList<>();
        private int captureCount;
        private int collisionCount;
        private int sampleFrameCount;
        private int appliedPerspectiveCount;
        private int restorePerspectiveCount;
        private int immediateRestoreCount;
        private int clearedFrameCount;
        private int worldResetCount;
        private boolean lastFreeLook;

        @Override
        public CameraRuntimeState captureState() {
            captureCount++;
            return new CameraRuntimeState(0, 70.0F);
        }

        @Override
        public CameraCollisionResult sampleCollision(
                LockOnSnapshot<String> snapshot,
                CameraProfile profile) {
            collisionCount++;
            return new CameraCollisionResult(4.0D, 4.0D, 0.0D);
        }

        @Override
        public CameraInput sampleFrame(
                LockOnSnapshot<String> snapshot,
                CameraProfile profile,
                CameraCollisionResult collision,
                long elapsedMillis,
                boolean freeLook,
                int requestedPerspective,
                float partialTicks) {
            sampleFrameCount++;
            elapsedSamples.add(elapsedMillis);
            lastFreeLook = freeLook;
            return new CameraInput(
                0.0F,
                0.0F,
                1.0D,
                0.0D,
                0.0D,
                0.9D,
                1.8D,
                6.0D,
                collision,
                elapsedMillis,
                freeLook,
                requestedPerspective
            );
        }

        @Override
        public void applyPerspective(PerspectiveDecision decision) {
            if (!decision.shouldApply()) {
                return;
            }
            appliedPerspectiveCount++;
            if (decision.getPerspective() == 0) {
                restorePerspectiveCount++;
            }
        }

        @Override
        public void applyFrame(CameraFrame frame) {
            appliedFrames.add(frame);
        }

        @Override
        public void restoreImmediate(PerspectiveDecision decision) {
            immediateRestoreCount++;
            applyPerspective(decision);
        }

        @Override
        public void clearFrame() {
            clearedFrameCount++;
        }

        @Override
        public void resetWorldSession() {
            worldResetCount++;
        }
    }
}

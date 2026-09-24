package com.zeldatargeting.mod.client.camera;

import com.zeldatargeting.mod.client.camera.core.CameraCollisionResult;
import com.zeldatargeting.mod.client.camera.core.CameraDirector;
import com.zeldatargeting.mod.client.camera.core.CameraFrame;
import com.zeldatargeting.mod.client.camera.core.CameraInput;
import com.zeldatargeting.mod.client.camera.core.CameraProfile;
import com.zeldatargeting.mod.client.camera.core.CameraRestoreMode;
import com.zeldatargeting.mod.client.camera.core.PerspectiveController;
import com.zeldatargeting.mod.client.camera.core.PerspectiveDecision;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;

public final class CameraCoordinator<T> {
    private static final long NO_RENDER_TIME = Long.MIN_VALUE;

    private final CameraDirector director;
    private final PerspectiveController perspectiveController;
    private final CameraRuntimeAdapter<T> runtime;
    private final CameraProfileResolver profileResolver;
    private LockOnSnapshot<T> snapshot;
    private CameraProfile profile;
    private CameraCollisionResult collision;
    private boolean sessionActive;
    private boolean restoring;
    private boolean freeLook;
    private boolean presentationSuspended;
    private boolean lifecycleResetHandled;
    private long lastRenderMillis = NO_RENDER_TIME;

    public CameraCoordinator(
            CameraDirector director,
            PerspectiveController perspectiveController,
            CameraRuntimeAdapter<T> runtime,
            CameraProfileResolver profileResolver) {
        if (director == null
                || perspectiveController == null
                || runtime == null
                || profileResolver == null) {
            throw new IllegalArgumentException("camera coordinator dependencies must not be null");
        }
        this.director = director;
        this.perspectiveController = perspectiveController;
        this.runtime = runtime;
        this.profileResolver = profileResolver;
    }

    public void onClientTick(
            LockOnSnapshot<T> nextSnapshot,
            boolean freeLook,
            boolean presentationSuspended,
            boolean lifecycleClear,
            boolean autoThirdPerson,
            long nowMillis) {
        if (!lifecycleClear) {
            lifecycleResetHandled = false;
        }
        if (lifecycleClear) {
            if (!lifecycleResetHandled) {
                restoreImmediately();
                runtime.resetWorldSession();
                lifecycleResetHandled = true;
            }
            this.presentationSuspended = false;
            snapshot = nextSnapshot;
            return;
        }

        snapshot = nextSnapshot;
        this.freeLook = freeLook;
        if (presentationSuspended) {
            if (!this.presentationSuspended) {
                restoreImmediately();
            }
            this.presentationSuspended = true;
            return;
        }
        this.presentationSuspended = false;

        if (restoring) {
            return;
        }
        if (sessionActive) {
            if (nextSnapshot == null || !nextSnapshot.isTracking()) {
                beginNormalRestore(nowMillis);
                return;
            }
            collision = runtime.sampleCollision(nextSnapshot, profile);
            applyPerspective(perspectiveController.update(
                collision.getSafeDistance(),
                profile,
                nowMillis
            ));
            return;
        }
        if (nextSnapshot != null && nextSnapshot.isTracking()) {
            startSession(nextSnapshot, autoThirdPerson, nowMillis);
        }
    }

    public void onRenderTick(float partialTicks, long nowMillis) {
        if (presentationSuspended) {
            return;
        }
        if (restoring) {
            CameraFrame frame = director.updateRestore(
                nowMillis,
                profile,
                perspectiveController.getRequestedPerspective()
            );
            runtime.applyFrame(frame);
            if (frame.isRestoreComplete()) {
                finishNormalRestore();
            }
            return;
        }
        if (!sessionActive || snapshot == null || !snapshot.isTracking()) {
            return;
        }

        long elapsedMillis = lastRenderMillis == NO_RENDER_TIME
            ? 0L
            : Math.max(0L, nowMillis - lastRenderMillis);
        CameraInput input = runtime.sampleFrame(
            snapshot,
            profile,
            collision,
            elapsedMillis,
            freeLook,
            perspectiveController.getRequestedPerspective(),
            partialTicks
        );
        if (!director.isActive()) {
            director.begin(input, profile);
        }
        runtime.applyFrame(director.update(input, profile));
        lastRenderMillis = nowMillis;
    }

    public boolean isActive() {
        return sessionActive || restoring;
    }

    private void startSession(
            LockOnSnapshot<T> trackingSnapshot,
            boolean autoThirdPerson,
            long nowMillis) {
        CameraRuntimeState state = runtime.captureState();
        profile = profileResolver.resolve(
            trackingSnapshot.getCameraProfileName(),
            state.getPerspective()
        );
        sessionActive = true;
        restoring = false;
        lastRenderMillis = NO_RENDER_TIME;
        applyPerspective(perspectiveController.begin(
            state.getPerspective(),
            state.getBaselineFov(),
            autoThirdPerson,
            nowMillis
        ));
        collision = runtime.sampleCollision(trackingSnapshot, profile);
        applyPerspective(perspectiveController.update(
            collision.getSafeDistance(),
            profile,
            nowMillis
        ));
    }

    private void beginNormalRestore(long nowMillis) {
        CameraFrame frame = director.beginRestore(CameraRestoreMode.EASED, nowMillis);
        if (frame.isRestoreComplete()) {
            finishNormalRestore();
            return;
        }
        restoring = true;
    }

    private void finishNormalRestore() {
        applyPerspective(perspectiveController.restore());
        runtime.clearFrame();
        clearSessionState();
    }

    private void restoreImmediately() {
        if (!sessionActive
                && !restoring
                && !director.isActive()
                && !perspectiveController.isActive()) {
            return;
        }
        director.beginRestore(CameraRestoreMode.IMMEDIATE, 0L);
        runtime.restoreImmediate(perspectiveController.restore());
        runtime.clearFrame();
        clearSessionState();
    }

    private void clearSessionState() {
        director.reset();
        snapshot = null;
        profile = null;
        collision = null;
        sessionActive = false;
        restoring = false;
        freeLook = false;
        lastRenderMillis = NO_RENDER_TIME;
    }

    private void applyPerspective(PerspectiveDecision decision) {
        runtime.applyPerspective(decision);
    }
}

package com.zeldatargeting.mod.client.camera;

import com.zeldatargeting.mod.client.camera.core.CameraCollisionResult;
import com.zeldatargeting.mod.client.camera.core.CameraFrame;
import com.zeldatargeting.mod.client.camera.core.CameraInput;
import com.zeldatargeting.mod.client.camera.core.CameraProfile;
import com.zeldatargeting.mod.client.camera.core.PerspectiveDecision;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;

public interface CameraRuntimeAdapter<T> {
    CameraRuntimeState captureState();

    CameraCollisionResult sampleCollision(
        LockOnSnapshot<T> snapshot,
        CameraProfile profile
    );

    CameraInput sampleFrame(
        LockOnSnapshot<T> snapshot,
        CameraProfile profile,
        CameraCollisionResult collision,
        long elapsedMillis,
        boolean freeLook,
        int requestedPerspective,
        float partialTicks
    );

    void applyPerspective(PerspectiveDecision decision);

    void applyFrame(CameraFrame frame);

    void restoreImmediate(PerspectiveDecision decision);

    void clearFrame();

    void resetWorldSession();
}

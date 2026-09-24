package com.zeldatargeting.mod.client.camera;

public final class CameraRuntimeState {
    private final int perspective;
    private final float baselineFov;

    public CameraRuntimeState(int perspective, float baselineFov) {
        this.perspective = perspective;
        this.baselineFov = baselineFov;
    }

    public int getPerspective() {
        return perspective;
    }

    public float getBaselineFov() {
        return baselineFov;
    }
}

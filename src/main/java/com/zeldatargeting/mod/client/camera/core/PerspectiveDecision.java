package com.zeldatargeting.mod.client.camera.core;

public final class PerspectiveDecision {
    private final boolean apply;
    private final int perspective;
    private final float baselineFov;
    private final boolean fallbackActive;

    private PerspectiveDecision(
            boolean apply,
            int perspective,
            float baselineFov,
            boolean fallbackActive) {
        this.apply = apply;
        this.perspective = perspective;
        this.baselineFov = baselineFov;
        this.fallbackActive = fallbackActive;
    }

    public static PerspectiveDecision apply(
            int perspective,
            float baselineFov,
            boolean fallbackActive) {
        return new PerspectiveDecision(true, perspective, baselineFov, fallbackActive);
    }

    public static PerspectiveDecision noChange(
            int perspective,
            float baselineFov,
            boolean fallbackActive) {
        return new PerspectiveDecision(false, perspective, baselineFov, fallbackActive);
    }

    public boolean shouldApply() {
        return apply;
    }

    public int getPerspective() {
        return perspective;
    }

    public float getBaselineFov() {
        return baselineFov;
    }

    public boolean isFallbackActive() {
        return fallbackActive;
    }
}

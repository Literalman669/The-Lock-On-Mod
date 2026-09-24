package com.zeldatargeting.mod.client.presentation.core;

public final class PresentationStatusResolver {
    private PresentationStatusResolver() {
    }

    public static PresentationStatus resolve(float healthRatio, int hitsToKill, boolean occluded) {
        if (occluded) {
            return PresentationStatus.OCCLUDED;
        }
        if (hitsToKill == 1) {
            return PresentationStatus.LETHAL;
        }
        if (!Float.isFinite(healthRatio)) {
            return PresentationStatus.NORMAL;
        }
        if (healthRatio <= 0.25F) {
            return PresentationStatus.LOW_HEALTH;
        }
        if (healthRatio <= 0.75F) {
            return PresentationStatus.WARNING;
        }
        if (hitsToKill > 1 && hitsToKill <= 3) {
            return PresentationStatus.WARNING;
        }
        return PresentationStatus.NORMAL;
    }
}

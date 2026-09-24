package com.zeldatargeting.mod.client.presentation.core;

public final class BossEligibility {
    private BossEligibility() {
    }

    public static boolean isEligible(boolean vanillaBoss, float maxHealth, float threshold) {
        return vanillaBoss || (Float.isFinite(maxHealth) && Float.isFinite(threshold)
            && maxHealth >= Math.max(1.0F, threshold));
    }
}

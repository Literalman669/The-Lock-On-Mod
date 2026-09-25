package com.zeldatargeting.mod.client.presentation.core;

/** Replaces only a locked vanilla boss's redundant vanilla health bar. */
public final class BossBarSuppressionPolicy {
    private BossBarSuppressionPolicy() {
    }

    public static boolean shouldReplace(boolean customBannerEnabled,
                                        boolean tracking,
                                        boolean vanillaBoss,
                                        String targetName,
                                        String barName) {
        return customBannerEnabled && tracking && vanillaBoss
            && targetName != null && !targetName.isEmpty()
            && targetName.equals(barName);
    }
}

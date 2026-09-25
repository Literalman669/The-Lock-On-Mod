package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BossBarSuppressionPolicyTest {
    @Test
    public void replacesOnlyTheLockedVanillaBossBar() {
        assertTrue(BossBarSuppressionPolicy.shouldReplace(
            true, true, true, "Wither", "Wither"
        ));
        assertFalse(BossBarSuppressionPolicy.shouldReplace(
            true, true, true, "Wither", "Ender Dragon"
        ));
    }

    @Test
    public void keepsVanillaBarsWhenTheCustomBannerIsNotShowing() {
        assertFalse(BossBarSuppressionPolicy.shouldReplace(
            false, true, true, "Wither", "Wither"
        ));
        assertFalse(BossBarSuppressionPolicy.shouldReplace(
            true, false, true, "Wither", "Wither"
        ));
        assertFalse(BossBarSuppressionPolicy.shouldReplace(
            true, true, false, "Iron Golem", "Wither"
        ));
    }
}

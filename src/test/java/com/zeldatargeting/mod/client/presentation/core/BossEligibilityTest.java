package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BossEligibilityTest {
    @Test
    public void bossEligibilityRequiresBossFlagOrConfiguredHealthThreshold() {
        assertTrue(BossEligibility.isEligible(true, 20.0F, 100.0F));
        assertTrue(BossEligibility.isEligible(false, 100.0F, 100.0F));
        assertFalse(BossEligibility.isEligible(false, 99.9F, 100.0F));
        assertFalse(BossEligibility.isEligible(false, Float.NaN, 100.0F));
    }

    @Test
    public void invalidThresholdNeverMakesAnOrdinaryTargetEligible() {
        assertFalse(BossEligibility.isEligible(false, 1000.0F, Float.NaN));
    }
}

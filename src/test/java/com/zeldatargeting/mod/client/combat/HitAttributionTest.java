package com.zeldatargeting.mod.client.combat;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HitAttributionTest {
    @Test
    public void healthLossShortlyAfterMyAttackIsMine() {
        HitAttribution attribution = new HitAttribution(10);
        attribution.recordAttack(42, 100L);

        assertTrue(attribution.isOwnHit(42, 104L));
    }

    @Test
    public void healthLossOnAnotherEntityIsNotMine() {
        HitAttribution attribution = new HitAttribution(10);
        attribution.recordAttack(42, 100L);

        assertFalse(attribution.isOwnHit(7, 101L));
    }

    @Test
    public void healthLossLongAfterMyAttackIsNotMine() {
        HitAttribution attribution = new HitAttribution(10);
        attribution.recordAttack(42, 100L);

        assertFalse(attribution.isOwnHit(42, 111L));
    }

    @Test
    public void staleAttacksArePruned() {
        HitAttribution attribution = new HitAttribution(10);
        attribution.recordAttack(1, 0L);
        attribution.recordAttack(2, 50L);

        attribution.prune(55L);

        assertFalse(attribution.isOwnHit(1, 5L));
        assertTrue(attribution.isOwnHit(2, 55L));
    }

    @Test
    public void clearForgetsEveryAttack() {
        HitAttribution attribution = new HitAttribution(10);
        attribution.recordAttack(42, 100L);

        attribution.clear();

        assertFalse(attribution.isOwnHit(42, 100L));
    }
}

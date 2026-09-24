package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TargetScorerTest {
    @Test
    public void emptyCandidateListHasNoWinner() {
        assertNull(TargetScorer.selectBest(Collections.emptyList(), TargetPriority.NEAREST));
    }

    @Test
    public void nearestUsesDistanceThenAngleThenEntityId() {
        assertEquals("close", select(TargetPriority.NEAREST,
            candidate("wide", 3, 4, 30, 10, false, false, 1),
            candidate("close", 9, 3, 40, 10, false, false, 1)));
        assertEquals("center", select(TargetPriority.NEAREST,
            candidate("wide", 3, 4, 30, 10, false, false, 1),
            candidate("center", 9, 4, 10, 10, false, false, 1)));
    }

    @Test
    public void healthUsesCurrentHealthThenDistanceThenEntityId() {
        assertEquals("hurt", select(TargetPriority.HEALTH,
            candidate("healthy", 1, 1, 1, 20, false, false, 1),
            candidate("hurt", 2, 9, 9, 4, false, false, 1)));
    }

    @Test
    public void angleUsesThreeDimensionalAngleThenDistanceThenEntityId() {
        assertEquals("center", select(TargetPriority.ANGLE,
            candidate("near", 1, 1, 20, 10, false, false, 1),
            candidate("center", 2, 9, 2, 10, false, false, 1)));
    }

    @Test
    public void threatPrioritizesEntitiesActivelyTargetingThePlayer() {
        assertEquals("aggro", select(TargetPriority.THREAT,
            candidate("boss", 1, 1, 1, 100, true, false, 20),
            candidate("aggro", 2, 16, 5, 5, false, true, 1)));
    }

    @Test
    public void threatUsesHostilityDamageHealthAndDistanceWithoutClassNames() {
        assertEquals("hostile", select(TargetPriority.THREAT,
            candidate("neutral", 1, 1, 1, 100, false, false, 20),
            candidate("hostile", 2, 20, 20, 1, true, false, 1)));
        assertEquals("damage", select(TargetPriority.THREAT,
            candidate("health", 1, 1, 1, 100, true, false, 2),
            candidate("damage", 2, 20, 20, 1, true, false, 8)));
    }

    @Test
    public void allPolicyTiesUseLowerEntityId() {
        assertEquals("lower", select(TargetPriority.NEAREST,
            candidate("higher", 9, 4, 10, 10, false, false, 1),
            candidate("lower", 2, 4, 10, 10, false, false, 1)));
    }

    private static TargetCandidate<String> candidate(
            String reference, int entityId, double distanceSquared, double angle,
            float health, boolean hostile, boolean targetingPlayer, double attackDamage) {
        TargetObservation<String> observation = new TargetObservation<>(
            reference, entityId, reference, health, 100.0F, true, true, true,
            Math.sqrt(distanceSquared), 0.0D, true, TargetAnchor.HEAD,
            new TargetPoint(0.0D, 1.0D, 0.0D), 1.0D, 2.0D
        );
        return new TargetCandidate<>(
            observation, distanceSquared, angle, hostile, targetingPlayer, attackDamage
        );
    }

    @SafeVarargs
    private static String select(TargetPriority priority, TargetCandidate<String>... candidates) {
        return TargetScorer.selectBest(Arrays.asList(candidates), priority)
            .getObservation()
            .getReference();
    }
}

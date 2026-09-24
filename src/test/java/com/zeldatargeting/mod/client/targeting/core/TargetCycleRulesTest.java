package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetCycleRulesTest {
    @Test
    public void bearingOrderUsesEntityIdForEqualBearings() {
        List<TargetCandidate<String>> candidates = new ArrayList<>(Arrays.asList(
            candidate("west", 4, 270.0D),
            candidate("east-high", 9, 90.0D),
            candidate("north", 1, 0.0D),
            candidate("east-low", 3, 90.0D)
        ));
        TargetCycleRules.sortByBearing(candidates);
        assertEquals("north", candidates.get(0).getObservation().getReference());
        assertEquals("east-low", candidates.get(1).getObservation().getReference());
        assertEquals("east-high", candidates.get(2).getObservation().getReference());
        assertEquals("west", candidates.get(3).getObservation().getReference());
    }

    @Test
    public void adjacentSelectionWrapsBothDirections() {
        List<TargetCandidate<String>> candidates = Arrays.asList(
            candidate("north", 1, 0.0D),
            candidate("east", 2, 90.0D),
            candidate("west", 3, 270.0D)
        );
        assertEquals("north",
            TargetCycleRules.selectAdjacent(candidates, 3, true).getObservation().getReference());
        assertEquals("west",
            TargetCycleRules.selectAdjacent(candidates, 1, false).getObservation().getReference());
    }

    @Test
    public void missingCurrentStartsAtDirectionalEdge() {
        List<TargetCandidate<String>> candidates = Arrays.asList(
            candidate("north", 1, 0.0D),
            candidate("east", 2, 90.0D),
            candidate("west", 3, 270.0D)
        );
        assertEquals("north",
            TargetCycleRules.selectAdjacent(candidates, 99, true).getObservation().getReference());
        assertEquals("west",
            TargetCycleRules.selectAdjacent(candidates, 99, false).getObservation().getReference());
    }

    @Test
    public void cooldownAllowsTheExactBoundary() {
        assertFalse(TargetCycleRules.isCooldownElapsed(1000L, 1249L, 250L));
        assertTrue(TargetCycleRules.isCooldownElapsed(1000L, 1250L, 250L));
    }

    private static TargetCandidate<String> candidate(String reference, int entityId, double bearing) {
        TargetObservation<String> observation = new TargetObservation<>(
            reference, entityId, reference, 10.0F, 10.0F, true, true, true,
            4.0D, bearing, true, TargetAnchor.HEAD,
            new TargetPoint(0.0D, 1.0D, 0.0D), 1.0D, 2.0D
        );
        return new TargetCandidate<>(observation, 16.0D, 10.0D, false, false, 1.0D);
    }
}

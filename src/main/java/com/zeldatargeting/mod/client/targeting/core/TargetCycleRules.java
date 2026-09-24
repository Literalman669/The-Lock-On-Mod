package com.zeldatargeting.mod.client.targeting.core;

import com.zeldatargeting.mod.client.math.TargetingMath;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class TargetCycleRules {
    private TargetCycleRules() {
    }

    public static <T> void sortByBearing(List<TargetCandidate<T>> candidates) {
        if (candidates == null) {
            return;
        }
        Collections.sort(candidates, new Comparator<TargetCandidate<T>>() {
            @Override
            public int compare(TargetCandidate<T> left, TargetCandidate<T> right) {
                int comparison = Double.compare(
                    left.getObservation().getHorizontalBearingDegrees(),
                    right.getObservation().getHorizontalBearingDegrees()
                );
                return comparison != 0
                    ? comparison
                    : Integer.compare(left.getEntityId(), right.getEntityId());
            }
        });
    }

    public static <T> TargetCandidate<T> selectAdjacent(
            List<TargetCandidate<T>> sortedCandidates,
            int currentEntityId,
            boolean forward) {
        if (sortedCandidates == null || sortedCandidates.isEmpty()) {
            return null;
        }

        int currentIndex = -1;
        for (int i = 0; i < sortedCandidates.size(); i++) {
            if (sortedCandidates.get(i).getEntityId() == currentEntityId) {
                currentIndex = i;
                break;
            }
        }

        int adjacentIndex = TargetingMath.adjacentIndex(
            currentIndex,
            sortedCandidates.size(),
            forward
        );
        return adjacentIndex < 0 ? null : sortedCandidates.get(adjacentIndex);
    }

    public static boolean isCooldownElapsed(
            long lastCycleMillis,
            long nowMillis,
            long cooldownMillis) {
        return lastCycleMillis == Long.MIN_VALUE
            || nowMillis - lastCycleMillis >= cooldownMillis;
    }

    public static <T> TargetCandidate<T> selectAutomaticReplacement(
            List<TargetCandidate<T>> candidates,
            TargetPriority priority,
            int avoidedEntityId) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        TargetCandidate<T> best = null;
        TargetCandidate<T> avoided = null;
        for (TargetCandidate<T> candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if (candidate.getEntityId() == avoidedEntityId) {
                if (TargetScorer.prefers(candidate, avoided, priority)) {
                    avoided = candidate;
                }
            } else if (TargetScorer.prefers(candidate, best, priority)) {
                best = candidate;
            }
        }
        return best == null ? avoided : best;
    }
}

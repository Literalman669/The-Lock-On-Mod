package com.zeldatargeting.mod.client.targeting.core;

import java.util.List;

public final class TargetScorer {
    private TargetScorer() {
    }

    public static <T> TargetCandidate<T> selectBest(
            List<TargetCandidate<T>> candidates,
            TargetPriority priority) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        TargetCandidate<T> best = null;
        for (TargetCandidate<T> candidate : candidates) {
            if (candidate != null && (best == null || prefers(candidate, best, priority))) {
                best = candidate;
            }
        }
        return best;
    }

    public static <T> boolean prefers(
            TargetCandidate<T> candidate,
            TargetCandidate<T> current,
            TargetPriority priority) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }

        TargetPriority resolved = priority == null ? TargetPriority.NEAREST : priority;
        switch (resolved) {
            case HEALTH:
                return preferAscendingHealth(candidate, current);
            case THREAT:
                return preferThreat(candidate, current);
            case ANGLE:
                return preferAscendingAngle(candidate, current);
            case NEAREST:
            default:
                return preferAscendingDistance(candidate, current);
        }
    }

    private static <T> boolean preferAscendingDistance(
            TargetCandidate<T> candidate,
            TargetCandidate<T> current) {
        int comparison = Double.compare(candidate.getDistanceSquared(), current.getDistanceSquared());
        if (comparison != 0) {
            return comparison < 0;
        }
        comparison = Double.compare(candidate.getViewAngleDegrees(), current.getViewAngleDegrees());
        return comparison != 0
            ? comparison < 0
            : Integer.compare(candidate.getEntityId(), current.getEntityId()) < 0;
    }

    private static <T> boolean preferAscendingHealth(
            TargetCandidate<T> candidate,
            TargetCandidate<T> current) {
        int comparison = Float.compare(
            candidate.getObservation().getHealth(),
            current.getObservation().getHealth()
        );
        if (comparison != 0) {
            return comparison < 0;
        }
        comparison = Double.compare(candidate.getDistanceSquared(), current.getDistanceSquared());
        return comparison != 0
            ? comparison < 0
            : Integer.compare(candidate.getEntityId(), current.getEntityId()) < 0;
    }

    private static <T> boolean preferAscendingAngle(
            TargetCandidate<T> candidate,
            TargetCandidate<T> current) {
        int comparison = Double.compare(candidate.getViewAngleDegrees(), current.getViewAngleDegrees());
        if (comparison != 0) {
            return comparison < 0;
        }
        comparison = Double.compare(candidate.getDistanceSquared(), current.getDistanceSquared());
        return comparison != 0
            ? comparison < 0
            : Integer.compare(candidate.getEntityId(), current.getEntityId()) < 0;
    }

    private static <T> boolean preferThreat(
            TargetCandidate<T> candidate,
            TargetCandidate<T> current) {
        int comparison = Boolean.compare(candidate.isTargetingPlayer(), current.isTargetingPlayer());
        if (comparison != 0) {
            return comparison > 0;
        }
        comparison = Boolean.compare(candidate.isHostile(), current.isHostile());
        if (comparison != 0) {
            return comparison > 0;
        }
        comparison = Double.compare(candidate.getAttackDamage(), current.getAttackDamage());
        if (comparison != 0) {
            return comparison > 0;
        }
        comparison = Float.compare(
            candidate.getObservation().getHealth(),
            current.getObservation().getHealth()
        );
        if (comparison != 0) {
            return comparison > 0;
        }
        comparison = Double.compare(candidate.getDistanceSquared(), current.getDistanceSquared());
        if (comparison != 0) {
            return comparison < 0;
        }
        comparison = Double.compare(candidate.getViewAngleDegrees(), current.getViewAngleDegrees());
        return comparison != 0
            ? comparison < 0
            : Integer.compare(candidate.getEntityId(), current.getEntityId()) < 0;
    }
}

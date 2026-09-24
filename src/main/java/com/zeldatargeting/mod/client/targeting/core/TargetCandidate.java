package com.zeldatargeting.mod.client.targeting.core;

public final class TargetCandidate<T> {
    private final TargetObservation<T> observation;
    private final double distanceSquared;
    private final double viewAngleDegrees;
    private final boolean hostile;
    private final boolean targetingPlayer;
    private final double attackDamage;

    public TargetCandidate(
            TargetObservation<T> observation,
            double distanceSquared,
            double viewAngleDegrees,
            boolean hostile,
            boolean targetingPlayer,
            double attackDamage) {
        if (observation == null) {
            throw new IllegalArgumentException("observation must not be null");
        }
        this.observation = observation;
        this.distanceSquared = distanceSquared;
        this.viewAngleDegrees = viewAngleDegrees;
        this.hostile = hostile;
        this.targetingPlayer = targetingPlayer;
        this.attackDamage = attackDamage;
    }

    public TargetObservation<T> getObservation() {
        return observation;
    }

    public double getDistanceSquared() {
        return distanceSquared;
    }

    public double getViewAngleDegrees() {
        return viewAngleDegrees;
    }

    public boolean isHostile() {
        return hostile;
    }

    public boolean isTargetingPlayer() {
        return targetingPlayer;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public int getEntityId() {
        return observation.getEntityId();
    }
}

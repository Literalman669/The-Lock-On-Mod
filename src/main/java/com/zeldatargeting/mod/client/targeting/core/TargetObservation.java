package com.zeldatargeting.mod.client.targeting.core;

public final class TargetObservation<T> {
    private final T reference;
    private final int entityId;
    private final String name;
    private final float health;
    private final float maxHealth;
    private final boolean alive;
    private final boolean present;
    private final boolean sameDimension;
    private final double distance;
    private final double horizontalBearingDegrees;
    private final boolean visible;
    private final TargetAnchor anchor;
    private final TargetPoint focus;
    private final double width;
    private final double height;

    public TargetObservation(
            T reference,
            int entityId,
            String name,
            float health,
            float maxHealth,
            boolean alive,
            boolean present,
            boolean sameDimension,
            double distance,
            double horizontalBearingDegrees,
            boolean visible,
            TargetAnchor anchor,
            TargetPoint focus,
            double width,
            double height) {
        this.reference = reference;
        this.entityId = entityId;
        this.name = name == null ? "" : name;
        this.health = health;
        this.maxHealth = maxHealth;
        this.alive = alive;
        this.present = present;
        this.sameDimension = sameDimension;
        this.distance = distance;
        this.horizontalBearingDegrees = horizontalBearingDegrees;
        this.visible = visible;
        this.anchor = anchor == null ? TargetAnchor.CENTER : anchor;
        this.focus = focus == null ? new TargetPoint(0.0D, 0.0D, 0.0D) : focus;
        this.width = width;
        this.height = height;
    }

    public T getReference() {
        return reference;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean isSameDimension() {
        return sameDimension;
    }

    public double getDistance() {
        return distance;
    }

    public double getHorizontalBearingDegrees() {
        return horizontalBearingDegrees;
    }

    public boolean isVisible() {
        return visible;
    }

    public TargetAnchor getAnchor() {
        return anchor;
    }

    public TargetPoint getFocus() {
        return focus;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public TargetObservation<T> withoutReference() {
        return new TargetObservation<>(
            null,
            entityId,
            name,
            health,
            maxHealth,
            alive,
            present,
            sameDimension,
            distance,
            horizontalBearingDegrees,
            visible,
            anchor,
            focus,
            width,
            height
        );
    }
}

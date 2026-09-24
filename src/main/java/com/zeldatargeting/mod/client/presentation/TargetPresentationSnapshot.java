package com.zeldatargeting.mod.client.presentation;

import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.session.LockPhase;
import net.minecraft.entity.EntityLivingBase;

public final class TargetPresentationSnapshot {
    private final EntityLivingBase target;
    private final String name;
    private final double interpolatedX;
    private final double interpolatedY;
    private final double interpolatedZ;
    private final float width;
    private final float height;
    private final float health;
    private final float maxHealth;
    private final float healthRatio;
    private final double distance;
    private final float predictedDamage;
    private final int hitsToKill;
    private final String vulnerabilityText;
    private final boolean vanillaBoss;
    private final long transitionId;
    private final LockPhase lockPhase;
    private final PresentationStatus status;

    public TargetPresentationSnapshot(
            EntityLivingBase target, String name, double interpolatedX, double interpolatedY,
            double interpolatedZ, float width, float height, float health, float maxHealth,
            float healthRatio, double distance, float predictedDamage, int hitsToKill,
            String vulnerabilityText, boolean vanillaBoss, long transitionId, LockPhase lockPhase,
            PresentationStatus status) {
        this.target = target;
        this.name = name == null ? "" : name;
        this.interpolatedX = interpolatedX;
        this.interpolatedY = interpolatedY;
        this.interpolatedZ = interpolatedZ;
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = maxHealth;
        this.healthRatio = healthRatio;
        this.distance = distance;
        this.predictedDamage = predictedDamage;
        this.hitsToKill = hitsToKill;
        this.vulnerabilityText = vulnerabilityText == null ? "" : vulnerabilityText;
        this.vanillaBoss = vanillaBoss;
        this.transitionId = transitionId;
        this.lockPhase = lockPhase == null ? LockPhase.IDLE : lockPhase;
        this.status = status == null ? PresentationStatus.NORMAL : status;
    }

    public EntityLivingBase getTarget() { return target; }
    public String getName() { return name; }
    public double getInterpolatedX() { return interpolatedX; }
    public double getInterpolatedY() { return interpolatedY; }
    public double getInterpolatedZ() { return interpolatedZ; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getHealthRatio() { return healthRatio; }
    public double getDistance() { return distance; }
    public float getPredictedDamage() { return predictedDamage; }
    public int getHitsToKill() { return hitsToKill; }
    public String getVulnerabilityText() { return vulnerabilityText; }
    public boolean isVanillaBoss() { return vanillaBoss; }
    public long getTransitionId() { return transitionId; }
    public LockPhase getLockPhase() { return lockPhase; }
    public PresentationStatus getStatus() { return status; }
}

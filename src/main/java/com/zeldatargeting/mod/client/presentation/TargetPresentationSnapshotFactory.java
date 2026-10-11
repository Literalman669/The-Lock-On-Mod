package com.zeldatargeting.mod.client.presentation;

import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.client.combat.DamageCalculator;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatusResolver;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;
import com.zeldatargeting.mod.client.session.LockPhase;
import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.world.World;

public final class TargetPresentationSnapshotFactory {
    private final Minecraft minecraft;
    // The HUD and the ring each build a snapshot every frame; the combat estimates
    // only change between game ticks, so they are computed once per tick and target.
    private World cachedWorld;
    private long cachedWorldTime = Long.MIN_VALUE;
    private int cachedTargetId = Integer.MIN_VALUE;
    private float cachedPredictedDamage;
    private int cachedHitsToKill;
    private String cachedVulnerabilityText = "";

    public TargetPresentationSnapshotFactory() {
        this(Minecraft.getMinecraft());
    }

    TargetPresentationSnapshotFactory(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public TargetPresentationSnapshot create(TargetingManager manager, float partialTicks) {
        if (manager == null || minecraft.player == null || minecraft.world == null) {
            return null;
        }

        LockOnSnapshot<EntityLivingBase> lock = manager.getLockSnapshot();
        if (lock == null || !lock.isPresentationVisible()) {
            return null;
        }

        TargetObservation<EntityLivingBase> observation = lock.getTarget();
        EntityLivingBase target = observation == null ? null : observation.getReference();
        if (target == null || !target.isEntityAlive()) {
            return null;
        }

        float safePartialTicks = Math.max(0.0F, Math.min(1.0F, partialTicks));
        float health = finiteOr(target.getHealth(), 0.0F);
        float maxHealth = Math.max(1.0F, finiteOr(target.getMaxHealth(), 1.0F));
        float healthRatio = Math.max(0.0F, Math.min(1.0F, health / maxHealth));
        refreshCombatEstimates(target, health);
        int hitsToKill = cachedHitsToKill;
        float predictedDamage = cachedPredictedDamage;
        String vulnerabilityText = TargetingConfig.showVulnerabilities ? cachedVulnerabilityText : "";
        boolean occluded = lock.getPhase() == LockPhase.OCCLUDED_GRACE || !observation.isVisible();
        PresentationStatus status = PresentationStatusResolver.resolve(
            healthRatio,
            TargetingConfig.highlightLethalTargets ? hitsToKill : -1,
            occluded
        );

        return new TargetPresentationSnapshot(
            target,
            target.getName(),
            interpolate(target.lastTickPosX, target.posX, safePartialTicks),
            interpolate(target.lastTickPosY, target.posY, safePartialTicks),
            interpolate(target.lastTickPosZ, target.posZ, safePartialTicks),
            target.width,
            target.height,
            health,
            maxHealth,
            healthRatio,
            Math.sqrt(minecraft.player.getDistanceSq(target)),
            finiteOr(predictedDamage, 0.0F),
            hitsToKill,
            vulnerabilityText,
            target instanceof EntityDragon || target instanceof EntityWither,
            lock.getTransitionId(),
            lock.getPhase(),
            status
        );
    }

    private void refreshCombatEstimates(EntityLivingBase target, float health) {
        World world = minecraft.world;
        long worldTime = world.getTotalWorldTime();
        if (world == cachedWorld
                && worldTime == cachedWorldTime
                && target.getEntityId() == cachedTargetId) {
            return;
        }
        cachedWorld = world;
        cachedWorldTime = worldTime;
        cachedTargetId = target.getEntityId();
        cachedPredictedDamage = DamageCalculator.calculateDamage(target);
        cachedHitsToKill = DamageCalculator.hitsToKill(health, cachedPredictedDamage);
        cachedVulnerabilityText = DamageCalculator.getVulnerabilityText(target);
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }

    private static float finiteOr(float value, float fallback) {
        return Float.isFinite(value) ? value : fallback;
    }
}

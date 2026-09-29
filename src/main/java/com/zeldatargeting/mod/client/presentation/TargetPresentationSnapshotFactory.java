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

public final class TargetPresentationSnapshotFactory {
    private final Minecraft minecraft;

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
        int hitsToKill = DamageCalculator.calculateHitsToKill(target);
        float predictedDamage = DamageCalculator.calculateDamage(target);
        String vulnerabilityText = TargetingConfig.showVulnerabilities
            ? DamageCalculator.getVulnerabilityText(target) : "";
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

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }

    private static float finiteOr(float value, float fallback) {
        return Float.isFinite(value) ? value : fallback;
    }
}

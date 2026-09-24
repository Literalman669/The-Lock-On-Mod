package com.zeldatargeting.mod.client.targeting;

import com.zeldatargeting.mod.client.math.TargetingMath;
import com.zeldatargeting.mod.client.targeting.core.TargetAnchor;
import com.zeldatargeting.mod.client.targeting.core.TargetAnchorResolver;
import com.zeldatargeting.mod.client.targeting.core.TargetCandidate;
import com.zeldatargeting.mod.client.targeting.core.TargetObservation;
import com.zeldatargeting.mod.client.targeting.core.TargetPoint;
import com.zeldatargeting.mod.client.targeting.core.TargetProvider;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public final class EntityDetector implements TargetProvider<EntityLivingBase> {
    private static final TargetAnchor[] ANCHOR_ORDER = {
        TargetAnchor.HEAD,
        TargetAnchor.CENTER,
        TargetAnchor.LOWER_BODY
    };

    private final Minecraft mc;

    public EntityDetector() {
        mc = Minecraft.getMinecraft();
    }

    @Override
    public void collectCandidates(
            List<TargetCandidate<EntityLivingBase>> output,
            boolean acquisitionCone) {
        output.clear();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null) {
            return;
        }

        double range = TargetingConfig.getTargetingRange();
        AxisAlignedBB box = new AxisAlignedBB(
            player.posX - range,
            player.posY - range,
            player.posZ - range,
            player.posX + range,
            player.posY + range,
            player.posZ + range
        );
        for (EntityLivingBase entity
                : mc.world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            TargetCandidate<EntityLivingBase> candidate = buildCandidate(
                player,
                entity,
                acquisitionCone
            );
            if (candidate != null) {
                output.add(candidate);
            }
        }
    }

    @Override
    public TargetObservation<EntityLivingBase> observe(
            EntityLivingBase target,
            TargetAnchor preferredAnchor) {
        EntityPlayer player = mc.player;
        if (target == null || player == null || mc.world == null) {
            return null;
        }
        return buildObservation(player, target, preferredAnchor);
    }

    private TargetCandidate<EntityLivingBase> buildCandidate(
            EntityPlayer player,
            EntityLivingBase entity,
            boolean acquisitionCone) {
        if (entity == player
                || !entity.isEntityAlive()
                || isSpectatorPlayer(entity)
                || !isTargetableEntityType(entity)) {
            return null;
        }

        double distanceSquared = player.getDistanceSq(entity);
        double range = TargetingConfig.getTargetingRange();
        if (distanceSquared > range * range) {
            return null;
        }

        TargetObservation<EntityLivingBase> observation = buildObservation(
            player,
            entity,
            TargetAnchor.combatDefault()
        );
        if (TargetingConfig.shouldRequireLineOfSight() && !observation.isVisible()) {
            return null;
        }

        Vec3d look = player.getLookVec();
        TargetPoint focus = observation.getFocus();
        double viewAngle = TargetingMath.angleDegrees(
            look.x,
            look.y,
            look.z,
            focus.getX() - player.posX,
            focus.getY() - (player.posY + player.getEyeHeight()),
            focus.getZ() - player.posZ
        );
        if (acquisitionCone && viewAngle > TargetingConfig.getMaxAngle()) {
            return null;
        }

        boolean hostile = entity instanceof IMob;
        boolean targetingPlayer = entity instanceof EntityLiving
            && ((EntityLiving) entity).getAttackTarget() == player;
        IAttributeInstance attackAttribute = entity.getEntityAttribute(
            SharedMonsterAttributes.ATTACK_DAMAGE
        );
        double attackDamage = attackAttribute == null
            ? 0.0D
            : attackAttribute.getAttributeValue();
        return new TargetCandidate<>(
            observation,
            distanceSquared,
            viewAngle,
            hostile,
            targetingPlayer,
            attackDamage
        );
    }

    private TargetObservation<EntityLivingBase> buildObservation(
            EntityPlayer player,
            EntityLivingBase target,
            TargetAnchor preferredAnchor) {
        TargetAnchor requested = preferredAnchor == null
            ? TargetAnchor.combatDefault()
            : preferredAnchor;
        AxisAlignedBB bounds = target.getEntityBoundingBox();
        TargetPoint requestedPoint = resolveAnchor(bounds, target, requested);
        TargetAnchor resolvedAnchor = requested;
        TargetPoint focus = requestedPoint;
        boolean visible = !TargetingConfig.shouldRequireLineOfSight();

        if (!visible) {
            if (hasLineOfSight(player, requestedPoint)) {
                visible = true;
            } else {
                for (TargetAnchor anchor : ANCHOR_ORDER) {
                    if (anchor == requested) {
                        continue;
                    }
                    TargetPoint point = resolveAnchor(bounds, target, anchor);
                    if (hasLineOfSight(player, point)) {
                        resolvedAnchor = anchor;
                        focus = point;
                        visible = true;
                        break;
                    }
                }
            }
        }

        boolean present = target.world == mc.world
            && mc.world.getEntityByID(target.getEntityId()) == target;
        boolean sameDimension = target.dimension == player.dimension;
        double width = Math.max(0.0D, bounds.maxX - bounds.minX);
        double height = Math.max(0.0D, bounds.maxY - bounds.minY);
        return new TargetObservation<>(
            target,
            target.getEntityId(),
            target.getName(),
            target.getHealth(),
            target.getMaxHealth(),
            target.isEntityAlive(),
            present,
            sameDimension,
            player.getDistance(target),
            TargetingMath.horizontalBearing(
                target.posX - player.posX,
                target.posZ - player.posZ
            ),
            visible,
            resolvedAnchor,
            focus,
            width,
            height
        );
    }

    private TargetPoint resolveAnchor(
            AxisAlignedBB bounds,
            EntityLivingBase target,
            TargetAnchor anchor) {
        return TargetAnchorResolver.resolve(
            anchor,
            (bounds.minX + bounds.maxX) * 0.5D,
            bounds.minY,
            (bounds.minZ + bounds.maxZ) * 0.5D,
            bounds.maxY,
            target.getEyeHeight()
        );
    }

    private boolean hasLineOfSight(EntityPlayer player, TargetPoint point) {
        Vec3d start = new Vec3d(
            player.posX,
            player.posY + player.getEyeHeight(),
            player.posZ
        );
        Vec3d end = new Vec3d(point.getX(), point.getY(), point.getZ());
        RayTraceResult result = player.world.rayTraceBlocks(start, end, false, true, false);
        return result == null || result.typeOfHit != RayTraceResult.Type.BLOCK;
    }

    private static boolean isSpectatorPlayer(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator();
    }

    private static boolean isTargetableEntityType(EntityLivingBase entity) {
        if (entity instanceof IMob) {
            return TargetingConfig.shouldTargetHostileMobs();
        }
        if (entity instanceof EntityAnimal) {
            return TargetingConfig.shouldTargetPassiveMobs();
        }
        if (entity instanceof EntityPlayer) {
            return TargetingConfig.shouldTargetPlayers();
        }
        return TargetingConfig.shouldTargetNeutralMobs();
    }
}

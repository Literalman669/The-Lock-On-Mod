package com.zeldatargeting.mod.client.targeting;

import com.zeldatargeting.mod.compat.CompatEntityFilter;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityDetector {
    
    private final Minecraft mc;
    private final TargetSelector targetSelector = new TargetSelector();
    private final List<Entity> cachedValidTargets = new ArrayList<>(16);
    private final List<Entity> cachedCyclingTargets = new ArrayList<>(16);
    
    public EntityDetector() {
        this.mc = Minecraft.getMinecraft();
    }
    
    public Entity findNearestTarget() {
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null) {
            return null;
        }
        
        List<Entity> validTargets = getValidTargetsInRange(player);
        if (validTargets.isEmpty()) {
            return null;
        }
        switch (TargetingConfig.targetPriority.toLowerCase()) {
            case "health":   targetSelector.setPriority(TargetSelector.TargetPriority.HEALTH);       break;
            case "threat":   targetSelector.setPriority(TargetSelector.TargetPriority.THREAT_LEVEL); break;
            case "angle":    targetSelector.setPriority(TargetSelector.TargetPriority.ANGLE);        break;
            default:         targetSelector.setPriority(TargetSelector.TargetPriority.DISTANCE);     break;
        }
        return targetSelector.selectBestTarget(validTargets, player);
    }
    
    public Entity findNextTarget(Entity currentTarget, boolean forward) {
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null) {
            return null;
        }
        
        // Use all nearby valid entities (no angle filter) so targets behind the
        // camera are still reachable, and sort by stable clockwise bearing so
        // Q/E always sweep consistently regardless of camera rotation.
        List<Entity> validTargets = getValidTargetsForCycling(player);
        if (validTargets.isEmpty()) {
            return null;
        }
        
        validTargets.sort((e1, e2) -> {
            int cmp = Double.compare(getHorizontalBearing(player, e1), getHorizontalBearing(player, e2));
            return cmp != 0 ? cmp : Integer.compare(e1.getEntityId(), e2.getEntityId());
        });
        
        if (currentTarget == null) {
            return validTargets.get(0);
        }
        
        int currentIndex = validTargets.indexOf(currentTarget);
        if (currentIndex == -1) {
            return findNearestTarget();
        }
        
        if (forward) {
            return validTargets.get((currentIndex + 1) % validTargets.size());
        } else {
            return validTargets.get((currentIndex - 1 + validTargets.size()) % validTargets.size());
        }
    }
    
    /** Clockwise horizontal bearing [0,360) from player to entity; stable regardless of camera rotation. */
    private double getHorizontalBearing(EntityPlayer player, Entity entity) {
        double dx = entity.posX - player.posX;
        double dz = entity.posZ - player.posZ;
        double bearing = Math.toDegrees(Math.atan2(dx, -dz));
        return bearing < 0 ? bearing + 360.0 : bearing;
    }
    
    /** Returns all valid targets in range without the angle/FOV check, for use during cycling. */
    private List<Entity> getValidTargetsForCycling(EntityPlayer player) {
        List<Entity> validTargets = cachedCyclingTargets;
        validTargets.clear();
        double range = TargetingConfig.getTargetingRange(player);
        double rangeSq = range * range;
        AxisAlignedBB searchBox = new AxisAlignedBB(
            player.posX - range, player.posY - range, player.posZ - range,
            player.posX + range, player.posY + range, player.posZ + range);
        for (Entity entity : player.world.getEntitiesWithinAABB(Entity.class, searchBox)) {
            if (entity == player) continue;
            if (!entity.isEntityAlive()) continue;
            if (!(entity instanceof EntityLiving)) continue;
            if (player.getDistanceSq(entity) > rangeSq) continue;
            if (!isTargetableEntityType(entity)) continue;
            if (CompatEntityFilter.isBlacklisted(entity)) continue;
            validTargets.add(entity);
        }
        return validTargets;
    }
    
    private List<Entity> getValidTargetsInRange(EntityPlayer player) {
        List<Entity> validTargets = cachedValidTargets;
        validTargets.clear();
        World world = player.world;
        
        // Create bounding box for search area
        double range = TargetingConfig.getTargetingRange(player);
        AxisAlignedBB searchBox = new AxisAlignedBB(
            player.posX - range, player.posY - range, player.posZ - range,
            player.posX + range, player.posY + range, player.posZ + range
        );
        
        List<Entity> nearbyEntities = world.getEntitiesWithinAABB(Entity.class, searchBox);
        
        for (Entity entity : nearbyEntities) {
            if (isValidTarget(player, entity)) {
                validTargets.add(entity);
            }
        }
        
        return validTargets;
    }
    
    private boolean isValidTarget(EntityPlayer player, Entity entity) {
        // Don't target the player themselves
        if (entity == player) {
            return false;
        }
        
        // Don't target dead entities
        if (!entity.isEntityAlive()) {
            return false;
        }
        
        // Only target living entities for now
        if (!(entity instanceof EntityLiving)) {
            return false;
        }
        
        // Check distance
        double distance = player.getDistanceSq(entity);
        double maxRange = TargetingConfig.getTargetingRange(player);
        if (distance > maxRange * maxRange) {
            return false;
        }
        
        // Check angle (within field of view)
        Vec3d playerLook = player.getLookVec();
        double angle = getAngleToEntity(player, entity, playerLook);
        if (angle > TargetingConfig.getMaxAngle()) {
            return false;
        }
        
        // Check line of sight
        if (TargetingConfig.shouldRequireLineOfSight() && !hasLineOfSight(player, entity)) {
            return false;
        }
        
        if (!isTargetableEntityType(entity)) {
            return false;
        }
        if (CompatEntityFilter.isBlacklisted(entity)) {
            return false;
        }
        return true;
    }
    
    private double getAngleToEntity(EntityPlayer player, Entity entity, Vec3d playerLook) {
        Vec3d toEntity = new Vec3d(
            entity.posX - player.posX,
            entity.posY - player.posY,
            entity.posZ - player.posZ
        ).normalize();
        
        double dot = playerLook.dotProduct(toEntity);
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
    }
    
    private boolean hasLineOfSight(EntityPlayer player, Entity target) {
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d end = new Vec3d(target.posX, target.posY + target.height * 0.5, target.posZ);
        
        RayTraceResult result = player.world.rayTraceBlocks(start, end, false, true, false);
        return result == null || result.typeOfHit != RayTraceResult.Type.BLOCK;
    }
    
    private boolean isTargetableEntityType(Entity entity) {
        // Target hostile mobs
        if (entity instanceof IMob) {
            return TargetingConfig.shouldTargetHostileMobs();
        }
        
        // Target animals
        if (entity instanceof EntityAnimal) {
            return TargetingConfig.shouldTargetPassiveMobs();
        }
        
        // Target other players
        if (entity instanceof EntityPlayer) {
            return TargetingConfig.shouldTargetPlayers();
        }
        
        // Target other living entities (neutral mobs, modded entities not covered by IMob/EntityAnimal/EntityPlayer)
        if (entity instanceof EntityLiving) {
            return TargetingConfig.shouldTargetNeutralMobs();
        }
        
        return false;
    }
}
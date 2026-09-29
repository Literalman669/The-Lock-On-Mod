package com.zeldatargeting.mod.client.combat;

import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.client.render.DamageNumbersRenderer;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

/**
 * Detects hits from health changes on the client's own entities, which the
 * server syncs to every nearby player, so it works in singleplayer and on
 * dedicated servers alike. Hits feed damage numbers and the locked target's
 * ring effects. Everything here runs on the client thread.
 */
@SideOnly(Side.CLIENT)
public class HitTracker {

    private static final double TRACKING_RANGE = 10.0D;
    // Server health updates can trail the local swing by a few ticks
    private static final int CRIT_ATTRIBUTION_TICKS = 10;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final DamageNumbersRenderer damageNumbers;
    private Map<Integer, Float> lastHealth = new HashMap<>();
    private Map<Integer, Float> currentHealth = new HashMap<>();
    private long clientTicks;
    private int critTargetId = -1;
    private long critAttackTick;

    public HitTracker(DamageNumbersRenderer damageNumbers) {
        this.damageNumbers = damageNumbers;
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        // Fires on both sides in singleplayer; only the local client swing counts.
        EntityPlayer player = event.getEntityPlayer();
        if (player != mc.player || !player.world.isRemote) return;

        // The attack cooldown has not been reset yet, so this matches the real hit.
        if (DamageFormula.isCritical(player.getCooledAttackStrength(0.5f), DamageCalculator.canCriticalHit(player))) {
            critTargetId = event.getTarget().getEntityId();
            critAttackTick = clientTicks;
        } else {
            critTargetId = -1;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.world == null || mc.player == null) {
            lastHealth.clear();
            critTargetId = -1;
            return;
        }
        if (mc.isGamePaused()) return;

        clientTicks++;
        // Damage numbers need every nearby entity; ring effects only the locked target.
        if (TargetingConfig.enableDamageNumbers) {
            double rangeSq = TRACKING_RANGE * TRACKING_RANGE;
            for (EntityLivingBase entity : mc.world.getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    mc.player.getEntityBoundingBox().grow(TRACKING_RANGE))) {
                if (mc.player.getDistanceSq(entity) < rangeSq) {
                    observe(entity);
                }
            }
        }
        int targetId = feedbackTargetId();
        Entity target = targetId < 0 ? null : mc.world.getEntityByID(targetId);
        if (target instanceof EntityLivingBase && !currentHealth.containsKey(targetId)) {
            observe((EntityLivingBase) target);
        }

        // Entities that left tracking are forgotten, so re-entering never reports a hit.
        Map<Integer, Float> previous = lastHealth;
        lastHealth = currentHealth;
        currentHealth = previous;
        currentHealth.clear();
    }

    private void observe(EntityLivingBase entity) {
        int id = entity.getEntityId();
        float health = entity.getHealth();
        currentHealth.put(id, health);
        Float previous = lastHealth.get(id);
        if (previous == null || health >= previous) return;

        boolean isLethal = health <= 0.0f;
        boolean isCritical = id == critTargetId && clientTicks - critAttackTick <= CRIT_ATTRIBUTION_TICKS;
        if (isCritical) {
            critTargetId = -1;
        }

        if (TargetingConfig.enableDamageNumbers) {
            damageNumbers.spawn(entity, previous - health, isCritical, isLethal);
        }
        // The lock may already be releasing on the killing blow, so match by id.
        if (id == feedbackTargetId()) {
            TargetingManager.getInstance().getPresentationFeedbackController().onDamage(isCritical, isLethal);
        }
    }

    private static int feedbackTargetId() {
        TargetingManager manager = TargetingManager.getInstance();
        return manager == null ? -1 : manager.getFeedbackTargetId();
    }
}

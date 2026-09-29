package com.zeldatargeting.mod.client.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches the real post-armor damage of the local player's last hit on each target.
 * <p>
 * LivingDamageEvent only fires on the logical server, so this only sees hits in
 * singleplayer or when hosting over LAN. On a dedicated server nothing is cached
 * and DamageCalculator falls back to its estimate.
 * <p>
 * The event runs on the integrated server thread while the HUD reads the cache on
 * the client thread, so the cache is a concurrent map of immutable entries.
 */
@SideOnly(Side.CLIENT)
public class DamageEventListener {

    private static final long CACHE_TTL_TICKS = 100L; // 5 seconds at 20 tps

    private static final Map<UUID, CachedHit> lastHits = new ConcurrentHashMap<>();

    private static final class CachedHit {
        private final float damage;
        private final long tick;

        private CachedHit(float damage, long tick) {
            this.damage = damage;
            this.tick = tick;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        EntityLivingBase target = event.getEntityLiving();
        if (target == null) return;

        // Only cache damage dealt by the local player
        Entity trueSource = event.getSource().getTrueSource();
        if (trueSource == null || !trueSource.getUniqueID().equals(mc.player.getUniqueID())) return;

        long now = mc.player.ticksExisted;
        lastHits.values().removeIf(hit -> now - hit.tick > CACHE_TTL_TICKS);
        lastHits.put(target.getUniqueID(), new CachedHit(event.getAmount(), now));
    }

    private static CachedHit freshHit(Entity target) {
        if (target == null) return null;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return null;
        CachedHit hit = lastHits.get(target.getUniqueID());
        if (hit == null || mc.player.ticksExisted - hit.tick > CACHE_TTL_TICKS) return null;
        return hit;
    }

    public static boolean hasDamageData(Entity target) {
        return freshHit(target) != null;
    }

    public static float getLastDamage(Entity target) {
        CachedHit hit = freshHit(target);
        return hit != null ? hit.damage : 0f;
    }
}

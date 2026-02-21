package com.zeldatargeting.mod.client.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class DamageEventListener {

    private static final long CACHE_TTL_TICKS = 100L; // 5 seconds at 20 tps

    private static final Map<UUID, Float> lastDamageDealt = new HashMap<>();
    private static final Map<UUID, Long> cacheTimestamp = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        EntityLivingBase target = event.getEntityLiving();
        if (target == null) return;

        // Only cache damage dealt by the local player
        Entity trueSource = event.getSource().getTrueSource();
        if (trueSource == null || trueSource.getEntityId() != mc.player.getEntityId()) return;

        UUID id = target.getUniqueID();
        long now = mc.player.ticksExisted;

        evictStale(now);

        lastDamageDealt.put(id, event.getAmount());
        cacheTimestamp.put(id, now);
    }

    private static void evictStale(long now) {
        Iterator<Map.Entry<UUID, Long>> it = cacheTimestamp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (now - entry.getValue() > CACHE_TTL_TICKS) {
                lastDamageDealt.remove(entry.getKey());
                it.remove();
            }
        }
    }

    public static boolean hasDamageData(Entity target) {
        if (target == null) return false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return false;
        UUID id = target.getUniqueID();
        Long ts = cacheTimestamp.get(id);
        if (ts == null) return false;
        return (mc.player.ticksExisted - ts) <= CACHE_TTL_TICKS;
    }

    public static float getLastDamage(Entity target) {
        if (target == null) return 0f;
        Float val = lastDamageDealt.get(target.getUniqueID());
        return val != null ? val : 0f;
    }
}

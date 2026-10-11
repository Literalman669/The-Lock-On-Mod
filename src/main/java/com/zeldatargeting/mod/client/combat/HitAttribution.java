package com.zeldatargeting.mod.client.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Remembers which entities the local player recently swung at, so a health loss
 * shortly afterwards can be counted as the player's own hit. Projectile hits are
 * not attributed.
 */
public final class HitAttribution {
    private final long windowTicks;
    private final Map<Integer, Long> lastAttackTick = new HashMap<>();

    /** @param windowTicks how long after a swing the server's health update may arrive */
    public HitAttribution(long windowTicks) {
        this.windowTicks = windowTicks;
    }

    public void recordAttack(int entityId, long tick) {
        lastAttackTick.put(entityId, tick);
    }

    public boolean isOwnHit(int entityId, long tick) {
        Long attackedAt = lastAttackTick.get(entityId);
        return attackedAt != null && tick >= attackedAt && tick - attackedAt <= windowTicks;
    }

    public void prune(long tick) {
        Iterator<Long> attacks = lastAttackTick.values().iterator();
        while (attacks.hasNext()) {
            if (tick - attacks.next() > windowTicks) {
                attacks.remove();
            }
        }
    }

    public void clear() {
        lastAttackTick.clear();
    }
}

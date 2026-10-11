# Event-Driven Damage Cache

Replace the manual damage prediction math in `DamageCalculator` with a `LivingDamageEvent`-driven cache so all modded armor, weapons, and damage pipelines are automatically accurate.

## Problem
`DamageCalculator.calculateDamage()` manually reimplements MC's damage formula. Mods that bypass standard attribute modifiers or override `applyArmorCalculations` produce wrong numbers with no compat path.

## Solution
Listen to `LivingDamageEvent` (fires after all reductions — armor, enchants, potions, modded resistances — are applied). Cache the final `amount` per entity UUID. Use it as the damage source of truth.

## Files Changed

### New: `DamageEventListener.java`
- `@SubscribeEvent` on `LivingDamageEvent`
- Only cache when the damage source is the local player (`mc.player`)
- Store `Map<UUID, Float> lastDamageDealt` — entity → final damage
- Store `Map<UUID, Long> cacheTimestamp` — evict stale entries after 5 seconds (100 ticks) to avoid showing old data after weapon swap
- Expose `static float getLastDamage(Entity)` and `static boolean hasDamageData(Entity)`

### Modified: `DamageCalculator.java`
- `calculateDamage(Entity)`: check `DamageEventListener.hasDamageData(target)` first → return cached value; fall back to existing manual estimate if no data yet
- `calculateHitsToKill(Entity)`: unchanged — already calls `calculateDamage()` so inherits the fix automatically

### Modified: `ClientProxy.java`
- Register `new DamageEventListener()` on `MinecraftForge.EVENT_BUS`

## Behaviour

| State | Display |
|---|---|
| First lock-on, no hit yet | Manual estimate (existing, prefixed `~`) |
| After first hit | Real post-armor/mod damage, updates every hit |
| Weapon swapped (>5s no hit) | Cache evicted, falls back to estimate |

## What this fixes
- Modded weapons with non-standard damage attributes
- Modded armor that overrides the reduction pipeline
- Any mod effect that hooks into `LivingDamageEvent`
- No compat code needed — the game does all the math, we just read the result

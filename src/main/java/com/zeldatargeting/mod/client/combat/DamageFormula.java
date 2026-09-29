package com.zeldatargeting.mod.client.combat;

/**
 * Minecraft 1.12.2 melee damage formulas, free of game classes so they can be
 * unit tested. Mirrors EntityPlayer#attackTargetEntityWithCurrentItem and
 * CombatRules#getDamageAfterAbsorb.
 */
public final class DamageFormula {
    private DamageFormula() {
    }

    /**
     * Damage of a player melee hit before the target's armor.
     *
     * @param attackDamage   attack damage attribute, including weapon and potion modifiers
     * @param enchantBonus   enchantment bonus against the target's creature type
     * @param cooldown       attack strength from 0.0 (just swung) to 1.0 (fully charged)
     * @param critConditions whether the player's movement allows a critical hit
     */
    public static float meleeDamage(
            float attackDamage,
            float enchantBonus,
            float cooldown,
            boolean critConditions) {
        float strength = clamp(cooldown, 0.0F, 1.0F);
        float damage = attackDamage * (0.2F + strength * strength * 0.8F);
        float bonus = enchantBonus * strength;
        if (critConditions && strength > 0.9F) {
            damage *= 1.5F;
        }
        return Math.max(0.0F, damage + bonus);
    }

    /**
     * Damage left after the target's armor and Resistance effect.
     *
     * @param resistanceLevel Resistance amplifier + 1, or 0 without the effect
     */
    public static float afterArmor(
            float damage,
            float armor,
            float toughness,
            int resistanceLevel) {
        float reduction = clamp(
            armor - damage / (2.0F + toughness / 4.0F),
            armor * 0.2F,
            20.0F
        );
        float result = damage * (1.0F - reduction / 25.0F);
        if (resistanceLevel > 0) {
            result *= Math.max(0, 25 - resistanceLevel * 5) / 25.0F;
        }
        return Math.max(0.0F, result);
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }
}

package com.zeldatargeting.mod.client.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DamageCalculator {
    
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    /**
     * Calculate the damage that would be dealt to the target entity with the current weapon.
     * Uses real post-armor damage from LivingDamageEvent when available; falls back to manual estimate.
     */
    public static float calculateDamage(Entity target) {
        if (DamageEventListener.hasDamageData(target)) {
            return DamageEventListener.getLastDamage(target);
        }

        EntityPlayer player = mc.player;
        if (player == null || !(target instanceof EntityLivingBase)) {
            return 0.0f;
        }
        
        // An empty hand has no modifiers, so the same vanilla formula covers it
        return calculateWeaponDamage(player, (EntityLivingBase) target, player.getHeldItemMainhand());
    }

    /**
     * Returns true if the damage value is a real observed hit (not a manual estimate).
     */
    public static boolean isRealDamageData(Entity target) {
        return DamageEventListener.hasDamageData(target);
    }
    
    /**
     * Calculate how many hits it would take to kill the target
     */
    public static int calculateHitsToKill(Entity target) {
        if (!(target instanceof EntityLivingBase)) {
            return -1; // Unknown for non-living entities
        }
        
        EntityLivingBase living = (EntityLivingBase) target;
        float damage = calculateDamage(target);
        
        if (damage <= 0) {
            return -1; // Can't kill with 0 damage
        }
        
        float targetHealth = living.getHealth();
        return (int) Math.ceil(targetHealth / damage);
    }
    
    /**
     * Get damage prediction text for display
     */
    public static String getDamagePredictionText(Entity target) {
        if (!(target instanceof EntityLivingBase)) {
            return "";
        }
        
        float damage = calculateDamage(target);
        if (damage <= 0) {
            return "No damage";
        }
        
        float targetHealth = ((EntityLivingBase) target).getHealth();
        int hitsToKill = (int) Math.ceil(targetHealth / damage);
        
        String prefix = isRealDamageData(target) ? "" : "~";
        if (hitsToKill == 1) {
            return String.format("%s%.1f dmg (LETHAL)", prefix, damage);
        } else if (hitsToKill <= 99) {
            return String.format("%s%.1f dmg (%d hits)", prefix, damage, hitsToKill);
        } else {
            return String.format("%s%.1f dmg", prefix, damage);
        }
    }
    
    /**
     * Get color for damage prediction based on lethality
     */
    public static int getDamagePredictionColor(Entity target) {
        int hitsToKill = calculateHitsToKill(target);
        
        if (hitsToKill == 1) {
            return 0xFFFF4444; // Bright red for lethal
        } else if (hitsToKill <= 3) {
            return 0xFFFFAA44; // Orange for few hits
        } else if (hitsToKill <= 6) {
            return 0xFFFFFF44; // Yellow for moderate
        } else {
            return 0xFFAAAAAA; // Gray for many hits
        }
    }
    
    private static float calculateWeaponDamage(EntityPlayer player, EntityLivingBase target, ItemStack weapon) {
        // Player base attack damage (1.0) plus the weapon's additive modifiers
        float attackDamage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
        for (AttributeModifier modifier : weapon.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
            if (modifier.getOperation() == 0) { // Addition operation
                attackDamage += modifier.getAmount();
            }
        }
        
        // Strength and Weakness are additive attack damage modifiers in 1.12.2
        PotionEffect strength = player.getActivePotionEffect(MobEffects.STRENGTH);
        if (strength != null) {
            attackDamage += (strength.getAmplifier() + 1) * 3.0f;
        }
        PotionEffect weakness = player.getActivePotionEffect(MobEffects.WEAKNESS);
        if (weakness != null) {
            attackDamage -= (weakness.getAmplifier() + 1) * 4.0f;
        }
        
        float damage = DamageFormula.meleeDamage(
            attackDamage,
            EnchantmentHelper.getModifierForCreature(weapon, target.getCreatureAttribute()),
            player.getCooledAttackStrength(0.5f),
            canCriticalHit(player)
        );
        return applyArmorReduction(damage, target);
    }
    
    private static boolean canCriticalHit(EntityPlayer player) {
        // Real critical hit conditions for MC 1.12.2
        return player.fallDistance > 0.0f && !player.onGround && !player.isOnLadder() &&
               !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) &&
               !player.isRiding() && !player.isSprinting();
    }
    
    /**
     * Apply armor damage reduction based on target's armor, toughness, and Resistance
     */
    private static float applyArmorReduction(float damage, EntityLivingBase target) {
        IAttributeInstance toughness = target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        PotionEffect resistance = target.getActivePotionEffect(MobEffects.RESISTANCE);
        return DamageFormula.afterArmor(
            damage,
            target.getTotalArmorValue(),
            toughness == null ? 0.0f : (float) toughness.getAttributeValue(),
            resistance == null ? 0 : resistance.getAmplifier() + 1
        );
    }
    
    /**
     * Check if the target has specific vulnerabilities
     */
    public static boolean hasWeakness(EntityLivingBase target) {
        return target.isPotionActive(MobEffects.WEAKNESS);
    }
    
    /**
     * Check if the target has damage resistance
     */
    public static boolean hasResistance(EntityLivingBase target) {
        return target.isPotionActive(MobEffects.RESISTANCE);
    }
    
    /**
     * Get vulnerability indicator text
     */
    public static String getVulnerabilityText(EntityLivingBase target) {
        if (hasWeakness(target)) {
            return "WEAK";
        } else if (hasResistance(target)) {
            return "RESIST";
        }
        return "";
    }
    
    /**
     * Get vulnerability indicator color
     */
    public static int getVulnerabilityColor(EntityLivingBase target) {
        if (hasWeakness(target)) {
            return 0xFF44FF44; // Green for weakness
        } else if (hasResistance(target)) {
            return 0xFF4444FF; // Blue for resistance
        }
        return 0xFFFFFFFF; // White default
    }
}
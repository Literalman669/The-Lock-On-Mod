package com.zeldatargeting.mod.client.combat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DamageFormulaTest {
    private static final float EPSILON = 0.0001F;

    @Test
    public void fullyChargedHitDealsFullAttackDamage() {
        assertEquals(7.0F, DamageFormula.meleeDamage(7.0F, 0.0F, 1.0F, false), EPSILON);
    }

    @Test
    public void unchargedHitDealsOneFifthOfAttackDamage() {
        assertEquals(1.4F, DamageFormula.meleeDamage(7.0F, 0.0F, 0.0F, false), EPSILON);
    }

    @Test
    public void halfChargedHitUsesVanillaCurve() {
        // 7 * (0.2 + 0.25 * 0.8) = 2.8
        assertEquals(2.8F, DamageFormula.meleeDamage(7.0F, 0.0F, 0.5F, false), EPSILON);
    }

    @Test
    public void enchantBonusScalesLinearlyWithChargeAndSkipsCritMultiplier() {
        // Sharpness V on a diamond sword: 7 * 1.5 + 3.0 = 13.5
        assertEquals(13.5F, DamageFormula.meleeDamage(7.0F, 3.0F, 1.0F, true), EPSILON);
        // 7 * 0.4 + 3.0 * 0.5 = 4.3
        assertEquals(4.3F, DamageFormula.meleeDamage(7.0F, 3.0F, 0.5F, false), EPSILON);
    }

    @Test
    public void critNeedsNearlyFullCharge() {
        assertEquals(10.5F, DamageFormula.meleeDamage(7.0F, 0.0F, 1.0F, true), EPSILON);
        assertEquals(2.8F, DamageFormula.meleeDamage(7.0F, 0.0F, 0.5F, true), EPSILON);
    }

    @Test
    public void negativeAttackDamageIsClampedToZero() {
        assertEquals(0.0F, DamageFormula.meleeDamage(-3.0F, 0.0F, 1.0F, false), EPSILON);
    }

    @Test
    public void unarmoredTargetTakesFullDamage() {
        assertEquals(7.0F, DamageFormula.afterArmor(7.0F, 0.0F, 0.0F, 0), EPSILON);
    }

    @Test
    public void armorReductionMatchesCombatRules() {
        // Full diamond: 20 armor, 8 toughness. 20 - 10 / 4 = 17.5 -> 10 * (1 - 17.5 / 25) = 3.0
        assertEquals(3.0F, DamageFormula.afterArmor(10.0F, 20.0F, 8.0F, 0), EPSILON);
        // Floor of armor / 5: 5 armor vs a 100 damage hit keeps 1 point -> 96
        assertEquals(96.0F, DamageFormula.afterArmor(100.0F, 5.0F, 0.0F, 0), EPSILON);
    }

    @Test
    public void resistanceRemovesTwentyPercentPerLevel() {
        assertEquals(8.0F, DamageFormula.afterArmor(10.0F, 0.0F, 0.0F, 1), EPSILON);
        assertEquals(0.0F, DamageFormula.afterArmor(10.0F, 0.0F, 0.0F, 5), EPSILON);
        assertEquals(0.0F, DamageFormula.afterArmor(10.0F, 0.0F, 0.0F, 7), EPSILON);
    }
}

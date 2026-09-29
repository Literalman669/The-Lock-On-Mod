package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityBlacklistTest {
    private static final String GOLEM_CLASS = "net.minecraft.entity.monster.EntityIronGolem";
    private static final String DRAGON_CLASS = "com.github.alexthe666.iceandfire.entity.EntityFireDragon";

    @Test
    public void emptyOrBlankConfigMatchesNothing() {
        assertTrue(EntityBlacklist.parse(null).isEmpty());
        assertTrue(EntityBlacklist.parse("  , ;  ").isEmpty());
        assertFalse(EntityBlacklist.parse("").matches("minecraft:iron_golem", GOLEM_CLASS));
    }

    @Test
    public void fullRegistryNameMatchesExactlyIgnoringCase() {
        EntityBlacklist blacklist = EntityBlacklist.parse("Minecraft:Iron_Golem");

        assertTrue(blacklist.matches("minecraft:iron_golem", GOLEM_CLASS));
        assertFalse(blacklist.matches("minecraft:snowman", "net.minecraft.entity.monster.EntitySnowman"));
    }

    @Test
    public void modPrefixMatchesEveryEntityFromThatMod() {
        EntityBlacklist blacklist = EntityBlacklist.parse("iceandfire:");

        assertTrue(blacklist.matches("iceandfire:firedragon", DRAGON_CLASS));
        assertFalse(blacklist.matches("minecraft:iron_golem", GOLEM_CLASS));
    }

    @Test
    public void bareNameMatchesRegistryPathOrClassNameFragment() {
        assertTrue(EntityBlacklist.parse("iron_golem").matches("minecraft:iron_golem", GOLEM_CLASS));
        assertTrue(EntityBlacklist.parse("golem").matches("minecraft:iron_golem", GOLEM_CLASS));
        assertTrue(EntityBlacklist.parse("dragon").matches(null, DRAGON_CLASS));
        assertFalse(EntityBlacklist.parse("zombie").matches("minecraft:iron_golem", GOLEM_CLASS));
    }

    @Test
    public void registryEntriesDoNotFallBackToClassNames() {
        // "minecraft:golem" is not a real registry name, so it must not match by class fragment.
        assertFalse(EntityBlacklist.parse("minecraft:golem").matches("minecraft:iron_golem", GOLEM_CLASS));
        assertFalse(EntityBlacklist.parse("iceandfire:").matches(null, DRAGON_CLASS));
    }

    @Test
    public void listsAcceptCommasSemicolonsAndWhitespace() {
        EntityBlacklist blacklist = EntityBlacklist.parse("minecraft:zombie; minecraft:iron_golem\ticeandfire:");

        assertTrue(blacklist.matches("minecraft:zombie", "net.minecraft.entity.monster.EntityZombie"));
        assertTrue(blacklist.matches("minecraft:iron_golem", GOLEM_CLASS));
        assertTrue(blacklist.matches("iceandfire:firedragon", DRAGON_CLASS));
    }
}

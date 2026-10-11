package com.zeldatargeting.mod.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.zeldatargeting.mod.client.gui.ConfigEntry.choice;
import static com.zeldatargeting.mod.client.gui.ConfigEntry.color;
import static com.zeldatargeting.mod.client.gui.ConfigEntry.preset;
import static com.zeldatargeting.mod.client.gui.ConfigEntry.slider;
import static com.zeldatargeting.mod.client.gui.ConfigEntry.text;
import static com.zeldatargeting.mod.client.gui.ConfigEntry.toggle;

/**
 * What each configuration page shows, in order: section titles and settings with
 * their player-facing labels, controls, units, help text, and dependencies.
 */
public final class ConfigLayout {
    /** Settings deliberately absent from the screen: internal, set by presets, or file-only. */
    public static final Set<String> EXCLUDED_FIELDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "schemaVersion", "lockOnPreset", "damageNumbersColor", "criticalDamageColor", "lethalDamageColor"
    )));

    private static final String VOLUME_HELP = "How loud this sound is, on top of Volume.";
    private static final String PITCH_HELP = "How high this sound plays.";

    /** One row of a page: a section title or a setting. */
    public static final class Item {
        private final String sectionTitle;
        private final ConfigEntry entry;

        private Item(String sectionTitle, ConfigEntry entry) {
            this.sectionTitle = sectionTitle;
            this.entry = entry;
        }

        public boolean isSection() {
            return sectionTitle != null;
        }

        public String getSectionTitle() {
            return sectionTitle;
        }

        public ConfigEntry getEntry() {
            return entry;
        }
    }

    private static final Map<ConfigPage, List<Item>> PAGES = new EnumMap<>(ConfigPage.class);

    static {
        page(ConfigPage.PRESETS,
            preset("cinematic", "Cinematic",
                "Slow, smooth camera that switches to third person when you lock on. Good for exploring and big fights."),
            preset("balanced", "Balanced",
                "The default. A steady camera that keeps up with most fights."),
            preset("snappy", "Snappy",
                "A fast camera that stays in your current view. Good for quick, close combat."));

        page(ConfigPage.TARGETING,
            slider("targetingRange", "Lock-On Range", 1, 64, 1, ConfigUnit.BLOCKS,
                "How far away a mob can be when you press lock.")
                .requiresOff("syncTargetingRangeWithReach"),
            slider("maxTrackingDistance", "Keep Lock Up To", 1, 128, 1, ConfigUnit.BLOCKS,
                "Once locked, the lock breaks if the target gets further away than this."),
            slider("maxAngle", "Search Angle", 15, 180, 5, ConfigUnit.DEGREES,
                "How far from the center of your view a mob can be and still be picked."),
            toggle("requireLineOfSight", "Need Clear View",
                "Only lock onto mobs you can actually see, not ones behind walls."),
            choice("targetPriority", "Pick Target By",
                new String[] {"nearest", "angle", "health", "threat"},
                new String[] {"Nearest", "Most Centered", "Lowest Health", "Biggest Threat"},
                "Which mob gets picked first when several are in range."),
            toggle("syncTargetingRangeWithReach", "Match Range to Reach",
                "Use your melee reach as the lock-on range instead of Lock-On Range."),
            toggle("disableLockOnWhenRiding", "Turn Off While Riding",
                "Stop lock-on while you ride a horse, boat, or other mount."),
            toggle("quickSwitchOnKill", "Switch Target on Kill",
                "When your target dies, lock onto the next one in range instead of letting go."),
            "What Can Be Targeted",
            toggle("targetHostileMobs", "Hostile Mobs",
                "Zombies, skeletons, creepers, and other monsters."),
            toggle("targetNeutralMobs", "Neutral Mobs",
                "Mobs like wolves, endermen, and golems that are neither monsters nor farm animals."),
            toggle("targetPassiveMobs", "Animals",
                "Pigs, cows, sheep, and other passive animals."),
            toggle("targetPlayers", "Players",
                "Other players. Usually off outside of PvP."),
            text("entityBlacklist", "Never Target",
                "Mobs that are never picked. Click to edit the list.",
                "Entities that are never targeted, separated by commas.",
                "minecraft:iron_golem = one mob, iceandfire: = a whole mod, golem = any name containing it"));

        page(ConfigPage.CAMERA,
            toggle("enableCameraLockOn", "Camera Follows Target",
                "Turn your view toward the target while locked. Off keeps the camera fully under your mouse."),
            slider("cameraSmoothness", "Smoothness", 0.01, 1.0, 0.05, ConfigUnit.PERCENT,
                "Higher is slower and softer; lower snaps to the target faster.")
                .requires("enableCameraLockOn"),
            toggle("autoThirdPerson", "Third Person on Lock",
                "Switch to third person when you lock on, and back when you let go.")
                .requires("enableCameraLockOn"),
            toggle("recenterWithoutTarget", "Level View Without Target",
                "When you press lock and nothing is in range, ease your view back to the horizon."));

        page(ConfigPage.HUD,
            "Target Panel",
            toggle("showTargetName", "Show Name", "Show the target's name on the panel."),
            toggle("showHealthBar", "Show Health Bar", "Show the target's health bar on the panel."),
            toggle("showDistance", "Show Distance", "Show how far away the target is."),
            toggle("compactHudMode", "Compact Panel", "A smaller panel with just the name and health."),
            toggle("bossStylePanel", "Boss Banner",
                "Show a wide banner at the top of the screen for bosses and mobs with 100 or more health."),
            choice("hudAnchor", "Panel Position",
                new String[] {"top-left", "top-right", "bottom-left", "bottom-right", "center"},
                new String[] {"Top Left", "Top Right", "Bottom Left", "Bottom Right", "Center"},
                "Which corner of the screen the panel sits in."),
            slider("hudOffsetX", "Nudge Sideways", -500, 500, 5, ConfigUnit.PIXELS,
                "Move the panel left or right from its corner."),
            slider("hudOffsetY", "Nudge Up/Down", -500, 500, 5, ConfigUnit.PIXELS,
                "Move the panel up or down from its corner."),
            slider("hudScale", "Panel Size", 0.75, 1.5, 0.05, ConfigUnit.PERCENT,
                "How large the panel is drawn."),
            slider("hudOpacity", "Panel Opacity", 0.35, 1.0, 0.05, ConfigUnit.PERCENT,
                "How see-through the panel is."),
            "Markers on Your Target",
            toggle("showReticle", "Show Target Markers",
                "The ring, corner brackets, and arrow drawn on the mob you are locked onto."),
            toggle("ringEnabled", "Glowing Ring", "The ring around your target's feet.")
                .requires("showReticle"),
            toggle("ringHealthArcEnabled", "Health Arc on Ring", "The ring shrinks as the target loses health.")
                .requires("showReticle").requires("ringEnabled"),
            slider("ringThickness", "Ring Thickness", 0.5, 2.5, 0.1, ConfigUnit.MULTIPLIER,
                "How thick the ring is.")
                .requires("showReticle").requires("ringEnabled"),
            slider("ringGlowStrength", "Ring Glow", 0.0, 1.5, 0.1, ConfigUnit.PERCENT,
                "How strongly the ring glows.")
                .requires("showReticle").requires("ringEnabled"),
            color("reticleColor", "Bracket Color", "Color of the corner brackets around your target.")
                .requires("showReticle"),
            toggle("softAimIndicator", "Aim Hint",
                "A small marker beside your crosshair that leans toward your target when your view drifts off it."),
            toggle("targetHistoryEnabled", "Recent Target Markers",
                "Small markers on the last few mobs you were locked onto."));

        page(ConfigPage.COMBAT,
            "On the Target Panel",
            toggle("showDamagePrediction", "Show Your Damage", "Show how much damage your next hit will do."),
            toggle("showHitsToKill", "Show Hits to Kill", "Show how many hits it will take to defeat the target."),
            toggle("showVulnerabilities", "Show Weak/Resist", "Tag targets that take extra or reduced damage."),
            toggle("highlightLethalTargets", "One-Hit Warning",
                "Highlight the target when your next hit will defeat it."),
            "Damage Numbers",
            toggle("enableDamageNumbers", "Damage Numbers",
                "Numbers that float up from mobs when they take damage."),
            toggle("damageNumbersOwnHitsOnly", "Only My Hits", "Only show numbers for your own melee hits.")
                .requires("enableDamageNumbers"),
            slider("damageNumbersScale", "Number Size", 0.5, 3.0, 0.1, ConfigUnit.MULTIPLIER,
                "How large the numbers are.")
                .requires("enableDamageNumbers"),
            slider("damageNumbersDuration", "Number Duration", 20, 200, 10, ConfigUnit.TICKS_AS_SECONDS,
                "How long each number stays on screen.")
                .requires("enableDamageNumbers"),
            slider("damageNumbersOffset", "Number Height", 0.0, 2.0, 0.1, ConfigUnit.BLOCKS,
                "How far above the mob the numbers start.")
                .requires("enableDamageNumbers"),
            choice("damageNumbersMotion", "Number Motion",
                new String[] {"default", "subtle", "arcade"},
                new String[] {"Normal", "Subtle", "Arcade"},
                "How much the numbers move as they float up.")
                .requires("enableDamageNumbers"),
            toggle("damageNumbersColors", "Colored Numbers", "Color critical and finishing hits differently.")
                .requires("enableDamageNumbers"),
            toggle("damageNumbersCrits", "Critical Hit Style",
                "Give critical-hit numbers their own color and animation.")
                .requires("enableDamageNumbers"),
            toggle("critEmphasis", "Critical Pop", "Critical-hit numbers pop in bigger when they appear.")
                .requires("enableDamageNumbers").requires("damageNumbersCrits"),
            toggle("damageNumbersFadeOut", "Fade Out", "Numbers fade away instead of disappearing at once.")
                .requires("enableDamageNumbers"));

        page(ConfigPage.SOUND,
            toggle("enableSounds", "Sounds", "All lock-on sounds."),
            slider("soundVolume", "Volume", 0.0, 1.0, 0.1, ConfigUnit.PERCENT,
                "Overall volume of lock-on sounds.")
                .requires("enableSounds"),
            choice("soundTheme", "Sound Style",
                new String[] {"default", "zelda", "modern", "subtle", "cinematic"},
                new String[] {"Default", "Chime", "Modern", "Subtle", "Cinematic"},
                "The set of sounds used.")
                .requires("enableSounds"),
            toggle("enableSoundVariety", "Vary Sounds",
                "Rotate between a few sounds instead of repeating the same one.")
                .requires("enableSounds"),
            "Events",
            toggle("enableTargetLockSound", "Lock-On Sound", "Plays when you lock onto a target.")
                .requires("enableSounds"),
            slider("targetLockVolume", "Lock-On Volume", 0.0, 1.0, 0.1, ConfigUnit.PERCENT, VOLUME_HELP)
                .requires("enableSounds").requires("enableTargetLockSound"),
            toggle("enableTargetSwitchSound", "Switch Sound", "Plays when you switch to another target.")
                .requires("enableSounds"),
            slider("targetSwitchVolume", "Switch Volume", 0.0, 1.0, 0.1, ConfigUnit.PERCENT, VOLUME_HELP)
                .requires("enableSounds").requires("enableTargetSwitchSound"),
            toggle("enableLethalTargetSound", "One-Hit Sound",
                "Plays instead of the lock-on sound when one hit will defeat the target.")
                .requires("enableSounds"),
            slider("lethalTargetVolume", "One-Hit Volume", 0.0, 1.0, 0.1, ConfigUnit.PERCENT, VOLUME_HELP)
                .requires("enableSounds").requires("enableLethalTargetSound"),
            toggle("enableTargetLostSound", "Lock Lost Sound", "Plays when the lock ends.")
                .requires("enableSounds"),
            slider("targetLostVolume", "Lock Lost Volume", 0.0, 1.0, 0.1, ConfigUnit.PERCENT, VOLUME_HELP)
                .requires("enableSounds").requires("enableTargetLostSound"));

        page(ConfigPage.ACCESSIBILITY,
            choice("presentationPalette", "Color Palette",
                new String[] {"default", "deuteranopia", "protanopia", "tritanopia"},
                new String[] {"Default", "Red-Green (Deuteranopia)", "Red-Green (Protanopia)", "Blue-Yellow (Tritanopia)"},
                "Health colors on the ring and panel, adjusted for color vision."),
            toggle("reducedMotion", "Reduced Motion",
                "Replace pulsing, popping, and shockwave effects with simple color flashes."));

        page(ConfigPage.ADVANCED,
            "Camera Tuning",
            slider("maxYawAdjustment", "Max Turn Sideways", 0, 180, 5, ConfigUnit.DEGREES,
                "The furthest the camera turns left or right toward a target in one go."),
            slider("maxPitchAdjustment", "Max Turn Up/Down", 0, 90, 5, ConfigUnit.DEGREES,
                "The furthest the camera tilts up or down toward a target."),
            slider("cameraFocusYOffset", "Aim Height", -1.0, 1.0, 0.05, ConfigUnit.SIGNED,
                "Aim higher (positive) or lower (negative) on the target's body."),
            toggle("perModeSmoothingEnabled", "Softer in First Person",
                "Use a slower camera in first person than in third person."),
            "Display Tuning",
            slider("reticleScale", "Marker Size", 0.5, 3.0, 0.1, ConfigUnit.MULTIPLIER,
                "How large the markers around your target are drawn."),
            slider("damagePredictionScale", "Readout Text Size", 0.5, 2.0, 0.1, ConfigUnit.MULTIPLIER,
                "Text size of the damage and hits-to-kill readouts."),
            "Sound Pitch",
            slider("targetLockPitch", "Lock-On Pitch", 0.5, 2.0, 0.1, ConfigUnit.MULTIPLIER, PITCH_HELP)
                .requires("enableSounds").requires("enableTargetLockSound"),
            slider("targetSwitchPitch", "Switch Pitch", 0.5, 2.0, 0.1, ConfigUnit.MULTIPLIER, PITCH_HELP)
                .requires("enableSounds").requires("enableTargetSwitchSound"),
            slider("lethalTargetPitch", "One-Hit Pitch", 0.5, 2.0, 0.1, ConfigUnit.MULTIPLIER, PITCH_HELP)
                .requires("enableSounds").requires("enableLethalTargetSound"),
            slider("targetLostPitch", "Lock Lost Pitch", 0.5, 2.0, 0.1, ConfigUnit.MULTIPLIER, PITCH_HELP)
                .requires("enableSounds").requires("enableTargetLostSound"),
            "Performance",
            slider("updateFrequency", "Target Check Rate", 1, 20, 1, ConfigUnit.TICK_INTERVAL,
                "How often the lock re-checks range and line of sight. Higher saves work but reacts slower."),
            slider("validationInterval", "Filter Check Rate", 5, 60, 5, ConfigUnit.TICK_INTERVAL,
                "How often the lock re-checks whether the target still passes your filters."),
            "Compatibility",
            toggle("ssrCompensationEnabled", "Shoulder Surfing / Epic Fight Aim",
                "Line up your attacks with Shoulder Surfing Reloaded's camera and Epic Fight's combat."
                    + " Leave on unless those mods misbehave."),
            toggle("debugCompatibility", "Debug Logging",
                "Write detailed aim and camera information to the game log, for bug reports."));
    }

    private ConfigLayout() {
    }

    /** The rows of {@code page}, top to bottom. */
    public static List<Item> page(ConfigPage page) {
        List<Item> items = PAGES.get(page);
        return items == null ? Collections.<Item>emptyList() : items;
    }

    /** The on-screen label of the setting stored in {@code fieldName}, or null if it is not shown. */
    public static String labelOf(String fieldName) {
        for (List<Item> items : PAGES.values()) {
            for (Item item : items) {
                if (!item.isSection() && fieldName.equals(item.getEntry().getFieldName())) {
                    return item.getEntry().getLabel();
                }
            }
        }
        return null;
    }

    /** Rows are {@link ConfigEntry} settings, or Strings for section titles. */
    private static void page(ConfigPage page, Object... rows) {
        List<Item> items = new ArrayList<>(rows.length);
        for (Object row : rows) {
            items.add(row instanceof String
                ? new Item((String) row, null)
                : new Item(null, (ConfigEntry) row));
        }
        PAGES.put(page, Collections.unmodifiableList(items));
    }
}

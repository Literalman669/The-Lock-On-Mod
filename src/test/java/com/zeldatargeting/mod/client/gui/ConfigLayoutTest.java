package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingSettings;
import com.zeldatargeting.mod.config.TargetingSettingsValidator;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigLayoutTest {
    @Test
    public void everySettingIsOnExactlyOnePage() {
        Map<String, Integer> counts = new HashMap<>();
        for (ConfigEntry entry : allEntries()) {
            if (entry.getKind() != ConfigEntry.Kind.PRESET) {
                assertNotNull("not a setting: " + entry.getFieldName(), field(entry.getFieldName()));
                counts.merge(entry.getFieldName(), 1, Integer::sum);
            }
        }
        for (Field field : TargetingSettings.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    || ConfigLayout.EXCLUDED_FIELDS.contains(field.getName())) {
                continue;
            }
            assertEquals(field.getName() + " must be on exactly one page",
                Integer.valueOf(1), counts.get(field.getName()));
        }
    }

    @Test
    public void entryKindsMatchFieldTypes() {
        for (ConfigEntry entry : allEntries()) {
            if (entry.getKind() == ConfigEntry.Kind.PRESET) {
                continue;
            }
            Class<?> type = field(entry.getFieldName()).getType();
            switch (entry.getKind()) {
                case TOGGLE:
                    assertEquals(entry.getFieldName(), boolean.class, type);
                    break;
                case SLIDER:
                    assertTrue(entry.getFieldName(),
                        type == double.class || type == float.class || type == int.class);
                    break;
                case CHOICE:
                case TEXT:
                    assertEquals(entry.getFieldName(), String.class, type);
                    break;
                case COLOR:
                    assertEquals(entry.getFieldName(), int.class, type);
                    break;
                default:
                    fail("unexpected kind " + entry.getKind());
            }
        }
    }

    @Test
    public void slidersStayInsideValidatorRanges() throws Exception {
        for (ConfigEntry entry : allEntries()) {
            if (entry.getKind() != ConfigEntry.Kind.SLIDER) {
                continue;
            }
            for (double bound : new double[] {entry.getMin(), entry.getMax()}) {
                Field field = field(entry.getFieldName());
                TargetingSettings settings = new TargetingSettings();
                setNumber(field, settings, bound);
                TargetingSettings sanitized = TargetingSettingsValidator.sanitize(settings);
                assertEquals(entry.getFieldName() + " at " + bound,
                    ((Number) field.get(settings)).doubleValue(),
                    ((Number) field.get(sanitized)).doubleValue(), 1.0E-6);
            }
        }
    }

    @Test
    public void choicesMatchStoredValues() {
        Map<String, String[]> expected = new HashMap<>();
        expected.put("targetPriority", new String[] {"nearest", "angle", "health", "threat"});
        expected.put("hudAnchor", new String[] {"top-left", "top-right", "bottom-left", "bottom-right", "center"});
        expected.put("soundTheme", new String[] {"default", "zelda", "modern", "subtle", "cinematic"});
        expected.put("damageNumbersMotion", new String[] {"default", "subtle", "arcade"});
        expected.put("presentationPalette", new String[] {"default", "deuteranopia", "protanopia", "tritanopia"});
        int choices = 0;
        for (ConfigEntry entry : allEntries()) {
            if (entry.getKind() == ConfigEntry.Kind.CHOICE) {
                choices++;
                assertArrayEquals(entry.getFieldName(), expected.get(entry.getFieldName()), entry.getChoiceValues());
                assertEquals(entry.getChoiceValues().length, entry.getChoiceLabels().length);
            }
        }
        assertEquals(expected.size(), choices);
    }

    @Test
    public void dependenciesNameBooleanFields() {
        List<String> onPage = new ArrayList<>();
        for (ConfigEntry entry : allEntries()) {
            onPage.add(entry.getFieldName());
        }
        for (ConfigEntry entry : allEntries()) {
            for (ConfigEntry.Dependency dependency : entry.getDependencies()) {
                Field field = field(dependency.getFieldName());
                assertNotNull(dependency.getFieldName(), field);
                assertEquals(boolean.class, field.getType());
                assertTrue(onPage.contains(dependency.getFieldName()));
            }
        }
    }

    @Test
    public void spotChecksFromTheSpec() {
        ConfigEntry range = entry(ConfigPage.TARGETING, "targetingRange");
        assertEquals("Lock-On Range", range.getLabel());
        assertEquals(ConfigUnit.BLOCKS, range.getUnit());
        assertEquals(1.0, range.getMin(), 0.0);
        assertEquals(64.0, range.getMax(), 0.0);
        assertEquals(1.0, range.getStep(), 0.0);
        assertEquals(1, range.getDependencies().size());
        assertEquals("syncTargetingRangeWithReach", range.getDependencies().get(0).getFieldName());
        assertFalse(range.getDependencies().get(0).isRequiredValue());

        assertEquals("Chime", entry(ConfigPage.SOUND, "soundTheme").choiceLabel("zelda"));
        assertEquals("Aim Hint", entry(ConfigPage.HUD, "softAimIndicator").getLabel());
        assertEquals("Debug Logging", entry(ConfigPage.ADVANCED, "debugCompatibility").getLabel());

        List<ConfigEntry.Dependency> crit = entry(ConfigPage.COMBAT, "critEmphasis").getDependencies();
        assertEquals(2, crit.size());
        assertEquals("enableDamageNumbers", crit.get(0).getFieldName());
        assertEquals("damageNumbersCrits", crit.get(1).getFieldName());
        assertTrue(crit.get(0).isRequiredValue() && crit.get(1).isRequiredValue());

        List<String> presets = new ArrayList<>();
        for (ConfigLayout.Item item : ConfigLayout.page(ConfigPage.PRESETS)) {
            if (!item.isSection()) {
                assertEquals(ConfigEntry.Kind.PRESET, item.getEntry().getKind());
                presets.add(item.getEntry().getPresetId());
            }
        }
        assertEquals(Arrays.asList("cinematic", "balanced", "snappy"), presets);
    }

    @Test
    public void presetsPageEndsWithThePreviewNote() {
        List<ConfigLayout.Item> items = ConfigLayout.page(ConfigPage.PRESETS);
        ConfigLayout.Item last = items.get(items.size() - 1);

        assertTrue(last.isSection());
        assertEquals("Picking a preset previews it. Save keeps it.", last.getSectionTitle());
    }

    @Test
    public void unknownChoiceValueShowsRawValue() {
        assertEquals("legacy-theme", entry(ConfigPage.SOUND, "soundTheme").choiceLabel("legacy-theme"));
    }

    @Test
    public void labelOfFindsTheSettingLabel() {
        assertEquals("Show Target Markers", ConfigLayout.labelOf("showReticle"));
        assertNull(ConfigLayout.labelOf("noSuchSetting"));
    }

    private static List<ConfigEntry> allEntries() {
        List<ConfigEntry> entries = new ArrayList<>();
        for (ConfigPage page : ConfigPage.values()) {
            for (ConfigLayout.Item item : ConfigLayout.page(page)) {
                if (!item.isSection()) {
                    entries.add(item.getEntry());
                }
            }
        }
        return entries;
    }

    private static ConfigEntry entry(ConfigPage page, String fieldName) {
        for (ConfigLayout.Item item : ConfigLayout.page(page)) {
            if (!item.isSection() && fieldName.equals(item.getEntry().getFieldName())) {
                return item.getEntry();
            }
        }
        throw new AssertionError(fieldName + " is not on " + page);
    }

    private static Field field(String name) {
        try {
            return TargetingSettings.class.getField(name);
        } catch (NoSuchFieldException missing) {
            return null;
        }
    }

    private static void setNumber(Field field, TargetingSettings settings, double value) throws Exception {
        if (field.getType() == double.class) {
            field.setDouble(settings, value);
        } else if (field.getType() == float.class) {
            field.setFloat(settings, (float) value);
        } else {
            field.setInt(settings, (int) Math.round(value));
        }
    }
}

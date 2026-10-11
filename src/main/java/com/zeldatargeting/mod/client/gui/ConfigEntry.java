package com.zeldatargeting.mod.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One setting on the configuration screen: which {@code TargetingSettings} field it
 * edits, how it is shown and changed, its help text, and which on/off settings must
 * be in a given state for it to apply. Immutable; {@link #requires} and
 * {@link #requiresOff} return copies.
 */
public final class ConfigEntry {
    public enum Kind {
        PRESET,
        TOGGLE,
        SLIDER,
        CHOICE,
        COLOR,
        TEXT
    }

    /** An on/off setting that must hold {@link #isRequiredValue()} for the entry to apply. */
    public static final class Dependency {
        private final String fieldName;
        private final boolean requiredValue;

        private Dependency(String fieldName, boolean requiredValue) {
            this.fieldName = fieldName;
            this.requiredValue = requiredValue;
        }

        public String getFieldName() {
            return fieldName;
        }

        public boolean isRequiredValue() {
            return requiredValue;
        }
    }

    private static final String[] NONE = new String[0];

    private final Kind kind;
    private final String fieldName;
    private final String presetId;
    private final String label;
    private final String help;
    private final double min;
    private final double max;
    private final double step;
    private final ConfigUnit unit;
    private final String[] choiceValues;
    private final String[] choiceLabels;
    private final String[] textHints;
    private final List<Dependency> dependencies;

    private ConfigEntry(Kind kind, String fieldName, String presetId, String label, String help,
                        double min, double max, double step, ConfigUnit unit,
                        String[] choiceValues, String[] choiceLabels, String[] textHints,
                        List<Dependency> dependencies) {
        this.kind = kind;
        this.fieldName = fieldName;
        this.presetId = presetId;
        this.label = label;
        this.help = help;
        this.min = min;
        this.max = max;
        this.step = step;
        this.unit = unit;
        this.choiceValues = choiceValues;
        this.choiceLabels = choiceLabels;
        this.textHints = textHints;
        this.dependencies = dependencies;
    }

    public static ConfigEntry preset(String presetId, String label, String help) {
        return new ConfigEntry(Kind.PRESET, null, presetId, label, help, 0, 0, 0, ConfigUnit.NONE,
            NONE, NONE, NONE, Collections.<Dependency>emptyList());
    }

    public static ConfigEntry toggle(String fieldName, String label, String help) {
        return simple(Kind.TOGGLE, fieldName, label, help);
    }

    public static ConfigEntry slider(String fieldName, String label, double min, double max, double step,
                                     ConfigUnit unit, String help) {
        return new ConfigEntry(Kind.SLIDER, fieldName, null, label, help, min, max, step, unit,
            NONE, NONE, NONE, Collections.<Dependency>emptyList());
    }

    /** {@code values} are what the settings file stores; {@code labels} are shown, in the same order. */
    public static ConfigEntry choice(String fieldName, String label, String[] values, String[] labels, String help) {
        if (values.length != labels.length) {
            throw new IllegalArgumentException("each choice value needs a label: " + fieldName);
        }
        return new ConfigEntry(Kind.CHOICE, fieldName, null, label, help, 0, 0, 0, ConfigUnit.NONE,
            values.clone(), labels.clone(), NONE, Collections.<Dependency>emptyList());
    }

    public static ConfigEntry color(String fieldName, String label, String help) {
        return simple(Kind.COLOR, fieldName, label, help);
    }

    /** Free text edited on its own screen; {@code hints} are shown above the text box. */
    public static ConfigEntry text(String fieldName, String label, String help, String... hints) {
        return new ConfigEntry(Kind.TEXT, fieldName, null, label, help, 0, 0, 0, ConfigUnit.NONE,
            NONE, NONE, hints.clone(), Collections.<Dependency>emptyList());
    }

    /** A copy that applies only while the on/off setting {@code dependencyField} is ON. */
    public ConfigEntry requires(String dependencyField) {
        return withDependency(new Dependency(dependencyField, true));
    }

    /** A copy that applies only while the on/off setting {@code dependencyField} is OFF. */
    public ConfigEntry requiresOff(String dependencyField) {
        return withDependency(new Dependency(dependencyField, false));
    }

    private static ConfigEntry simple(Kind kind, String fieldName, String label, String help) {
        return new ConfigEntry(kind, fieldName, null, label, help, 0, 0, 0, ConfigUnit.NONE,
            NONE, NONE, NONE, Collections.<Dependency>emptyList());
    }

    private ConfigEntry withDependency(Dependency dependency) {
        List<Dependency> combined = new ArrayList<>(dependencies);
        combined.add(dependency);
        return new ConfigEntry(kind, fieldName, presetId, label, help, min, max, step, unit,
            choiceValues, choiceLabels, textHints, Collections.unmodifiableList(combined));
    }

    /** The label for a stored choice value, or the raw value when it is not one of the choices. */
    public String choiceLabel(String storedValue) {
        for (int i = 0; i < choiceValues.length; i++) {
            if (choiceValues[i].equalsIgnoreCase(storedValue)) {
                return choiceLabels[i];
            }
        }
        return storedValue;
    }

    public Kind getKind() {
        return kind;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getPresetId() {
        return presetId;
    }

    public String getLabel() {
        return label;
    }

    public String getHelp() {
        return help;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public ConfigUnit getUnit() {
        return unit;
    }

    public String[] getChoiceValues() {
        return choiceValues.clone();
    }

    public String[] getChoiceLabels() {
        return choiceLabels.clone();
    }

    public String[] getTextHints() {
        return textHints.clone();
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}

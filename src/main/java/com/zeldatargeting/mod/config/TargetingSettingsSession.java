package com.zeldatargeting.mod.config;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Keeps configuration-screen changes transactional until the player explicitly
 * saves them. Callers may apply {@link #preview()} at any time without changing
 * this session's entry snapshot.
 */
public final class TargetingSettingsSession {

    private final TargetingSettings entrySnapshot;
    private TargetingSettings working;

    public TargetingSettingsSession(TargetingSettings active) {
        TargetingSettings validActive = TargetingSettingsValidator.sanitize(active);
        this.entrySnapshot = validActive.copy();
        this.working = validActive.copy();
    }

    public TargetingSettings workingCopy() {
        return working;
    }

    public TargetingSettings preview() {
        return TargetingSettingsValidator.sanitize(working);
    }

    public void reset(TargetingPreset preset) {
        TargetingPreset selected = preset == null ? TargetingPreset.BALANCED : preset;
        working = selected.createSettings();
    }

    /**
     * Restores only the named settings to the preset's values, leaving every other
     * working change in place. Unknown names are ignored.
     */
    public void resetFields(TargetingPreset preset, Collection<String> fieldNames) {
        if (fieldNames == null) {
            return;
        }
        TargetingSettings defaults = (preset == null ? TargetingPreset.BALANCED : preset).createSettings();
        for (String fieldName : fieldNames) {
            try {
                Field field = TargetingSettings.class.getField(fieldName);
                field.set(working, field.get(defaults));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Not a settings field; nothing to reset.
            }
        }
    }

    public TargetingSettings save() {
        return TargetingSettingsValidator.sanitize(working);
    }

    public TargetingSettings cancel() {
        return entrySnapshot.copy();
    }
}

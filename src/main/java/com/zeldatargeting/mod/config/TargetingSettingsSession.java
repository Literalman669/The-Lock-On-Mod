package com.zeldatargeting.mod.config;

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

    public TargetingSettings save() {
        return TargetingSettingsValidator.sanitize(working);
    }

    public TargetingSettings cancel() {
        return entrySnapshot.copy();
    }
}

package com.zeldatargeting.mod.client.gui;

import com.zeldatargeting.mod.config.TargetingPreset;
import com.zeldatargeting.mod.config.TargetingSettings;
import com.zeldatargeting.mod.config.TargetingSettingsSession;

/**
 * GUI-level adapter around the pure settings session. It keeps the Minecraft
 * screen free to preview a working copy without confusing preview with Save.
 */
public final class ConfigEditSession {

    private final TargetingSettingsSession session;

    public ConfigEditSession(TargetingSettings entrySettings) {
        this.session = new TargetingSettingsSession(entrySettings);
    }

    public TargetingSettings workingCopy() {
        return session.workingCopy();
    }

    public TargetingSettings preview() {
        return session.preview();
    }

    public void reset(TargetingPreset preset) {
        session.reset(preset);
    }

    public TargetingSettings save() {
        return session.save();
    }

    public TargetingSettings cancel() {
        return session.cancel();
    }
}

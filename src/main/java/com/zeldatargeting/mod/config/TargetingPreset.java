package com.zeldatargeting.mod.config;

/** Curated, client-side starting points for the 1.4 configuration experience. */
public enum TargetingPreset {
    CINEMATIC("cinematic"),
    BALANCED("balanced"),
    SNAPPY("snappy");

    private final String id;

    TargetingPreset(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public TargetingSettings createSettings() {
        TargetingSettings settings = new TargetingSettings();
        settings.lockOnPreset = id;

        if (this == CINEMATIC) {
            settings.cameraSmoothness = 0.65F;
            settings.autoThirdPerson = true;
            settings.cameraFocusYOffset = 0.15F;
        } else if (this == SNAPPY) {
            settings.cameraSmoothness = 0.15F;
            settings.autoThirdPerson = false;
            settings.cameraFocusYOffset = 0.0F;
        }

        return settings;
    }

    public static TargetingPreset fromId(String id) {
        if (id != null) {
            for (TargetingPreset preset : values()) {
                if (preset.id.equalsIgnoreCase(id)) {
                    return preset;
                }
            }
        }
        return BALANCED;
    }
}

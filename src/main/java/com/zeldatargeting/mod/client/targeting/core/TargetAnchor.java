package com.zeldatargeting.mod.client.targeting.core;

public enum TargetAnchor {
    HEAD,
    CENTER,
    LOWER_BODY;

    public static TargetAnchor combatDefault() {
        return CENTER;
    }

    public TargetAnchor cycle(boolean forward) {
        TargetAnchor[] anchors = values();
        int next = Math.floorMod(ordinal() + (forward ? 1 : -1), anchors.length);
        return anchors[next];
    }
}

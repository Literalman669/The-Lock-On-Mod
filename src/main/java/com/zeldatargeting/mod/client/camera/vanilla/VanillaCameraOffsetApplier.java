package com.zeldatargeting.mod.client.camera.vanilla;

import com.zeldatargeting.mod.client.camera.core.CameraFrame;
import net.minecraft.client.renderer.GlStateManager;

public final class VanillaCameraOffsetApplier {
    public void apply(CameraFrame frame, int perspective) {
        if (frame == null) {
            return;
        }
        double translation = translationFor(perspective, frame.getTranslationDelta());
        if (translation != 0.0D) {
            GlStateManager.translate(0.0D, 0.0D, translation);
        }
    }

    static double translationFor(int perspective, double delta) {
        if (Double.isNaN(delta) || Double.isInfinite(delta)) {
            return 0.0D;
        }
        if (perspective == 1) {
            return -delta;
        }
        if (perspective == 2) {
            return delta;
        }
        return 0.0D;
    }
}

package com.zeldatargeting.mod.client.camera.vanilla;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;

public final class CloseCameraTransparency {
    private float alpha = 1.0F;
    private boolean blendApplied;

    public void setAlpha(float alpha) {
        if (Float.isNaN(alpha) || Float.isInfinite(alpha)) {
            this.alpha = 1.0F;
            return;
        }
        this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
    }

    public void onPre(RenderPlayerEvent.Pre event, EntityPlayer localPlayer) {
        if (event == null
                || localPlayer == null
                || event.getEntityPlayer() != localPlayer
                || alpha >= 1.0F) {
            return;
        }
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        blendApplied = true;
    }

    public void onPost(RenderPlayerEvent.Post event, EntityPlayer localPlayer) {
        if (!blendApplied
                || event == null
                || localPlayer == null
                || event.getEntityPlayer() != localPlayer) {
            return;
        }
        restoreGlState();
    }

    public void reset() {
        alpha = 1.0F;
        if (blendApplied) {
            restoreGlState();
        }
    }

    private void restoreGlState() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        blendApplied = false;
    }
}

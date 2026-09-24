package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.core.BossEligibility;
import com.zeldatargeting.mod.client.presentation.core.PanelAnchor;
import com.zeldatargeting.mod.client.presentation.core.PanelLayout;
import com.zeldatargeting.mod.client.presentation.core.PanelLayoutCalculator;
import com.zeldatargeting.mod.client.presentation.core.PresentationStyle;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BossPanelRenderer {
    private static final int HUD_MARGIN = 8;
    private static final int HUD_PADDING = 8;
    private static final int HUD_ACCENT_HEIGHT = 2;
    private static final int HUD_TITLE_GAP = 14;
    private static final int HUD_BG_COLOR = 0xB0101016;
    private static final int HUD_SHADOW_COLOR = 0x50000000;
    private static final int BOSS_BAR_WIDTH = 200;
    private static final int BOSS_BAR_HEIGHT = 8;
    private static final float BOSS_HP_THRESHOLD = 100.0F;

    private final Minecraft minecraft;
    private boolean warned;

    public BossPanelRenderer() {
        minecraft = Minecraft.getMinecraft();
    }

    public boolean render(TargetPresentationSnapshot snapshot, ScaledResolution resolution) {
        if (!TargetingConfig.bossStylePanel || snapshot == null || resolution == null
                || !BossEligibility.isEligible(snapshot.isVanillaBoss(), snapshot.getMaxHealth(), BOSS_HP_THRESHOLD)) {
            return false;
        }
        try {
            renderPanel(snapshot, resolution);
            return true;
        } catch (RuntimeException exception) {
            warn(exception);
            return false;
        }
    }

    private void renderPanel(TargetPresentationSnapshot snapshot, ScaledResolution resolution) {
        PresentationStyle style = PresentationStyle.fromConfig();
        int panelWidth = BOSS_BAR_WIDTH + HUD_PADDING * 2;
        int panelHeight = HUD_PADDING + HUD_TITLE_GAP + BOSS_BAR_HEIGHT + 6 + HUD_PADDING;
        int centerOffsetY = (resolution.getScaledHeight() - panelHeight) / 2 - 22;
        PanelLayout layout = PanelLayoutCalculator.calculate(
            resolution.getScaledWidth(), resolution.getScaledHeight(), panelWidth, panelHeight,
            PanelAnchor.CENTER, 0, centerOffsetY, HUD_MARGIN
        );
        String title = snapshot.getName().isEmpty() ? "Boss" : snapshot.getName();
        String hpText = String.format("%.0f / %.0f", snapshot.getHealth(), snapshot.getMaxHealth());

        Gui.drawRect(layout.getX() + 1, layout.getY() + 1,
            layout.getX() + layout.getWidth() + 1, layout.getY() + layout.getHeight() + 1,
            style.applyHudOpacity(HUD_SHADOW_COLOR));
        Gui.drawRect(layout.getX(), layout.getY(),
            layout.getX() + layout.getWidth(), layout.getY() + layout.getHeight(),
            style.applyHudOpacity(HUD_BG_COLOR));
        Gui.drawRect(layout.getX(), layout.getY(),
            layout.getX() + layout.getWidth(), layout.getY() + HUD_ACCENT_HEIGHT,
            style.applyHudOpacity(style.getStatusColor(snapshot.getStatus())));

        int titleX = layout.getX() + layout.getWidth() / 2 - minecraft.fontRenderer.getStringWidth(title) / 2;
        minecraft.fontRenderer.drawString(title, titleX, layout.getY() + HUD_PADDING, 0xFFFFFFFF);
        int barX = layout.getX() + HUD_PADDING;
        int barY = layout.getY() + HUD_PADDING + HUD_TITLE_GAP;
        int fillWidth = Math.round(BOSS_BAR_WIDTH * snapshot.getHealthRatio());
        Gui.drawRect(barX - 1, barY - 1, barX + BOSS_BAR_WIDTH + 1, barY + BOSS_BAR_HEIGHT + 1,
            style.applyHudOpacity(0xAA000000));
        Gui.drawRect(barX, barY, barX + BOSS_BAR_WIDTH, barY + BOSS_BAR_HEIGHT,
            style.applyHudOpacity(0xFF2B2E33));
        Gui.drawRect(barX, barY, barX + fillWidth, barY + BOSS_BAR_HEIGHT,
            style.applyHudOpacity(style.getStatusColor(snapshot.getStatus())));
        int hpX = layout.getX() + layout.getWidth() / 2 - minecraft.fontRenderer.getStringWidth(hpText) / 2;
        minecraft.fontRenderer.drawString(hpText, hpX, barY + BOSS_BAR_HEIGHT + 2, 0xFFC7CCD1);
    }

    private void warn(RuntimeException exception) {
        if (!warned && ZeldaTargetingMod.getLogger() != null) {
            warned = true;
            ZeldaTargetingMod.getLogger().warn("Boss panel renderer skipped a frame", exception);
        }
    }
}

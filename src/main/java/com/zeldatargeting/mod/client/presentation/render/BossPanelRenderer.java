package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.core.BossEligibility;
import com.zeldatargeting.mod.client.presentation.core.PresentationStyle;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class BossPanelRenderer {
    private static final int HUD_MARGIN = 8;
    private static final int HUD_TOP = 30;
    private static final int BOSS_MIN_WIDTH = 220;
    private static final int BOSS_MAX_WIDTH = 320;
    private static final int BAR_INSET = 15;
    private static final float BOSS_HP_THRESHOLD = 100.0F;

    private final Minecraft minecraft;
    private boolean warned;

    public BossPanelRenderer() {
        minecraft = Minecraft.getMinecraft();
    }

    public boolean render(TargetPresentationSnapshot snapshot, ScaledResolution resolution) {
        if (!TargetingConfig.bossStylePanel || snapshot == null || resolution == null
                || resolution.getScaledWidth() < 96 || resolution.getScaledHeight() < 80
                || !BossEligibility.isEligible(snapshot.isVanillaBoss(),
                    snapshot.getMaxHealth(), BOSS_HP_THRESHOLD)) {
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
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        int width = Math.min(screenWidth - HUD_MARGIN * 2,
            Math.min(BOSS_MAX_WIDTH,
                Math.max(BOSS_MIN_WIDTH, Math.round(screenWidth * 0.72F))));
        boolean health = TargetingConfig.showHealthBar;
        boolean compact = TargetingConfig.compactHudMode;
        int height = health ? (compact ? 52 : 62) : 42;
        if (width < 80 || screenHeight < height + HUD_MARGIN * 2) {
            return;
        }
        int x = (screenWidth - width) / 2;
        int y = Math.min(HUD_TOP, screenHeight - height - HUD_MARGIN);
        PresentationStyle style = PresentationStyle.fromConfig();
        CrestPanelPainter.frame(x, y, width, height, true, style);

        int centerX = x + width / 2;
        CrestPanelPainter.diamond(centerX, y + 8,
            style.applyHudOpacity(CrestPanelPainter.GOLD));
        String heading = "B O S S";
        int headingWidth = minecraft.fontRenderer.getStringWidth(heading);
        minecraft.fontRenderer.drawString(heading,
            centerX - headingWidth / 2, y + 13,
            style.applyHudOpacity(CrestPanelPainter.GOLD));

        String name = TargetingConfig.showTargetName && !snapshot.getName().isEmpty()
            ? snapshot.getName() : "Boss Target";
        name = fit(name, width - 48);
        int nameWidth = minecraft.fontRenderer.getStringWidth(name);
        minecraft.fontRenderer.drawStringWithShadow(name,
            centerX - nameWidth / 2, y + 25,
            style.applyHudOpacity(CrestPanelPainter.TEXT));
        Gui.drawRect(x + 15, y + 29, centerX - nameWidth / 2 - 8, y + 30,
            style.applyHudOpacity(CrestPanelPainter.GOLD_DIM));
        Gui.drawRect(centerX + nameWidth / 2 + 8, y + 29,
            x + width - 15, y + 30,
            style.applyHudOpacity(CrestPanelPainter.GOLD_DIM));

        if (health) {
            int barX = x + BAR_INSET;
            int barY = y + 37;
            int barWidth = width - BAR_INSET * 2;
            CrestPanelPainter.healthBar(barX, barY, barWidth, 9,
                snapshot.getHealthRatio(), style.getStatusColor(snapshot.getStatus()),
                true, style);
            if (!compact) {
                String hp = String.format(Locale.ROOT, "%.0f / %.0f HP",
                    snapshot.getHealth(), snapshot.getMaxHealth());
                int hpWidth = minecraft.fontRenderer.getStringWidth(hp);
                minecraft.fontRenderer.drawString(hp,
                    centerX - hpWidth / 2, barY + 12,
                    style.applyHudOpacity(CrestPanelPainter.MUTED));
            }
        }
    }

    private String fit(String value, int maxWidth) {
        if (minecraft.fontRenderer.getStringWidth(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int textWidth = maxWidth - minecraft.fontRenderer.getStringWidth(ellipsis);
        return textWidth <= 0 ? "" : minecraft.fontRenderer.trimStringToWidth(value, textWidth)
            + ellipsis;
    }

    private void warn(RuntimeException exception) {
        if (!warned && ZeldaTargetingMod.getLogger() != null) {
            warned = true;
            ZeldaTargetingMod.getLogger().warn("Boss panel renderer skipped a frame", exception);
        }
    }
}

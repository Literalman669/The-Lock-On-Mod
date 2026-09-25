package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.core.PanelAnchor;
import com.zeldatargeting.mod.client.presentation.core.PanelLayout;
import com.zeldatargeting.mod.client.presentation.core.PanelLayoutCalculator;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.presentation.core.PresentationStyle;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class DetailPanelRenderer {
    private static final int HUD_MARGIN = 8;
    private static final int PADDING = 10;
    private static final int MIN_WIDTH = 156;
    private static final int MAX_WIDTH = 220;
    private static final int BADGE_GAP = 12;

    private final Minecraft minecraft;
    private boolean warned;

    public DetailPanelRenderer() {
        minecraft = Minecraft.getMinecraft();
    }

    public void render(TargetPresentationSnapshot snapshot, ScaledResolution resolution) {
        if (snapshot == null || resolution == null) {
            return;
        }
        try {
            renderPanel(snapshot, resolution);
        } catch (RuntimeException exception) {
            warn(exception);
        }
    }

    private void renderPanel(TargetPresentationSnapshot snapshot, ScaledResolution resolution) {
        PresentationStyle style = PresentationStyle.fromConfig();
        String title = TargetingConfig.showTargetName && !snapshot.getName().isEmpty()
            ? snapshot.getName() : "Target";
        boolean health = TargetingConfig.showHealthBar;
        boolean details = !TargetingConfig.compactHudMode;
        String hp = health ? healthValue(snapshot.getHealth()) + " / "
            + healthValue(snapshot.getMaxHealth()) : "";
        List<String> badges = details ? badges(snapshot) : new ArrayList<>();
        String condition = details && TargetingConfig.showVulnerabilities
            ? snapshot.getVulnerabilityText() : "";

        int wantedWidth = Math.max(MIN_WIDTH,
            PADDING * 2 + 11 + minecraft.fontRenderer.getStringWidth(title)
                + (health ? minecraft.fontRenderer.getStringWidth(hp) + 10 : 0));
        wantedWidth = Math.max(wantedWidth, PADDING * 2 + badgeWidth(badges));
        if (!condition.isEmpty()) {
            wantedWidth = Math.max(wantedWidth,
                PADDING * 2 + minecraft.fontRenderer.getStringWidth(condition));
        }
        wantedWidth = Math.min(MAX_WIDTH, wantedWidth);

        int panelHeight = 8 + 14 + (health ? 11 : 0)
            + (!badges.isEmpty() ? 11 : 0) + (!condition.isEmpty() ? 11 : 0) + 7;
        PanelLayout layout = PanelLayoutCalculator.calculate(
            resolution.getScaledWidth(), resolution.getScaledHeight(),
            wantedWidth, panelHeight,
            PanelAnchor.fromConfig(TargetingConfig.hudAnchor),
            TargetingConfig.hudOffsetX, TargetingConfig.hudOffsetY, HUD_MARGIN
        );
        int x = layout.getX();
        int y = layout.getY();
        int width = layout.getWidth();
        int contentWidth = Math.max(0, width - PADDING * 2);
        CrestPanelPainter.frame(x, y, width, layout.getHeight(), false, style);

        int titleY = y + 8;
        CrestPanelPainter.diamond(x + PADDING + 2, titleY + 4,
            style.applyHudOpacity(CrestPanelPainter.GOLD));
        int titleX = x + PADDING + 12;
        int hpWidth = health ? minecraft.fontRenderer.getStringWidth(hp) : 0;
        int nameWidth = Math.max(0, x + width - PADDING - titleX
            - (health ? hpWidth + 8 : 0));
        minecraft.fontRenderer.drawStringWithShadow(
            fit(title, nameWidth), titleX, titleY,
            style.applyHudOpacity(snapshot.getStatus() == PresentationStatus.OCCLUDED
                ? CrestPanelPainter.MUTED : CrestPanelPainter.TEXT)
        );
        if (health) {
            minecraft.fontRenderer.drawString(hp,
                x + width - PADDING - hpWidth, titleY,
                style.applyHudOpacity(CrestPanelPainter.MUTED));
        }

        int nextY = titleY + 14;
        if (health) {
            CrestPanelPainter.healthBar(x + PADDING, nextY, contentWidth, 6,
                snapshot.getHealthRatio(), style.getStatusColor(snapshot.getStatus()),
                false, style);
            nextY += 11;
        }
        if (!badges.isEmpty()) {
            drawBadges(badges, x + PADDING, nextY, contentWidth, snapshot, style);
            nextY += 11;
        }
        if (!condition.isEmpty()) {
            Gui.drawRect(x + PADDING, nextY - 1,
                x + width - PADDING, nextY,
                style.applyHudOpacity(CrestPanelPainter.GOLD_DIM));
            minecraft.fontRenderer.drawString(
                fit(condition, contentWidth), x + PADDING, nextY + 2,
                style.applyHudOpacity(CrestPanelPainter.MUTED));
        }
    }

    private List<String> badges(TargetPresentationSnapshot snapshot) {
        List<String> badges = new ArrayList<>(3);
        if (TargetingConfig.showDistance) {
            badges.add(String.format(Locale.ROOT, "%.1fm", snapshot.getDistance()));
        }
        if (TargetingConfig.showDamagePrediction) {
            badges.add(snapshot.getPredictedDamage() > 0.0F
                ? String.format(Locale.ROOT, "DMG %.1f", snapshot.getPredictedDamage())
                : "DMG --");
        }
        if (TargetingConfig.showHitsToKill) {
            badges.add(snapshot.getHitsToKill() > 0
                ? snapshot.getHitsToKill()
                    + (snapshot.getHitsToKill() == 1 ? " HIT" : " HITS")
                : "HITS --");
        }
        return badges;
    }

    private int badgeWidth(List<String> badges) {
        int width = 0;
        for (String badge : badges) {
            width += minecraft.fontRenderer.getStringWidth(badge);
        }
        return width + Math.max(0, badges.size() - 1) * BADGE_GAP;
    }

    private void drawBadges(List<String> badges, int x, int y, int availableWidth,
                            TargetPresentationSnapshot snapshot, PresentationStyle style) {
        int used = 0;
        for (String badge : badges) {
            int badgeWidth = minecraft.fontRenderer.getStringWidth(badge);
            int nextWidth = used == 0 ? badgeWidth : used + BADGE_GAP + badgeWidth;
            if (nextWidth > availableWidth) {
                break;
            }
            if (used > 0) {
                Gui.drawRect(x + used + 5, y + 2, x + used + 6, y + 7,
                    style.applyHudOpacity(CrestPanelPainter.GOLD_DIM));
                used += BADGE_GAP;
            }
            boolean lethal = snapshot.getStatus() == PresentationStatus.LETHAL
                && (badge.endsWith("HIT") || badge.endsWith("HITS"));
            minecraft.fontRenderer.drawString(badge, x + used, y,
                style.applyHudOpacity(lethal
                    ? style.getStatusColor(snapshot.getStatus())
                    : CrestPanelPainter.MUTED));
            used += badgeWidth;
        }
    }

    private String fit(String value, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (minecraft.fontRenderer.getStringWidth(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int textWidth = maxWidth - minecraft.fontRenderer.getStringWidth(ellipsis);
        return textWidth <= 0 ? "" : minecraft.fontRenderer.trimStringToWidth(value, textWidth)
            + ellipsis;
    }

    private static String healthValue(float value) {
        return Math.abs(value - Math.round(value)) < 0.05F
            ? Integer.toString(Math.round(value))
            : String.format(Locale.ROOT, "%.1f", value);
    }

    private void warn(RuntimeException exception) {
        if (!warned && ZeldaTargetingMod.getLogger() != null) {
            warned = true;
            ZeldaTargetingMod.getLogger().warn("Target detail panel skipped a frame", exception);
        }
    }
}

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

@SideOnly(Side.CLIENT)
public final class DetailPanelRenderer {
    private static final int HUD_MARGIN = 8;
    private static final int HUD_PADDING = 8;
    private static final int HUD_MIN_WIDTH = 130;
    private static final int HUD_BAR_WIDTH = 124;
    private static final int HUD_ACCENT_HEIGHT = 2;
    private static final int HUD_TITLE_GAP = 14;
    private static final int HUD_VALUE_GAP = 12;
    private static final int HUD_HEALTH_BLOCK_HEIGHT = 22;
    private static final int HUD_BG_COLOR = 0xB0101016;
    private static final int HUD_SHADOW_COLOR = 0x50000000;
    private static final int HUD_TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int HUD_TEXT_SECONDARY = 0xFFC7CCD1;
    private static final int HUD_TEXT_MUTED = 0xFF9EA6AD;

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
            ? snapshot.getName() : "Locked Target";
        boolean drawHealth = TargetingConfig.showHealthBar;
        String[] lines = new String[4];
        int[] colors = new int[4];
        int lineCount = populateLines(snapshot, lines, colors, style);

        int contentWidth = minecraft.fontRenderer.getStringWidth(title);
        if (drawHealth) {
            contentWidth = Math.max(contentWidth, HUD_BAR_WIDTH);
            contentWidth = Math.max(contentWidth, minecraft.fontRenderer.getStringWidth(healthText(snapshot)));
        }
        for (int index = 0; index < lineCount; index++) {
            contentWidth = Math.max(contentWidth, minecraft.fontRenderer.getStringWidth(lines[index]));
        }

        int panelWidth = Math.max(HUD_MIN_WIDTH, contentWidth + HUD_PADDING * 2);
        int panelHeight = HUD_PADDING + HUD_TITLE_GAP + HUD_PADDING - 2 + lineCount * HUD_VALUE_GAP;
        if (drawHealth) {
            panelHeight += HUD_HEALTH_BLOCK_HEIGHT;
        }
        PanelLayout layout = PanelLayoutCalculator.calculate(
            resolution.getScaledWidth(),
            resolution.getScaledHeight(),
            panelWidth,
            panelHeight,
            PanelAnchor.fromConfig(TargetingConfig.hudAnchor),
            TargetingConfig.hudOffsetX,
            TargetingConfig.hudOffsetY,
            HUD_MARGIN
        );

        Gui.drawRect(layout.getX() + 1, layout.getY() + 1,
            layout.getX() + layout.getWidth() + 1, layout.getY() + layout.getHeight() + 1,
            style.applyHudOpacity(HUD_SHADOW_COLOR));
        Gui.drawRect(layout.getX(), layout.getY(),
            layout.getX() + layout.getWidth(), layout.getY() + layout.getHeight(),
            style.applyHudOpacity(HUD_BG_COLOR));
        Gui.drawRect(layout.getX(), layout.getY(),
            layout.getX() + layout.getWidth(), layout.getY() + HUD_ACCENT_HEIGHT,
            style.applyHudOpacity(style.getStatusColor(snapshot.getStatus())));

        int textX = layout.getX() + HUD_PADDING;
        int currentY = layout.getY() + HUD_PADDING;
        minecraft.fontRenderer.drawString(title, textX, currentY, titleColor(snapshot.getStatus()));
        currentY += HUD_TITLE_GAP;

        if (drawHealth) {
            renderHealth(snapshot, textX, currentY, style);
            currentY += HUD_HEALTH_BLOCK_HEIGHT;
        }
        for (int index = 0; index < lineCount; index++) {
            minecraft.fontRenderer.drawString(lines[index], textX, currentY, colors[index]);
            currentY += HUD_VALUE_GAP;
        }
    }

    private int populateLines(TargetPresentationSnapshot snapshot, String[] lines, int[] colors,
                              PresentationStyle style) {
        int count = 0;
        if (!TargetingConfig.compactHudMode && TargetingConfig.showDamagePrediction) {
            if (snapshot.getPredictedDamage() <= 0.0F) {
                lines[count] = "Damage: no effective damage";
                colors[count++] = HUD_TEXT_MUTED;
            } else {
                lines[count] = String.format("Damage: %.1f", snapshot.getPredictedDamage());
                colors[count++] = style.getStatusColor(snapshot.getStatus());
            }
        }
        if (!TargetingConfig.compactHudMode && TargetingConfig.showHitsToKill) {
            if (snapshot.getHitsToKill() > 0) {
                lines[count] = "Hits to Kill: " + snapshot.getHitsToKill()
                    + (snapshot.getStatus() == PresentationStatus.LETHAL ? " (LETHAL)" : "");
                colors[count++] = snapshot.getStatus() == PresentationStatus.LETHAL
                    ? style.getStatusColor(snapshot.getStatus()) : HUD_TEXT_SECONDARY;
            } else {
                lines[count] = "Hits to Kill: N/A";
                colors[count++] = HUD_TEXT_MUTED;
            }
        }
        if (!TargetingConfig.compactHudMode && TargetingConfig.showVulnerabilities
                && !snapshot.getVulnerabilityText().isEmpty()) {
            lines[count] = "Status: " + snapshot.getVulnerabilityText();
            colors[count++] = HUD_TEXT_SECONDARY;
        }
        if (!TargetingConfig.compactHudMode && TargetingConfig.showDistance) {
            lines[count] = String.format("Distance: %.1fm", snapshot.getDistance());
            colors[count++] = HUD_TEXT_SECONDARY;
        }
        return count;
    }

    private void renderHealth(TargetPresentationSnapshot snapshot, int x, int y, PresentationStyle style) {
        int fillWidth = Math.round(HUD_BAR_WIDTH * snapshot.getHealthRatio());
        Gui.drawRect(x - 1, y - 1, x + HUD_BAR_WIDTH + 1, y + 5, style.applyHudOpacity(0xAA000000));
        Gui.drawRect(x, y, x + HUD_BAR_WIDTH, y + 4, style.applyHudOpacity(0xFF2B2E33));
        Gui.drawRect(x, y, x + fillWidth, y + 4,
            style.applyHudOpacity(style.getStatusColor(snapshot.getStatus())));
        minecraft.fontRenderer.drawString(healthText(snapshot), x, y + 8, HUD_TEXT_SECONDARY);
    }

    private static String healthText(TargetPresentationSnapshot snapshot) {
        return String.format("HP %.1f/%.1f (%.0f%%)", snapshot.getHealth(), snapshot.getMaxHealth(),
            snapshot.getHealthRatio() * 100.0F);
    }

    private static int titleColor(PresentationStatus status) {
        return status == PresentationStatus.OCCLUDED ? HUD_TEXT_MUTED : HUD_TEXT_PRIMARY;
    }

    private void warn(RuntimeException exception) {
        if (!warned && ZeldaTargetingMod.getLogger() != null) {
            warned = true;
            ZeldaTargetingMod.getLogger().warn("Target detail panel skipped a frame", exception);
        }
    }
}

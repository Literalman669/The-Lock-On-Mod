package com.zeldatargeting.mod.client.render;

import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.client.combat.DamageCalculator;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class TargetRenderer {

    private static final int HUD_MARGIN = 8;
    private static final int HUD_PADDING = 8;
    private static final int HUD_MIN_WIDTH = 130;
    private static final int HUD_BAR_WIDTH = 124;
    private static final int HUD_ACCENT_HEIGHT = 2;
    private static final int HUD_LINE_HEIGHT = 10;
    private static final int HUD_TITLE_GAP = 14;
    private static final int HUD_VALUE_GAP = 12;
    private static final int HUD_HEALTH_BLOCK_HEIGHT = 22;

    private static final int HUD_BG_COLOR = 0xB0101016;
    private static final int HUD_SHADOW_COLOR = 0x50000000;
    private static final int HUD_TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int HUD_TEXT_SECONDARY = 0xFFC7CCD1;
    private static final int HUD_TEXT_MUTED = 0xFF9EA6AD;
    private static final int HUD_ACCENT_DEFAULT = 0xFFDE3E3E;
    private static final int HUD_ACCENT_WARNING = 0xFFFFA340;
    private static final int HUD_ACCENT_LETHAL = 0xFFFF4B4B;

    private static final int BOSS_BAR_WIDTH = 200;
    private static final int BOSS_BAR_HEIGHT = 8;
    private static final int BOSS_HP_THRESHOLD = 100;

    private final Minecraft mc;
    private long animationTime = 0;
    private final List<String> statLines = new ArrayList<>(6);
    private final List<Integer> statColors = new ArrayList<>(6);

    // Target history ring — stores up to 3 recently locked entities
    private final java.util.ArrayDeque<Entity> targetHistory = new java.util.ArrayDeque<>(4);
    private Entity lastHistoryTarget = null;

    public TargetRenderer() {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {

        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        TargetingManager manager = TargetingManager.getInstance();
        if (manager == null || !manager.isActive()) {
            return;
        }

        Entity target = manager.getCurrentTarget();
        if (target == null) {
            return;
        }

        animationTime = System.currentTimeMillis();

        // Update target history
        if (TargetingConfig.targetHistoryEnabled && target != lastHistoryTarget) {
            if (lastHistoryTarget != null) {
                targetHistory.remove(lastHistoryTarget);
                targetHistory.addFirst(lastHistoryTarget);
                while (targetHistory.size() > 3) targetHistory.removeLast();
            }
            lastHistoryTarget = target;
        }

        if (TargetingConfig.showReticle) {
            render3DReticle(target);
        }

        render2DHUD(target, event.getResolution());

        if (TargetingConfig.softAimIndicator) {
            renderSoftAimIndicator(target, event.getResolution());
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        TargetingManager manager = TargetingManager.getInstance();
        if (manager == null) {
            return;
        }

        Entity currentTarget = manager.getCurrentTarget();
        if (currentTarget != null && manager.isActive() && TargetingConfig.showReticle) {
            renderRedIndicator(currentTarget, event.getPartialTicks());
        }
        // Render faint history rings above recently targeted entities
        if (TargetingConfig.targetHistoryEnabled) {
            for (Entity hist : targetHistory) {
                if (hist != null && hist.isEntityAlive()) {
                    renderHistoryRing(hist, event.getPartialTicks());
                }
            }
        }
    }

    private void render3DReticle(Entity target) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Calculate target position
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.getRenderPartialTicks();
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.getRenderPartialTicks();
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.getRenderPartialTicks();

        // Offset for camera position
        x -= mc.getRenderManager().viewerPosX;
        y -= mc.getRenderManager().viewerPosY;
        z -= mc.getRenderManager().viewerPosZ;

        // Calculate reticle size based on target size and distance
        float targetHeight = target.height;
        float targetWidth = target.width;
        double distance = mc.player.getDistanceSq(target);
        float scale = (float) Math.max(0.5, Math.min(2.0, 10.0 / Math.sqrt(distance)));

        // Animation pulse
        float pulse = (float) (0.8 + 0.2 * Math.sin(animationTime * 0.01));
        scale *= pulse;

        // Draw reticle rings
        drawReticleRing(x, y + targetHeight * 0.5, z, targetWidth * scale, targetHeight * scale);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("null")
    private void drawReticleRing(double x, double y, double z, float width, float height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Get current target for enhanced coloring
        TargetingManager manager = TargetingManager.getInstance();
        Entity target = manager != null ? manager.getCurrentTarget() : null;

        // Enhanced color system based on lethality and target type
        float red = 1.0f, green = 0.2f, blue = 0.2f, alpha = 0.8f;

        int hitsToKill = (target instanceof EntityLiving && TargetingConfig.highlightLethalTargets)
            ? DamageCalculator.calculateHitsToKill(target) : Integer.MAX_VALUE;

        if (hitsToKill == 1) {
            float pulse = (float) (0.8 + 0.2 * Math.sin(animationTime * 0.015));
            red = 1.0f;
            green = 0.1f * pulse;
            blue = 0.1f * pulse;
            alpha = 0.9f + 0.1f * pulse;
        } else if (hitsToKill <= 3) {
            red = 1.0f;
            green = 0.6f;
            blue = 0.1f;
        }

        GlStateManager.color(red, green, blue, alpha);

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        // Draw circular reticle with enhanced thickness for lethal targets
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            double offsetX = Math.cos(angle) * width;
            double offsetZ = Math.sin(angle) * width;
            buffer.pos(x + offsetX, y, z + offsetZ).endVertex();
        }

        tessellator.draw();

        // Draw additional inner ring for lethal targets
        if (hitsToKill == 1) {
            GlStateManager.color(1.0f, 0.8f, 0.0f, 0.6f); // Golden inner ring
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

            float innerWidth = width * 0.7f;
            for (int i = 0; i < segments; i++) {
                double angle = 2.0 * Math.PI * i / segments;
                double offsetX = Math.cos(angle) * innerWidth;
                double offsetZ = Math.sin(angle) * innerWidth;
                buffer.pos(x + offsetX, y, z + offsetZ).endVertex();
            }

            tessellator.draw();
        }

        // Draw corner brackets
        drawCornerBrackets(x, y, z, width, height);
    }

    @SuppressWarnings("null")
    private void drawCornerBrackets(double x, double y, double z, float width, float height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        float bracketSize = width * 0.3f;
        float halfWidth = width * 0.7f;
        float halfHeight = height * 0.5f;

        // Top-left bracket
        buffer.pos(x - halfWidth, y + halfHeight, z).endVertex();
        buffer.pos(x - halfWidth + bracketSize, y + halfHeight, z).endVertex();
        buffer.pos(x - halfWidth, y + halfHeight, z).endVertex();
        buffer.pos(x - halfWidth, y + halfHeight - bracketSize, z).endVertex();

        // Top-right bracket
        buffer.pos(x + halfWidth, y + halfHeight, z).endVertex();
        buffer.pos(x + halfWidth - bracketSize, y + halfHeight, z).endVertex();
        buffer.pos(x + halfWidth, y + halfHeight, z).endVertex();
        buffer.pos(x + halfWidth, y + halfHeight - bracketSize, z).endVertex();

        // Bottom-left bracket
        buffer.pos(x - halfWidth, y - halfHeight, z).endVertex();
        buffer.pos(x - halfWidth + bracketSize, y - halfHeight, z).endVertex();
        buffer.pos(x - halfWidth, y - halfHeight, z).endVertex();
        buffer.pos(x - halfWidth, y - halfHeight + bracketSize, z).endVertex();

        // Bottom-right bracket
        buffer.pos(x + halfWidth, y - halfHeight, z).endVertex();
        buffer.pos(x + halfWidth - bracketSize, y - halfHeight, z).endVertex();
        buffer.pos(x + halfWidth, y - halfHeight, z).endVertex();
        buffer.pos(x + halfWidth, y - halfHeight + bracketSize, z).endVertex();

        tessellator.draw();
    }

    @SuppressWarnings("null")
    private void render2DHUD(Entity target, ScaledResolution resolution) {
        if (mc.player == null) {
            return;
        }

        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();

        boolean isLiving = target instanceof EntityLiving;
        double distanceMeters = Math.sqrt(mc.player.getDistanceSq(target));

        float health = 0.0f;
        float maxHealth = 1.0f;
        float healthRatio = 0.0f;

        int hitsToKill = -1;
        float predictedDamage = 0.0f;
        String vulnerabilityText = "";

        if (isLiving) {
            EntityLiving livingTarget = (EntityLiving) target;
            health = livingTarget.getHealth();
            maxHealth = Math.max(1.0f, livingTarget.getMaxHealth());
            healthRatio = health / maxHealth;
            hitsToKill = DamageCalculator.calculateHitsToKill(target);
            predictedDamage = DamageCalculator.calculateDamage(target);
            if (TargetingConfig.showVulnerabilities) {
                String vulnerability = DamageCalculator.getVulnerabilityText(livingTarget);
                vulnerabilityText = vulnerability == null ? "" : vulnerability;
            }
        }

        boolean isLethal = TargetingConfig.highlightLethalTargets && hitsToKill == 1;
        int accentColor = isLethal ? HUD_ACCENT_LETHAL : (hitsToKill > 1 && hitsToKill <= 3 ? HUD_ACCENT_WARNING : HUD_ACCENT_DEFAULT);

        // Boss-style panel overrides normal HUD for high-HP targets
        if (TargetingConfig.bossStylePanel && isLiving && maxHealth >= BOSS_HP_THRESHOLD) {
            renderBossPanel(target, screenWidth, screenHeight, health, maxHealth, healthRatio, isLethal, accentColor);
            return;
        }

        statLines.clear();
        statColors.clear();

        // Compact mode: skip all stat lines
        if (TargetingConfig.compactHudMode) {
            renderCompactHud(target, screenWidth, "Locked Target", isLiving, health, maxHealth, healthRatio, isLethal, accentColor);
            return;
        }

        if (TargetingConfig.showDamagePrediction && isLiving) {
            if (predictedDamage <= 0.0f) {
                statLines.add("Damage: no effective damage");
                statColors.add(HUD_TEXT_MUTED);
            } else {
                statLines.add(String.format("Damage: %.1f", predictedDamage));
                statColors.add(DamageCalculator.getDamagePredictionColor(target));
            }
        }

        if (TargetingConfig.showHitsToKill && isLiving) {
            if (hitsToKill > 0) {
                statLines.add("Hits to Kill: " + hitsToKill + (hitsToKill == 1 ? " (LETHAL)" : ""));
                statColors.add(isLethal ? HUD_ACCENT_LETHAL : HUD_TEXT_SECONDARY);
            } else {
                statLines.add("Hits to Kill: N/A");
                statColors.add(HUD_TEXT_MUTED);
            }
        }

        if (TargetingConfig.showVulnerabilities && !vulnerabilityText.isEmpty()) {
            statLines.add("Status: " + vulnerabilityText);
            statColors.add(DamageCalculator.getVulnerabilityColor((EntityLiving) target));
        }

        if (TargetingConfig.showDistance) {
            statLines.add(String.format("Distance: %.1fm", distanceMeters));
            statColors.add(HUD_TEXT_SECONDARY);
        }

        String targetName = target.getName();
        String titleText = TargetingConfig.showTargetName && targetName != null ? targetName : "Locked Target";

        int contentWidth = mc.fontRenderer.getStringWidth(titleText);

        if (isLiving && TargetingConfig.showHealthBar) {
            contentWidth = Math.max(contentWidth, HUD_BAR_WIDTH);
            String healthText = String.format("HP %.1f/%.1f (%.0f%%)", health, maxHealth, healthRatio * 100.0f);
            contentWidth = Math.max(contentWidth, mc.fontRenderer.getStringWidth(healthText));
        }

        for (String line : statLines) {
            contentWidth = Math.max(contentWidth, mc.fontRenderer.getStringWidth(line));
        }

        int panelWidth = Math.max(HUD_MIN_WIDTH, contentWidth + HUD_PADDING * 2);
        int panelHeight = HUD_PADDING + HUD_TITLE_GAP;

        if (isLiving && TargetingConfig.showHealthBar) {
            panelHeight += HUD_HEALTH_BLOCK_HEIGHT;
        }

        panelHeight += statLines.size() * HUD_VALUE_GAP;
        panelHeight += HUD_PADDING - 2;

        int panelX = Math.max(HUD_MARGIN, screenWidth - panelWidth - HUD_MARGIN);
        int panelY = HUD_MARGIN;
        if (panelY + panelHeight > screenHeight - HUD_MARGIN) {
            panelY = Math.max(HUD_MARGIN, screenHeight - panelHeight - HUD_MARGIN);
        }

        Gui.drawRect(panelX + 1, panelY + 1, panelX + panelWidth + 1, panelY + panelHeight + 1, HUD_SHADOW_COLOR);
        Gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, HUD_BG_COLOR);
        Gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + HUD_ACCENT_HEIGHT, accentColor);

        int textX = panelX + HUD_PADDING;
        int currentY = panelY + HUD_PADDING;

        int titleColor = isLethal
            ? ((int)(220 + 35 * Math.sin(animationTime * 0.012)) << 16) | 0x004B4B
            : HUD_TEXT_PRIMARY;
        mc.fontRenderer.drawString(titleText, textX, currentY, titleColor);
        currentY += HUD_TITLE_GAP;

        if (isLiving && TargetingConfig.showHealthBar) {
            int barX = textX;
            int barY = currentY;
            int fillWidth = (int) (HUD_BAR_WIDTH * Math.max(0f, Math.min(1f, healthRatio)));

            Gui.drawRect(barX - 1, barY - 1, barX + HUD_BAR_WIDTH + 1, barY + 5, 0xAA000000);
            Gui.drawRect(barX, barY, barX + HUD_BAR_WIDTH, barY + 4, 0xFF2B2E33);

            int healthColor = healthRatio > 0.75f ? 0xFF4CD964
                : (healthRatio > 0.5f ? 0xFFB2E35D
                : (healthRatio > 0.25f ? 0xFFFFC44D : 0xFFFF5A5A));
            Gui.drawRect(barX, barY, barX + fillWidth, barY + 4, healthColor);

            // Low-health warning: pulsing red tint overlay below 25%
            if (healthRatio <= 0.25f && healthRatio > 0f) {
                float pulse = 0.4f + 0.4f * (float) Math.abs(Math.sin(animationTime * 0.018));
                Gui.drawRect(barX, barY, barX + HUD_BAR_WIDTH, barY + 4, ((int)(pulse * 180) << 24) | 0xFF2020);
            }

            String healthText = String.format("HP %.1f/%.1f (%.0f%%)", health, maxHealth, healthRatio * 100.0f);
            mc.fontRenderer.drawString(healthText, barX, barY + 8, HUD_TEXT_SECONDARY);
            currentY += HUD_HEALTH_BLOCK_HEIGHT;
        }

        for (int i = 0; i < statLines.size(); i++) {
            mc.fontRenderer.drawString(statLines.get(i), textX, currentY, statColors.get(i));
            currentY += HUD_LINE_HEIGHT + 2;
        }
    }

    @SuppressWarnings("null")
    private void renderCompactHud(Entity target, int screenWidth, String titleText,
            boolean isLiving, float health, float maxHealth, float healthRatio,
            boolean isLethal, int accentColor) {
        int panelWidth = Math.max(HUD_MIN_WIDTH, Math.max(
            mc.fontRenderer.getStringWidth(titleText),
            isLiving && TargetingConfig.showHealthBar ? HUD_BAR_WIDTH : 0) + HUD_PADDING * 2);
        int panelHeight = HUD_PADDING + HUD_TITLE_GAP
            + (isLiving && TargetingConfig.showHealthBar ? HUD_HEALTH_BLOCK_HEIGHT : 0)
            + HUD_PADDING - 2;

        int panelX = Math.max(HUD_MARGIN, screenWidth - panelWidth - HUD_MARGIN);
        int panelY = HUD_MARGIN;

        Gui.drawRect(panelX + 1, panelY + 1, panelX + panelWidth + 1, panelY + panelHeight + 1, HUD_SHADOW_COLOR);
        Gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, HUD_BG_COLOR);
        Gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + HUD_ACCENT_HEIGHT, accentColor);

        int textX = panelX + HUD_PADDING;
        int currentY = panelY + HUD_PADDING;

        int titleColor = isLethal
            ? ((int)(220 + 35 * Math.sin(animationTime * 0.012)) << 16) | 0x004B4B
            : HUD_TEXT_PRIMARY;
        mc.fontRenderer.drawString(titleText, textX, currentY, titleColor);
        currentY += HUD_TITLE_GAP;

        if (isLiving && TargetingConfig.showHealthBar) {
            int fillWidth = (int)(HUD_BAR_WIDTH * Math.max(0f, Math.min(1f, healthRatio)));
            Gui.drawRect(textX - 1, currentY - 1, textX + HUD_BAR_WIDTH + 1, currentY + 5, 0xAA000000);
            Gui.drawRect(textX, currentY, textX + HUD_BAR_WIDTH, currentY + 4, 0xFF2B2E33);
            int healthColor = healthRatio > 0.75f ? 0xFF4CD964
                : (healthRatio > 0.5f ? 0xFFB2E35D
                : (healthRatio > 0.25f ? 0xFFFFC44D : 0xFFFF5A5A));
            Gui.drawRect(textX, currentY, textX + fillWidth, currentY + 4, healthColor);
            if (healthRatio <= 0.25f && healthRatio > 0f) {
                float pulse = 0.4f + 0.4f * (float) Math.abs(Math.sin(animationTime * 0.018));
                Gui.drawRect(textX, currentY, textX + HUD_BAR_WIDTH, currentY + 4, ((int)(pulse * 180) << 24) | 0xFF2020);
            }
        }
    }

    @SuppressWarnings("null")
    private void renderBossPanel(Entity target, int screenWidth, int screenHeight,
            float health, float maxHealth, float healthRatio,
            boolean isLethal, int accentColor) {
        String name = target.getName();
        String titleText = name != null ? name : "Boss";
        String hpText = String.format("%.0f / %.0f", health, maxHealth);

        int panelWidth = BOSS_BAR_WIDTH + HUD_PADDING * 2;
        int panelHeight = HUD_PADDING + HUD_TITLE_GAP + BOSS_BAR_HEIGHT + 6 + HUD_PADDING;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = screenHeight - panelHeight - 22;

        Gui.drawRect(panelX + 1, panelY + 1, panelX + panelWidth + 1, panelY + panelHeight + 1, HUD_SHADOW_COLOR);
        Gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, HUD_BG_COLOR);
        Gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + HUD_ACCENT_HEIGHT, accentColor);

        int titleColor = isLethal
            ? ((int)(220 + 35 * Math.sin(animationTime * 0.012)) << 16) | 0x004B4B
            : HUD_TEXT_PRIMARY;
        mc.fontRenderer.drawString(titleText, panelX + panelWidth / 2 - mc.fontRenderer.getStringWidth(titleText) / 2, panelY + HUD_PADDING, titleColor);

        int barX = panelX + HUD_PADDING;
        int barY = panelY + HUD_PADDING + HUD_TITLE_GAP;
        int fillWidth = (int)(BOSS_BAR_WIDTH * Math.max(0f, Math.min(1f, healthRatio)));

        Gui.drawRect(barX - 1, barY - 1, barX + BOSS_BAR_WIDTH + 1, barY + BOSS_BAR_HEIGHT + 1, 0xAA000000);
        Gui.drawRect(barX, barY, barX + BOSS_BAR_WIDTH, barY + BOSS_BAR_HEIGHT, 0xFF2B2E33);

        int healthColor = healthRatio > 0.75f ? 0xFF4CD964
            : (healthRatio > 0.5f ? 0xFFB2E35D
            : (healthRatio > 0.25f ? 0xFFFFC44D : 0xFFFF5A5A));
        Gui.drawRect(barX, barY, barX + fillWidth, barY + BOSS_BAR_HEIGHT, healthColor);

        if (healthRatio <= 0.25f && healthRatio > 0f) {
            float pulse = 0.4f + 0.4f * (float) Math.abs(Math.sin(animationTime * 0.018));
            Gui.drawRect(barX, barY, barX + BOSS_BAR_WIDTH, barY + BOSS_BAR_HEIGHT, ((int)(pulse * 180) << 24) | 0xFF2020);
        }

        mc.fontRenderer.drawString(hpText, panelX + panelWidth / 2 - mc.fontRenderer.getStringWidth(hpText) / 2, barY + BOSS_BAR_HEIGHT + 2, HUD_TEXT_SECONDARY);
    }

    @SuppressWarnings("null")
    private void renderSoftAimIndicator(Entity target, ScaledResolution resolution) {
        if (mc.player == null || mc.getRenderViewEntity() == null) return;

        int cx = resolution.getScaledWidth() / 2;
        int cy = resolution.getScaledHeight() / 2;

        // Project target to screen-space direction from crosshair
        net.minecraft.util.math.Vec3d look = mc.player.getLookVec();
        double dx = target.posX - mc.player.posX;
        double dy = (target.posY + target.height * 0.5) - (mc.player.posY + mc.player.getEyeHeight());
        double dz = target.posZ - mc.player.posZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) return;
        dx /= len; dy /= len; dz /= len;

        // Dot product — how aligned is the look direction with the target
        double dot = look.x * dx + look.y * dy + look.z * dz;
        if (dot < 0.2) return; // Target is behind or too far off-axis

        // Cross product to get left/right and up/down offset direction
        double crossX = look.z * dy - look.y * dz;
        double crossY = look.x * dz - look.z * dx;

        // Scale nudge: max 12px, fades when nearly aligned
        float nudge = (float) Math.min(12.0, (1.0 - dot) * 80.0);
        int nx = cx + (int)(crossX * nudge * -1);
        int ny = cy + (int)(crossY * nudge);

        // Draw a small triangle arrow pointing from crosshair toward target
        float alpha = (float) Math.min(1.0, (1.0 - dot) * 4.0);
        int a = (int)(alpha * 200);
        int color = (a << 24) | 0xFFFFAA;

        // Simple 5px triangle pointing toward target offset
        int size = 4;
        Gui.drawRect(nx - 1, ny - size, nx + 1, ny + size, color);
        Gui.drawRect(nx - size, ny - 1, nx + size, ny + 1, color);
    }

    @SuppressWarnings("null")
    private void renderHistoryRing(Entity entity, float partialTicks) {
        GlStateManager.pushMatrix();

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
            - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
            - mc.getRenderManager().viewerPosY + entity.height + 0.35;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
            - mc.getRenderManager().viewerPosZ;

        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(0.8f, 0.8f, 1.0f, 0.35f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        int segments = 20;
        float r = 0.3f;
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            buffer.pos(Math.cos(angle) * r, Math.sin(angle) * r, 0).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("null")
    private void renderRedIndicator(Entity target, float partialTicks) {

        GlStateManager.pushMatrix();

        // Calculate target position
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks;

        
        // Offset for camera position
        x -= mc.getRenderManager().viewerPosX;
        y -= mc.getRenderManager().viewerPosY;
        z -= mc.getRenderManager().viewerPosZ;
        
        // Position indicator above target's head
        y += target.height + 0.8;
        
        // Apply billboard transformation to face the player (like name tags)
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        
        // Disable depth test and texture, enable blending
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        // Animation pulse
        float pulse = (float) (0.8 + 0.2 * Math.sin(animationTime * 0.008));
        float size = 0.4f * pulse;
        
        // Set bright red color with good alpha
        GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f);
        
        // Draw animated red downward-pointing triangle (filled)
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        
        // Triangle pointing DOWN (towards the target) - fixed coordinates
        buffer.pos(0, -size * 0.5, 0).endVertex();          // Bottom point (pointing down)
        buffer.pos(-size, size * 0.5, 0).endVertex();       // Top left
        buffer.pos(size, size * 0.5, 0).endVertex();        // Top right
        
        tessellator.draw();
        
        // Draw black outline for better visibility
        GlStateManager.color(0.0f, 0.0f, 0.0f, 1.0f);
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        buffer.pos(0, -size * 0.5, 0).endVertex();          // Bottom point (pointing down)
        buffer.pos(-size, size * 0.5, 0).endVertex();       // Top left
        buffer.pos(size, size * 0.5, 0).endVertex();        // Top right
        tessellator.draw();
        
        // Restore GL state
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }
    
}
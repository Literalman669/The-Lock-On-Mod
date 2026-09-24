package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.core.PresentationStatus;
import com.zeldatargeting.mod.client.presentation.core.NeonRingAppearance;
import com.zeldatargeting.mod.client.presentation.core.PresentationStyle;
import com.zeldatargeting.mod.client.presentation.core.RingGeometry;
import com.zeldatargeting.mod.client.presentation.core.TorusGeometry;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class TargetRingRenderer {
    private static final int SEGMENTS = 32;
    private static final int TUBE_SEGMENTS = 8;

    private final Minecraft minecraft;
    private boolean warned;

    public TargetRingRenderer() {
        minecraft = Minecraft.getMinecraft();
    }

    public void render(TargetPresentationSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        try {
            renderRing(snapshot);
        } catch (RuntimeException exception) {
            warn(exception);
        }
    }

    public void renderTargetMarker(TargetPresentationSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        try {
            renderMarker(snapshot);
        } catch (RuntimeException exception) {
            warn(exception);
        }
    }

    private void renderRing(TargetPresentationSnapshot snapshot) {
        PresentationStyle style = PresentationStyle.fromConfig();
        RingGeometry geometry = RingGeometry.create(
            snapshot.getWidth() * TargetingConfig.reticleScale,
            snapshot.getHeight() * TargetingConfig.reticleScale,
            snapshot.getDistance(),
            snapshot.getHealthRatio(),
            false
        );
        NeonRingAppearance appearance = NeonRingAppearance.from(geometry, style.getRingGlowStrength());
        float pulse = style.isReducedMotion() ? 1.0F : 1.0F + (geometry.getMotionScale() - 1.0F)
            * (float) Math.sin(System.currentTimeMillis() * 0.008D);
        double halfWidth = geometry.getHalfWidth() * pulse;
        double halfHeight = geometry.getHalfHeight() * pulse;
        double x = snapshot.getInterpolatedX() - minecraft.getRenderManager().viewerPosX;
        double y = snapshot.getInterpolatedY() - minecraft.getRenderManager().viewerPosY
            + snapshot.getHeight() * 0.5D;
        double z = snapshot.getInterpolatedZ() - minecraft.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

        int ringColor = style.getStatusColor(snapshot.getStatus());
            float alpha = geometry.getAlpha()
                * (snapshot.getStatus() == PresentationStatus.OCCLUDED ? 0.45F : 1.0F);
            drawNeonTorus(
                x, y, z, halfWidth, appearance, ringColor, alpha,
                -90.0D, geometry.getHealthArcDegrees()
            );
            color(0xFFFFFFFF, alpha);
            drawCornerBrackets(x, y, z, halfWidth, halfHeight);
        drawStatusMarker(x, y, z, halfWidth, snapshot.getStatus(), alpha, style);
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private void renderMarker(TargetPresentationSnapshot snapshot) {
        double x = snapshot.getInterpolatedX() - minecraft.getRenderManager().viewerPosX;
        double y = snapshot.getInterpolatedY() - minecraft.getRenderManager().viewerPosY
            + snapshot.getHeight() + 0.8D;
        double z = snapshot.getInterpolatedZ() - minecraft.getRenderManager().viewerPosZ;
        PresentationStyle style = PresentationStyle.fromConfig();
        float size = style.isReducedMotion()
            ? 0.4F
            : 0.4F * (0.9F + 0.1F * (float) Math.sin(System.currentTimeMillis() * 0.008D));

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
        color(style.getStatusColor(snapshot.getStatus()), 1.0F);

            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
            buffer.pos(0.0D, -size * 0.5D, 0.0D).endVertex();
            buffer.pos(-size, size * 0.5D, 0.0D).endVertex();
            buffer.pos(size, size * 0.5D, 0.0D).endVertex();
            Tessellator.getInstance().draw();
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private static void drawCircle(double x, double y, double z, double radius) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int index = 0; index < SEGMENTS; index++) {
            double angle = 2.0D * Math.PI * index / SEGMENTS;
            buffer.pos(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius).endVertex();
        }
        Tessellator.getInstance().draw();
    }

    private static void drawNeonTorus(
            double x, double y, double z, double radius, NeonRingAppearance appearance,
            int ringColor, float alpha, double startDegrees, double spanDegrees) {
        if (appearance == null || spanDegrees <= 0.0D) {
            return;
        }
        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE
        );
        drawTorus(
            x, y, z, radius, appearance.getAuraHalfThickness(), ringColor,
            alpha * appearance.getAuraAlphaMultiplier(), 0.18F, startDegrees, spanDegrees
        );

        GlStateManager.blendFunc(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        drawTorus(
            x, y, z, radius, appearance.getCoreHalfThickness(), ringColor,
            alpha * appearance.getCoreAlphaMultiplier(), 0.78F, startDegrees, spanDegrees
        );
    }

    private static void drawTorus(
            double x, double y, double z, double radius, double tubeRadius, int ringColor,
            float alpha, float highlightStrength,
            double startDegrees, double spanDegrees) {
        double safeStart = finiteOr(startDegrees, 0.0D);
        double safeSpan = Math.max(0.0D, Math.min(360.0D, finiteOr(spanDegrees, 0.0D)));
        if (safeSpan <= 0.0D) {
            return;
        }

        TorusGeometry geometry = TorusGeometry.create(radius, tubeRadius);
        int segmentCount = Math.max(1, (int) Math.round(SEGMENTS * safeSpan / 360.0D));
        float safeAlpha = Math.max(0.0F, Math.min(1.0F, alpha));
        float safeHighlightStrength = Math.max(0.0F, Math.min(1.0F, highlightStrength));
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int majorIndex = 0; majorIndex < segmentCount; majorIndex++) {
            double majorAngle = Math.toRadians(safeStart + safeSpan * majorIndex / segmentCount);
            double nextMajorAngle = Math.toRadians(safeStart + safeSpan * (majorIndex + 1) / segmentCount);
            for (int tubeIndex = 0; tubeIndex < TUBE_SEGMENTS; tubeIndex++) {
                double tubeAngle = 2.0D * Math.PI * tubeIndex / TUBE_SEGMENTS;
                double nextTubeAngle = 2.0D * Math.PI * (tubeIndex + 1) / TUBE_SEGMENTS;
                addTorusVertex(
                    buffer, x, y, z, geometry, majorAngle, tubeAngle,
                    ringColor, safeAlpha, safeHighlightStrength
                );
                addTorusVertex(
                    buffer, x, y, z, geometry, majorAngle, nextTubeAngle,
                    ringColor, safeAlpha, safeHighlightStrength
                );
                addTorusVertex(
                    buffer, x, y, z, geometry, nextMajorAngle, nextTubeAngle,
                    ringColor, safeAlpha, safeHighlightStrength
                );
                addTorusVertex(
                    buffer, x, y, z, geometry, nextMajorAngle, tubeAngle,
                    ringColor, safeAlpha, safeHighlightStrength
                );
            }
        }
        Tessellator.getInstance().draw();
    }

    private static void addTorusVertex(
            BufferBuilder buffer, double x, double y, double z, TorusGeometry geometry,
            double majorAngle, double tubeAngle, int ringColor, float alpha, float highlightStrength) {
        double radialDistance = geometry.getRadialDistance(tubeAngle);
        double topFacing = 0.5D + 0.5D * Math.sin(tubeAngle);
        int vertexColor = mixColor(
            scaleColor(ringColor, (float) (0.55D + 0.45D * topFacing)),
            0xFFFFFFFF,
            highlightStrength * (float) topFacing
        );
        buffer.pos(
            x + Math.cos(majorAngle) * radialDistance,
            y + geometry.getVerticalOffset(tubeAngle),
            z + Math.sin(majorAngle) * radialDistance
        ).color(
            (vertexColor >>> 16) & 0xFF,
            (vertexColor >>> 8) & 0xFF,
            vertexColor & 0xFF,
            Math.round(alpha * 255.0F)
        ).endVertex();
    }

    private static void drawCornerBrackets(
            double x, double y, double z, double halfWidth, double halfHeight) {
        double bracketSize = Math.max(0.15D, halfWidth * 0.28D);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        addBracket(buffer, x - halfWidth, y + halfHeight, z, bracketSize, 1.0D, -1.0D);
        addBracket(buffer, x + halfWidth, y + halfHeight, z, bracketSize, -1.0D, -1.0D);
        addBracket(buffer, x - halfWidth, y - halfHeight, z, bracketSize, 1.0D, 1.0D);
        addBracket(buffer, x + halfWidth, y - halfHeight, z, bracketSize, -1.0D, 1.0D);
        Tessellator.getInstance().draw();
    }

    private static void addBracket(
            BufferBuilder buffer, double x, double y, double z, double size, double xDirection, double yDirection) {
        buffer.pos(x, y, z).endVertex();
        buffer.pos(x + size * xDirection, y, z).endVertex();
        buffer.pos(x, y, z).endVertex();
        buffer.pos(x, y + size * yDirection, z).endVertex();
    }

    private static void drawStatusMarker(
            double x, double y, double z, double halfWidth, PresentationStatus status, float alpha,
            PresentationStyle style) {
        if (status == PresentationStatus.LETHAL) {
            drawDiamondMarker(x, y + 0.02D, z, halfWidth * 0.26D,
                style.getStatusColor(status), alpha);
        } else if (status == PresentationStatus.LOW_HEALTH) {
            drawDiamondMarker(x, y + 0.02D, z, Math.max(0.12D, halfWidth * 0.22D),
                style.getStatusColor(status), alpha);
        }
    }

    private static void drawDiamondMarker(
            double x, double y, double z, double size, int markerColor, float alpha) {
        color(markerColor, alpha);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        buffer.pos(x, y, z - size).endVertex();
        buffer.pos(x + size, y, z).endVertex();
        buffer.pos(x, y, z + size).endVertex();
        buffer.pos(x - size, y, z).endVertex();
        Tessellator.getInstance().draw();
    }

    private static int mixColor(int source, int target, float targetWeight) {
        float weight = Math.max(0.0F, Math.min(1.0F, targetWeight));
        int red = mixChannel(source >>> 16, target >>> 16, weight);
        int green = mixChannel(source >>> 8, target >>> 8, weight);
        int blue = mixChannel(source, target, weight);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int scaleColor(int source, float brightness) {
        float safeBrightness = Math.max(0.0F, Math.min(1.0F, brightness));
        int red = Math.round(((source >>> 16) & 0xFF) * safeBrightness);
        int green = Math.round(((source >>> 8) & 0xFF) * safeBrightness);
        int blue = Math.round((source & 0xFF) * safeBrightness);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int mixChannel(int source, int target, float targetWeight) {
        int sourceChannel = source & 0xFF;
        int targetChannel = target & 0xFF;
        return Math.round(sourceChannel + (targetChannel - sourceChannel) * targetWeight);
    }

    private static double finiteOr(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }

    private static void color(int argb, float alphaMultiplier) {
        float alpha = ((argb >>> 24) & 0xFF) / 255.0F * alphaMultiplier;
        GlStateManager.color(
            ((argb >>> 16) & 0xFF) / 255.0F,
            ((argb >>> 8) & 0xFF) / 255.0F,
            (argb & 0xFF) / 255.0F,
            alpha
        );
    }

    private void warn(RuntimeException exception) {
        if (!warned && ZeldaTargetingMod.getLogger() != null) {
            warned = true;
            ZeldaTargetingMod.getLogger().warn("Target ring renderer skipped a frame", exception);
        }
    }
}

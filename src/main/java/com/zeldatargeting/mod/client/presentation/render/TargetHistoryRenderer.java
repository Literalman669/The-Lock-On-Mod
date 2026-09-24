package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.core.RecentTargetHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class TargetHistoryRenderer {
    private final Minecraft minecraft;
    private final RecentTargetHistory<Entity> targetHistory = new RecentTargetHistory<>(3);
    private boolean warned;

    public TargetHistoryRenderer() {
        minecraft = Minecraft.getMinecraft();
    }

    public void track(Entity target) {
        try {
            removeExpiredTargets();
            targetHistory.track(target);
        } catch (RuntimeException exception) {
            warn(exception);
        }
    }

    public void clear() {
        targetHistory.clear();
    }

    public void render(float partialTicks) {
        try {
            removeExpiredTargets();
            for (Entity entity : targetHistory.entries()) {
                renderRing(entity, partialTicks);
            }
        } catch (RuntimeException exception) {
            warn(exception);
        }
    }

    private void renderRing(Entity entity, float partialTicks) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
            - minecraft.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
            - minecraft.getRenderManager().viewerPosY + entity.height + 0.35D;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
            - minecraft.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            GlStateManager.color(0.8F, 0.8F, 1.0F, 0.35F);
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            for (int index = 0; index < 20; index++) {
                double angle = 2.0D * Math.PI * index / 20.0D;
                buffer.pos(Math.cos(angle) * 0.3D, Math.sin(angle) * 0.3D, 0.0D).endVertex();
            }
            Tessellator.getInstance().draw();
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private void warn(RuntimeException exception) {
        if (!warned && ZeldaTargetingMod.getLogger() != null) {
            warned = true;
            ZeldaTargetingMod.getLogger().warn("Target history renderer skipped a frame", exception);
        }
    }

    private void removeExpiredTargets() {
        targetHistory.removeIf(entity -> entity == null || !entity.isEntityAlive());
    }
}

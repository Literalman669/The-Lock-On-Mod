package com.zeldatargeting.mod.client.render;

import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@SideOnly(Side.CLIENT)
public class DamageNumbersRenderer {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();
    private static final ConcurrentLinkedQueue<DamageNumber> damageNumbers = new ConcurrentLinkedQueue<>();

    public static class DamageNumber {
        public final Entity entity;
        public final double startX, startY, startZ;
        public double x, y, z;
        public final float damage;
        public final int color;
        public final boolean isCritical;
        public final boolean isLethal;
        public final boolean isHeal;
        public final DamageType damageType;
        public final int maxAge;
        public int age;
        public float scale;
        public final float velocityY;
        public final float velocityX;
        public final float velocityZ;
        public final float rotationSpeed;
        public float rotation;

        public enum DamageType {
            NORMAL, CRITICAL, LETHAL, HEAL, SHIELD_BLOCKED, ARMOR_REDUCED
        }

        public DamageNumber(Entity entity, float damage, boolean isCritical, boolean isLethal) {
            this(entity, damage, isCritical, isLethal, false, DamageType.NORMAL);
        }

        public DamageNumber(Entity entity, float damage, boolean isCritical, boolean isLethal, boolean isHeal, DamageType type) {
            this.entity = entity;
            this.damage = Math.abs(damage);
            this.isCritical = isCritical;
            this.isLethal = isLethal && !isHeal;
            this.isHeal = isHeal;
            this.damageType = type;
            this.maxAge = TargetingConfig.damageNumbersDuration + (isCritical ? 20 : 0);
            this.age = 0;
            this.scale = TargetingConfig.damageNumbersScale * (isCritical ? 1.3f : 1.0f);
            this.rotation = 0.0f;
            this.rotationSpeed = (random.nextFloat() - 0.5f) * 2.0f;

            double offsetX = (random.nextFloat() - 0.5f) * 0.3f;
            double offsetZ = (random.nextFloat() - 0.5f) * 0.3f;
            this.startX = entity.posX + offsetX;
            this.startY = entity.posY + entity.height + TargetingConfig.damageNumbersOffset + random.nextFloat() * 0.2f;
            this.startZ = entity.posZ + offsetZ;
            this.x = startX;
            this.y = startY;
            this.z = startZ;

            float motionScale;
            switch (TargetingConfig.damageNumbersMotion.toLowerCase()) {
                case "subtle":  motionScale = 0.4f; break;
                case "arcade":  motionScale = 2.2f; break;
                default:        motionScale = 1.0f; break;
            }
            if (isLethal) {
                this.velocityY = (0.04f + random.nextFloat() * 0.02f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.02f * motionScale;
                this.velocityZ = (random.nextFloat() - 0.5f) * 0.01f * motionScale;
            } else if (isCritical) {
                this.velocityY = (0.03f + random.nextFloat() * 0.015f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.015f * motionScale;
                this.velocityZ = (random.nextFloat() - 0.5f) * 0.008f * motionScale;
            } else if (isHeal) {
                this.velocityY = (0.015f + random.nextFloat() * 0.01f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.005f * motionScale;
                this.velocityZ = (random.nextFloat() - 0.5f) * 0.005f * motionScale;
            } else {
                this.velocityY = (0.02f + random.nextFloat() * 0.01f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.01f * motionScale;
                this.velocityZ = (random.nextFloat() - 0.5f) * 0.006f * motionScale;
            }

            this.color = determineColor();
        }

        private int determineColor() {
            if (!TargetingConfig.damageNumbersColors) {
                return TargetingConfig.damageNumbersColor;
            }
            if (isHeal) {
                return 0xFF00FF88;
            } else if (isLethal) {
                return TargetingConfig.lethalDamageColor;
            } else if (isCritical && TargetingConfig.damageNumbersCrits) {
                return TargetingConfig.criticalDamageColor;
            } else {
                if (damage >= 10.0f) return 0xFFFF6666;
                else if (damage >= 5.0f) return 0xFFFFAA66;
                else if (damage >= 2.0f) return 0xFFFFDD66;
                else return 0xFFCCCCCC;
            }
        }

        public void update() {
            age++;
            y += velocityY;
            x += velocityX * Math.sin(age * 0.1);
            if (isCritical && TargetingConfig.damageNumbersCrits) {
                if (TargetingConfig.critEmphasis && age < 12) {
                    // Stronger pop: scale surges then settles
                    float popFactor = age < 6
                        ? 1.0f + 0.8f * ((float) age / 6.0f)
                        : 1.8f - 0.8f * ((float)(age - 6) / 6.0f);
                    scale = TargetingConfig.damageNumbersScale * popFactor;
                } else if (!TargetingConfig.critEmphasis && age < 10) {
                    scale = TargetingConfig.damageNumbersScale * (1.0f + 0.5f * (float) Math.sin(age * 0.5));
                }
            }
        }

        public boolean shouldRemove() {
            return age >= maxAge;
        }

        public float getAlpha() {
            if (!TargetingConfig.damageNumbersFadeOut) return 1.0f;
            if (age < maxAge * 0.7f) return 1.0f;
            float fadeProgress = (age - maxAge * 0.7f) / (maxAge * 0.3f);
            return 1.0f - fadeProgress;
        }

        public String getText() {
            if (isLethal) return "FATAL!";
            else if (damage == (int) damage) return String.valueOf((int) damage);
            else return String.format("%.1f", damage);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!TargetingConfig.enableDamageNumbers || mc.world == null || mc.player == null) return;

        EntityLivingBase entity = event.getEntityLiving();
        float damage = event.getAmount();

        TargetingManager manager = TargetingManager.getInstance();
        boolean isTargeted = manager != null && manager.getCurrentTarget() == entity;
        boolean isNearby = mc.player.getDistanceSq(entity) < 100;

        if (!isTargeted && !isNearby) return;

        boolean isCritical = damage > entity.getMaxHealth() * 0.3f || random.nextFloat() < 0.1f;
        boolean isLethal = entity.getHealth() - damage <= 0;

        damageNumbers.add(new DamageNumber(entity, damage, isCritical, isLethal));
        while (damageNumbers.size() > 50) damageNumbers.poll();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!TargetingConfig.enableDamageNumbers || damageNumbers.isEmpty()) return;

        float partialTicks = event.getPartialTicks();
        Iterator<DamageNumber> iterator = damageNumbers.iterator();
        while (iterator.hasNext()) {
            DamageNumber dn = iterator.next();
            dn.update();
            if (dn.shouldRemove()) {
                damageNumbers.remove(dn);
                continue;
            }
            renderDamageNumber(dn, partialTicks);
        }
    }

    private void renderDamageNumber(DamageNumber dn, float partialTicks) {
        double x = dn.x - mc.getRenderManager().viewerPosX;
        double y = dn.y - mc.getRenderManager().viewerPosY;
        double z = dn.z - mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = dn.scale * 0.025f;
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        FontRenderer fontRenderer = mc.fontRenderer;
        String text = dn.getText();
        int textWidth = fontRenderer.getStringWidth(text);

        float alpha = dn.getAlpha();
        int color = dn.color;
        int finalColor = ((int)(alpha * 255) << 24) | (color & 0x00FFFFFF);

        if (alpha > 0.1f) {
            int bgAlpha = (int)(alpha * 128);
            drawRect(-textWidth / 2 - 2, -4, textWidth / 2 + 2, 8, (bgAlpha << 24));
        }

        fontRenderer.drawString(text, -textWidth / 2, 0, finalColor);

        if (dn.isCritical && TargetingConfig.damageNumbersCrits && dn.age < 20) {
            if (TargetingConfig.critEmphasis && dn.age < 12) {
                // Alternate between crit color and white for a flash effect
                int flashColor = (dn.age % 4 < 2) ? TargetingConfig.criticalDamageColor : 0xFFFFFF;
                int flashFinal = ((int)(alpha * 255) << 24) | (flashColor & 0x00FFFFFF);
                fontRenderer.drawString("*", -textWidth / 2 - 8, -4, flashFinal);
                fontRenderer.drawString("*", textWidth / 2 + 2, -4, flashFinal);
            } else if (dn.age % 4 < 2) {
                fontRenderer.drawString("!", -textWidth / 2 - 10, -2, finalColor);
                fontRenderer.drawString("!", textWidth / 2 + 6, -2, finalColor);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        if (left > right) { int t = left; left = right; right = t; }
        if (top > bottom) { int t = top; top = bottom; bottom = t; }

        float alpha = (float)(color >>> 24) / 255.0F;
        float red   = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8  & 255) / 255.0F;
        float blue  = (float)(color       & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,       GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, alpha);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(left,  bottom, 0.0D).endVertex();
        buffer.pos(right, bottom, 0.0D).endVertex();
        buffer.pos(right, top,    0.0D).endVertex();
        buffer.pos(left,  top,    0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void clearAll() {
        damageNumbers.clear();
    }

    public static int getActiveCount() {
        return damageNumbers.size();
    }
}

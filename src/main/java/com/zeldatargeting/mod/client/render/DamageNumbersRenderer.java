package com.zeldatargeting.mod.client.render;

import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.client.combat.DamageCalculator;
import com.zeldatargeting.mod.client.combat.DamageFormula;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Floating damage numbers.
 * <p>
 * Hits are detected from health changes on the client's own entities, which
 * the server syncs to every nearby player, so numbers work in singleplayer and
 * on dedicated servers alike. Everything here runs on the client thread.
 */
@SideOnly(Side.CLIENT)
public class DamageNumbersRenderer {

    private static final double TRACKING_RANGE = 10.0D;
    private static final int MAX_NUMBERS = 50;
    // Server health updates can trail the local swing by a few ticks
    private static final int CRIT_ATTRIBUTION_TICKS = 10;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    private final ArrayDeque<DamageNumber> damageNumbers = new ArrayDeque<>();
    private Map<Integer, Float> lastHealth = new HashMap<>();
    private Map<Integer, Float> currentHealth = new HashMap<>();
    private long clientTicks;
    private int critTargetId = -1;
    private long critAttackTick;

    public static class DamageNumber {
        public double prevX, prevY, prevZ;
        public double x, y, z;
        public final float damage;
        public final int color;
        public final boolean isCritical;
        public final boolean isLethal;
        public final int maxAge;
        public int age;
        public float scale;
        public final float velocityY;
        public final float velocityX;

        public DamageNumber(Entity entity, float damage, boolean isCritical, boolean isLethal) {
            this.damage = Math.abs(damage);
            this.isCritical = isCritical;
            this.isLethal = isLethal;
            this.maxAge = TargetingConfig.damageNumbersDuration + (isCritical ? 20 : 0);
            this.age = 0;
            this.scale = TargetingConfig.damageNumbersScale * (isCritical ? 1.3f : 1.0f);

            double offsetX = (random.nextFloat() - 0.5f) * 0.3f;
            double offsetZ = (random.nextFloat() - 0.5f) * 0.3f;
            this.x = entity.posX + offsetX;
            this.y = entity.posY + entity.height + TargetingConfig.damageNumbersOffset + random.nextFloat() * 0.2f;
            this.z = entity.posZ + offsetZ;
            this.prevX = x;
            this.prevY = y;
            this.prevZ = z;

            float motionScale;
            switch (TargetingConfig.damageNumbersMotion.toLowerCase()) {
                case "subtle":  motionScale = 0.4f; break;
                case "arcade":  motionScale = 2.2f; break;
                default:        motionScale = 1.0f; break;
            }
            if (isLethal) {
                this.velocityY = (0.04f + random.nextFloat() * 0.02f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.02f * motionScale;
            } else if (isCritical) {
                this.velocityY = (0.03f + random.nextFloat() * 0.015f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.015f * motionScale;
            } else {
                this.velocityY = (0.02f + random.nextFloat() * 0.01f) * motionScale;
                this.velocityX = (random.nextFloat() - 0.5f) * 0.01f * motionScale;
            }

            this.color = determineColor();
        }

        private int determineColor() {
            if (!TargetingConfig.damageNumbersColors) {
                return TargetingConfig.damageNumbersColor;
            }
            if (isLethal) {
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

        /** Advances one client tick. */
        public void update() {
            age++;
            prevX = x;
            prevY = y;
            prevZ = z;
            y += velocityY;
            x += velocityX * Math.sin(age * 0.1);
            if (isCritical && TargetingConfig.damageNumbersCrits && !TargetingConfig.reducedMotion) {
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
    public void onAttackEntity(AttackEntityEvent event) {
        // Fires on both sides in singleplayer; only the local client swing counts.
        EntityPlayer player = event.getEntityPlayer();
        if (player != mc.player || !player.world.isRemote) return;

        // The attack cooldown has not been reset yet, so this matches the real hit.
        if (DamageFormula.isCritical(player.getCooledAttackStrength(0.5f), DamageCalculator.canCriticalHit(player))) {
            critTargetId = event.getTarget().getEntityId();
            critAttackTick = clientTicks;
        } else {
            critTargetId = -1;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!TargetingConfig.enableDamageNumbers || mc.world == null || mc.player == null) {
            reset();
            return;
        }
        if (mc.isGamePaused()) return;

        clientTicks++;
        Iterator<DamageNumber> iterator = damageNumbers.iterator();
        while (iterator.hasNext()) {
            DamageNumber dn = iterator.next();
            dn.update();
            if (dn.shouldRemove()) {
                iterator.remove();
            }
        }
        trackHealth();
    }

    private void trackHealth() {
        double rangeSq = TRACKING_RANGE * TRACKING_RANGE;
        for (EntityLivingBase entity : mc.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                mc.player.getEntityBoundingBox().grow(TRACKING_RANGE))) {
            if (mc.player.getDistanceSq(entity) < rangeSq) {
                observe(entity);
            }
        }
        TargetingManager manager = TargetingManager.getInstance();
        Entity target = manager == null ? null : manager.getCurrentTarget();
        if (target instanceof EntityLivingBase && !currentHealth.containsKey(target.getEntityId())) {
            observe((EntityLivingBase) target);
        }

        // Entities that left range are forgotten, so re-entering never spawns a number.
        Map<Integer, Float> previous = lastHealth;
        lastHealth = currentHealth;
        currentHealth = previous;
        currentHealth.clear();
    }

    private void observe(EntityLivingBase entity) {
        int id = entity.getEntityId();
        float health = entity.getHealth();
        currentHealth.put(id, health);
        Float previous = lastHealth.get(id);
        if (previous == null || health >= previous) return;

        boolean isLethal = health <= 0.0f;
        boolean isCritical = id == critTargetId && clientTicks - critAttackTick <= CRIT_ATTRIBUTION_TICKS;
        if (isCritical) {
            critTargetId = -1;
        }

        damageNumbers.add(new DamageNumber(entity, previous - health, isCritical, isLethal));
        while (damageNumbers.size() > MAX_NUMBERS) damageNumbers.poll();

        TargetingManager manager = TargetingManager.getInstance();
        if (manager != null) {
            manager.getPresentationFeedbackController().onDamage(isCritical, isLethal);
        }
    }

    private void reset() {
        damageNumbers.clear();
        lastHealth.clear();
        currentHealth.clear();
        critTargetId = -1;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!TargetingConfig.enableDamageNumbers || damageNumbers.isEmpty()) return;

        float partialTicks = event.getPartialTicks();
        for (DamageNumber dn : damageNumbers) {
            renderDamageNumber(dn, partialTicks);
        }
    }

    private void renderDamageNumber(DamageNumber dn, float partialTicks) {
        double x = dn.prevX + (dn.x - dn.prevX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = dn.prevY + (dn.y - dn.prevY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = dn.prevZ + (dn.z - dn.prevZ) * partialTicks - mc.getRenderManager().viewerPosZ;

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
            // Reduced motion keeps the crit markers but stops them flashing
            boolean steady = TargetingConfig.reducedMotion;
            if (TargetingConfig.critEmphasis && dn.age < 12) {
                // Alternate between crit color and white for a flash effect
                int flashColor = (steady || dn.age % 4 < 2) ? TargetingConfig.criticalDamageColor : 0xFFFFFF;
                int flashFinal = ((int)(alpha * 255) << 24) | (flashColor & 0x00FFFFFF);
                fontRenderer.drawString("*", -textWidth / 2 - 8, -4, flashFinal);
                fontRenderer.drawString("*", textWidth / 2 + 2, -4, flashFinal);
            } else if (steady || dn.age % 4 < 2) {
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
}

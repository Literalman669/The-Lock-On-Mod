package com.zeldatargeting.mod.client.diagnostics;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.camera.compat.ShoulderCameraState;
import com.zeldatargeting.mod.client.math.CameraMath;
import com.zeldatargeting.mod.client.math.TargetingMath;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

/**
 * While Compatibility Debug Output is on, logs each swing made during a lock:
 * how far the view points off the target, the distance to its hitbox, both
 * entities' speeds, what the crosshair ray hit, and the camera and combat mode.
 * About 15 ticks later (long enough for Epic Fight's attack wind-up) it logs
 * whether the target actually lost health.
 */
public final class AttackAimDiagnostics {
    private static final String PREFIX = "[ZT Aim] ";
    private static final int RESULT_DELAY_TICKS = 15;

    private final Minecraft minecraft;
    private boolean attackWasDown;
    private int pendingTargetId = -1;
    private float pendingHealth;
    private int ticksUntilResult;
    private int swingNumber;

    public AttackAimDiagnostics() {
        minecraft = Minecraft.getMinecraft();
    }

    /** Call once per client tick with the locked target, or null without a lock. */
    public void onClientTick(EntityLivingBase target) {
        EntityPlayer player = minecraft.player;
        boolean attackDown = minecraft.gameSettings.keyBindAttack.isKeyDown()
            && minecraft.currentScreen == null;
        boolean pressed = attackDown && !attackWasDown;
        attackWasDown = attackDown;
        if (!TargetingConfig.debugCompatibility || player == null || minecraft.world == null) {
            pendingTargetId = -1;
            return;
        }

        reportPendingResult();
        if (pressed && target != null) {
            logSwing(player, target);
        }
    }

    private void logSwing(EntityPlayer player, EntityLivingBase target) {
        swingNumber++;
        Vec3d eye = player.getPositionEyes(1.0F);
        AxisAlignedBB box = target.getEntityBoundingBox();
        double hitboxDistance = TargetingMath.distanceToBox(
            eye.x, eye.y, eye.z, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ
        );
        float bearing = CameraMath.lookAt(
            (box.minX + box.maxX) * 0.5D - eye.x,
            (box.minY + box.maxY) * 0.5D - eye.y,
            (box.minZ + box.maxZ) * 0.5D - eye.z
        ).getYaw();
        float yawError = MouseTurnAudit.wrappedDelta(bearing, player.rotationYaw);
        float halfWidthDegrees = (float) Math.toDegrees(Math.atan2(
            (box.maxX - box.minX) * 0.5D,
            Math.max(0.01D, Math.sqrt(Math.pow((box.minX + box.maxX) * 0.5D - eye.x, 2)
                + Math.pow((box.minZ + box.maxZ) * 0.5D - eye.z, 2)))
        ));

        RayTraceResult mouseOver = minecraft.objectMouseOver;
        String crosshair;
        if (mouseOver == null || mouseOver.typeOfHit == RayTraceResult.Type.MISS) {
            crosshair = "nothing";
        } else if (mouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
            Entity hit = mouseOver.entityHit;
            crosshair = hit == target ? "TARGET" : "other entity " + (hit == null ? "?" : hit.getName());
        } else {
            crosshair = "block";
        }

        ShoulderCameraState shoulder = ZeldaTargetingMod.getShoulderSurfingBridge().captureState();
        String camera = !shoulder.isActive() ? "vanilla"
            : shoulder.canCompensate() ? "SSR static (shoulder ray)" : "SSR dynamic (eye ray)";

        log(String.format(Locale.ROOT,
            "swing #%d at %s: hitbox %.2f blocks away, aim off by %+.1f deg (target spans +/-%.1f),"
                + " target %.1f b/s, you %.1f b/s%s, crosshair ray hit %s | %s | Epic Fight %s",
            swingNumber,
            target.getName(),
            hitboxDistance,
            yawError,
            halfWidthDegrees,
            horizontalSpeed(target),
            horizontalSpeed(player),
            player.isSprinting() ? " (sprinting)" : "",
            crosshair,
            camera,
            ZeldaTargetingMod.getEpicFightBridge().isBattleMode() ? "Battle" : "Mining/off"));

        pendingTargetId = target.getEntityId();
        pendingHealth = target.getHealth();
        ticksUntilResult = RESULT_DELAY_TICKS;
    }

    private void reportPendingResult() {
        if (pendingTargetId < 0 || --ticksUntilResult > 0) {
            return;
        }
        Entity entity = minecraft.world.getEntityByID(pendingTargetId);
        float health = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getHealth() : 0.0F;
        log(String.format(Locale.ROOT, "swing #%d result: %s (health %.1f -> %.1f)",
            swingNumber, health < pendingHealth ? "HIT" : "no damage", pendingHealth, health));
        pendingTargetId = -1;
    }

    private static double horizontalSpeed(Entity entity) {
        double dx = entity.posX - entity.lastTickPosX;
        double dz = entity.posZ - entity.lastTickPosZ;
        return Math.sqrt(dx * dx + dz * dz) * 20.0D;
    }

    private static void log(String message) {
        if (ZeldaTargetingMod.getLogger() != null) {
            ZeldaTargetingMod.getLogger().info(PREFIX + message);
        }
    }
}

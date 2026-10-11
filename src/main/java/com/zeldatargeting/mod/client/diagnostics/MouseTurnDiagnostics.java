package com.zeldatargeting.mod.client.diagnostics;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.Display;

import java.util.Locale;

/**
 * Logs where the player's view turning comes from while Compatibility Debug
 * Output is on. Each frame is split into three parts:
 * <ul>
 *   <li>between frames (game ticks, server packets, other mods' tick code),</li>
 *   <li>Zelda Targeting's own camera writes at the start of the frame,</li>
 *   <li>the rest of the frame, where vanilla applies the mouse.</li>
 * </ul>
 * Turning the mouse counts do not explain is logged with its part, rate-limited,
 * plus a summary every ten seconds.
 */
public final class MouseTurnDiagnostics {
    private static final String PREFIX = "[ZT Mouse] ";
    private static final long SUMMARY_INTERVAL_MILLIS = 10_000L;
    private static final long LINE_INTERVAL_MILLIS = 250L;
    private static final long SLOW_FRAME_MILLIS = 50L;
    private static final long UNSET = Long.MIN_VALUE;
    // Vanilla clamps pitch here, which would read as missing turn.
    private static final float PITCH_LIMIT_MARGIN = 89.0F;

    private final Minecraft minecraft;
    private boolean frameOpen;
    private boolean hasFrameEnd;
    private float frameEndYaw;
    private float frameEndPitch;
    private float afterModYaw;
    private float afterModPitch;
    private float modYaw;
    private float modPitch;
    private long frameStartedAtMillis = UNSET;
    private long lastLineMillis = UNSET;
    private long summaryStartedAtMillis = UNSET;
    private int frames;
    private long slowestFrameMillis;
    private int largestDeltaX;
    private int unexplainedFrameTurns;
    private int betweenFrameTurns;
    private float modTurnTotal;

    public MouseTurnDiagnostics() {
        minecraft = Minecraft.getMinecraft();
    }

    /** Call at the start of the render tick, before Zelda Targeting's camera code. */
    public void beforeModCamera(long nowMillis) {
        EntityPlayer player = minecraft.player;
        if (!TargetingConfig.debugCompatibility || player == null) {
            reset();
            return;
        }
        if (summaryStartedAtMillis == UNSET) {
            summaryStartedAtMillis = nowMillis;
            log(nowMillis, true, "diagnostics on | sensitivity " + minecraft.gameSettings.mouseSensitivity
                + " | invert " + minecraft.gameSettings.invertMouse
                + " | cinematic camera " + minecraft.gameSettings.smoothCamera);
        }
        if (hasFrameEnd) {
            float yawGap = MouseTurnAudit.wrappedDelta(frameEndYaw, player.rotationYaw);
            float pitchGap = player.rotationPitch - frameEndPitch;
            if (Math.abs(yawGap) > MouseTurnAudit.TOLERANCE_DEGREES
                    || Math.abs(pitchGap) > MouseTurnAudit.TOLERANCE_DEGREES) {
                betweenFrameTurns++;
                log(nowMillis, false, String.format(Locale.ROOT,
                    "turned between frames (ticks/packets/other mods): yaw %+.2f pitch %+.2f",
                    yawGap, pitchGap));
            }
        }
        if (frameStartedAtMillis != UNSET) {
            slowestFrameMillis = Math.max(slowestFrameMillis, nowMillis - frameStartedAtMillis);
        }
        frameStartedAtMillis = nowMillis;
        modYaw = player.rotationYaw;
        modPitch = player.rotationPitch;
        frameOpen = true;
    }

    /** Call at the start of the render tick, after Zelda Targeting's camera code. */
    public void afterModCamera() {
        EntityPlayer player = minecraft.player;
        if (!frameOpen || player == null) {
            return;
        }
        afterModYaw = player.rotationYaw;
        afterModPitch = player.rotationPitch;
        modTurnTotal += Math.abs(MouseTurnAudit.wrappedDelta(modYaw, afterModYaw))
            + Math.abs(afterModPitch - modPitch);
    }

    /** Call at the end of the render tick, after vanilla applied the mouse. */
    public void onFrameEnd(long nowMillis) {
        EntityPlayer player = minecraft.player;
        if (!frameOpen || player == null) {
            return;
        }
        frameOpen = false;
        frames++;
        long frameMillis = nowMillis - frameStartedAtMillis;

        // Vanilla only reads the mouse while the game has focus; otherwise no turn is expected.
        boolean mouseRead = minecraft.inGameHasFocus && Display.isActive();
        int deltaX = mouseRead ? minecraft.mouseHelper.deltaX : 0;
        int deltaY = mouseRead ? minecraft.mouseHelper.deltaY : 0;
        if (minecraft.gameSettings.invertMouse) {
            deltaY = -deltaY;
        }
        largestDeltaX = Math.max(largestDeltaX, Math.abs(deltaX));

        float yawChange = MouseTurnAudit.wrappedDelta(afterModYaw, player.rotationYaw);
        float pitchChange = player.rotationPitch - afterModPitch;
        boolean nearPitchLimit = Math.abs(player.rotationPitch) >= PITCH_LIMIT_MARGIN;
        String report = minecraft.gameSettings.smoothCamera
            ? null
            : MouseTurnAudit.unexplainedFrameTurn(
                deltaX,
                deltaY,
                yawChange,
                nearPitchLimit ? -deltaY * MouseTurnAudit.degreesPerCount(minecraft.gameSettings.mouseSensitivity)
                    : pitchChange,
                minecraft.gameSettings.mouseSensitivity
            );
        if (report != null) {
            unexplainedFrameTurns++;
            log(nowMillis, false, "unexplained turn during frame (" + frameMillis + " ms): " + report);
        } else if (frameMillis >= SLOW_FRAME_MILLIS && (deltaX != 0 || deltaY != 0)) {
            log(nowMillis, false, String.format(Locale.ROOT,
                "slow frame %d ms carried mouse dx=%d dy=%d in one step", frameMillis, deltaX, deltaY));
        }

        frameEndYaw = player.rotationYaw;
        frameEndPitch = player.rotationPitch;
        hasFrameEnd = true;

        if (nowMillis - summaryStartedAtMillis >= SUMMARY_INTERVAL_MILLIS) {
            log(nowMillis, true, String.format(Locale.ROOT,
                "10 s summary: %d frames, slowest %d ms, largest dx %d, unexplained frame turns %d,"
                    + " between-frame turns %d, Zelda camera turn %.1f deg",
                frames, slowestFrameMillis, largestDeltaX, unexplainedFrameTurns,
                betweenFrameTurns, modTurnTotal));
            summaryStartedAtMillis = nowMillis;
            frames = 0;
            slowestFrameMillis = 0L;
            largestDeltaX = 0;
            unexplainedFrameTurns = 0;
            betweenFrameTurns = 0;
            modTurnTotal = 0.0F;
        }
    }

    private void reset() {
        frameOpen = false;
        hasFrameEnd = false;
        frameStartedAtMillis = UNSET;
        summaryStartedAtMillis = UNSET;
        frames = 0;
        slowestFrameMillis = 0L;
        largestDeltaX = 0;
        unexplainedFrameTurns = 0;
        betweenFrameTurns = 0;
        modTurnTotal = 0.0F;
    }

    private void log(long nowMillis, boolean always, String message) {
        if (ZeldaTargetingMod.getLogger() == null) {
            return;
        }
        if (!always && lastLineMillis != UNSET && nowMillis - lastLineMillis < LINE_INTERVAL_MILLIS) {
            return;
        }
        if (!always) {
            lastLineMillis = nowMillis;
        }
        ZeldaTargetingMod.getLogger().info(PREFIX + message);
    }
}

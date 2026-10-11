package com.zeldatargeting.mod.client.diagnostics;

import java.util.Locale;

/**
 * Compares how far the view turned during a frame with how far vanilla's mouse
 * handling should have turned it, so turning that the mouse does not explain
 * can be traced to its source.
 */
public final class MouseTurnAudit {
    /** Differences below this many degrees are float rounding, not extra turning. */
    static final float TOLERANCE_DEGREES = 0.5F;

    private MouseTurnAudit() {
    }

    /** Degrees of turn per mouse count, as EntityRenderer.updateCameraAndRender computes it. */
    public static float degreesPerCount(float sensitivity) {
        float scaled = sensitivity * 0.6F + 0.2F;
        return scaled * scaled * scaled * 8.0F * 0.15F;
    }

    /** Shortest signed change from one yaw to another, in degrees. */
    public static float wrappedDelta(float fromDegrees, float toDegrees) {
        float delta = (toDegrees - fromDegrees) % 360.0F;
        if (delta >= 180.0F) {
            delta -= 360.0F;
        } else if (delta < -180.0F) {
            delta += 360.0F;
        }
        return delta;
    }

    /**
     * Describes turning during a frame beyond what the mouse counts explain, or
     * returns null when the mouse accounts for it.
     *
     * @param deltaY mouse counts with any invert-mouse already applied; positive
     *               counts lower the pitch, as in vanilla
     */
    public static String unexplainedFrameTurn(
            int deltaX,
            int deltaY,
            float yawChange,
            float pitchChange,
            float sensitivity) {
        float perCount = degreesPerCount(sensitivity);
        float expectedYaw = deltaX * perCount;
        float expectedPitch = -deltaY * perCount;
        float extraYaw = yawChange - expectedYaw;
        float extraPitch = pitchChange - expectedPitch;
        boolean yawOff = Math.abs(extraYaw) > TOLERANCE_DEGREES;
        boolean pitchOff = Math.abs(extraPitch) > TOLERANCE_DEGREES;
        if (!yawOff && !pitchOff) {
            return null;
        }
        return String.format(
            Locale.ROOT,
            "%s%s (mouse dx=%d dy=%d expected yaw %.2f pitch %.2f, got yaw %.2f pitch %.2f)",
            yawOff ? String.format(Locale.ROOT, "yaw %+.2f deg ", extraYaw) : "",
            pitchOff ? String.format(Locale.ROOT, "pitch %+.2f deg ", extraPitch) : "",
            deltaX,
            deltaY,
            expectedYaw,
            expectedPitch,
            yawChange,
            pitchChange
        ).trim();
    }
}

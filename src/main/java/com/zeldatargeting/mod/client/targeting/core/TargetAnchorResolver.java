package com.zeldatargeting.mod.client.targeting.core;

public final class TargetAnchorResolver {
    private static final double HITBOX_SAFETY_MARGIN_RATIO = 0.1D;

    private TargetAnchorResolver() {
    }

    public static TargetPoint resolve(
            TargetAnchor anchor,
            double centerX,
            double minimumY,
            double centerZ,
            double maximumY,
            double eyeHeight) {
        TargetAnchor resolved = anchor == null ? TargetAnchor.CENTER : anchor;
        double height = Math.max(0.0D, maximumY - minimumY);
        double y;
        switch (resolved) {
            case HEAD:
                y = Math.min(maximumY, minimumY + Math.max(0.0D, eyeHeight));
                break;
            case LOWER_BODY:
                y = minimumY + height * 0.25D;
                break;
            case CENTER:
            default:
                y = minimumY + height * 0.5D;
                break;
        }
        return new TargetPoint(centerX, y, centerZ);
    }

    public static double offsetWithinHitbox(
            double anchorY,
            double minimumY,
            double maximumY,
            double relativeOffset) {
        double lowerBound = Math.min(minimumY, maximumY);
        double upperBound = Math.max(minimumY, maximumY);
        double height = upperBound - lowerBound;
        if (height == 0.0D) {
            return lowerBound;
        }
        double safetyMargin = height * HITBOX_SAFETY_MARGIN_RATIO;
        double shifted = anchorY + relativeOffset * height;
        return Math.max(
            lowerBound + safetyMargin,
            Math.min(upperBound - safetyMargin, shifted)
        );
    }
}

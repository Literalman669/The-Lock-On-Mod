package com.zeldatargeting.mod.client.camera.vanilla;

import com.zeldatargeting.mod.client.camera.CameraProfileResolver;
import com.zeldatargeting.mod.client.camera.core.CameraProfile;
import com.zeldatargeting.mod.client.camera.core.CameraProfiles;
import com.zeldatargeting.mod.config.TargetingConfig;

public final class LegacyCameraProfileAdapter implements CameraProfileResolver {
    private static final float OVERRIDE_EPSILON = 0.001F;

    @Override
    public CameraProfile resolve(String profileName, int perspective) {
        CameraProfile base = CameraProfiles.fromName(profileName);
        float configuredSmoothness = TargetingConfig.cameraSmoothness;
        double halfLife = base.getRotationHalfLifeMillis();
        if (isFinite(configuredSmoothness)
                && Math.abs(configuredSmoothness - legacyDefault(base)) > OVERRIDE_EPSILON) {
            double boundedSmoothness = Math.max(0.0D, Math.min(1.0D, configuredSmoothness));
            halfLife = 35.0D + (1.0D - boundedSmoothness) * 215.0D;
        }
        if (TargetingConfig.perModeSmoothingEnabled && perspective == 0) {
            halfLife *= 5.0D / 3.0D;
        }

        float maxYaw = finiteNonNegativeOr(
            TargetingConfig.maxYawAdjustment,
            base.getMaxYawAdjustment()
        );
        float maxPitch = finiteNonNegativeOr(
            TargetingConfig.maxPitchAdjustment,
            base.getMaxPitchAdjustment()
        );
        double focusY = isFinite(TargetingConfig.cameraFocusYOffset)
            ? TargetingConfig.cameraFocusYOffset
            : base.getFocusYOffset();
        return base.withLegacyOverrides(halfLife, maxYaw, maxPitch, focusY);
    }

    private static float legacyDefault(CameraProfile profile) {
        if ("cinematic".equals(profile.getName())) {
            return 0.15F;
        }
        if ("snappy".equals(profile.getName())) {
            return 0.75F;
        }
        return 0.4F;
    }

    private static float finiteNonNegativeOr(float value, float fallback) {
        return isFinite(value) && value >= 0.0F ? value : fallback;
    }

    private static boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }
}

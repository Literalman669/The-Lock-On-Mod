package com.zeldatargeting.mod.client.camera.compat;

import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Read-only optional link to Shoulder Surfing Reloaded 2.9.x. SSR owns the
 * shoulder camera and crosshair; Zelda only samples its current ray geometry.
 */
public final class ShoulderSurfingBridge {
    private static final String INSTANCE_CLASS =
        "com.teamderpy.shouldersurfing.client.ShoulderInstance";
    private static final String RENDERER_CLASS =
        "com.teamderpy.shouldersurfing.client.ShoulderRenderer";
    private static final String CONFIG_CLASS =
        "com.teamderpy.shouldersurfing.config.Config";
    private static final String CROSSHAIR_TYPE_CLASS =
        "com.teamderpy.shouldersurfing.config.CrosshairType";

    private final Logger logger;
    private final Method getInstance;
    private final Method doShoulderSurfing;
    private final Method getOffsetX;
    private final Method getOffsetY;
    private final Method getOffsetZ;
    private final Method getRendererInstance;
    private final Method getCameraDistance;
    private final Object clientConfig;
    private final Method getCrosshairType;
    private final Method isDynamicCrosshair;
    private boolean operational;
    private boolean invocationWarningLogged;

    private ShoulderSurfingBridge(
            Logger logger,
            boolean operational,
            Method getInstance,
            Method doShoulderSurfing,
            Method getOffsetX,
            Method getOffsetY,
            Method getOffsetZ,
            Method getRendererInstance,
            Method getCameraDistance,
            Object clientConfig,
            Method getCrosshairType,
            Method isDynamicCrosshair) {
        this.logger = logger;
        this.operational = operational;
        this.getInstance = getInstance;
        this.doShoulderSurfing = doShoulderSurfing;
        this.getOffsetX = getOffsetX;
        this.getOffsetY = getOffsetY;
        this.getOffsetZ = getOffsetZ;
        this.getRendererInstance = getRendererInstance;
        this.getCameraDistance = getCameraDistance;
        this.clientConfig = clientConfig;
        this.getCrosshairType = getCrosshairType;
        this.isDynamicCrosshair = isDynamicCrosshair;
    }

    public static ShoulderSurfingBridge unavailable() {
        return new ShoulderSurfingBridge(
            null, false, null, null, null, null, null, null, null,
            null, null, null
        );
    }

    public static ShoulderSurfingBridge detect(boolean installed, Logger logger) {
        if (!installed) {
            return unavailable();
        }
        try {
            Class<?> instanceType = Class.forName(INSTANCE_CLASS);
            Class<?> rendererType = Class.forName(RENDERER_CLASS);
            Class<?> configType = Class.forName(CONFIG_CLASS);
            Class<?> crosshairType = Class.forName(CROSSHAIR_TYPE_CLASS);
            Method getInstance = instanceType.getMethod("getInstance");
            Field clientField = configType.getField("CLIENT");
            Object clientConfig = clientField.get(null);
            if (clientConfig == null) {
                throw new IllegalStateException("SSR client config has not initialized");
            }
            ShoulderSurfingBridge bridge = new ShoulderSurfingBridge(
                logger,
                true,
                getInstance,
                instanceType.getMethod("doShoulderSurfing"),
                instanceType.getMethod("getOffsetX"),
                instanceType.getMethod("getOffsetY"),
                instanceType.getMethod("getOffsetZ"),
                rendererType.getMethod("getInstance"),
                rendererType.getMethod("getCameraDistance"),
                clientConfig,
                clientConfig.getClass().getMethod("getCrosshairType"),
                crosshairType.getMethod("isDynamic")
            );
            if (logger != null) {
                logger.info("Shoulder Surfing Reloaded detected - shoulder-aware aim integration enabled");
            }
            return bridge;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            if (logger != null) {
                logger.warn(
                    "Shoulder Surfing Reloaded was detected but its 2.9.x camera state could not be linked",
                    failure
                );
            }
            return unavailable();
        }
    }

    public boolean isLoaded() {
        return operational;
    }

    public boolean isActive() {
        if (!operational) {
            return false;
        }
        try {
            return isActiveInternal();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            disableAfterInvocationFailure(failure);
            return false;
        }
    }

    public ShoulderCameraState captureState() {
        if (!operational) {
            return ShoulderCameraState.inactive();
        }
        try {
            Object instance = getInstance.invoke(null);
            if (!Boolean.TRUE.equals(doShoulderSurfing.invoke(instance))) {
                return ShoulderCameraState.inactive();
            }
            double offsetX = ((Number) getOffsetX.invoke(instance)).doubleValue();
            double offsetY = ((Number) getOffsetY.invoke(instance)).doubleValue();
            double offsetZ = ((Number) getOffsetZ.invoke(instance)).doubleValue();
            Object renderer = getRendererInstance.invoke(null);
            double cameraDistance = ((Number) getCameraDistance.invoke(renderer)).doubleValue();
            if (!isFinitePositive(cameraDistance)) {
                cameraDistance = Math.sqrt(
                    offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ
                );
            }
            Object crosshairType = getCrosshairType.invoke(clientConfig);
            boolean shoulderRay = !Boolean.TRUE.equals(
                isDynamicCrosshair.invoke(crosshairType)
            );
            return ShoulderCameraState.active(
                offsetX,
                offsetY,
                offsetZ,
                cameraDistance,
                shoulderRay
            );
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            disableAfterInvocationFailure(failure);
            return ShoulderCameraState.inactive();
        }
    }

    private boolean isActiveInternal() throws ReflectiveOperationException {
        Object instance = getInstance.invoke(null);
        return Boolean.TRUE.equals(doShoulderSurfing.invoke(instance));
    }

    private void disableAfterInvocationFailure(Throwable failure) {
        operational = false;
        if (!invocationWarningLogged && logger != null) {
            logger.warn(
                "Shoulder Surfing Reloaded compatibility failed and was disabled for this session",
                failure
            );
            invocationWarningLogged = true;
        }
    }

    private static boolean isFinitePositive(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value > 0.0D;
    }

}

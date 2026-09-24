package com.zeldatargeting.mod.client.camera.compat;

import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Contains every optional link to Shoulder Surfing Reloaded 2.9.x. During a
 * Zelda lock it centers SSR's runtime-only X/Y offset, leaving the saved SSR
 * configuration untouched and restoring the exact values afterward.
 */
public final class ShoulderSurfingBridge {
    private static final String INSTANCE_CLASS =
        "com.teamderpy.shouldersurfing.client.ShoulderInstance";
    private static final String RENDERER_CLASS =
        "com.teamderpy.shouldersurfing.client.ShoulderRenderer";
    private static final String CONFIG_CLASS =
        "com.teamderpy.shouldersurfing.config.Config";

    private final Logger logger;
    private final Method getInstance;
    private final Method doShoulderSurfing;
    private final Method getOffsetX;
    private final Method getOffsetY;
    private final Method getOffsetZ;
    private final Method getRendererInstance;
    private final Method getCameraDistance;
    private final ShoulderOffsetSession offsetSession = new ShoulderOffsetSession();
    private final ShoulderOffsetSession.OffsetAccess runtimeOffsets;
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
            ShoulderOffsetSession.OffsetAccess runtimeOffsets) {
        this.logger = logger;
        this.operational = operational;
        this.getInstance = getInstance;
        this.doShoulderSurfing = doShoulderSurfing;
        this.getOffsetX = getOffsetX;
        this.getOffsetY = getOffsetY;
        this.getOffsetZ = getOffsetZ;
        this.getRendererInstance = getRendererInstance;
        this.getCameraDistance = getCameraDistance;
        this.runtimeOffsets = runtimeOffsets;
    }

    public static ShoulderSurfingBridge unavailable() {
        return new ShoulderSurfingBridge(
            null, false, null, null, null, null, null, null, null, null
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
            Method getInstance = instanceType.getMethod("getInstance");
            Object instance = getInstance.invoke(null);
            Field clientField = configType.getField("CLIENT");
            Object clientConfig = clientField.get(null);
            Field configOffsetX = accessibleField(clientConfig.getClass(), "offsetX");
            Field configOffsetY = accessibleField(clientConfig.getClass(), "offsetY");
            RuntimeOffsetAccess runtimeOffsets = new RuntimeOffsetAccess(
                clientConfig,
                configOffsetX,
                configOffsetY,
                instance,
                accessibleField(instanceType, "offsetX"),
                accessibleField(instanceType, "lastOffsetX"),
                accessibleField(instanceType, "targetOffsetX"),
                accessibleField(instanceType, "offsetY"),
                accessibleField(instanceType, "lastOffsetY"),
                accessibleField(instanceType, "targetOffsetY")
            );
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
                runtimeOffsets
            );
            if (logger != null) {
                logger.info("Shoulder Surfing Reloaded detected - centered lock integration enabled");
            }
            return bridge;
        } catch (ReflectiveOperationException | RuntimeException failure) {
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
        } catch (ReflectiveOperationException | RuntimeException failure) {
            disableAfterInvocationFailure(failure);
            return false;
        }
    }

    public boolean beginCenteredLock() {
        if (!operational || runtimeOffsets == null) {
            return false;
        }
        try {
            return offsetSession.begin(isActiveInternal(), runtimeOffsets);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            disableAfterInvocationFailure(failure);
            return false;
        }
    }

    public void endCenteredLock() {
        if (!offsetSession.isActive()) {
            return;
        }
        try {
            offsetSession.end(runtimeOffsets);
        } catch (RuntimeException failure) {
            disableAfterInvocationFailure(failure);
        }
    }

    public boolean isLockCentered() {
        return offsetSession.isActive();
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
            return ShoulderCameraState.active(
                offsetX,
                offsetY,
                offsetZ,
                cameraDistance
            );
        } catch (ReflectiveOperationException | RuntimeException failure) {
            disableAfterInvocationFailure(failure);
            return ShoulderCameraState.inactive();
        }
    }

    private boolean isActiveInternal() throws ReflectiveOperationException {
        Object instance = getInstance.invoke(null);
        return Boolean.TRUE.equals(doShoulderSurfing.invoke(instance));
    }

    private void disableAfterInvocationFailure(Throwable failure) {
        try {
            offsetSession.end(runtimeOffsets);
        } catch (RuntimeException ignored) {
            // Preserve the original failure while still attempting restoration.
        }
        operational = false;
        if (!invocationWarningLogged && logger != null) {
            logger.warn(
                "Shoulder Surfing Reloaded compatibility failed and was disabled for this session",
                failure
            );
            invocationWarningLogged = true;
        }
    }

    private static Field accessibleField(Class<?> type, String name)
            throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static boolean isFinitePositive(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value > 0.0D;
    }

    private static final class RuntimeOffsetAccess
            implements ShoulderOffsetSession.OffsetAccess {
        private final Object clientConfig;
        private final Field configOffsetX;
        private final Field configOffsetY;
        private final Object instance;
        private final Field offsetX;
        private final Field lastOffsetX;
        private final Field targetOffsetX;
        private final Field offsetY;
        private final Field lastOffsetY;
        private final Field targetOffsetY;

        private RuntimeOffsetAccess(
                Object clientConfig,
                Field configOffsetX,
                Field configOffsetY,
                Object instance,
                Field offsetX,
                Field lastOffsetX,
                Field targetOffsetX,
                Field offsetY,
                Field lastOffsetY,
                Field targetOffsetY) {
            this.clientConfig = clientConfig;
            this.configOffsetX = configOffsetX;
            this.configOffsetY = configOffsetY;
            this.instance = instance;
            this.offsetX = offsetX;
            this.lastOffsetX = lastOffsetX;
            this.targetOffsetX = targetOffsetX;
            this.offsetY = offsetY;
            this.lastOffsetY = lastOffsetY;
            this.targetOffsetY = targetOffsetY;
        }

        @Override
        public ShoulderOffsetSession.Offset read() {
            return new ShoulderOffsetSession.Offset(
                readConfigValue(configOffsetX),
                readConfigValue(configOffsetY)
            );
        }

        @Override
        public void center() {
            writeConfigValue(configOffsetX, 0.0D);
            writeConfigValue(configOffsetY, 0.0D);
            setDouble(offsetX, 0.0D);
            setDouble(lastOffsetX, 0.0D);
            setDouble(targetOffsetX, 0.0D);
            setDouble(offsetY, 0.0D);
            setDouble(lastOffsetY, 0.0D);
            setDouble(targetOffsetY, 0.0D);
        }

        @Override
        public void restore(ShoulderOffsetSession.Offset original) {
            writeConfigValue(configOffsetX, original.getX());
            writeConfigValue(configOffsetY, original.getY());
            setDouble(targetOffsetX, original.getX());
            setDouble(targetOffsetY, original.getY());
        }

        private double readConfigValue(Field configField) {
            try {
                Object holder = configField.get(clientConfig);
                Field value = accessibleField(holder.getClass(), "value");
                return ((Number) value.get(holder)).doubleValue();
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Could not read SSR runtime offset", failure);
            }
        }

        private void writeConfigValue(Field configField, double valueToWrite) {
            try {
                Object holder = configField.get(clientConfig);
                Field value = accessibleField(holder.getClass(), "value");
                value.set(holder, valueToWrite);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Could not write SSR runtime offset", failure);
            }
        }

        private void setDouble(Field field, double value) {
            try {
                field.setDouble(instance, value);
            } catch (IllegalAccessException failure) {
                throw new IllegalStateException("Could not update SSR camera interpolation", failure);
            }
        }
    }
}

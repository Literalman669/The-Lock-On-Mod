package com.zeldatargeting.mod.client.camera.compat;

import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Optional, read-only link to Epic Fight's 1.12.2 client combat mode. */
public final class EpicFightBridge {
    private static final String ENGINE_CLASS =
        "maninthehouse.epicfight.client.ClientEngine";

    private final Logger logger;
    private final Field instanceField;
    private final Method isBattleMode;
    private boolean operational;
    private boolean warningLogged;

    private EpicFightBridge(
            Logger logger,
            boolean operational,
            Field instanceField,
            Method isBattleMode) {
        this.logger = logger;
        this.operational = operational;
        this.instanceField = instanceField;
        this.isBattleMode = isBattleMode;
    }

    public static EpicFightBridge unavailable() {
        return new EpicFightBridge(null, false, null, null);
    }

    public static EpicFightBridge detect(boolean installed, Logger logger) {
        if (!installed) {
            return unavailable();
        }
        try {
            Class<?> engine = Class.forName(ENGINE_CLASS);
            EpicFightBridge bridge = new EpicFightBridge(
                logger,
                true,
                engine.getField("INSTANCE"),
                engine.getMethod("isBattleMode")
            );
            if (logger != null) {
                logger.info("Epic Fight detected - combat aim integration enabled");
            }
            return bridge;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            if (logger != null) {
                logger.warn("Epic Fight was detected but its combat mode could not be linked", failure);
            }
            return unavailable();
        }
    }

    public boolean isBattleMode() {
        if (!operational) {
            return false;
        }
        try {
            Object engine = instanceField.get(null);
            return engine != null && Boolean.TRUE.equals(isBattleMode.invoke(engine));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            operational = false;
            if (!warningLogged && logger != null) {
                logger.warn("Epic Fight combat aim integration disabled for this session", failure);
                warningLogged = true;
            }
            return false;
        }
    }
}

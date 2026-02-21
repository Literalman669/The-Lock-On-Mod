package com.zeldatargeting.mod;

import com.zeldatargeting.mod.config.TargetingConfig;
import com.zeldatargeting.mod.proxy.CommonProxy;
import net.minecraftforge.fml.common.Loader;
import java.lang.reflect.Method;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ZeldaTargetingMod.MODID, name = ZeldaTargetingMod.NAME, version = ZeldaTargetingMod.VERSION, clientSideOnly = false, guiFactory = "com.zeldatargeting.mod.client.gui.GuiFactory")
public class ZeldaTargetingMod {
    public static final String MODID = "zeldatargeting";
    public static final String NAME = "Zelda Targeting";
    public static final String VERSION = "1.3.0";
    
    @Mod.Instance(MODID)
    public static ZeldaTargetingMod instance;
    
    @SidedProxy(clientSide = "com.zeldatargeting.mod.proxy.ClientProxy", serverSide = "com.zeldatargeting.mod.proxy.ServerProxy")
    public static CommonProxy proxy;
    
    private static Logger logger;
    private static boolean betterThirdPersonLoaded;
    private static boolean shoulderSurfingLoaded;

    private static Method ssrGetInstanceMethod;
    private static Method ssrDoShoulderSurfingMethod;
    private static Method ssrGetOffsetXMethod;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Zelda Targeting Mod - Pre-Initialization");
        
// Check for Better Third Person compatibility
        betterThirdPersonLoaded = Loader.isModLoaded("betterthirdperson");
        if (betterThirdPersonLoaded) {
            logger.info("Better Third Person detected - Camera lock-on will be disabled to prevent conflicts");
        }
        
        // Check for Shoulder Surfing Reloaded compatibility
        shoulderSurfingLoaded = Loader.isModLoaded("shouldersurfing");
        if (shoulderSurfingLoaded) {
            logger.info("Shoulder Surfing Reloaded detected - Attack alignment compensation enabled");
            initSsrReflection();
        }
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Zelda Targeting Mod - Initialization");
        
        // Note: Removed network initialization as it's not needed for this client-side mod
        // and was causing crashes due to FML networking changes
        
        proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("Zelda Targeting Mod - Post-Initialization");
        
        proxy.postInit(event);
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static boolean isBetterThirdPersonLoaded() {
        return betterThirdPersonLoaded;
    }
    
    public static boolean isShoulderSurfingLoaded() {
        return shoulderSurfingLoaded;
    }
    
    /** Caches SSR's reflection handles once at startup to avoid per-frame Class.forName overhead. */
    private static void initSsrReflection() {
        try {
            Class<?> cls = Class.forName("com.teamderpy.shouldersurfing.client.ShoulderInstance");
            ssrGetInstanceMethod      = cls.getMethod("getInstance");
            ssrDoShoulderSurfingMethod = cls.getMethod("doShoulderSurfing");
            ssrGetOffsetXMethod       = cls.getMethod("getOffsetX");
        } catch (Exception e) {
            logger.warn("Failed to cache Shoulder Surfing Reloaded reflection handles; compensation disabled", e);
            shoulderSurfingLoaded = false;
        }
    }

    /** Returns true only when SSR's shoulder-surfing perspective is actually active. */
    public static boolean isShoulderSurfingActive() {
        if (!shoulderSurfingLoaded || ssrGetInstanceMethod == null) return false;
        try {
            Object instance = ssrGetInstanceMethod.invoke(null);
            return (boolean) ssrDoShoulderSurfingMethod.invoke(instance);
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns SSR's current runtime x-offset, or the config fallback if reflection fails. */
    public static double getShoulderSurfingOffsetX() {
        if (!shoulderSurfingLoaded || ssrGetInstanceMethod == null) return TargetingConfig.ssrXOffset;
        try {
            Object instance = ssrGetInstanceMethod.invoke(null);
            return (double) ssrGetOffsetXMethod.invoke(instance);
        } catch (Exception e) {
            return TargetingConfig.ssrXOffset;
        }
    }
}
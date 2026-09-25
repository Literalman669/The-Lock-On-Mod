package com.zeldatargeting.mod;

import com.zeldatargeting.mod.client.camera.compat.ShoulderSurfingBridge;
import com.zeldatargeting.mod.proxy.CommonProxy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ZeldaTargetingMod.MODID, name = ZeldaTargetingMod.NAME, version = ZeldaTargetingMod.VERSION, clientSideOnly = true, guiFactory = "com.zeldatargeting.mod.client.gui.GuiFactory")
public class ZeldaTargetingMod {
    public static final String MODID = "zeldatargeting";
    public static final String NAME = "Zelda Targeting";
    public static final String VERSION = "1.4.0";
    
    @Mod.Instance(MODID)
    public static ZeldaTargetingMod instance;
    
    @SidedProxy(clientSide = "com.zeldatargeting.mod.proxy.ClientProxy", serverSide = "com.zeldatargeting.mod.proxy.ServerProxy")
    public static CommonProxy proxy;
    
    private static Logger logger;
    private static ShoulderSurfingBridge shoulderSurfingBridge =
        ShoulderSurfingBridge.unavailable();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Zelda Targeting Mod - Pre-Initialization");
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Zelda Targeting Mod - Initialization");

        // All mods have completed pre-initialization by this point. SSR creates
        // its client config there, before its camera singleton can be queried.
        shoulderSurfingBridge = ShoulderSurfingBridge.detect(
            Loader.isModLoaded("shouldersurfing"),
            logger
        );
        
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
    
    public static boolean isShoulderSurfingLoaded() {
        return shoulderSurfingBridge.isLoaded();
    }

    public static boolean isShoulderSurfingActive() {
        return shoulderSurfingBridge.isActive();
    }

    public static ShoulderSurfingBridge getShoulderSurfingBridge() {
        return shoulderSurfingBridge;
    }
}

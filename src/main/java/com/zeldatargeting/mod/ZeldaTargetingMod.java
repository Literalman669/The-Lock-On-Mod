package com.zeldatargeting.mod;

import com.zeldatargeting.mod.compat.CompatBTP;
import com.zeldatargeting.mod.compat.CompatSSR;
import com.zeldatargeting.mod.compat.ModCompat;
import com.zeldatargeting.mod.config.TargetingConfig;
import com.zeldatargeting.mod.proxy.CommonProxy;
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
    public static final String VERSION = "1.3.1";
    
    @Mod.Instance(MODID)
    public static ZeldaTargetingMod instance;
    
    @SidedProxy(clientSide = "com.zeldatargeting.mod.proxy.ClientProxy", serverSide = "com.zeldatargeting.mod.proxy.ServerProxy")
    public static CommonProxy proxy;
    
    private static Logger logger;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Zelda Targeting Mod - Pre-Initialization");
        
        ModCompat.init(event);
        
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
    
    /** Delegates to CompatBTP for backward compatibility. */
    public static boolean isBetterThirdPersonLoaded() {
        return CompatBTP.isLoaded();
    }
    
    /** Delegates to CompatSSR for backward compatibility. */
    public static boolean isShoulderSurfingLoaded() {
        return CompatSSR.isLoaded();
    }

    /** Delegates to CompatSSR for backward compatibility. */
    public static boolean isShoulderSurfingActive() {
        return CompatSSR.isActive();
    }

    /** Delegates to CompatSSR for backward compatibility. */
    public static double getShoulderSurfingOffsetX() {
        return CompatSSR.getOffsetX();
    }
}
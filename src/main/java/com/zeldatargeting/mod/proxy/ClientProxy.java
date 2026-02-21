package com.zeldatargeting.mod.proxy;

import com.zeldatargeting.mod.client.KeyBindings;
import com.zeldatargeting.mod.client.TargetingManager;
import com.zeldatargeting.mod.client.combat.DamageEventListener;
import com.zeldatargeting.mod.client.render.TargetRenderer;
import com.zeldatargeting.mod.client.render.DamageNumbersRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        
        // Register key bindings
        KeyBindings.init();
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        
        // Initialize client-side targeting system
        TargetingManager.init();
        
        // Register renderers and event listeners
        MinecraftForge.EVENT_BUS.register(new TargetRenderer());
        MinecraftForge.EVENT_BUS.register(new DamageNumbersRenderer());
        MinecraftForge.EVENT_BUS.register(new DamageEventListener());
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        
        // Client-side post-initialization
    }
}
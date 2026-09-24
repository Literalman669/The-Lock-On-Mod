package com.zeldatargeting.mod.client;

import com.zeldatargeting.mod.client.gui.GuiPresetSelection;
import com.zeldatargeting.mod.config.ConfigOnboardingGate;
import com.zeldatargeting.mod.config.TargetingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Opens the one-session preset chooser after the real main menu is ready. */
@SideOnly(Side.CLIENT)
public final class ConfigOnboardingClientHandler {

    private final ConfigOnboardingGate gate = new ConfigOnboardingGate();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        GuiScreen currentScreen = minecraft.currentScreen;
        boolean mainMenuVisible = currentScreen instanceof GuiMainMenu;

        if (!gate.shouldOpen(TargetingConfig.requiresPresetSelection(), mainMenuVisible, false)) {
            return;
        }

        gate.markOpened();
        minecraft.displayGuiScreen(new GuiPresetSelection(
                currentScreen,
                new Runnable() {
                    @Override
                    public void run() {
                        gate.confirm();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        gate.dismiss();
                    }
                }));
    }
}

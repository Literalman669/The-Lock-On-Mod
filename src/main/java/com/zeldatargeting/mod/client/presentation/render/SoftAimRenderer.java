package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import com.zeldatargeting.mod.client.presentation.core.SoftAimOffset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoftAimRenderer {
    private final Minecraft minecraft;
    private boolean warned;

    public SoftAimRenderer() {
        minecraft = Minecraft.getMinecraft();
    }

    public void render(TargetPresentationSnapshot snapshot, ScaledResolution resolution) {
        if (snapshot == null || resolution == null || minecraft.player == null
                || minecraft.getRenderViewEntity() == null) {
            return;
        }
        try {
            // From the player's eyes to the target's middle, both at this frame.
            float partialTicks = minecraft.getRenderPartialTicks();
            double dx = snapshot.getInterpolatedX()
                - interpolate(minecraft.player.lastTickPosX, minecraft.player.posX, partialTicks);
            double dy = snapshot.getInterpolatedY() + snapshot.getHeight() * 0.5D
                - (interpolate(minecraft.player.lastTickPosY, minecraft.player.posY, partialTicks)
                    + minecraft.player.getEyeHeight());
            double dz = snapshot.getInterpolatedZ()
                - interpolate(minecraft.player.lastTickPosZ, minecraft.player.posZ, partialTicks);
            SoftAimOffset offset = SoftAimOffset.compute(
                minecraft.player.rotationYaw, minecraft.player.rotationPitch, dx, dy, dz);
            if (offset == null) {
                return;
            }
            int x = resolution.getScaledWidth() / 2 + offset.getX();
            int y = resolution.getScaledHeight() / 2 + offset.getY();
            int color = offset.getAlpha() << 24 | 0xFFFFAA;
            int size = 4;
            Gui.drawRect(x - 1, y - size, x + 1, y + size, color);
            Gui.drawRect(x - size, y - 1, x + size, y + 1, color);
        } catch (RuntimeException exception) {
            if (!warned && ZeldaTargetingMod.getLogger() != null) {
                warned = true;
                ZeldaTargetingMod.getLogger().warn("Soft aim renderer skipped a frame", exception);
            }
        }
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }
}

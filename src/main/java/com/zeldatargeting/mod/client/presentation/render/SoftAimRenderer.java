package com.zeldatargeting.mod.client.presentation.render;

import com.zeldatargeting.mod.ZeldaTargetingMod;
import com.zeldatargeting.mod.client.presentation.TargetPresentationSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.Vec3d;
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
            Vec3d look = minecraft.player.getLookVec();
            double dx = snapshot.getTarget().posX - minecraft.player.posX;
            double dy = snapshot.getTarget().posY + snapshot.getTarget().height * 0.5D
                - (minecraft.player.posY + minecraft.player.getEyeHeight());
            double dz = snapshot.getTarget().posZ - minecraft.player.posZ;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 0.001D) {
                return;
            }
            dx /= length;
            dy /= length;
            dz /= length;

            double dot = look.x * dx + look.y * dy + look.z * dz;
            if (dot < 0.2D) {
                return;
            }
            double crossX = look.z * dy - look.y * dz;
            double crossY = look.x * dz - look.z * dx;
            float nudge = (float) Math.min(12.0D, (1.0D - dot) * 80.0D);
            int x = resolution.getScaledWidth() / 2 + (int) (crossX * nudge * -1.0D);
            int y = resolution.getScaledHeight() / 2 + (int) (crossY * nudge);
            int alpha = (int) (Math.min(1.0D, (1.0D - dot) * 4.0D) * 200.0D);
            int color = alpha << 24 | 0xFFFFAA;
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
}

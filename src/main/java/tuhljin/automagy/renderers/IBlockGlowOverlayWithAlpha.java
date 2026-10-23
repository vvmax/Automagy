package tuhljin.automagy.renderers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.IBlockAccess;

public interface IBlockGlowOverlayWithAlpha extends IBlockGlowOverlay {
    @SideOnly(Side.CLIENT)
    float getOverlayAlpha(IBlockAccess var1, int var2, int var3, int var4);

    @SideOnly(Side.CLIENT)
    float getOverlayInvAlpha(int var1);
}

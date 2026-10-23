package tuhljin.automagy.renderers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public interface IBlockGlowOverlay {
    @SideOnly(Side.CLIENT)
    IIcon getOverlayTexture(IBlockAccess var1, int var2, int var3, int var4, int var5);

    @SideOnly(Side.CLIENT)
    IIcon getOverlayInvTexture(int var1, int var2);
}

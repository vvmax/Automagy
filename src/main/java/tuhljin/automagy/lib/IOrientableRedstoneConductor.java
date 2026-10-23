package tuhljin.automagy.lib;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IOrientableRedstoneConductor extends IRedcrystalPowerConductor, IAmplifiedRedstoneOutput {
    int getRedstoneSignalStrength(IBlockAccess var1, int var2, int var3, int var4, boolean var5);

    int getOrientation(IBlockAccess var1, int var2, int var3, int var4);

    int getRedstoneSignalSourceSide(IBlockAccess var1, int var2, int var3, int var4);

    boolean canSendRedstoneSignalInDirection(IBlockAccess var1, int var2, int var3, int var4, int var5);

    boolean canReceiveRedstoneSignalFromDirection(IBlockAccess var1, int var2, int var3, int var4, int var5);

    void onNeighborRedstoneConductorUpdate(World var1, int var2, int var3, int var4, ForgeDirection var5, int var6);

    boolean isRedstoneSignalInputPoint(IBlockAccess var1, int var2, int var3, int var4);
}

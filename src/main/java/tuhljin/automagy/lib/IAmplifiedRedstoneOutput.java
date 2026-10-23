package tuhljin.automagy.lib;

import net.minecraft.world.IBlockAccess;

public interface IAmplifiedRedstoneOutput {
    int getRedstoneSignalStrength(IBlockAccess var1, int var2, int var3, int var4, int var5, boolean var6);

    boolean isRedstoneWire();
}

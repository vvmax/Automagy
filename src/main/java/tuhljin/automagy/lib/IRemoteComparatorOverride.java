package tuhljin.automagy.lib;

import net.minecraft.world.World;
import tuhljin.automagy.tiles.TileEntityRemoteComparator;

public interface IRemoteComparatorOverride {
    int getRemoteComparatorParticleColor(World var1, int var2, int var3, int var4, TileEntityRemoteComparator var5);

    boolean hasActiveRedstoneSignal(World var1, int var2, int var3, int var4, TileEntityRemoteComparator var5);
}

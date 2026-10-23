package tuhljin.automagy.lib;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IRestrictedInventoryAccess {
    boolean greedyChestCanSteal(TileEntity var1, ForgeDirection var2);
}

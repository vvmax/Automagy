package tuhljin.automagy.api.inventarium;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface IInventariumContentsProvider {
    boolean canHandleTile(TileEntity var1);

    int getSlotCount(TileEntity var1, int var2);

    ItemStack[] getContents(TileEntity var1, int var2);
}

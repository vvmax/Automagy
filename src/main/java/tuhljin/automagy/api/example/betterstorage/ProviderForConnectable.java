package tuhljin.automagy.api.example.betterstorage;

import net.mcft.copy.betterstorage.tile.entity.TileEntityConnectable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import tuhljin.automagy.api.inventarium.DefaultInventariumContentsProvider;

public class ProviderForConnectable extends DefaultInventariumContentsProvider {
    public static final int PRIORITY = 101;

    @Override
    public boolean canHandleTile(TileEntity te) {
        return te instanceof TileEntityConnectable;
    }

    @Override
    public int getSlotCount(TileEntity te, int setID) {
        return ((TileEntityConnectable)te).isMain() ? super.getSlotCount(te, setID) : 0;
    }

    @Override
    public ItemStack[] getContents(TileEntity te, int setID) {
        return super.getContents(te, setID);
    }
}

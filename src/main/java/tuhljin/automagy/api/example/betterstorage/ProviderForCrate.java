package tuhljin.automagy.api.example.betterstorage;

import java.util.ArrayList;
import java.util.List;
import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import tuhljin.automagy.api.inventarium.IInventariumContentsProvider;

public class ProviderForCrate implements IInventariumContentsProvider {
    public static final int PRIORITY = 100;
    private Integer prevID = Integer.MIN_VALUE;
    private List crateIDs = new ArrayList();

    @Override
    public boolean canHandleTile(TileEntity te) {
        return te instanceof ICrateStorage;
    }

    @Override
    public int getSlotCount(TileEntity te, int setID) {
        Object crateID = ((ICrateStorage)te).getCrateIdentifier();
        if (setID == this.prevID) {
            if (this.crateIDs.contains(crateID)) {
                return 0;
            }
        } else {
            this.prevID = setID;
            this.crateIDs.clear();
        }

        this.crateIDs.add(crateID);
        return ((ICrateStorage)te).getCapacity();
    }

    @Override
    public ItemStack[] getContents(TileEntity te, int setID) {
        ICrateStorage crate = (ICrateStorage)te;
        List<ItemStack> list = new ArrayList<>();

        for (ItemStack stack : crate.getContents()) {
            list.add(stack);
        }

        return list.toArray(new ItemStack[list.size()]);
    }
}

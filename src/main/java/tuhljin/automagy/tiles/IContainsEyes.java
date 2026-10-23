package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;
import tuhljin.automagy.codechicken.lib.vec.BlockCoord;

public interface IContainsEyes {
    void onEyesInventoryChanged(int var1, ItemStack var2);

    int getDimensionID();

    BlockCoord getPosition();

    int getMaxDistance();
}

package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;

public interface IContainsFilter {
    void onFilterInventoryChanged(int var1, ItemStack var2);
}

package tuhljin.automagy.lib.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;

public interface IItemMap {
    Iterator<Entry<HashableItemWithoutSize, Integer>> getIterator();

    boolean containsKey(HashableItemWithoutSize var1);

    int get(HashableItemWithoutSize var1);

    void set(HashableItemWithoutSize var1, int var2);

    ArrayList<ItemStack> getItemStacks();

    IItemMap copy();
}

package tuhljin.automagy.lib.inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class FilteringItemListLinked extends FilteringItemList {
    public FilteringItemListLinked() {
        this.map = this.createNewMap();
    }

    public FilteringItemListLinked(ArrayList<ItemStack> list, boolean allowBelowOne) {
        super(list, allowBelowOne);
    }

    public FilteringItemListLinked(FilteringItemList copySource) {
        this.map = new LinkedHashMap<>(copySource.map);
    }

    @Override
    protected Map<HashableItemWithoutSize, Integer> createNewMap() {
        return new LinkedHashMap<>();
    }

    public FilteringItemListLinked populateFromInventory(IInventory inv, boolean checkExtract) {
        super.populateFromInventory(inv, checkExtract);
        return this;
    }
}

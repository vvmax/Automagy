package tuhljin.automagy.items;

import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.InventoryObjectFilter;

public class InventoryForItemRestrictedToFilters extends InventoryForItem {
    public InventoryForItemRestrictedToFilters(ItemStack containerStack, String inventoryName, int numSlots) {
        super(containerStack, inventoryName, numSlots);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return InventoryObjectFilter.isItemPopulatedFilter(stack);
    }
}

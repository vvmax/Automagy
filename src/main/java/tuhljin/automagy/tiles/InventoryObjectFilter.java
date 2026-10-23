package tuhljin.automagy.tiles;

import net.minecraft.item.ItemStack;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.inventory.InventorySimple;

public class InventoryObjectFilter extends InventorySimple {
    private IContainsFilter parent;

    public InventoryObjectFilter(IContainsFilter parent, String name, int numSlots) {
        super(name, numSlots, "FilterItems");
        this.notifyOnInventoryChanged = true;
        this.parent = parent;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isItemPopulatedFilter(stack);
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        this.parent.onFilterInventoryChanged(slot, prevStack);
    }

    public static boolean isItemPopulatedFilter(ItemStack stack) {
        if (stack != null) {
            InventoryWithFilterOptions inv = ItemEnchantedPaper.getFilterInventory(stack);
            if (inv != null) {
                if (!inv.nameFilter.isEmpty()) {
                    return true;
                }

                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (inv.getStackInSlot(i) != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}

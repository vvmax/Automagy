package tuhljin.automagy.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotRestricted extends Slot {
    public SlotRestricted(IInventory inventory, int id, int x, int y) {
        super(inventory, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }
}

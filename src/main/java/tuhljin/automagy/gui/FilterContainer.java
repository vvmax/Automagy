package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.InventoryObjectFilter;

public abstract class FilterContainer extends Container {
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack stack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (slot instanceof SlotRestrictedWithReporting) {
                if (!this.mergeItemStack(stackInSlot, 0, 35, true)) {
                    return null;
                }
            } else {
                if (!InventoryObjectFilter.isItemPopulatedFilter(stackInSlot)) {
                    return null;
                }

                if (!this.mergeItemStack(stackInSlot, 36, this.inventorySlots.size(), false)) {
                    return null;
                }
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (stackInSlot.stackSize == stack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }
}

package tuhljin.automagy.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import thaumcraft.common.container.SlotGhost;

public class SlotGhostRestricted extends SlotGhost {
    public SlotGhostRestricted(IInventory inventory, int id, int x, int y, int limit) {
        super(inventory, id, x, y, limit);
    }

    public SlotGhostRestricted(IInventory inventory, int id, int x, int y) {
        super(inventory, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }
}

package tuhljin.automagy.items;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;
import tuhljin.automagy.lib.inventory.InventorySimple;

public class InventoryForItem extends InventorySimple {
    public InventoryForItem(ItemStack containerStack, String inventoryName, int numSlots) {
        super(inventoryName, numSlots);
        if (containerStack != null && containerStack.hasTagCompound()) {
            this.readCustomNBT(containerStack.getTagCompound());
        }
    }

    public void removeDuplicates() {
        Map<HashableItemWithoutSize, Integer> mapAmt = new HashMap<>();
        Map<HashableItemWithoutSize, Integer> mapSlot = new HashMap<>();

        for (int i = 0; i < this.numSlots; i++) {
            ItemStack stack = this.inventorySlots[i];
            if (stack != null) {
                HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                int amt = stack.stackSize;
                if (mapAmt.containsKey(item)) {
                    if (amt <= mapAmt.get(item)) {
                        this.inventorySlots[i] = null;
                    } else {
                        this.inventorySlots[mapSlot.get(item)] = null;
                        mapAmt.put(item, amt);
                        mapSlot.put(item, i);
                    }
                } else {
                    mapAmt.put(item, amt);
                    mapSlot.put(item, i);
                }
            }
        }
    }

    public void saveToItemStack(ItemStack stack) {
        boolean hasTagCompound = stack.hasTagCompound();
        NBTTagCompound nbttagcompound = hasTagCompound ? stack.getTagCompound() : new NBTTagCompound();
        this.writeCustomNBT(nbttagcompound);
        if (!hasTagCompound) {
            stack.setTagCompound(nbttagcompound);
        }
    }
}

package tuhljin.automagy.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tuhljin.automagy.lib.TjUtil;

public class InventoryForItemLargeStacks extends InventoryForItem {
    public InventoryForItemLargeStacks(ItemStack containerStack, String inventoryName, int numSlots) {
        super(containerStack, inventoryName, numSlots);
    }

    @Override
    protected ItemStack loadItemStack(NBTTagCompound nbt) {
        return TjUtil.readLargeItemStackFromNBT(nbt);
    }

    @Override
    protected void writeItemStack(ItemStack stack, NBTTagCompound nbt) {
        TjUtil.writeLargeItemStackToNBT(stack, nbt);
    }
}

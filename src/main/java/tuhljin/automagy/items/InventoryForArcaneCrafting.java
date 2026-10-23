package tuhljin.automagy.items;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class InventoryForArcaneCrafting extends InventoryForItem {
    private Container container;
    private IInventory wandRealInv;
    private int wandRealSlotNum;

    public InventoryForArcaneCrafting(
        ItemStack containerStack, String inventoryName, int numSlots, Container container, IInventory wandRealInv, int wandRealSlotNum
    ) {
        super(containerStack, inventoryName, numSlots);
        this.container = container;
        this.wandRealInv = wandRealInv;
        this.wandRealSlotNum = wandRealSlotNum;
        this.notifyOnInventoryChanged = true;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        this.markDirty();
        this.container.onCraftMatrixChanged(this);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 10) {
            ItemStack stack = this.wandRealInv.getStackInSlot(this.wandRealSlotNum);
            return ThaumcraftExtension.wandIsStaff(stack) ? null : stack;
        } else {
            return super.getStackInSlot(slot);
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < 9; i++) {
            if (this.inventorySlots[i] != null) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setByte("Slot", (byte)i);
                this.inventorySlots[i].writeToNBT(tagList);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag(this.nbtKey, nbttaglist);
    }
}

package tuhljin.automagy.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventorySimple implements IInventory {
    protected ItemStack[] inventorySlots;
    protected String inventoryName;
    protected int numSlots;
    protected final String nbtKey;
    protected boolean notifyOnInventoryChanged = false;

    public InventorySimple(String inventoryName, int numSlots, String nbtKey) {
        this.inventoryName = inventoryName;
        this.numSlots = numSlots;
        this.nbtKey = nbtKey;
        this.inventorySlots = new ItemStack[numSlots];
    }

    public InventorySimple(String inventoryName, int numSlots) {
        this(inventoryName, numSlots, "ContainedItems");
    }

    public void onInventoryChanged(int slot, ItemStack prevStack) {
    }

    @Override
    public int getSizeInventory() {
        return this.numSlots;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventorySlots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int num) {
        if (this.inventorySlots[slot] != null) {
            if (this.inventorySlots[slot].stackSize <= num) {
                ItemStack stack = this.inventorySlots[slot];
                this.inventorySlots[slot] = null;
                this.markDirty();
                if (this.notifyOnInventoryChanged) {
                    this.onInventoryChanged(slot, stack.copy());
                }

                return stack;
            } else {
                ItemStack prev = this.notifyOnInventoryChanged ? this.inventorySlots[slot].copy() : null;
                ItemStack stack = this.inventorySlots[slot].splitStack(num);
                if (this.inventorySlots[slot].stackSize == 0) {
                    this.inventorySlots[slot] = null;
                }

                this.markDirty();
                if (this.notifyOnInventoryChanged) {
                    this.onInventoryChanged(slot, prev);
                }

                return stack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return this.getStackInSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (stack != this.inventorySlots[slot]) {
            ItemStack prev = this.notifyOnInventoryChanged ? this.inventorySlots[slot] : null;
            this.inventorySlots[slot] = stack;
            if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
                stack.stackSize = this.getInventoryStackLimit();
            }

            this.markDirty();
            if (this.notifyOnInventoryChanged) {
                this.onInventoryChanged(slot, prev);
            }
        }
    }

    public void setInventorySlotContentsSoftly(int slot, ItemStack stack) {
        this.inventorySlots[slot] = stack;
    }

    @Override
    public String getInventoryName() {
        return this.inventoryName;
    }

    public boolean isCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    public void openChest() {
    }

    public void closeChest() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void markDirty() {
    }

    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = nbttagcompound.getTagList(this.nbtKey, 10);
        this.inventorySlots = new ItemStack[this.numSlots];
        if (nbttaglist.tagCount() > 0) {
            for (int i = 0; i < this.numSlots; i++) {
                NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
                byte slot = tagList.getByte("Slot");
                if (slot >= 0 && slot < this.numSlots && this.inventorySlots[slot] == null) {
                    this.inventorySlots[slot] = this.loadItemStack(tagList);
                }
            }
        }
    }

    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.numSlots; i++) {
            if (this.inventorySlots[i] != null) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setByte("Slot", (byte)i);
                this.writeItemStack(this.inventorySlots[i], tagList);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag(this.nbtKey, nbttaglist);
    }

    protected ItemStack loadItemStack(NBTTagCompound nbt) {
        return ItemStack.loadItemStackFromNBT(nbt);
    }

    protected void writeItemStack(ItemStack stack, NBTTagCompound nbt) {
        stack.writeToNBT(nbt);
    }
}

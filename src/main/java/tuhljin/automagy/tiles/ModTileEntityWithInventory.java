package tuhljin.automagy.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;

public abstract class ModTileEntityWithInventory extends ModTileEntity implements ISidedInventory {
    protected ItemStack[] inventorySlots;
    protected final int numSlots;
    private String inventoryName;
    protected int[] accessibleSlots;
    protected boolean notifyOnInventoryChanged = false;

    public ModTileEntityWithInventory(String inventoryName, int numSlots) {
        this(inventoryName, numSlots, true);
    }

    public ModTileEntityWithInventory(String inventoryName, int numSlots, boolean createDefaultAccessibleSlots) {
        this.inventoryName = inventoryName;
        this.numSlots = numSlots;
        this.inventorySlots = new ItemStack[numSlots];
        if (createDefaultAccessibleSlots) {
            this.accessibleSlots = new int[numSlots];
            int i = 0;

            while (i < numSlots) {
                this.accessibleSlots[i] = i++;
            }
        }
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

    @Override
    public String getInventoryName() {
        return StatCollector.translateToLocal("tile." + this.inventoryName + ".name");
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
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
            && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64.0;
    }

    public void openChest() {
    }

    public void closeChest() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    public int[] getSlotsForFace(int side) {
        return this.accessibleSlots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int var3) {
        return this.isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int var1, ItemStack var2, int var3) {
        return true;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
        this.inventorySlots = new ItemStack[this.numSlots];
        if (nbttaglist.tagCount() > 0) {
            for (int i = 0; i < this.numSlots; i++) {
                NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
                byte slot = tagList.getByte("Slot");
                if (slot >= 0 && slot < this.numSlots && this.inventorySlots[slot] == null) {
                    this.inventorySlots[slot] = ItemStack.loadItemStackFromNBT(tagList);
                }
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.numSlots; i++) {
            if (this.inventorySlots[i] != null) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setByte("Slot", (byte)i);
                this.inventorySlots[i].writeToNBT(tagList);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
    }
}

package tuhljin.automagy.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public abstract class InventoryWrapper implements IInventory {
    protected final IInventory inv;

    public InventoryWrapper(IInventory inv) {
        this.inv = inv;
    }

    @Override
    public int getSizeInventory() {
        return this.inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {
        return this.inv.getStackInSlot(p_70301_1_);
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return this.inv.decrStackSize(p_70298_1_, p_70298_2_);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return this.inv.getStackInSlotOnClosing(p_70304_1_);
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
        this.inv.setInventorySlotContents(p_70299_1_, p_70299_2_);
    }

    @Override
    public String getInventoryName() {
        return this.inv.getInventoryName();
    }

    public boolean isCustomInventoryName() {
        return this.inv.isCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        return this.inv.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        this.inv.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return this.inv.isUseableByPlayer(p_70300_1_);
    }

    public void openChest() {
        this.inv.openChest();
    }

    public void closeChest() {
        this.inv.closeChest();
    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return this.inv.isItemValidForSlot(p_94041_1_, p_94041_2_);
    }
}

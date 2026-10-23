package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.common.tiles.TileMagicWorkbench;

public class FakeTileMagicWorkbench extends TileMagicWorkbench {
    protected IInventory inv;

    public FakeTileMagicWorkbench(IInventory inv, World world) {
        this.inv = inv;
        this.setWorldObj(world);
    }

    @Override
    public int getSizeInventory() {
        return this.inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int num) {
        return this.inv.decrStackSize(slot, num);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return this.inv.getStackInSlotOnClosing(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.inv.setInventorySlotContents(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return this.inv.getInventoryName();
    }

    @Override
    public boolean isCustomInventoryName() {
        return this.inv.isCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        return this.inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.inv.isUseableByPlayer(player);
    }

    @Override
    public void openChest() {
        this.inv.openChest();
    }

    @Override
    public void closeChest() {
        this.inv.closeChest();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return this.inv.isItemValidForSlot(slot, stack);
    }

    @Override
    public void markDirty() {
        this.inv.markDirty();
    }
}

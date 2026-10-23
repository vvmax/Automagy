package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.IChestForGui;

public class ContainerChestBase extends Container {
    private IChestForGui tileChest;
    protected int numRows;
    protected int numColumns;

    public ContainerChestBase(InventoryPlayer inventoryPlayer, IChestForGui te) {
        this.tileChest = te;
        this.tileChest.openChest();
        this.numRows = te.getNumRows();
        this.numColumns = te.getNumColumns();
        IInventory chestInv = te;
        int i = (this.numRows - 4) * 18;

        for (int j = 0; j < this.numRows; j++) {
            for (int k = 0; k < this.numColumns; k++) {
                this.addSlotToContainer(new Slot(chestInv, k + j * this.numColumns, 8 + k * 18, 18 + j * 18));
            }
        }

        for (int var7 = 0; var7 < 3; var7++) {
            for (int k = 0; k < 9; k++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, k + var7 * 9 + 9, 8 + k * 18, 103 + var7 * 18 + i));
            }
        }

        for (int var8 = 0; var8 < 9; var8++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, var8, 8 + var8 * 18, 161 + i));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tileChest.isUseableByPlayer(player);
    }

    public IChestForGui getChest() {
        return this.tileChest;
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        this.tileChest.closeChest();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotNum < this.numRows * this.numColumns) {
                if (!this.mergeItemStack(itemstack1, this.numRows * this.numColumns, this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.numRows * this.numColumns, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack((ItemStack)null);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
}

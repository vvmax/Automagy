package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.TileEntityGreedyChest;

public class ContainerGreedyChest extends Container {
    private TileEntityGreedyChest teGC;
    private int numRows;

    public ContainerGreedyChest(InventoryPlayer inventoryPlayer, TileEntityGreedyChest te) {
        this.teGC = te;
        this.teGC.openChest();
        this.numRows = te.getSizeInventory() / 9;
        int i = (this.numRows - 4) * 18;

        for (int j = 0; j < this.numRows; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlotToContainer(new Slot(te, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (int var6 = 0; var6 < 3; var6++) {
            for (int k = 0; k < 9; k++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, k + var6 * 9 + 9, 8 + k * 18, 103 + var6 * 18 + i));
            }
        }

        for (int var7 = 0; var7 < 9; var7++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, var7, 8 + var7 * 18, 161 + i));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teGC.isUseableByPlayer(player);
    }

    public TileEntityGreedyChest getTileEntity() {
        return this.teGC;
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        this.teGC.closeChest();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotNum < this.numRows * 9) {
                if (!this.mergeItemStack(itemstack1, this.numRows * 9, this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.numRows * 9, false)) {
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

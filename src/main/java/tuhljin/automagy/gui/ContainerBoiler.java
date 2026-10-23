package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.TileEntityBoiler;

public class ContainerBoiler extends Container {
    private TileEntityBoiler boiler;
    private SlotRestricted fuelSlot;
    private int prevCookTime;
    private int prevBurnTime;
    private int prevItemBurnTime;
    private int prevVis;
    private int prevSmeltTime;

    public ContainerBoiler(InventoryPlayer inventoryPlayer, TileEntityBoiler te) {
        this.boiler = te;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 151));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 93 + i * 18));
            }
        }

        this.fuelSlot = new SlotRestrictedWithReporting(0, te, 0, 81, 53);
        this.addSlotToContainer(this.fuelSlot);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.boiler.isUseableByPlayer(player);
    }

    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);
        player.sendProgressBarUpdate(this, 0, this.boiler.furnaceCookTime);
        player.sendProgressBarUpdate(this, 1, this.boiler.furnaceBurnTime);
        player.sendProgressBarUpdate(this, 2, this.boiler.currentItemBurnTime);
        player.sendProgressBarUpdate(this, 3, this.boiler.vis);
        player.sendProgressBarUpdate(this, 4, this.boiler.smeltTime);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                this.boiler.furnaceCookTime = data;
                break;
            case 1:
                this.boiler.furnaceBurnTime = data;
                break;
            case 2:
                this.boiler.currentItemBurnTime = data;
                break;
            case 3:
                this.boiler.vis = data;
                break;
            case 4:
                this.boiler.smeltTime = data;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : this.crafters) {
            ICrafting crafter = (ICrafting)player;
            if (this.prevCookTime != this.boiler.furnaceCookTime) {
                crafter.sendProgressBarUpdate(this, 0, this.boiler.furnaceCookTime);
            }

            if (this.prevBurnTime != this.boiler.furnaceBurnTime) {
                crafter.sendProgressBarUpdate(this, 1, this.boiler.furnaceBurnTime);
            }

            if (this.prevItemBurnTime != this.boiler.currentItemBurnTime) {
                crafter.sendProgressBarUpdate(this, 2, this.boiler.currentItemBurnTime);
            }

            if (this.prevVis != this.boiler.vis) {
                crafter.sendProgressBarUpdate(this, 3, this.boiler.vis);
            }

            if (this.prevSmeltTime != this.boiler.smeltTime) {
                crafter.sendProgressBarUpdate(this, 4, this.boiler.smeltTime);
            }
        }

        this.prevCookTime = this.boiler.furnaceCookTime;
        this.prevBurnTime = this.boiler.furnaceBurnTime;
        this.prevItemBurnTime = this.boiler.currentItemBurnTime;
        this.prevVis = this.boiler.vis;
        this.prevSmeltTime = this.boiler.smeltTime;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack stack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (slot instanceof SlotRestricted) {
                if (!this.mergeItemStack(stackInSlot, 0, 35, true)) {
                    return null;
                }
            } else {
                if (!this.boiler.isItemValidForSlot(0, stackInSlot)) {
                    return null;
                }

                if (!this.mergeItemStack(stackInSlot, 36, this.inventorySlots.size(), false)) {
                    return null;
                }
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (stackInSlot.stackSize == stack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }
}

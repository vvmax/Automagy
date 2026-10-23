package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.items.ItemEntityEye;
import tuhljin.automagy.tiles.TileEntityEntitySignal;

public class ContainerEntitySignal extends Container {
    private TileEntityEntitySignal teES;
    public GUIEntitySignal attachedGUI = null;
    private SlotRestrictedWithReporting filterSlot;
    private boolean oldFilterAdults = true;
    private boolean oldFilterChildren = true;
    private boolean oldSpecificPlayer = false;

    public ContainerEntitySignal(InventoryPlayer inventoryPlayer, TileEntityEntitySignal te) {
        this.teES = te;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        this.filterSlot = new SlotRestrictedWithReporting(0, te, 0, 16, 30);
        this.addSlotToContainer(this.filterSlot);
    }

    @SideOnly(Side.CLIENT)
    public void attachToGui(GUIEntitySignal gui) {
        this.filterSlot.setParent(gui);
        this.attachedGUI = gui;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teES.isUseableByPlayer(player);
    }

    public TileEntityEntitySignal getTileEntity() {
        return this.teES;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack stack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (slot instanceof SlotRestrictedWithReporting) {
                if (!this.mergeItemStack(stackInSlot, 0, 35, true)) {
                    return null;
                }
            } else {
                if (!ItemEntityEye.hasLinkedMob(stackInSlot)) {
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

    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);
        player.sendProgressBarUpdate(this, 0, this.teES.filterAdults ? 1 : 0);
        player.sendProgressBarUpdate(this, 1, this.teES.filterChildren ? 1 : 0);
        player.sendProgressBarUpdate(this, 2, this.teES.specificPlayer ? 1 : 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                this.teES.filterAdults = data == 1;
                break;
            case 1:
                this.teES.filterChildren = data == 1;
                break;
            case 2:
                this.teES.specificPlayer = data == 1;
        }

        if (this.attachedGUI != null) {
            this.attachedGUI.updateButton(id, data);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : this.crafters) {
            if (this.teES.filterAdults != this.oldFilterAdults) {
                ((ICrafting)player).sendProgressBarUpdate(this, 0, this.teES.filterAdults ? 1 : 0);
            }

            if (this.teES.filterChildren != this.oldFilterChildren) {
                ((ICrafting)player).sendProgressBarUpdate(this, 1, this.teES.filterChildren ? 1 : 0);
            }

            if (this.teES.specificPlayer != this.oldSpecificPlayer) {
                ((ICrafting)player).sendProgressBarUpdate(this, 2, this.teES.specificPlayer ? 1 : 0);
            }
        }

        this.oldFilterAdults = this.teES.filterAdults;
        this.oldFilterChildren = this.teES.filterChildren;
        this.oldSpecificPlayer = this.teES.specificPlayer;
    }
}

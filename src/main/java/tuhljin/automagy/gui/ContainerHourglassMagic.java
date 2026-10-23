package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.TileEntityHourglassMagic;

public class ContainerHourglassMagic extends Container {
    private TileEntityHourglassMagic teH;
    public GUIHourglassMagic attachedGUI = null;
    private int oldModeSignal = -1;
    private int oldModeSandFlip = -1;
    private boolean oldRepeat = false;
    private int oldModeSignalReact = -1;

    public ContainerHourglassMagic(InventoryPlayer inventoryPlayer, TileEntityHourglassMagic te) {
        this.teH = te;
        int offsetY = 20;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142 + offsetY));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18 + offsetY));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return !this.teH.isInvalid() && player.getDistanceSq(this.teH.xCoord + 0.5, this.teH.yCoord + 0.5, this.teH.zCoord + 0.5) <= 64.0;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
        return null;
    }

    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);
        player.sendProgressBarUpdate(this, 2, this.teH.modeSignal);
        player.sendProgressBarUpdate(this, 3, this.teH.modeSandFlip);
        player.sendProgressBarUpdate(this, 4, this.teH.modeRepeat ? 1 : 0);
        player.sendProgressBarUpdate(this, 5, this.teH.modeSignalReact);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 2:
                this.teH.modeSignal = data;
                break;
            case 3:
                this.teH.modeSandFlip = data;
                break;
            case 4:
                this.teH.modeRepeat = data == 1;
                break;
            case 5:
                this.teH.modeSignalReact = data;
        }

        if (this.attachedGUI != null) {
            this.attachedGUI.updateButton(id, data);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : this.crafters) {
            if (this.teH.modeSignal != this.oldModeSignal) {
                ((ICrafting)player).sendProgressBarUpdate(this, 2, this.teH.modeSignal);
            }

            if (this.teH.modeSandFlip != this.oldModeSandFlip) {
                ((ICrafting)player).sendProgressBarUpdate(this, 3, this.teH.modeSandFlip);
            }

            if (this.teH.modeRepeat != this.oldRepeat) {
                ((ICrafting)player).sendProgressBarUpdate(this, 4, this.teH.modeRepeat ? 1 : 0);
            }

            if (this.teH.modeSignalReact != this.oldModeSignalReact) {
                ((ICrafting)player).sendProgressBarUpdate(this, 5, this.teH.modeSignalReact);
            }
        }

        this.oldModeSignal = this.teH.modeSignal;
        this.oldModeSandFlip = this.teH.modeSandFlip;
        this.oldRepeat = this.teH.modeRepeat;
        this.oldModeSignalReact = this.teH.modeSignalReact;
    }

    public TileEntityHourglassMagic getTileEntity() {
        return this.teH;
    }
}

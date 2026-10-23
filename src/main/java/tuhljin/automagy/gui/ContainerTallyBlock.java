package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import tuhljin.automagy.tiles.TileEntityTallyBase;

public class ContainerTallyBlock extends FilterContainer {
    private TileEntityTallyBase teT;
    public GUITallyBlock attachedGUI = null;
    private SlotRestrictedWithReporting filterSlot;
    private boolean oldRequireAllMatches = true;

    public ContainerTallyBlock(InventoryPlayer inventoryPlayer, TileEntityTallyBase te) {
        this.teT = te;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        this.filterSlot = new SlotRestrictedWithReporting(0, te, 0, 80, 28);
        this.addSlotToContainer(this.filterSlot);
    }

    @SideOnly(Side.CLIENT)
    public void attachToGui(GUITallyBlock gui) {
        this.filterSlot.setParent(gui);
        this.attachedGUI = gui;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teT.isUseableByPlayer(player);
    }

    public TileEntityTallyBase getTileEntity() {
        return this.teT;
    }

    public void onCraftGuiOpened(ICrafting player) {
        super.onCraftGuiOpened(player);
        player.sendProgressBarUpdate(this, 0, this.teT.requireAllMatches ? 1 : 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                this.teT.requireAllMatches = data == 1;
            default:
                if (this.attachedGUI != null) {
                    this.attachedGUI.updateButton(id, data == 0);
                }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object player : this.crafters) {
            if (this.teT.requireAllMatches != this.oldRequireAllMatches) {
                ((ICrafting)player).sendProgressBarUpdate(this, 0, this.teT.requireAllMatches ? 1 : 0);
            }
        }

        this.oldRequireAllMatches = this.teT.requireAllMatches;
    }
}

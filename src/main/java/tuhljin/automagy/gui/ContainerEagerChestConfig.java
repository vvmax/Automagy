package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import tuhljin.automagy.tiles.TileEntityEagerChest;

public class ContainerEagerChestConfig extends FilterContainer {
    private TileEntityEagerChest teEC;
    private GUIEagerChestConfig attachedGUI = null;
    private SlotRestrictedWithReporting[] filterSlot = new SlotRestrictedWithReporting[4];

    public ContainerEagerChestConfig(InventoryPlayer inventoryPlayer, TileEntityEagerChest te) {
        this.teEC = te;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        this.filterSlot[0] = new SlotRestrictedWithReporting(0, te.filterContainingInventory, 0, 19, 13);
        this.addSlotToContainer(this.filterSlot[0]);
        this.filterSlot[1] = new SlotRestrictedWithReporting(1, te.filterContainingInventory, 1, 19, 41);
        this.addSlotToContainer(this.filterSlot[1]);
        this.filterSlot[2] = new SlotRestrictedWithReporting(2, te.filterContainingInventory, 2, 106, 13);
        this.addSlotToContainer(this.filterSlot[2]);
        this.filterSlot[3] = new SlotRestrictedWithReporting(3, te.filterContainingInventory, 3, 106, 41);
        this.addSlotToContainer(this.filterSlot[3]);
    }

    @SideOnly(Side.CLIENT)
    public void attachToGui(GUIEagerChestConfig gui) {
        this.attachedGUI = gui;

        for (int i = 0; i < this.filterSlot.length; i++) {
            this.filterSlot[i].setParent(gui);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teEC.isUseableByPlayer(player);
    }

    public TileEntityEagerChest getTileEntity() {
        return this.teEC;
    }
}

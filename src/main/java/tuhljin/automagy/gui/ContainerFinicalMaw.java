package tuhljin.automagy.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import tuhljin.automagy.tiles.TileEntityMawFinical;

public class ContainerFinicalMaw extends FilterContainer {
    private TileEntityMawFinical teFM;
    private SlotRestrictedWithReporting filterSlot;

    public ContainerFinicalMaw(InventoryPlayer inventoryPlayer, TileEntityMawFinical te) {
        this.teFM = te;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        this.filterSlot = new SlotRestrictedWithReporting(0, te.filterContainingInventory, 0, 80, 28);
        this.addSlotToContainer(this.filterSlot);
    }

    @SideOnly(Side.CLIENT)
    public void attachToGui(GUIFinicalMaw gui) {
        this.filterSlot.setParent(gui);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.teFM.isUseableByPlayer(player);
    }

    public TileEntityMawFinical getTileEntity() {
        return this.teFM;
    }
}

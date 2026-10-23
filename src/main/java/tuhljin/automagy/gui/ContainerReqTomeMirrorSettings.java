package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import thaumcraft.common.container.ContainerGhostSlots;
import tuhljin.automagy.tiles.IInventariumBook;
import tuhljin.automagy.tiles.IInventariumBookLinksMirror;

public class ContainerReqTomeMirrorSettings extends ContainerGhostSlots {
    public IInventariumBook book;
    private EntityPlayer player;

    public ContainerReqTomeMirrorSettings(InventoryPlayer inventoryPlayer, IInventariumBookLinksMirror book, boolean showOutputSettings) {
        this.book = book;
        this.player = inventoryPlayer.player;

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        IInventory eyes = book.getMirrorSettingsInventory();
        if (eyes != null) {
            this.addSlotToContainer(new SlotGhostRestricted(eyes, 0, 16, 30, 1));
            if (showOutputSettings) {
                this.addSlotToContainer(new SlotGhostRestricted(eyes, 1, 104, 30, 1));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer p_75145_1_) {
        return this.book.isInventariumLoaded() && this.book.isUseableByPlayer(this.player);
    }
}

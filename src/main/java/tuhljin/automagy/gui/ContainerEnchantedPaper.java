package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.common.container.ContainerGhostSlots;
import thaumcraft.common.container.SlotGhost;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;

public class ContainerEnchantedPaper extends ContainerGhostSlots {
    public final boolean blacklist;
    public final int blockedSlotID;
    private ItemStack modifyingStack = null;
    public InventoryWithFilterOptions filterInventory;

    public ContainerEnchantedPaper(boolean isBlacklist, EntityPlayer player) {
        this.blacklist = isBlacklist;
        InventoryPlayer inventoryPlayer = player.inventory;
        this.blockedSlotID = inventoryPlayer.currentItem;

        for (int i = 0; i < 9; i++) {
            Slot slot = new Slot(inventoryPlayer, i, 8 + i * 18, 156);
            this.addSlotToContainer(slot);
            if (i == this.blockedSlotID) {
                ItemStack stack = slot.getStack();
                if (this.blacklist && ItemEnchantedPaper.stackIsBlacklist(stack) || !this.blacklist && ItemEnchantedPaper.stackIsWhitelist(stack)) {
                    this.modifyingStack = stack;
                }
            }
        }

        for (int ix = 0; ix < 3; ix++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + ix * 9, 8 + j * 18, 98 + ix * 18));
            }
        }

        this.filterInventory = ItemEnchantedPaper.getFilterInventory(this.modifyingStack);
        if (this.filterInventory != null) {
            for (int ix = 0; ix < 9; ix++) {
                this.addSlotToContainer(new SlotGhost(this.filterInventory, ix, 18 + ix % 3 * 19, 18 + ix / 3 * 19, 999));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.modifyingStack != null && ItemStack.areItemStacksEqual(this.modifyingStack, player.inventory.getStackInSlot(this.blockedSlotID));
    }

    @Override
    public ItemStack slotClick(int slot, int par2, int par3, EntityPlayer par4EntityPlayer) {
        return slot == this.blockedSlotID ? null : super.slotClick(slot, par2, par3, par4EntityPlayer);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote && this.modifyingStack != null && this.filterInventory != null) {
            boolean properActiveStack = ItemStack.areItemStacksEqual(this.modifyingStack, player.inventory.getStackInSlot(this.blockedSlotID));
            this.filterInventory.removeDuplicates();
            this.filterInventory.saveToItemStack(this.modifyingStack);
            if (properActiveStack) {
                player.inventory.setInventorySlotContents(this.blockedSlotID, this.modifyingStack);
            }
        }
    }

    public void receiveMessageFromClient(int type, String data) {
        switch (type) {
            case 0:
                this.filterInventory.useItemCount = !data.equals("1");
                break;
            case 1:
                this.filterInventory.ignoreMetadata = data.equals("1");
                break;
            case 2:
                this.filterInventory.ignoreNBT = data.equals("1");
                break;
            case 3:
                this.filterInventory.nameFilter = data;
                break;
            default:
                FMLLog.warning("[Automagy] ContainerEnchantedPaper received invalid packet data. Ignoring. (type=" + type + ")");
        }
    }
}

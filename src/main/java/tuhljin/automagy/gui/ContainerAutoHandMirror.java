package tuhljin.automagy.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.common.container.InventoryHandMirror;
import tuhljin.automagy.items.CachedFilterData;
import tuhljin.automagy.items.InventoryForItemRestrictedToFilters;
import tuhljin.automagy.items.ItemAutoHandMirror;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.items.ModItems;

public class ContainerAutoHandMirror extends FilterContainer implements IReporteeForSlot {
    public final int blockedSlotID;
    private boolean closed;
    private ItemStack modifyingStack = null;
    private GUIAutoHandMirror attachedGUI = null;
    private EntityPlayer player;
    private World worldObj;
    public InventoryForItemRestrictedToFilters inventoryHoldingFilter;
    private SlotRestrictedWithReporting[] filterSlot = new SlotRestrictedWithReporting[2];
    public IInventory input = new InventoryHandMirror(this);

    public ContainerAutoHandMirror(InventoryPlayer inventoryPlayer, World world) {
        this.blockedSlotID = inventoryPlayer.currentItem;
        this.player = inventoryPlayer.player;
        this.worldObj = world;
        this.addSlotToContainer(new Slot(this.input, 0, 80, 24));

        for (int i = 0; i < 9; i++) {
            Slot slot = new Slot(inventoryPlayer, i, 8 + i * 18, 142);
            this.addSlotToContainer(slot);
            if (i == this.blockedSlotID) {
                ItemStack stack = slot.getStack();
                if (stack != null && stack.getItem() == ModItems.autoHandMirror) {
                    this.modifyingStack = stack;
                }
            }
        }

        for (int ix = 0; ix < 3; ix++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + ix * 9, 8 + j * 18, 84 + ix * 18));
            }
        }

        if (this.modifyingStack == null) {
            FMLLog.severe("[Automagy] ContainerAutoHandMirror failed to find hungry hand mirror's itemstack.");
        }

        this.inventoryHoldingFilter = new InventoryForItemRestrictedToFilters(this.modifyingStack, "Hungry Hand Mirror Inventory", 2);
        this.filterSlot[0] = new SlotRestrictedWithReporting(0, this.inventoryHoldingFilter, 0, 21, 24);
        this.addSlotToContainer(this.filterSlot[0]);
        this.filterSlot[0].setParent(this);
        this.filterSlot[1] = new SlotRestrictedWithReporting(1, this.inventoryHoldingFilter, 1, 139, 24);
        this.addSlotToContainer(this.filterSlot[1]);
        this.filterSlot[1].setParent(this);
        this.onCraftMatrixChanged(this.input);
    }

    @SideOnly(Side.CLIENT)
    public void attachToGui(GUIAutoHandMirror gui) {
        this.attachedGUI = gui;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return !this.closed && player == this.player && this.modifyingStack != null
            && this.modifyingStack == player.inventory.getStackInSlot(this.blockedSlotID);
    }

    @Override
    public ItemStack slotClick(int slot, int par2, int par3, EntityPlayer par4EntityPlayer) {
        if (!this.canInteractWith(par4EntityPlayer) || par3 == 2 && par2 == this.blockedSlotID) return null;
        boolean outsideClick = slot == -999 && (par3 == 0 || par3 == 1 || par3 == 5 && getDragEvent(par2) != 1);
        if (slot < 0 && !outsideClick || slot >= this.inventorySlots.size()) return null;
        return slot == this.blockedSlotID + 1 ? null : super.slotClick(slot, par2, par3, par4EntityPlayer);
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot.slotNumber != this.blockedSlotID + 1 && super.canDragIntoSlot(slot);
    }

    @Override
    public boolean func_94530_a(ItemStack stack, Slot slot) {
        return slot.slotNumber != this.blockedSlotID + 1 && super.func_94530_a(stack, slot);
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        if (this.attachedGUI != null) {
            this.attachedGUI.onSlotChanged(slot);
        }

        if (!this.player.worldObj.isRemote && this.modifyingStack != null && this.inventoryHoldingFilter != null) {
            boolean properActiveStack = ItemStack.areItemStacksEqual(this.modifyingStack, this.player.inventory.getStackInSlot(this.blockedSlotID));
            this.inventoryHoldingFilter.saveToItemStack(this.modifyingStack);
            CachedFilterData.clearData(this.modifyingStack);
            if (properActiveStack) {
                this.player.inventory.setInventorySlotContents(this.blockedSlotID, this.modifyingStack);
            }
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        if (this.closed) return;
        if (this.input.getStackInSlot(0) != null && ItemStack.areItemStacksEqual(this.input.getStackInSlot(0), this.modifyingStack)) {
            // Leave the input available for normal retrieval/close; never abandon a live container.
            return;
        } else if (!this.worldObj.isRemote
            && this.input.getStackInSlot(0) != null
            && this.player != null
            && ItemAutoHandMirror.transport(this.modifyingStack, this.input.getStackInSlot(0), this.player, this.worldObj)) {
            this.input.setInventorySlotContents(0, null);

            for (int var4 = 0; var4 < this.crafters.size(); var4++) {
                ((ICrafting)this.crafters.get(var4)).sendSlotContents(this, 0, null);
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        if (!this.canInteractWith(player) || slotNum < 0 || slotNum >= this.inventorySlots.size()
            || slotNum == this.blockedSlotID + 1) return null;
        ItemStack stack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null) {
            if (slot instanceof SlotRestrictedWithReporting) {
                return super.transferStackInSlot(player, slotNum);
            }

            if (slot.getHasStack()) {
                ItemStack stackInSlot = slot.getStack();
                if (ItemEnchantedPaper.stackIsFilter(stackInSlot)) {
                    return super.transferStackInSlot(player, slotNum);
                }

                if (slotNum == 0) {
                    if (!this.mergeItemStack(stackInSlot, 1, this.inventorySlots.size(), true)) {
                        return null;
                    }
                } else if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
                    return null;
                }

                if (stackInSlot.stackSize == 0) {
                    slot.putStack(null);
                } else {
                    slot.onSlotChanged();
                }
            }
        }

        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        if (this.closed) return;
        this.closed = true;
        super.onContainerClosed(player);
        if (!this.worldObj.isRemote) {
            for (int i = 0; i < 1; i++) {
                ItemStack stack = this.input.getStackInSlotOnClosing(i);
                if (stack != null) {
                    this.input.setInventorySlotContents(i, null);
                    player.dropPlayerItemWithRandomChoice(stack, false);
                }
            }
        }
    }
}

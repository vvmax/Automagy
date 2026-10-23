package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.tiles.TileEntityGolemWorkbench;

public class ContainerGolemWorkbench extends ContainerPartialGhostSlots {
    private TileEntityGolemWorkbench teGW;
    private int numRows;
    public static int FIRST_CRAFTING_SLOT;
    protected int recipeIndex = 0;
    public int maxRecipeIndex;
    private InventoryPlayer inventoryPlayer;

    public ContainerGolemWorkbench(InventoryPlayer inventoryPlayer, TileEntityGolemWorkbench te) {
        super(45, 53);
        this.inventoryPlayer = inventoryPlayer;
        this.teGW = te;
        this.teGW.openChest();
        this.maxRecipeIndex = te.getNumRecipes() - 1;
        this.numRows = te.getSizeInventory() / 9;
        this.addContainerInventory();
        this.addPlayerInventory();
        this.addCraftingInventory();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.maxRecipeIndex != this.teGW.getNumRecipes() - 1 ? false : this.teGW.isUseableByPlayer(player);
    }

    public void refreshInventory() {
        this.inventoryItemStacks.clear();
        this.inventorySlots.clear();
        this.ghostContainer.inventoryItemStacks.clear();
        this.ghostContainer.inventorySlots.clear();
        this.addContainerInventory();
        this.addPlayerInventory();
        this.addCraftingInventory();
    }

    protected void addContainerInventory() {
        int i = (this.numRows - 4) * 18;

        for (int j = 0; j < this.numRows; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlotToContainer(new Slot(this.teGW, k + j * 9, 8 + k * 18, 86 + j * 18));
            }
        }
    }

    protected void addPlayerInventory() {
        int i = (this.numRows - 4) * 18;
        int startZ = 177;

        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlotToContainer(new Slot(this.inventoryPlayer, k + j * 9 + 9, 8 + k * 18, startZ + j * 18 + i));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlotToContainer(new Slot(this.inventoryPlayer, j, 8 + j * 18, startZ + 58 + i));
        }
    }

    protected void addCraftingInventory() {
        FIRST_CRAFTING_SLOT = this.inventorySlots.size();

        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 3; k++) {
                this.addSlotToContainer(new SlotGhostSingle(this.teGW.invCrafting[this.recipeIndex], k + j * 3, 59 + k * 21, 5 + j * 20));
            }
        }
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int button) {
        this.recipeIndex = button;
        if (this.recipeIndex > this.maxRecipeIndex) {
            this.recipeIndex = 0;
        } else if (this.recipeIndex < 0) {
            this.recipeIndex = this.maxRecipeIndex;
        }

        this.refreshInventory();
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        this.teGW.closeChest();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotNum < this.numRows * 9) {
                if (!this.mergeItemStack(itemstack1, this.numRows * 9, this.inventorySlots.size() - 9, true)) {
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

    public int getRecipeIndex() {
        return this.recipeIndex;
    }
}

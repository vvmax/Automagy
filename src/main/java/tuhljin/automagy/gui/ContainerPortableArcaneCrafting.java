package tuhljin.automagy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import thaumcraft.common.container.ContainerDummy;
import thaumcraft.common.container.SlotLimitedByWand;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import tuhljin.automagy.entities.EntityItemDelayedFall;
import tuhljin.automagy.items.InventoryForArcaneCrafting;
import tuhljin.automagy.items.ItemFocusCrafting;
import tuhljin.automagy.lib.ThaumcraftExtension;

public class ContainerPortableArcaneCrafting extends Container implements IReporteeForSlot {
    public static boolean droppedItemsHover = true;
    public final int blockedSlotID;
    public final int blockedSlotHotbarPos;
    public final int displayWandSlotID;
    public InventoryForArcaneCrafting craftingInv;
    public EntityPlayer player;
    public boolean hasStorage = false;
    protected ItemStack storagestack = null;
    protected FakeTileMagicWorkbench fakeTile;
    private final ItemStack boundWand;
    private boolean closed;

    public ContainerPortableArcaneCrafting(EntityPlayer player, int wandSlot) {
        this.player = player;
        this.blockedSlotHotbarPos = wandSlot;
        InventoryPlayer inventoryPlayer = player.inventory;
        int blockedSlot = -1;
        ItemStack wandstack = inventoryPlayer.getStackInSlot(wandSlot);
        this.boundWand = wandstack;
        ItemFocusCrafting focus = ItemFocusCrafting.getCraftingFocusOnWand(wandstack);
        if (focus != null) {
            this.storagestack = ThaumcraftExtension.getWandFocusStack(wandstack);
            this.hasStorage = focus.isUpgradedWith(this.storagestack, ItemFocusCrafting.storage);
        }

        this.craftingInv = new InventoryForArcaneCrafting(
            this.hasStorage ? this.storagestack : null, "Portable Arcane Crafting", 11, this, inventoryPlayer, wandSlot
        );
        this.fakeTile = new FakeTileMagicWorkbench(this.craftingInv, player.worldObj);
        this.addSlotToContainer(new SlotCraftingArcanePortable(player, this.fakeTile, this.craftingInv, 9, 160, 53, this));
        Slot slot = new SlotLimitedByWand(this.craftingInv, 10, 160, 13);
        this.addSlotToContainer(slot);
        this.displayWandSlotID = slot.slotNumber;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                SlotRestrictedWithReporting rSlot = new SlotRestrictedWithReporting(
                    slot.slotNumber + 1, this.craftingInv, col + row * 3, 40 + col * 24, 29 + row * 24
                );
                this.addSlotToContainer(rSlot);
                rSlot.setParent(this);
            }
        }

        for (int i = 0; i < 9; i++) {
            slot = new Slot(inventoryPlayer, i, 16 + i * 18, 188);
            this.addSlotToContainer(slot);
            if (i == wandSlot) {
                blockedSlot = slot.slotNumber;
                ItemStack var14 = slot.getStack();
            }
        }

        for (int ix = 0; ix < 3; ix++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + j + ix * 9, 16 + j * 18, 130 + ix * 18));
            }
        }

        this.blockedSlotID = blockedSlot;
        this.onCraftMatrixChanged(this.craftingInv);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        InventoryCrafting ic = new InventoryCrafting(new ContainerDummy(), 3, 3);

        for (int a = 0; a < 9; a++) {
            ic.setInventorySlotContents(a, this.craftingInv.getStackInSlot(a));
        }

        this.craftingInv.setInventorySlotContentsSoftly(9, CraftingManager.getInstance().findMatchingRecipe(ic, this.player.worldObj));
        if (this.craftingInv.getStackInSlot(9) == null
            && this.craftingInv.getStackInSlot(10) != null
            && this.craftingInv.getStackInSlot(10).getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting)this.craftingInv.getStackInSlot(10).getItem();
            ItemStack wandstack = this.craftingInv.getStackInSlot(10);
            wandstack = wandstack.copy();
            if (wand.consumeAllVisCrafting(wandstack, this.player, ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(this.fakeTile, this.player), true)
                && ItemFocusCrafting.wandHasRequiredVis(this.player, wandstack, true)) {
                this.craftingInv.setInventorySlotContentsSoftly(9, ThaumcraftCraftingManager.findMatchingArcaneRecipe(this.fakeTile, this.player));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        if (this.closed || player != this.player) {
            return false;
        }
        ItemStack newStack = player.inventory.getStackInSlot(this.blockedSlotHotbarPos);
        if (newStack == null || newStack != this.boundWand || !ItemFocusCrafting.isWandWithCraftingFocus(newStack)) {
            return false;
        }
        ItemStack focus = ThaumcraftExtension.getWandFocusStack(newStack);
        return focus != null && this.storagestack != null
            && focus.getItem() == this.storagestack.getItem()
            && focus.getTagCompound() == this.storagestack.getTagCompound();
    }

    @Override
    public ItemStack slotClick(int slot, int button, int modifiers, EntityPlayer par4EntityPlayer) {
        if (this.closed || !this.canInteractWith(par4EntityPlayer)) {
            return null;
        }
        // A number-key click also addresses a second slot in the player's hotbar.
        if (modifiers == 2 && button == this.blockedSlotHotbarPos) {
            return null;
        }
        boolean outsideClick = slot == -999 && (modifiers == 0 || modifiers == 1
            || modifiers == 5 && getDragEvent(button) != 1);
        if (slot < 0 && !outsideClick || slot >= this.inventorySlots.size()) {
            return null;
        }
        if (slot != this.blockedSlotID && slot != this.displayWandSlotID) {
            if (slot == 0) {
                this.onCraftMatrixChanged(this.craftingInv);
            }

            if (modifiers == 4) {
                int var5 = 1;
                return super.slotClick(slot, var5, modifiers, par4EntityPlayer);
            } else {
                if ((modifiers == 0 || modifiers == 1) && (slot == 0 || slot == 1) && button > 0) {
                    button = 0;
                }

                return super.slotClick(slot, button, modifiers, par4EntityPlayer);
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean func_94530_a(ItemStack par1ItemStack, Slot par2Slot) {
        return par2Slot.slotNumber != this.blockedSlotID && par2Slot.inventory != this.craftingInv
            && super.func_94530_a(par1ItemStack, par2Slot);
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return slot.slotNumber != this.blockedSlotID && slot.slotNumber != this.displayWandSlotID
            && super.canDragIntoSlot(slot);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        ItemStack var2x = null;
        Slot var3 = (Slot)this.inventorySlots.get(slot);
        if (var3 != null && var3.getHasStack()) {
            ItemStack var4 = var3.getStack();
            var2x = var4.copy();
            if (slot == 0) {
                if (!this.mergeItemStack(var4, 11, 47, true)) {
                    return null;
                }

                var3.onSlotChange(var4, var2x);
            } else if (slot >= 20 && slot < 47) {
                if (!this.mergeItemStack(var4, 11, 20, false)) {
                    return null;
                }
            } else if (slot >= 11 && slot < 20) {
                if (!this.mergeItemStack(var4, 20, 47, false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(var4, 11, 47, false)) {
                return null;
            }

            if (var4.stackSize == 0) {
                var3.putStack((ItemStack)null);
            } else {
                var3.onSlotChanged();
            }

            if (var4.stackSize == var2x.stackSize) {
                return null;
            }

            var3.onPickupFromSlot(player, var4);
        }

        return var2x;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        if (this.closed) {
            return;
        }
        this.closed = true;
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote) {
            if (this.hasStorage) {
                this.craftingInv.saveToItemStack(this.storagestack);
            } else {
                boolean first = true;

                for (int i = 0; i < 9; i++) {
                    ItemStack stack = this.craftingInv.getStackInSlot(i);
                    if (stack != null) {
                        this.craftingInv.setInventorySlotContentsSoftly(i, null);
                        if (droppedItemsHover) {
                            EntityItemDelayedFall entityitem = EntityItemDelayedFall.toss(stack, player);
                            if (first) {
                                first = false;
                            } else {
                                entityitem.motionX = (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.1F;
                                entityitem.motionZ = (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.1F;
                            }
                        } else {
                            tuhljin.automagy.lib.TjUtil.dropItemIntoWorld(stack, player.worldObj, player.posX, player.posY + 0.5, player.posZ);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (!this.player.worldObj.isRemote) {
            ItemStack stack = ((Slot)this.inventorySlots.get(1)).getStack();
            if (stack != null) {
                ItemStack stack2 = (ItemStack)this.inventoryItemStacks.get(1);
                if (!ItemStack.areItemStacksEqual(stack, stack2)) {
                    ((EntityPlayerMP)this.player).sendContainerToPlayer(this);
                }
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onSlotChanged(SlotRestrictedWithReporting slot) {
        if (this.hasStorage) {
            this.craftingInv.saveToItemStack(this.storagestack);
        }
    }
}

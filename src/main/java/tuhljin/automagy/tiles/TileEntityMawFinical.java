package tuhljin.automagy.tiles;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class TileEntityMawFinical extends TileEntityMawHungry implements IContainsFilter {
    public InventoryObjectFilter filterContainingInventory;
    private InventoryWithFilterOptions filter = null;
    private boolean filterIsBlacklist;
    private Pattern filterNamePattern;

    public TileEntityMawFinical() {
        this.filterContainingInventory = new InventoryObjectFilter(this, "Finical Maw Filter", 1);
    }

    @Override
    protected ItemStack depositItem(ItemStack stack, IInventory inv, int side) {
        if (this.filter == null) {
            return super.depositItem(stack, inv, side);
        } else {
            int amt = Math.min(this.getMaxAllowedAfterFilter(stack, inv), stack.stackSize);
            ItemStack depositStack = stack.copy();
            depositStack.stackSize = amt;
            int remaining = stack.stackSize - amt;
            ItemStack leftovers = super.depositItem(depositStack, inv, side);
            if (leftovers != null) {
                remaining += leftovers.stackSize;
            }

            if (remaining < 1) {
                return null;
            } else {
                ItemStack newStack = stack.copy();
                newStack.stackSize = remaining;
                return newStack;
            }
        }
    }

    @Override
    public void onFilterInventoryChanged(int slot, ItemStack prevStack) {
        ItemStack stack = this.filterContainingInventory.getStackInSlot(slot);
        if (stack == null) {
            this.filter = null;
        } else {
            this.filter = ItemEnchantedPaper.getFilterInventory(stack);
            if (this.filter != null) {
                this.filterIsBlacklist = ItemEnchantedPaper.stackIsBlacklist(stack);
                if (this.filter.nameFilter.isEmpty()) {
                    this.filterNamePattern = null;
                } else {
                    this.filterNamePattern = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter.nameFilter.toLowerCase());
                }
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        this.filterContainingInventory.writeCustomNBT(nbttagcompound);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.filterContainingInventory.readCustomNBT(nbttagcompound);
        ItemStack filterStack = this.filterContainingInventory.getStackInSlot(0);
        if (filterStack == null) {
            this.filter = null;
        } else {
            this.filter = ItemEnchantedPaper.getFilterInventory(filterStack);
            if (this.filter != null) {
                this.filterIsBlacklist = ItemEnchantedPaper.stackIsBlacklist(filterStack);
                if (this.filter.nameFilter.isEmpty()) {
                    this.filterNamePattern = null;
                } else {
                    this.filterNamePattern = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter.nameFilter.toLowerCase());
                }
            }
        }
    }

    private int getMaxAllowedAfterFilter(ItemStack keyStack, IInventory invDest) {
        ItemStack stack = null;
        boolean found = false;

        for (int i = 0; i < this.filter.getSizeInventory(); i++) {
            stack = this.filter.getStackInSlot(i);
            if (stack != null && stack.getItem() == keyStack.getItem() && (this.filter.ignoreMetadata || stack.getMetadata() == keyStack.getMetadata())) {
                if (this.filter.ignoreNBT) {
                    found = true;
                    break;
                }

                if (ItemStack.areItemStackTagsEqual(stack, keyStack)) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            if (this.filterNamePattern != null) {
                String name = keyStack.getDisplayName().toLowerCase();
                Matcher m = this.filterNamePattern.matcher(name);
                if (m.matches()) {
                    return this.filterIsBlacklist ? 0 : Integer.MAX_VALUE;
                }
            }

            return this.filterIsBlacklist ? Integer.MAX_VALUE : 0;
        } else if (!this.filter.useItemCount) {
            return this.filterIsBlacklist ? 0 : Integer.MAX_VALUE;
        } else if (this.filterIsBlacklist) {
            int qtyNeighborTotal = keyStack.stackSize;
            return qtyNeighborTotal - stack.stackSize;
        } else {
            FilteringItemList dest = new FilteringItemList().populateFromInventory(invDest, false);
            HashableItemWithoutSize item = new HashableItemWithoutSize(keyStack);
            int qtyHere = dest.get(item, this.filter.ignoreMetadata, this.filter.ignoreNBT);
            return stack.stackSize - qtyHere;
        }
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
            && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64.0;
    }
}

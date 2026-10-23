package tuhljin.automagy.tiles;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class ModTileEntityWithFilterMainInventory extends ModTileEntityWithInventory {
    protected InventoryWithFilterOptions filter = null;
    protected boolean filterIsBlacklist;
    protected Pattern filterNamePattern;

    public ModTileEntityWithFilterMainInventory(String inventoryName, boolean createDefaultAccessibleSlots) {
        super(inventoryName, 1, createDefaultAccessibleSlots);
        this.notifyOnInventoryChanged = true;
    }

    public ModTileEntityWithFilterMainInventory(String inventoryName) {
        this(inventoryName, true);
    }

    public boolean nameFilterMatches(ItemStack stack) {
        if (this.filterNamePattern != null) {
            String name = stack.getDisplayName().toLowerCase();
            Matcher m = this.filterNamePattern.matcher(name);
            return m.matches();
        } else {
            return false;
        }
    }

    public boolean nameFilterMatches(HashableItemWithoutSize item) {
        return this.filterNamePattern == null ? false : this.nameFilterMatches(item.getItemStack(1));
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return InventoryObjectFilter.isItemPopulatedFilter(stack);
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        ItemStack stack = this.getStackInSlot(slot);
        if (stack == null) {
            this.filter = null;
        } else {
            this.filter = this.getFilterFromStack(stack);
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
    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        ItemStack filterStack = this.getStackInSlot(0);
        if (filterStack == null) {
            this.filter = null;
        } else {
            this.filter = this.getFilterFromStack(filterStack);
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

    public InventoryWithFilterOptions getFilterFromStack(ItemStack stack) {
        return ItemEnchantedPaper.getFilterInventory(stack);
    }
}

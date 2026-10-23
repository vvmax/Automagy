package tuhljin.automagy.items;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.FilteringItemList;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class CachedFilterData {
    public static String NBT_KEY = "AutomagyFilterID";
    private static Map<String, CachedFilterData> map = new HashMap<>();
    private static long prefix;
    private static int count;
    public InventoryForItem inventoryOfFilters;
    private int numFilters;
    private InventoryWithFilterOptions[] filter;
    private boolean[] filterIsBlacklist;
    private Pattern[] filterNamePattern;

    public CachedFilterData(ItemStack stack, int numFilters) {
        this.numFilters = numFilters;
        this.filter = new InventoryWithFilterOptions[numFilters];
        this.filterIsBlacklist = new boolean[numFilters];
        this.filterNamePattern = new Pattern[numFilters];
        this.populateFilter(stack);
    }

    public CachedFilterData(ItemStack stack) {
        this(stack, 2);
    }

    public void populateFilter(ItemStack stack) {
        this.inventoryOfFilters = new InventoryForItem(stack, "FilterInv", this.numFilters);

        for (int slot = 0; slot < this.numFilters; slot++) {
            ItemStack filterStack = this.inventoryOfFilters.getStackInSlot(slot);
            if (filterStack == null) {
                this.filter[slot] = null;
            } else {
                this.filter[slot] = ItemEnchantedPaper.getFilterInventory(filterStack);
                if (this.filter[slot] != null) {
                    this.filterIsBlacklist[slot] = ItemEnchantedPaper.stackIsBlacklist(filterStack);
                    if (this.filter[slot].nameFilter.isEmpty()) {
                        this.filterNamePattern[slot] = null;
                    } else {
                        this.filterNamePattern[slot] = TjUtil.getSafePatternUsingAsteriskForWildcard(this.filter[slot].nameFilter.toLowerCase());
                    }
                }
            }
        }
    }

    public static void newCache() {
        prefix = System.currentTimeMillis();
        count = 0;
        map.clear();
    }

    public static CachedFilterData getData(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        } else if (stack.stackTagCompound.hasKey(NBT_KEY)) {
            String id = stack.stackTagCompound.getString(NBT_KEY);
            CachedFilterData data = map.get(id);
            if (data != null) {
                return data;
            }
        }

        CachedFilterData data = new CachedFilterData(stack);
        count++;
        String key = prefix + "-" + count;
        map.put(key, data);
        stack.stackTagCompound.setString(NBT_KEY, key);
        return data;
    }

    public static void clearData(ItemStack stack) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey(NBT_KEY)) {
            String id = stack.stackTagCompound.getString(NBT_KEY);
            stack.stackTagCompound.removeTag(NBT_KEY);
            map.remove(id);
        }
    }

    public int getMaxToMoveAfterFilter(HashableItemWithoutSize item, FilteringItemList source, FilteringItemList dest, boolean defaultZero) {
        ItemStack keyStack = null;
        ItemStack stack = null;
        int found = -1;

        label114:
        for (int fs = 0; fs < this.numFilters; fs++) {
            if (this.filter[fs] != null) {
                for (int i = 0; i < this.filter[fs].getSizeInventory(); i++) {
                    stack = this.filter[fs].getStackInSlot(i);
                    if (stack != null && stack.getItem() == item.getItem() && (this.filter[fs].ignoreMetadata || stack.getMetadata() == item.damage)) {
                        if (this.filter[fs].ignoreNBT) {
                            found = fs;
                            break label114;
                        }

                        if (keyStack == null) {
                            keyStack = item.getItemStack(1);
                        }

                        if (ItemStack.areItemStackTagsEqual(stack, keyStack)) {
                            found = fs;
                            break label114;
                        }
                    }
                }
            }
        }

        if (found == -1) {
            for (int fsx = 0; fsx < this.numFilters; fsx++) {
                if (this.filter[fsx] != null && this.filterNamePattern[fsx] != null) {
                    if (keyStack == null) {
                        keyStack = item.getItemStack(1);
                    }

                    String name = keyStack.getDisplayName().toLowerCase();
                    Matcher m = this.filterNamePattern[fsx].matcher(name);
                    if (m.matches()) {
                        return this.filterIsBlacklist[fsx] ? 0 : Integer.MAX_VALUE;
                    }
                }
            }

            if (defaultZero) {
                return 0;
            } else {
                for (int fsxx = 0; fsxx < this.numFilters; fsxx++) {
                    if (this.filter[fsxx] != null && !this.filterIsBlacklist[fsxx]) {
                        return 0;
                    }
                }

                return Integer.MAX_VALUE;
            }
        } else if (!this.filter[found].useItemCount) {
            return this.filterIsBlacklist[found] ? 0 : Integer.MAX_VALUE;
        } else if (this.filterIsBlacklist[found]) {
            if (source == null) {
                return 0;
            } else {
                int qtySource = source.get(item, this.filter[found].ignoreMetadata, this.filter[found].ignoreNBT);
                return qtySource - stack.stackSize;
            }
        } else if (dest == null) {
            return stack.stackSize;
        } else {
            int qtyDestination = dest.get(item, this.filter[found].ignoreMetadata, this.filter[found].ignoreNBT);
            return stack.stackSize - qtyDestination;
        }
    }

    public int getMaxToMoveFromPlayer(FilteringItemList playerInvMap, HashableItemWithoutSize item) {
        return this.getMaxToMoveAfterFilter(item, playerInvMap, null, true);
    }

    public static FilteringItemList getPlayerInvMap(EntityPlayer player, HashableItemWithoutSize item, int amount) {
        FilteringItemList playerInvMap = new FilteringItemList().populateFromInventory(player.inventory, false);
        playerInvMap.add(item, amount);
        return playerInvMap;
    }
}

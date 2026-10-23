package tuhljin.automagy.lib.inventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import tuhljin.automagy.lib.TjUtil;

public abstract class MappedItemsWithGroupedCounts<T> {
    public String nbtKey;
    protected Map<HashableItemWithoutSize, Map<T, Integer>> map;

    public MappedItemsWithGroupedCounts(String key) {
        this.nbtKey = key;
        this.map = new LinkedHashMap<>();
    }

    public MappedItemsWithGroupedCounts(MappedItemsWithGroupedCounts copySource) {
        this.nbtKey = copySource.nbtKey;
        this.map = new HashMap<>(copySource.map);
    }

    public void add(T group, ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return;
        Map<T, Integer> submap = this.getSubmap(stack, true);
        Integer c = submap.get(group);
        submap.put(group, (int)Math.min(Integer.MAX_VALUE, (long)Math.max(0, c == null ? 0 : c) + stack.stackSize));
    }

    public int subtract(T group, HashableItemWithoutSize item, int amount) {
        if (amount <= 0) return 0;
        int numRemoved = 0;
        Map<T, Integer> submap = this.map.get(item);
        if (submap != null) {
            Integer c = submap.get(group);
            if (c != null) {
                int newC = c - amount;
                if (newC > 0) {
                    submap.put(group, newC);
                    numRemoved = amount;
                } else {
                    submap.remove(group);
                    if (submap.isEmpty()) {
                        this.map.remove(item);
                    }

                    numRemoved = c;
                }
            }
        }

        return numRemoved;
    }

    public int subtract(T group, ItemStack stack) {
        return this.subtract(group, new HashableItemWithoutSize(stack), stack.stackSize);
    }

    public void subtractFromAnyGroup(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return;
        HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
        Map<T, Integer> submap = this.map.get(item);
        if (submap != null) {
            int toRemove = stack.stackSize;
            Iterator<Entry<T, Integer>> iter = submap.entrySet().iterator();

            while (toRemove > 0 && iter.hasNext()) {
                Entry<T, Integer> entry = iter.next();
                int removed = Math.min(toRemove, Math.max(0, entry.getValue()));
                int remaining = entry.getValue() - removed;
                if (remaining > 0) entry.setValue(remaining);
                else iter.remove();
                toRemove -= removed;
            }
            if (submap.isEmpty()) this.map.remove(item);
        }
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public void clear() {
        this.map.clear();
    }

    public void removeGroup(T group) {
        Iterator<Map<T, Integer>> iter = this.map.values().iterator();
        while (iter.hasNext()) {
            Map<T, Integer> submap = iter.next();
            submap.remove(group);
            if (submap.isEmpty()) iter.remove();
        }
    }

    public Iterator<Entry<HashableItemWithoutSize, Map<T, Integer>>> getIterator() {
        return this.map.entrySet().iterator();
    }

    public boolean containsKey(HashableItemWithoutSize item) {
        return this.map.containsKey(item);
    }

    public int get(T group, HashableItemWithoutSize item) {
        Map<T, Integer> submap = this.map.get(item);
        if (submap == null) {
            return 0;
        } else {
            Integer c = submap.get(group);
            return c == null ? 0 : c;
        }
    }

    public Map<T, Integer> getGroupsForItem(HashableItemWithoutSize item) {
        return this.map.get(item);
    }

    public void set(T group, ItemStack stack) {
        if (stack.stackSize > 0) {
            Map<T, Integer> submap = this.getSubmap(stack, true);
            submap.put(group, stack.stackSize);
        } else {
            Map<T, Integer> submap = this.getSubmap(stack, false);
            if (submap != null && submap.containsKey(group)) {
                submap.remove(group);
                if (submap.isEmpty()) {
                    this.map.remove(new HashableItemWithoutSize(stack));
                }
            }
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        this.map.clear();
        NBTTagList nbttaglist = nbttagcompound.getTagList(this.nbtKey, 10);

        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
            ItemStack stack = TjUtil.readLargeItemStackFromNBT(tagList);
            if (stack != null && stack.stackSize > 0) {
                T group = this.getGroupFromNBT(tagList);

                try {
                    assert group != null;

                    this.set(group, stack);
                } catch (Exception var8) {
                    TjUtil.printError("Group data could not be loaded from NBT. Associated item/request may be lost and won't be saved.");
                    var8.printStackTrace();
                }
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (Entry<HashableItemWithoutSize, Map<T, Integer>> entry : this.map.entrySet()) {
            Map<T, Integer> submap = entry.getValue();
            if (!submap.isEmpty()) {
                HashableItemWithoutSize item = entry.getKey();

                for (Entry<T, Integer> subentry : submap.entrySet()) {
                    T group = subentry.getKey();
                    ItemStack stack = item.getItemStack(subentry.getValue());
                    NBTTagCompound tagList = new NBTTagCompound();
                    TjUtil.writeLargeItemStackToNBT(stack, tagList);
                    this.saveGroupToNBT(tagList, group);
                    nbttaglist.appendTag(tagList);
                }
            }
        }

        nbttagcompound.setTag(this.nbtKey, nbttaglist);
    }

    protected abstract T getGroupFromNBT(NBTTagCompound var1);

    protected abstract void saveGroupToNBT(NBTTagCompound var1, T var2);

    private Map<T, Integer> getSubmap(ItemStack stack, boolean createIfMissing) {
        HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
        Map<T, Integer> submap = this.map.get(item);
        if (submap == null && createIfMissing) {
            submap = new HashMap<>();
            this.map.put(item, submap);
        }

        return submap;
    }

    private static NBTTagCompound writeLargeItemStackToNBT(ItemStack stack, NBTTagCompound nbt) {
        nbt.setShort("id", (short)Item.getIdFromItem(stack.getItem()));
        nbt.setInteger("Count", stack.stackSize);
        nbt.setShort("Damage", (short)stack.getMetadata());
        if (stack.stackTagCompound != null) {
            nbt.setTag("tag", stack.stackTagCompound);
        }

        return nbt;
    }
}

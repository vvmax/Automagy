package tuhljin.automagy.lib.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import tuhljin.automagy.lib.TjUtil;

public class MappedItemsOrderedByTimeAdded implements IItemMap {
    public String nbtKey;
    protected Map<HashableItemWithoutSize, Integer> map;

    public MappedItemsOrderedByTimeAdded(String key) {
        this.nbtKey = key;
        this.map = new LinkedHashMap<>();
    }

    public MappedItemsOrderedByTimeAdded(MappedItemsOrderedByTimeAdded copySource) {
        this.nbtKey = copySource.nbtKey;
        this.map = new LinkedHashMap<>(copySource.map);
    }

    public void add(ItemStack stack, boolean rearrange) {
        HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
        Integer c = this.map.get(item);
        if (c == null) {
            this.map.put(item, stack.stackSize);
        } else {
            if (rearrange) {
                this.map.remove(item);
            }

            this.map.put(item, c + stack.stackSize);
        }
    }

    public void add(ItemStack stack) {
        this.add(stack, true);
    }

    public void subtract(ItemStack stack) {
        HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
        Integer c = this.map.get(item);
        int newC = (c == null ? 0 : c) - stack.stackSize;
        if (newC > 0) {
            this.map.put(item, newC);
        } else if (c != null) {
            this.map.remove(item);
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

    @Override
    public Iterator<Entry<HashableItemWithoutSize, Integer>> getIterator() {
        return this.map.entrySet().iterator();
    }

    @Override
    public boolean containsKey(HashableItemWithoutSize item) {
        return this.map.containsKey(item);
    }

    @Override
    public int get(HashableItemWithoutSize item) {
        Integer c = this.map.get(item);
        return c == null ? 0 : c;
    }

    public void setAll(ArrayList<ItemStack> list) {
        this.map.clear();

        for (ItemStack stack : list) {
            this.map.put(new HashableItemWithoutSize(stack), stack.stackSize);
        }
    }

    public void set(HashableItemWithoutSize item, int count, boolean rearrange) {
        if (count != 0) {
            if (rearrange && this.map.containsKey(item)) {
                this.map.remove(item);
            }

            this.map.put(item, count);
        } else if (this.map.containsKey(item)) {
            this.map.remove(item);
        }
    }

    @Override
    public void set(HashableItemWithoutSize item, int count) {
        this.set(item, count, true);
    }

    @Override
    public ArrayList<ItemStack> getItemStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();

        for (Entry<HashableItemWithoutSize, Integer> entry : this.map.entrySet()) {
            stacks.add(entry.getKey().getItemStack(entry.getValue()));
        }

        return stacks;
    }

    public MappedItemsOrderedByTimeAdded copy() {
        return new MappedItemsOrderedByTimeAdded(this);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        this.map.clear();
        NBTTagList nbttaglist = nbttagcompound.getTagList(this.nbtKey, 10);

        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
            ItemStack stack = TjUtil.readLargeItemStackFromNBT(tagList);
            if (stack != null && stack.stackSize > 0) {
                this.map.put(new HashableItemWithoutSize(stack), stack.stackSize);
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (Entry<HashableItemWithoutSize, Integer> entry : this.map.entrySet()) {
            ItemStack stack = entry.getKey().getItemStack(entry.getValue());
            NBTTagCompound tagList = new NBTTagCompound();
            TjUtil.writeLargeItemStackToNBT(stack, tagList);
            nbttaglist.appendTag(tagList);
        }

        nbttagcompound.setTag(this.nbtKey, nbttaglist);
    }
}

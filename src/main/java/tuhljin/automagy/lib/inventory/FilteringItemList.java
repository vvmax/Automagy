package tuhljin.automagy.lib.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import tuhljin.automagy.api.inventarium.IInventariumContentsProvider;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.tiles.TileEntityInventarium;

public class FilteringItemList implements IItemMap {
    protected Map<HashableItemWithoutSize, Integer> map;
    protected Map<HashableItemWithoutSize, Integer> cacheNoMetadata = null;
    protected Map<HashableItemWithoutSize, Integer> cacheNoNBT = null;
    protected Map<HashableItemWithoutSize, Integer> cacheNoMetadataOrNBT = null;

    public FilteringItemList() {
        this.map = this.createNewMap();
    }

    public FilteringItemList(ArrayList<ItemStack> list, boolean allowBelowOne) {
        this();

        for (ItemStack stack : list) {
            if (allowBelowOne || stack.stackSize > 0) {
                this.map.put(new HashableItemWithoutSize(stack), stack.stackSize);
            }
        }
    }

    public FilteringItemList(FilteringItemList copySource) {
        this.map = new HashMap<>(copySource.map);
    }

    public FilteringItemList(MappedItemsOrderedByTimeAdded copySource) {
        this.map = new HashMap<>(copySource.map);
    }

    public FilteringItemList populateFromInventory(IInventory inv, boolean checkExtract) {
        return this.populateFromInventory(inv, -1, checkExtract);
    }

    public FilteringItemList populateFromInventory(IInventory inv, int side, boolean checkExtract) {
        this.cacheNoMetadata = null;
        this.cacheNoNBT = null;
        this.cacheNoMetadataOrNBT = null;
        this.map.clear();
        this.popFromInv(inv, side, checkExtract);
        return this;
    }

    public FilteringItemList populateFromInventories(ArrayList<IInventory> invs) {
        this.cacheNoMetadata = null;
        this.cacheNoNBT = null;
        this.cacheNoMetadataOrNBT = null;
        this.map.clear();

        for (IInventory inv : invs) {
            this.popFromInv(inv, -1, false);
        }

        return this;
    }

    public FilteringItemList populateFromInventariumContentsProviders(ArrayList<TileEntityInventarium.PairedContentsProvider> providers, int identifier) {
        this.cacheNoMetadata = null;
        this.cacheNoNBT = null;
        this.cacheNoMetadataOrNBT = null;
        this.map.clear();
        int size = providers.size();

        for (int i = 0; i < size; i++) {
            TileEntityInventarium.PairedContentsProvider pair = providers.get(i);
            this.popFromInventariumContentsProvider(pair.provider, pair.te, identifier);
        }

        return this;
    }

    private void popFromInv(IInventory inv, int side, boolean checkExtract) {
        ISidedInventory sidedInv = null;
        if (side != -1 && inv instanceof ISidedInventory) {
            sidedInv = (ISidedInventory)inv;
        }

        int[] slots;
        if (sidedInv == null) {
            int numSlots = inv.getSizeInventory();
            slots = new int[numSlots];
            int i = 0;

            while (i < numSlots) {
                slots[i] = i++;
            }
        } else {
            slots = sidedInv.getSlotsForFace(side);
        }

        for (int i1 = 0; i1 < slots.length; i1++) {
            int slotNum = slots[i1];
            ItemStack stack = inv.getStackInSlot(slotNum);
            if (stack != null && (sidedInv == null || !checkExtract || sidedInv.canExtractItem(slotNum, stack, side))) {
                HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                if (this.map.containsKey(item)) {
                    this.map.put(item, TjUtil.overflowSafeAddInt(this.map.get(item), stack.stackSize));
                } else {
                    this.map.put(item, stack.stackSize);
                }
            }
        }
    }

    private void popFromInventariumContentsProvider(IInventariumContentsProvider inv, TileEntity te, int tickIdentifier) {
        ItemStack[] stacks = inv.getContents(te, tickIdentifier);
        if (stacks != null) {
            for (int i = 0; i < stacks.length; i++) {
                ItemStack stack = stacks[i];
                if (stack != null) {
                    HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                    if (this.map.containsKey(item)) {
                        this.map.put(item, TjUtil.overflowSafeAddInt(this.map.get(item), stack.stackSize));
                    } else {
                        this.map.put(item, stack.stackSize);
                    }
                }
            }
        }
    }

    @Override
    public int get(HashableItemWithoutSize item) {
        Integer c = this.map.get(item);
        return c == null ? 0 : c;
    }

    @Override
    public void set(HashableItemWithoutSize item, int count) {
        this.set(item, count, false);
    }

    public void setZero(HashableItemWithoutSize item) {
        this.set(item, 0, true);
    }

    public void add(HashableItemWithoutSize item, int amount) {
        int count = this.get(item);
        this.set(item, TjUtil.overflowSafeAddInt(count, amount));
    }

    public void add(ItemStack stack) {
        this.add(new HashableItemWithoutSize(stack), stack.stackSize);
    }

    public void subtract(HashableItemWithoutSize item, int amount) {
        int count = this.get(item);
        this.set(item, TjUtil.overflowSafeSubtractInt(count, amount));
    }

    public void subtract(ItemStack stack) {
        HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
        this.subtract(item, stack.stackSize);
    }

    @Override
    public boolean containsKey(HashableItemWithoutSize item) {
        return this.map.containsKey(item);
    }

    @Override
    public Iterator<Entry<HashableItemWithoutSize, Integer>> getIterator() {
        return this.map.entrySet().iterator();
    }

    public void set(HashableItemWithoutSize item, int count, boolean allowZero) {
        count = Math.max(count, 0);
        if (count == 0 && allowZero && !this.map.containsKey(item)) {
            this.map.put(item, 0);
        } else {
            int diff = count - this.get(item);
            if (diff != 0) {
                if (count > 0) {
                    this.map.put(item, count);
                } else if (this.map.containsKey(item)) {
                    if (allowZero) {
                        this.map.put(item, 0);
                    } else {
                        this.map.remove(item);
                    }
                }

                if (this.cacheNoMetadata != null) {
                    HashableItemWithoutSize item2 = new HashableItemWithoutSize(item, true, false);
                    Integer c = this.cacheNoMetadata.get(item2);
                    int val = TjUtil.overflowSafeAddInt(c == null ? 0 : c, diff);
                    if (val > 0) {
                        this.cacheNoMetadata.put(item2, val);
                    } else if (this.cacheNoMetadata.containsKey(item2)) {
                        if (allowZero) {
                            this.cacheNoMetadata.put(item2, 0);
                        } else {
                            this.cacheNoMetadata.remove(item2);
                        }
                    }
                }

                if (this.cacheNoNBT != null) {
                    HashableItemWithoutSize item2 = new HashableItemWithoutSize(item, false, true);
                    Integer c = this.cacheNoNBT.get(item2);
                    int val = TjUtil.overflowSafeAddInt(c == null ? 0 : c, diff);
                    if (val > 0) {
                        this.cacheNoNBT.put(item2, val);
                    } else if (this.cacheNoNBT.containsKey(item2)) {
                        if (allowZero) {
                            this.cacheNoNBT.put(item2, 0);
                        } else {
                            this.cacheNoNBT.remove(item2);
                        }
                    }
                }

                if (this.cacheNoMetadataOrNBT != null) {
                    HashableItemWithoutSize item2 = new HashableItemWithoutSize(item, true, true);
                    Integer c = this.cacheNoMetadataOrNBT.get(item2);
                    int val = TjUtil.overflowSafeAddInt(c == null ? 0 : c, diff);
                    if (val > 0) {
                        this.cacheNoMetadataOrNBT.put(item2, val);
                    } else if (this.cacheNoMetadataOrNBT.containsKey(item2)) {
                        if (allowZero) {
                            this.cacheNoMetadataOrNBT.put(item2, 0);
                        } else {
                            this.cacheNoMetadataOrNBT.remove(item2);
                        }
                    }
                }
            }
        }
    }

    public int get(HashableItemWithoutSize item, boolean ignoreMetadata, boolean ignoreNBT) {
        Map<HashableItemWithoutSize, Integer> ref;
        if (ignoreMetadata) {
            if (ignoreNBT) {
                if (this.cacheNoMetadataOrNBT == null) {
                    this.generateCache(true, true);
                }

                ref = this.cacheNoMetadataOrNBT;
            } else {
                if (this.cacheNoMetadata == null) {
                    this.generateCache(true, false);
                }

                ref = this.cacheNoMetadata;
            }
        } else if (ignoreNBT) {
            if (this.cacheNoNBT == null) {
                this.generateCache(false, true);
            }

            ref = this.cacheNoNBT;
        } else {
            ref = this.map;
        }

        Integer c = ref.get(new HashableItemWithoutSize(item, ignoreMetadata, ignoreNBT));
        return c == null ? 0 : c;
    }

    public int size() {
        return this.map.size();
    }

    public void clearCache() {
        this.cacheNoMetadata = null;
        this.cacheNoNBT = null;
        this.cacheNoMetadataOrNBT = null;
    }

    public FilteringItemList getDifferences(FilteringItemList previousList) {
        FilteringItemList diff = new FilteringItemList();

        for (Entry<HashableItemWithoutSize, Integer> entry : this.map.entrySet()) {
            HashableItemWithoutSize key = entry.getKey();
            if (previousList.map.containsKey(key)) {
                int oldValue = previousList.map.get(key);
                int newValue = entry.getValue();
                if (newValue > oldValue) {
                    diff.map.put(key, newValue - oldValue);
                }
            } else {
                diff.map.put(key, entry.getValue());
            }
        }

        return diff;
    }

    protected void generateCache(boolean ignoreMetadata, boolean ignoreNBT) {
        Map<HashableItemWithoutSize, Integer> cache = this.createNewMap();

        for (Entry<HashableItemWithoutSize, Integer> entry : this.map.entrySet()) {
            HashableItemWithoutSize item = new HashableItemWithoutSize(entry.getKey(), ignoreMetadata, ignoreNBT);
            Integer c = cache.get(item);
            cache.put(item, TjUtil.overflowSafeAddInt(c == null ? 0 : c, entry.getValue()));
        }

        if (ignoreMetadata) {
            if (ignoreNBT) {
                this.cacheNoMetadataOrNBT = cache;
            } else {
                this.cacheNoMetadata = cache;
            }
        } else if (ignoreNBT) {
            this.cacheNoNBT = cache;
        }
    }

    protected Map<HashableItemWithoutSize, Integer> createNewMap() {
        return new HashMap<>();
    }

    @Override
    public ArrayList<ItemStack> getItemStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();

        for (Entry<HashableItemWithoutSize, Integer> entry : this.map.entrySet()) {
            stacks.add(entry.getKey().getItemStack(entry.getValue()));
        }

        return stacks;
    }

    public FilteringItemList copy() {
        return new FilteringItemList(this);
    }
}

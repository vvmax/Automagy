package tuhljin.automagy.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import tuhljin.automagy.lib.TjUtil;
import tuhljin.automagy.lib.inventory.HashableItemWithoutSize;

public class TileEntityTenaciousChest extends TileEntityChestBase {
    public static int SORT_TIME = 200;
    private boolean needItemSort = true;
    private int ticksUntilSort = 0;
    private boolean getStackFlagsForSort = true;

    public TileEntityTenaciousChest() {
        super("blockTenaciousChest", 9, 3);
        this.notifyOnInventoryChanged = true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (this.needItemSort) {
            if (this.worldObj.isRemote) {
                this.needItemSort = false;
            } else if (this.ticksUntilSort < 1) {
                this.needItemSort = false;
                this.ticksUntilSort = SORT_TIME;
                this.doItemSort();
            } else {
                this.ticksUntilSort--;
            }
        }
    }

    public void flagForSort(int t) {
        this.needItemSort = true;
        this.ticksUntilSort = Math.min(this.ticksUntilSort, t);
    }

    public void flagForSort() {
        this.flagForSort(SORT_TIME);
    }

    protected void doItemSort() {
        int lastFreeSlot = -1;
        int firstFreeSlot = -1;
        int lastSlot = this.getSizeInventory() - 1;
        Map<HashableItemWithoutSize, Integer> items = new HashMap<>();

        for (int i = lastSlot; i >= 0; i--) {
            ItemStack stack = this.inventorySlots[i];
            if (stack == null) {
                if (lastFreeSlot == -1) {
                    lastFreeSlot = i;
                }

                firstFreeSlot = i;
            } else if (stack.stackSize > 0) {
                HashableItemWithoutSize key = new HashableItemWithoutSize(stack);
                if (items.containsKey(key)) {
                    items.put(key, -1);
                } else if (stack.stackSize < 2) {
                    items.put(key, -1);
                } else {
                    items.put(key, i);
                }
            }
        }

        if (lastFreeSlot != -1) {
            for (Entry<HashableItemWithoutSize, Integer> entry : items.entrySet()) {
                Integer v = entry.getValue();
                if (v != -1 && this.splitOneFromSlot(v, lastFreeSlot > v ? lastFreeSlot : firstFreeSlot)) {
                    this.flagForSort(0);
                    return;
                }
            }
        }

        for (int ix = lastSlot; ix >= 1; ix--) {
            ItemStack stack = this.inventorySlots[ix];
            if (stack != null && stack.stackSize > 1 && this.isHighestNumberedSlotWithItem(ix, stack)) {
                ItemStack copy = stack.copy();
                copy.stackSize--;
                int moved = copy.stackSize;
                this.getStackFlagsForSort = false;
                ItemStack leftover = TjUtil.addToInventory(copy, this, 0, ix - 1);
                if (leftover != null && ix < lastSlot) {
                    leftover = TjUtil.addToInventory(leftover, this, ix + 1, lastSlot);
                }

                this.getStackFlagsForSort = true;
                if (leftover != null) {
                    moved -= leftover.stackSize;
                }

                if (moved > 0) {
                    stack.stackSize -= moved;
                    this.markDirty(false);
                    this.flagForSort(0);
                    return;
                }
            }
        }
    }

    private boolean splitOneFromSlot(int oldSlot, int newSlot) {
        ItemStack stack = this.inventorySlots[oldSlot];
        if (stack != null && stack.stackSize >= 2) {
            int amt = oldSlot < newSlot ? 1 : stack.stackSize - 1;
            ItemStack stack2 = this.inventorySlots[oldSlot].splitStack(amt);
            this.inventorySlots[newSlot] = stack2;
            this.markDirty(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        if (this.inventorySlots[slot] == null) {
            return true;
        } else {
            if (stack != null) {
                if (!this.isHighestNumberedSlotWithItem(slot, stack)) {
                    return true;
                }

                if (this.inventorySlots[slot].stackSize == 1 && this.calledBySortingGolem(5)) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean calledBySortingGolem(int callStackDepth) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        callStackDepth += 2;
        if (stackTraceElements.length < callStackDepth) {
            return false;
        } else {
            String className = stackTraceElements[callStackDepth].getClassName();
            return "thaumcraft.common.entities.ai.inventory.AISortingGoto".equals(className)
                    || "thaumcraft.common.entities.ai.inventory.AISortingPlace".equals(stackTraceElements[callStackDepth].getClassName())
                ? true
                : "thaumcraft.common.entities.golems.GolemHelper".equals(className)
                    && "findSomethingSortCore".equals(stackTraceElements[callStackDepth].getMethodName());
        }
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prevStack) {
        if (!this.worldObj.isRemote) {
            this.flagForSort(0);
        }
    }

    @Override
    public void closeChest() {
        super.closeChest();
        if (!this.worldObj.isRemote) {
            this.flagForSort(0);
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = super.getStackInSlot(slot);
        if (stack != null && this.getStackFlagsForSort) {
            this.flagForSort();
        }

        return stack;
    }

    protected boolean isHighestNumberedSlotWithItem(int slot, ItemStack stack) {
        for (int i = this.getSizeInventory() - 1; i >= 0; i--) {
            if (slot == i) {
                return true;
            }

            ItemStack stack2 = this.inventorySlots[i];
            if (stack2 != null && TjUtil.areItemsEqualIgnoringSize(stack, stack2)) {
                return false;
            }
        }

        return false;
    }
}

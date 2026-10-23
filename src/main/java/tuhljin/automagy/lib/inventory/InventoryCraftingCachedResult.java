package tuhljin.automagy.lib.inventory;

import java.util.HashMap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import tuhljin.automagy.gui.ContainerEmpty;

public class InventoryCraftingCachedResult extends InventoryCrafting {
    private static int WIDTH = 3;
    private static int HEIGHT = 3;
    protected final String nbtKey;
    protected boolean hasResult = false;
    protected ItemStack result = null;
    protected World worldObj;
    private HashMap<HashableItemWithoutSize, Integer> map = null;

    public InventoryCraftingCachedResult(Container container, String nbtKey) {
        super(container, WIDTH, HEIGHT);
        this.nbtKey = nbtKey;
    }

    public InventoryCraftingCachedResult(String nbtKey) {
        this(new ContainerEmpty(), nbtKey);
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        this.hasResult = false;
        this.result = null;
        this.map = null;
        return super.decrStackSize(p_70298_1_, p_70298_2_);
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
        this.hasResult = false;
        this.result = null;
        this.map = null;
        super.setInventorySlotContents(p_70299_1_, p_70299_2_);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    public void setWorld(World world) {
        this.worldObj = world;
    }

    public ItemStack getCraftingResult() {
        if (!this.hasResult) {
            this.result = CraftingManager.getInstance().findMatchingRecipe(this, this.worldObj);
            this.hasResult = true;
        }

        return this.result;
    }

    public boolean containsItem(ItemStack stack) {
        if (this.map == null) {
            this.getItemMap();
        }

        return this.map.containsKey(new HashableItemWithoutSize(stack));
    }

    public HashMap<HashableItemWithoutSize, Integer> getItemMap() {
        if (this.map == null) {
            this.map = new HashMap<>();
            int size = this.getSizeInventory();

            for (int slot = 0; slot < size; slot++) {
                ItemStack stack = this.getStackInSlot(slot);
                if (stack != null) {
                    HashableItemWithoutSize item = new HashableItemWithoutSize(stack);
                    if (this.map.containsKey(item)) {
                        this.map.put(item, this.map.get(item) + 1);
                    } else {
                        this.map.put(item, 1);
                    }
                }
            }
        }

        return this.map;
    }

    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = nbttagcompound.getTagList(this.nbtKey, 10);
        if (nbttaglist.tagCount() > 0) {
            int numSlots = this.getSizeInventory();

            for (int i = 0; i < numSlots; i++) {
                NBTTagCompound tagList = nbttaglist.getCompoundTagAt(i);
                byte slot = tagList.getByte("Slot");
                if (slot >= 0 && slot < numSlots && this.getStackInSlot(slot) == null) {
                    this.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tagList));
                }
            }
        }
    }

    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();
        int numSlots = this.getSizeInventory();

        for (int i = 0; i < numSlots; i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null) {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setByte("Slot", (byte)i);
                stack.writeToNBT(tagList);
                nbttaglist.appendTag(tagList);
            }
        }

        nbttagcompound.setTag(this.nbtKey, nbttaglist);
    }

    public boolean recipeFitsSmallGrid() {
        boolean top = false;
        boolean left = false;
        if (this.getStackInSlot(0) != null) {
            top = true;
            left = true;
        } else {
            if (this.getStackInSlot(1) != null || this.getStackInSlot(2) != null) {
                top = true;
            }

            if (this.getStackInSlot(3) != null || this.getStackInSlot(6) != null) {
                left = true;
            }
        }

        return !top || this.getStackInSlot(6) == null && this.getStackInSlot(7) == null && this.getStackInSlot(8) == null
            ? !left || this.getStackInSlot(2) == null && this.getStackInSlot(5) == null && this.getStackInSlot(8) == null
            : false;
    }

    public void clearSlots() {
        for (int i = 0; i < this.getSizeInventory(); i++) {
            this.setInventorySlotContents(i, null);
        }
    }

    public ItemStack[] getCopyOfStacks() {
        int size = this.getSizeInventory();
        ItemStack[] copy = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null) {
                stack = stack.copy();
                if (stack.stackSize != 1) {
                    stack.stackSize = 1;
                }

                copy[i] = stack;
            }
        }

        return copy;
    }
}

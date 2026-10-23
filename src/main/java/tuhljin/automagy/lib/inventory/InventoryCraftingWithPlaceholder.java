package tuhljin.automagy.lib.inventory;

import java.util.HashMap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import tuhljin.automagy.gui.ContainerEmpty;
import tuhljin.automagy.lib.TjUtil;

public class InventoryCraftingWithPlaceholder extends InventoryCraftingCachedResult {
    protected ItemStack placeholder = null;
    protected ItemStack replacement;
    protected InventoryCrafting craftCalc = null;
    private Container container;
    private HashMap<HashableItemWithoutSize, Integer> mapConverted = null;

    public InventoryCraftingWithPlaceholder(Container container, String nbtKey) {
        super(container, nbtKey);
        this.container = container;
    }

    public InventoryCraftingWithPlaceholder(String nbtKey) {
        this(new ContainerEmpty(), nbtKey);
    }

    @Override
    public ItemStack getCraftingResult() {
        if (this.placeholder == null) {
            return super.getCraftingResult();
        } else if (this.replacement == null) {
            return null;
        } else {
            if (!this.hasResult) {
                if (this.craftCalc == null) {
                    this.craftCalc = new InventoryCrafting(this.container, 3, 3);
                }

                for (int i = 0; i < 9; i++) {
                    ItemStack stack = this.getStackInSlot(i);
                    if (stack != null && TjUtil.areItemsEqualIgnoringSize(stack, this.placeholder)) {
                        this.craftCalc.setInventorySlotContents(i, this.replacement);
                    } else {
                        this.craftCalc.setInventorySlotContents(i, stack);
                    }
                }

                this.result = CraftingManager.getInstance().findMatchingRecipe(this.craftCalc, this.worldObj);
                this.hasResult = true;
            }

            return this.result;
        }
    }

    public HashMap<HashableItemWithoutSize, Integer> getItemMap(boolean fuzzy) {
        if (!fuzzy || this.placeholder == null) {
            return super.getItemMap();
        } else if (this.replacement == null) {
            return null;
        } else {
            if (this.mapConverted == null) {
                HashableItemWithoutSize replaceItem = new HashableItemWithoutSize(this.replacement);
                this.mapConverted = new HashMap<>();
                int size = this.getSizeInventory();

                for (int slot = 0; slot < size; slot++) {
                    ItemStack stack = this.getStackInSlot(slot);
                    if (stack != null) {
                        HashableItemWithoutSize item;
                        if (TjUtil.areItemsEqualIgnoringSize(stack, this.placeholder)) {
                            item = replaceItem;
                        } else {
                            item = new HashableItemWithoutSize(stack);
                        }

                        if (this.mapConverted.containsKey(item)) {
                            this.mapConverted.put(item, this.mapConverted.get(item) + 1);
                        } else {
                            this.mapConverted.put(item, 1);
                        }
                    }
                }
            }

            return this.mapConverted;
        }
    }

    public void setPlaceholder(ItemStack placeholder, ItemStack replacement) {
        if (!ItemStack.areItemStacksEqual(placeholder, this.placeholder) || !ItemStack.areItemStacksEqual(replacement, this.replacement)) {
            this.placeholder = placeholder;
            this.replacement = replacement;
            this.hasResult = false;
            this.mapConverted = null;
        }
    }

    public ItemStack getStackInSlot(int slot, boolean convert) {
        ItemStack stack = this.getStackInSlot(slot);
        return convert && this.placeholder != null && TjUtil.areItemsEqualIgnoringSize(stack, this.placeholder) ? this.replacement : stack;
    }
}

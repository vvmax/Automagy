package tuhljin.automagy.lib.recipe;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import tuhljin.automagy.items.InventoryWithFilterOptions;
import tuhljin.automagy.items.ItemEnchantedPaper;

public class PreserveFilterRecipe extends ShapelessArcaneRecipe {
    public PreserveFilterRecipe(String research, Block result, AspectList aspects, Object... recipe) {
        super(research, result, aspects, recipe);
    }

    public PreserveFilterRecipe(String research, Item result, AspectList aspects, Object... recipe) {
        super(research, result, aspects, recipe);
    }

    public PreserveFilterRecipe(String research, ItemStack result, AspectList aspects, Object... recipe) {
        super(research, result, aspects, recipe);
    }

    @Override
    public ItemStack getCraftingResult(IInventory craftGrid) {
        return getCraftingResult(craftGrid, super.getCraftingResult(craftGrid));
    }

    public static ItemStack getCraftingResult(IInventory craftGrid, ItemStack result) {
        if (result != null) {
            boolean foundSource = false;

            for (int slot = 0; slot < craftGrid.getSizeInventory(); slot++) {
                ItemStack stack = craftGrid.getStackInSlot(slot);
                if (stack != null) {
                    if (!foundSource) {
                        InventoryWithFilterOptions filter = ItemEnchantedPaper.getFilterInventory(stack);
                        if (filter != null) {
                            boolean hasTagCompound = result.hasTagCompound();
                            NBTTagCompound nbttagcompound = hasTagCompound ? result.getTagCompound() : new NBTTagCompound();
                            filter.writeCustomNBT(nbttagcompound);
                            if (!hasTagCompound) {
                                result.setTagCompound(nbttagcompound);
                            }

                            foundSource = true;
                        }
                    } else if (ItemEnchantedPaper.stackIsFilter(stack)) {
                        result.setMetadata(stack.getMetadata());
                        break;
                    }
                }
            }
        }

        return result;
    }
}
